package com.hostel.gui.common;

import com.hostel.dao.*;
import com.hostel.model.*;
import com.hostel.gui.admin.AdminDashboard;
import com.hostel.gui.girl.GirlDashboard;
import com.hostel.gui.parent.ParentDashboard;
import com.hostel.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Single login screen for all 3 roles.
 * Triggered from HostelApp on startup.
 */
public class LoginFrame extends JFrame {

    private final JComboBox<String> roleBox = new JComboBox<>(AppConstants.USER_ROLES);
    private final JTextField usernameField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final JButton loginBtn;
    private final JLabel hostelNameLabel;

    private final AdminDAO adminDAO = new AdminDAO();
    private final GirlDAO girlDAO = new GirlDAO();
    private final ParentDAO parentDAO = new ParentDAO();
    private final HostelConfigDAO configDAO = new HostelConfigDAO();

    public LoginFrame() {
        loginBtn = UITheme.primaryButton("Login");
        hostelNameLabel = UITheme.titleLabel(configDAO.getHostelName());
        buildUI();
        setTitle(AppConstants.APP_NAME + " – Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setSize(480, 420);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void buildUI() {
        UITheme.applyGlobalLookAndFeel();
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);

        // ---- TOP BANNER ----
        JPanel banner = new JPanel(new GridLayout(2, 1, 0, 4));
        banner.setBackground(UITheme.PRIMARY);
        banner.setBorder(new EmptyBorder(20, 28, 20, 28));
        hostelNameLabel.setForeground(Color.WHITE);
        hostelNameLabel.setFont(UITheme.FONT_TITLE);
        JLabel subtitle = new JLabel("Hostel Management System", SwingConstants.LEFT);
        subtitle.setFont(UITheme.FONT_LABEL); subtitle.setForeground(new Color(240, 220, 235));
        banner.add(hostelNameLabel); banner.add(subtitle);
        root.add(banner, BorderLayout.NORTH);

        // ---- FORM ----
        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(28, 36, 28, 36));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 6, 8, 6);

        int row = 0;
        addFormRow(card, gbc, row++, "Login As:", roleBox);
        addFormRow(card, gbc, row++, "Username:", usernameField);
        addFormRow(card, gbc, row, "Password:", passwordField);

        root.add(card, BorderLayout.CENTER);

        // ---- BOTTOM BAR ----
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        bottom.setBackground(UITheme.BACKGROUND);
        bottom.setBorder(new EmptyBorder(0, 20, 0, 20));
        bottom.add(loginBtn);
        root.add(bottom, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> doLogin());
        passwordField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });
        getRootPane().setDefaultButton(loginBtn);
        setContentPane(root);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row; gbc.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD_LABEL);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        field.setFont(UITheme.FONT_LABEL);
        panel.add(field, gbc);
        gbc.fill = GridBagConstraints.NONE;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Logging in...");

        new SwingWorker<Void, Void>() {
            private String errorMsg = null;
            @Override
            protected Void doInBackground() {
                try {
                    if ("Admin".equals(role)) loginAdmin(username, password);
                    else if ("Girl Student".equals(role)) loginGirl(username, password);
                    else loginParent(username, password);
                } catch (Exception ex) {
                    errorMsg = ex.getMessage();
                }
                return null;
            }
            @Override
            protected void done() {
                loginBtn.setEnabled(true); loginBtn.setText("Login");
                if (errorMsg != null) {
                    JOptionPane.showMessageDialog(LoginFrame.this, errorMsg, "Login Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void loginAdmin(String username, String password) {
        Admin admin = adminDAO.authenticate(username, password);
        if (admin == null) {
            JOptionPane.showMessageDialog(this, "Invalid admin credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SessionContext.login(SessionContext.Role.ADMIN, admin.getAdminId(), admin.getFullName(), -1);
        AuditLogger.log("ADMIN", admin.getAdminId(), "LOGIN", "Admin logged in: " + admin.getUsername());
        SwingUtilities.invokeLater(() -> {
            dispose();
            new AdminDashboard(admin).setVisible(true);
        });
    }

    private void loginGirl(String username, String password) {
        Girl girl = girlDAO.authenticateGirl(username, password);
        if (girl == null) {
            JOptionPane.showMessageDialog(this, "Invalid student credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SessionContext.login(SessionContext.Role.GIRL, girl.getGirlId(), girl.getName(), girl.getGirlId());
        AuditLogger.log("GIRL", girl.getGirlId(), "LOGIN", "Girl logged in: " + girl.getName());
        SwingUtilities.invokeLater(() -> {
            dispose();
            // Force password change on first login
            if (girlDAO.isTempPassword(girl.getGirlId())) {
                JOptionPane.showMessageDialog(this,
                        "Welcome! This is your first login. Please change your temporary password now.",
                        "Welcome", JOptionPane.INFORMATION_MESSAGE);
                ChangePasswordDialog dlg = new ChangePasswordDialog(null, false);
                dlg.setVisible(true);
                if (dlg.isSuccess()) {
                    girlDAO.changeGirlPassword(girl.getGirlId(), dlg.getNewPassword());
                    JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            new GirlDashboard(girl).setVisible(true);
        });
    }

    private void loginParent(String username, String password) {
        int[] result = parentDAO.authenticateParent(username, password);
        if (result == null) {
            JOptionPane.showMessageDialog(this, "Invalid parent credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int parentId = result[0], girlId = result[1];
        Girl girl = girlDAO.getById(girlId);
        Parent parent = parentDAO.getByGirlId(girlId).stream()
                .filter(p -> p.getParentId() == parentId).findFirst().orElse(null);
        SessionContext.login(SessionContext.Role.PARENT, parentId, parent != null ? parent.getName() : "Parent", girlId);
        AuditLogger.log("PARENT", parentId, "LOGIN", "Parent logged in for girl_id=" + girlId);
        SwingUtilities.invokeLater(() -> {
            dispose();
            if (parentDAO.isTempPassword(parentId)) {
                JOptionPane.showMessageDialog(this, "Welcome! Please change your temporary password.", "Welcome", JOptionPane.INFORMATION_MESSAGE);
                ChangePasswordDialog dlg = new ChangePasswordDialog(null, false);
                dlg.setVisible(true);
                if (dlg.isSuccess()) {
                    parentDAO.changeParentPassword(parentId, dlg.getNewPassword());
                }
            }
            new ParentDashboard(parent, girl).setVisible(true);
        });
    }
}
