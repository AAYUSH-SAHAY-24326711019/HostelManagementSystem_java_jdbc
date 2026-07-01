package com.hostel.gui.admin;

import com.hostel.dao.AdminDAO;
import com.hostel.dao.HostelConfigDAO;
import com.hostel.model.Admin;
import com.hostel.gui.common.ChangePasswordDialog;
import com.hostel.gui.common.ClientChatPanel;
import com.hostel.socket.ChatServer;
import com.hostel.util.*;
import com.hostel.util.PasswordUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AdminDashboard extends JFrame {

    private final Admin admin;
    private ChatServer chatServer;

    public AdminDashboard(Admin admin) {
        this.admin = admin;
        UITheme.applyGlobalLookAndFeel();
        buildUI();
        startChatServer();
        setTitle(new HostelConfigDAO().getHostelName() + " — Admin Panel");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { onClose(); }
        });
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);

        // ── Top bar ──────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PRIMARY_DARK);
        topBar.setBorder(new EmptyBorder(8, 16, 8, 16));
        JLabel titleLbl = new JLabel(new HostelConfigDAO().getHostelName() + "  |  Admin: " + admin.getFullName());
        titleLbl.setFont(UITheme.FONT_HEADER); titleLbl.setForeground(Color.WHITE);
        topBar.add(titleLbl, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton chPwdBtn = UITheme.secondaryButton("Change Password");
        chPwdBtn.setForeground(Color.WHITE); chPwdBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        JButton logoutBtn = UITheme.dangerButton("Logout");
        chPwdBtn.addActionListener(e -> changePassword());
        logoutBtn.addActionListener(e -> onClose());
        btnPanel.add(chPwdBtn); btnPanel.add(logoutBtn);
        topBar.add(btnPanel, BorderLayout.EAST);
        root.add(topBar, BorderLayout.NORTH);

        // ── Tabbed pane ──────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(UITheme.FONT_BOLD_LABEL);
        tabs.setBackground(UITheme.BACKGROUND);

        tabs.addTab("🏠  Dashboard",        new AdminHomePanel(admin));
        tabs.addTab("➕  Admission Form",   new AdmissionFormPanel());
        tabs.addTab("👧  Girl List",         new GirlListPanel());
        tabs.addTab("🔑  Credentials",       new CredentialManagerPanel());
        tabs.addTab("💰  Bills & Payments",  new BillPaymentPanel(admin));
        tabs.addTab("⚠️  Dues & Fines",      new DuesFinesPanel(admin));
        tabs.addTab("📢  Notices",           new NoticePanel(admin));
        tabs.addTab("📋  Complaints",        new ComplaintPanel());
        tabs.addTab("🍽️  Canteen",          new CanteenPanel(admin));
        tabs.addTab("⚙️  Hostel Config",    new HostelConfigPanel());
        tabs.addTab("📊  Reports",           new ReportPanel());
        tabs.addTab("💬  Chat Server",       buildChatServerPanel());

        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Embedded chat-server panel ────────────────────────────────────────
    private JPanel buildChatServerPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false); logArea.setFont(UITheme.FONT_TABLE);
        logArea.setBackground(new Color(30, 30, 40)); logArea.setForeground(Color.GREEN);

        JPanel topP = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topP.setBackground(UITheme.BACKGROUND);
        JLabel statusLbl = new JLabel("Server Status: Running on port " + AppConstants.CHAT_SERVER_PORT);
        statusLbl.setFont(UITheme.FONT_BOLD_LABEL); statusLbl.setForeground(UITheme.SUCCESS);
        JButton broadcastBtn = UITheme.primaryButton("Broadcast Message");
        topP.add(statusLbl); topP.add(broadcastBtn);
        panel.add(topP, BorderLayout.NORTH);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Admin also has a client panel to chat
        ClientChatPanel adminChat = new ClientChatPanel(admin.getFullName(), "ADMIN", admin.getAdminId());
        panel.add(adminChat, BorderLayout.SOUTH);

        broadcastBtn.addActionListener(e -> {
            String msg = JOptionPane.showInputDialog(this, "Enter broadcast message:");
            if (msg != null && !msg.isBlank() && chatServer != null) chatServer.broadcastFromServer(msg);
        });

        // Store log area reference for server callback
        chatLogArea = logArea;
        return panel;
    }

    private JTextArea chatLogArea;

    private void startChatServer() {
        try {
            chatServer = new ChatServer(AppConstants.CHAT_SERVER_PORT, new ChatServer.ChatServerListener() {
                @Override public void onMessageReceived(com.hostel.socket.ChatMessage msg) {
                    appendChatLog(msg.toString());
                }
                @Override public void onClientConnected(String name) { appendChatLog("[+] Connected: " + name); }
                @Override public void onClientDisconnected(String name) { appendChatLog("[-] Disconnected: " + name); }
                @Override public void onServerLog(String log) { appendChatLog(log); }
            });
            chatServer.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not start chat server: " + e.getMessage() +
                "\nAnother instance may already be running.", "Chat Server", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void appendChatLog(String text) {
        SwingUtilities.invokeLater(() -> {
            if (chatLogArea != null) {
                chatLogArea.append(text + "\n");
                chatLogArea.setCaretPosition(chatLogArea.getDocument().getLength());
            }
        });
    }

    private void changePassword() {
        ChangePasswordDialog dlg = new ChangePasswordDialog(this, true);
        dlg.setVisible(true);
        if (dlg.isSuccess()) {
            // Verify current password
            AdminDAO dao = new AdminDAO();
            if (!PasswordUtil.verify(dlg.getCurrentPassword(), admin.getSalt(), admin.getPasswordHash())) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (dao.changePassword(admin.getAdminId(), dlg.getNewPassword())) {
                JOptionPane.showMessageDialog(this, "Password changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void onClose() {
        int c = JOptionPane.showConfirmDialog(this, "Logout and close admin panel?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            AuditLogger.log("ADMIN", admin.getAdminId(), "LOGOUT", "Admin logged out");
            SessionContext.logout();
            if (chatServer != null) chatServer.stop();
            dispose();
            new com.hostel.gui.common.LoginFrame().setVisible(true);
        }
    }
}
