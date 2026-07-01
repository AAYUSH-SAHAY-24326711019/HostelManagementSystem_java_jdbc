package com.hostel.gui.parent;

import com.hostel.dao.*;
import com.hostel.model.*;
import com.hostel.gui.common.ChangePasswordDialog;
import com.hostel.gui.common.ClientChatPanel;
import com.hostel.report.PDFReportGenerator;
import com.hostel.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class ParentDashboard extends JFrame {

    private final Parent parent;   // may be null if lookup failed — we fall back gracefully
    private final Girl   girl;

    private final BillDAO      billDAO    = new BillDAO();
    private final FineDAO      fineDAO    = new FineDAO();
    private final NoticeDAO    noticeDAO  = new NoticeDAO();
    private final CanteenDAO   canteenDAO = new CanteenDAO();
    private final ParentDAO    parentDAO  = new ParentDAO();

    public ParentDashboard(Parent parent, Girl girl) {
        this.parent = parent;
        this.girl   = girl;
        UITheme.applyGlobalLookAndFeel();
        buildUI();
        String who = (parent != null) ? parent.getName() + " (" + parent.getRelationType() + ")" : "Parent";
        setTitle("Parent Portal — " + who + " → " + girl.getName());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmLogout(); }
        });
    }

    // ── Top-level frame ──────────────────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);

        // banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(60, 90, 140));
        banner.setBorder(new EmptyBorder(8, 16, 8, 16));

        String parentName = (parent != null) ? parent.getName() + " (" + parent.getRelationType() + ")" : "Parent";
        JLabel title = new JLabel("Parent Portal  |  " + parentName + "  →  " + girl.getName());
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(Color.WHITE);
        banner.add(title, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton chPwd  = UITheme.secondaryButton("Change Password");
        JButton logout = UITheme.dangerButton("Logout");
        chPwd.setForeground(Color.WHITE);
        chPwd.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        chPwd.addActionListener(e -> changePassword());
        logout.addActionListener(e -> confirmLogout());
        btns.add(chPwd);
        btns.add(logout);
        banner.add(btns, BorderLayout.EAST);
        root.add(banner, BorderLayout.NORTH);

        // tabs
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(UITheme.FONT_BOLD_LABEL);

        tabs.addTab("🏠  Ward's Profile",   buildProfilePanel());
        tabs.addTab("💰  Payments",          buildPaymentsPanel());
        tabs.addTab("📋  Dues",              buildDuesPanel());
        tabs.addTab("⚠️  Fines",             buildFinesPanel());
        tabs.addTab("🍽️  Canteen Log",      buildCanteenPanel());
        tabs.addTab("📢  Notices",           buildNoticesPanel());
        tabs.addTab("📊  Reports",           buildReportsPanel());
        tabs.addTab("💬  Chat",              buildChatPanel());

        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── WARD'S PROFILE ──────────────────────────────────────────────────
    private JScrollPane buildProfilePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        p.add(section("Ward's Details", new String[][]{
            {"Name",         girl.getName()},
            {"Gender",       girl.getGender()},
            {"Age",          String.valueOf(girl.getAge())},
            {"Mobile",       girl.getMobile()},
            {"Email",        girl.getEmail()},
            {"Aadhar",       girl.getAadharNumber()},
            {"College",      girl.getCollegeAddress()},
            {"Address",      girl.getAddress()},
            {"Room",         girl.getRoomNumber()  != null ? girl.getRoomNumber()  : "-"},
            {"Plan",         girl.getPlanName()    != null ? girl.getPlanName()    : "-"},
            {"Kitchen Plan", girl.getKitchenPlanName() != null ? girl.getKitchenPlanName() : "-"},
            {"Admission",    String.valueOf(girl.getAdmissionDate())},
            {"Status",       girl.getStatus()}
        }));

        FeeStructure fs = new FeeStructureDAO().getByGirlId(girl.getGirlId());
        if (fs != null) {
            p.add(Box.createVerticalStrut(10));
            p.add(section("Monthly Fee Breakdown", new String[][]{
                {"Monthly Stay",      "₹ " + fs.getMonthlyStayBill()},
                {"Emergency Dep.",    "₹ " + fs.getEmergencyDeposit()},
                {"Electricity Dep.",  "₹ " + fs.getElectricityDeposit()},
                {"WiFi Deposit",      "₹ " + fs.getWifiDeposit()},
                {"Plan Extra Charge", "₹ " + fs.getPlanExtraCharge()},
                {"Kitchen Charge",    "₹ " + fs.getKitchenCharge()}
            }));
        }

        // Also show the other parents / guardians as contacts
        List<Parent> others = new ParentDAO().getByGirlId(girl.getGirlId());
        for (Parent other : others) {
            p.add(Box.createVerticalStrut(8));
            p.add(section(other.getRelationType() + " Contact", new String[][]{
                {"Name",       other.getName()},
                {"Mobile",     other.getMobile()},
                {"Email",      other.getEmail()},
                {"Occupation", other.getOccupation()}
            }));
        }

        return new JScrollPane(p);
    }

    // ── PAYMENTS ────────────────────────────────────────────────────────
    private JPanel buildPaymentsPanel() {
        JPanel p = panel();
        DefaultTableModel m = model("Date", "Bill Type", "Mode", "Amount", "Receipt No.");
        JTable t = table(m);
        for (Payment pay : billDAO.getPaymentsByGirlId(girl.getGirlId()))
            m.addRow(new Object[]{
                pay.getPaymentDate(), pay.getBillType(), pay.getPaymentMode(),
                String.format("₹ %.2f", pay.getAmountPaid()), pay.getReceiptNo()
            });
        p.add(new JScrollPane(t), BorderLayout.CENTER);

        JButton pdf = UITheme.primaryButton("Download Payment PDF");
        pdf.addActionListener(e -> genPDF(
            () -> new PDFReportGenerator().generatePaymentReport(girl.getGirlId()), p));
        p.add(southBtn(pdf), BorderLayout.SOUTH);
        return p;
    }

    // ── DUES ─────────────────────────────────────────────────────────────
    private JPanel buildDuesPanel() {
        JPanel p = panel();
        DefaultTableModel m = model("Bill Type", "Amount Due", "Due Date", "Status");
        JTable t = table(m);
        for (Due d : billDAO.getDuesByGirlId(girl.getGirlId()))
            m.addRow(new Object[]{
                d.getBillType(), String.format("₹ %.2f", d.getAmountDue()),
                d.getDueDate(), d.getStatus()
            });
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    // ── FINES ────────────────────────────────────────────────────────────
    private JPanel buildFinesPanel() {
        JPanel p = panel();
        DefaultTableModel m = model("Reason", "Amount", "Date", "Status");
        JTable t = table(m);
        for (Fine f : fineDAO.getFinesByGirlId(girl.getGirlId()))
            m.addRow(new Object[]{
                f.getReason(), String.format("₹ %.2f", f.getAmount()),
                f.getFineDate(), f.getStatus()
            });
        p.add(new JScrollPane(t), BorderLayout.CENTER);

        JButton pdf = UITheme.primaryButton("Download Fines PDF");
        pdf.addActionListener(e -> genPDF(
            () -> new PDFReportGenerator().generateFinesReport(girl.getGirlId()), p));
        p.add(southBtn(pdf), BorderLayout.SOUTH);
        return p;
    }

    // ── CANTEEN LOG ──────────────────────────────────────────────────────
    private JPanel buildCanteenPanel() {
        JPanel p = panel();
        DefaultTableModel m = model("Date", "Status", "Remarks");
        JTable t = table(m);
        for (CanteenServiceLog log : canteenDAO.getServiceLogs())
            m.addRow(new Object[]{log.getServiceDate(), log.getStatus(), log.getRemarks()});
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    // ── NOTICES ─────────────────────────────────────────────────────────
    private JPanel buildNoticesPanel() {
        JPanel p = panel();
        int parentId = (parent != null) ? parent.getParentId() : -1;
        List<Notice> notices = parentId > 0
            ? noticeDAO.getNoticesForParent(parentId)
            : noticeDAO.getNoticesForGirl(girl.getGirlId()); // fallback

        DefaultTableModel m = model("Title", "Date");
        JTable t = table(m);
        for (Notice n : notices)
            m.addRow(new Object[]{n.getTitle(), n.getCreatedDate()});
        p.add(new JScrollPane(t), BorderLayout.CENTER);

        JTextArea detail = new JTextArea(5, 40);
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        detail.setFont(UITheme.FONT_LABEL);
        detail.setBorder(new EmptyBorder(8, 8, 8, 8));
        t.getSelectionModel().addListSelectionListener(e -> {
            int row = t.getSelectedRow();
            if (row >= 0 && row < notices.size())
                detail.setText(notices.get(row).getMessage());
        });
        p.add(new JScrollPane(detail), BorderLayout.SOUTH);
        return p;
    }

    // ── REPORTS ─────────────────────────────────────────────────────────
    private JPanel buildReportsPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 14, 14));
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        p.add(reportCard("Payment History",
            "All ward's payment records as PDF",
            e -> genPDF(() -> new PDFReportGenerator()
                    .generatePaymentReport(girl.getGirlId()), p)));

        p.add(reportCard("Current Stay Plan",
            "Ward's room plan & fee structure as PDF",
            e -> genPDF(() -> new PDFReportGenerator()
                    .generatePlanReport(girl.getGirlId()), p)));

        p.add(reportCard("Fines Report",
            "All fines imposed on ward",
            e -> genPDF(() -> new PDFReportGenerator()
                    .generateFinesReport(girl.getGirlId()), p)));

        return p;
    }

    // ── CHAT ────────────────────────────────────────────────────────────
    private ClientChatPanel buildChatPanel() {
        String name = (parent != null) ? parent.getName() : "Parent";
        int id = (parent != null) ? parent.getParentId() : girl.getGirlId();
        return new ClientChatPanel(name, "PARENT", id);
    }

    // ── Password change ──────────────────────────────────────────────────
    private void changePassword() {
        if (parent == null) return;
        ChangePasswordDialog dlg = new ChangePasswordDialog(this, true);
        dlg.setVisible(true);
        if (dlg.isSuccess()) {
            if (parentDAO.changeParentPassword(parent.getParentId(), dlg.getNewPassword())) {
                JOptionPane.showMessageDialog(this,
                    "Password changed successfully.", "Done", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Password change failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Logout ──────────────────────────────────────────────────────────
    private void confirmLogout() {
        int c = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            int uid = (parent != null) ? parent.getParentId() : -1;
            AuditLogger.log("PARENT", uid, "LOGOUT", "Parent logged out, ward=" + girl.getName());
            SessionContext.logout();
            dispose();
            new com.hostel.gui.common.LoginFrame().setVisible(true);
        }
    }

    // ── Shared helpers ───────────────────────────────────────────────────
    interface PDFTask { String run() throws Exception; }

    private void genPDF(PDFTask task, Component parent) {
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception { return task.run(); }
            @Override protected void done() {
                try {
                    JOptionPane.showMessageDialog(ParentDashboard.this,
                        "PDF saved:\n" + get(), "Done", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ParentDashboard.this,
                        "PDF error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private JPanel panel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        return p;
    }

    private DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setFont(UITheme.FONT_TABLE);
        t.setRowHeight(24);
        t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return t;
    }

    private JPanel southBtn(JButton btn) {
        JPanel s = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        s.setBackground(UITheme.BACKGROUND);
        s.add(btn);
        return s;
    }

    private JPanel section(String title, String[][] rows) {
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 4));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(BorderFactory.createTitledBorder(title));
        for (String[] r : rows) {
            JLabel k = new JLabel(r[0] + ":"); k.setFont(UITheme.FONT_BOLD_LABEL);
            JLabel v = new JLabel(r[1] != null ? r[1] : "-"); v.setFont(UITheme.FONT_LABEL);
            p.add(k); p.add(v);
        }
        return p;
    }

    private JPanel reportCard(String title, String desc, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            new EmptyBorder(14, 16, 14, 16)));
        JLabel t = new JLabel(title); t.setFont(UITheme.FONT_BOLD_LABEL); t.setForeground(UITheme.PRIMARY_DARK);
        JLabel d = new JLabel("<html>" + desc + "</html>"); d.setFont(UITheme.FONT_LABEL); d.setForeground(Color.GRAY);
        JButton btn = UITheme.primaryButton("Generate PDF"); btn.addActionListener(action);
        card.add(t, BorderLayout.NORTH);
        card.add(d, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }
}
