package com.hostel.gui.girl;

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

public class GirlDashboard extends JFrame {

    private final Girl girl;
    private final GirlDAO     girlDAO     = new GirlDAO();
    private final BillDAO     billDAO     = new BillDAO();
    private final FineDAO     fineDAO     = new FineDAO();
    private final NoticeDAO   noticeDAO   = new NoticeDAO();
    private final ComplaintDAO cDao       = new ComplaintDAO();
    private final CanteenDAO  canteenDAO  = new CanteenDAO();

    public GirlDashboard(Girl girl) {
        this.girl = girl;
        UITheme.applyGlobalLookAndFeel();
        buildUI();
        setTitle("Student Portal — " + girl.getName());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmLogout(); }
        });
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);

        // ── Top bar ──
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PRIMARY);
        topBar.setBorder(new EmptyBorder(8,16,8,16));
        JLabel titleLbl = new JLabel("Welcome, " + girl.getName() + "  |  Student Portal");
        titleLbl.setFont(UITheme.FONT_HEADER); titleLbl.setForeground(Color.WHITE);
        topBar.add(titleLbl, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        btnPanel.setOpaque(false);
        JButton chPwd = UITheme.secondaryButton("Change Password");
        chPwd.setForeground(Color.WHITE); chPwd.setBorder(BorderFactory.createLineBorder(Color.WHITE,1));
        JButton logout = UITheme.dangerButton("Logout");
        chPwd.addActionListener(e -> changePassword());
        logout.addActionListener(e -> confirmLogout());
        btnPanel.add(chPwd); btnPanel.add(logout);
        topBar.add(btnPanel, BorderLayout.EAST);
        root.add(topBar, BorderLayout.NORTH);

        // ── Tabs ──
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(UITheme.FONT_BOLD_LABEL);

        tabs.addTab("🏠  My Profile",     buildProfilePanel());
        tabs.addTab("💰  Payments",        buildPaymentsPanel());
        tabs.addTab("📋  Dues",            buildDuesPanel());
        tabs.addTab("⚠️  Fines",           buildFinesPanel());
        tabs.addTab("🍽️  Canteen",        buildCanteenPanel());
        tabs.addTab("📢  Notices",         buildNoticesPanel());
        tabs.addTab("📝  Complaints",      buildComplaintsPanel());
        tabs.addTab("📊  Reports",         buildReportsPanel());
        tabs.addTab("💬  Chat",            new ClientChatPanel(girl.getName(), "GIRL", girl.getGirlId()));

        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── PROFILE ─────────────────────────────────────────────────────────
    private Component buildProfilePanel() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.BACKGROUND); p.setBorder(new EmptyBorder(16,16,16,16));

        FeeStructure fs = new FeeStructureDAO().getByGirlId(girl.getGirlId());
        List<Parent> parents = new ParentDAO().getByGirlId(girl.getGirlId());

        p.add(section("My Details", new String[][]{
            {"Name",girl.getName()},{"Gender",girl.getGender()},{"Age",String.valueOf(girl.getAge())},
            {"Mobile",girl.getMobile()},{"Email",girl.getEmail()},
            {"Aadhar",girl.getAadharNumber()},{"College",girl.getCollegeAddress()},
            {"Address",girl.getAddress()},{"Room",girl.getRoomNumber()},
            {"Plan",girl.getPlanName()},{"Kitchen Plan",girl.getKitchenPlanName()},
            {"Admission",String.valueOf(girl.getAdmissionDate())},{"Status",girl.getStatus()}
        }));

        if (fs != null) {
            p.add(Box.createVerticalStrut(10));
            p.add(section("My Fee Structure", new String[][]{
                {"Monthly Stay","₹ "+fs.getMonthlyStayBill()},
                {"Emergency Dep.","₹ "+fs.getEmergencyDeposit()},
                {"Electricity Dep.","₹ "+fs.getElectricityDeposit()},
                {"WiFi","₹ "+fs.getWifiDeposit()},{"Extra","₹ "+fs.getPlanExtraCharge()},
                {"Kitchen","₹ "+fs.getKitchenCharge()}
            }));
        }

        for (Parent par : parents) {
            p.add(Box.createVerticalStrut(10));
            p.add(section(par.getRelationType()+" Contact", new String[][]{
                {"Name",par.getName()},{"Mobile",par.getMobile()},
                {"Email",par.getEmail()},{"Occupation",par.getOccupation()}
            }));
        }
        return wrap(p);
    }

    // ── PAYMENTS ────────────────────────────────────────────────────────
    private JPanel buildPaymentsPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8)); p.setBackground(UITheme.BACKGROUND); p.setBorder(new EmptyBorder(12,12,12,12));
        DefaultTableModel m = new DefaultTableModel(new String[]{"Date","Bill Type","Mode","Amount","Receipt"},0);
        JTable t = new JTable(m); t.setFont(UITheme.FONT_TABLE); t.setRowHeight(24); t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        for (Payment pay : billDAO.getPaymentsByGirlId(girl.getGirlId()))
            m.addRow(new Object[]{pay.getPaymentDate(),pay.getBillType(),pay.getPaymentMode(),
                String.format("₹%.2f",pay.getAmountPaid()),pay.getReceiptNo()});
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JButton pdf = UITheme.primaryButton("Download PDF");
        pdf.addActionListener(e -> genPDF(() -> new PDFReportGenerator().generatePaymentReport(girl.getGirlId()), p));
        JPanel btn = south(pdf); p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    // ── DUES ─────────────────────────────────────────────────────────────
    private JPanel buildDuesPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8)); p.setBackground(UITheme.BACKGROUND); p.setBorder(new EmptyBorder(12,12,12,12));
        DefaultTableModel m = new DefaultTableModel(new String[]{"Bill Type","Amount Due","Due Date","Status"},0);
        JTable t = new JTable(m); t.setFont(UITheme.FONT_TABLE); t.setRowHeight(24); t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        for (Due d : billDAO.getDuesByGirlId(girl.getGirlId()))
            m.addRow(new Object[]{d.getBillType(),String.format("₹%.2f",d.getAmountDue()),d.getDueDate(),d.getStatus()});
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    // ── FINES ─────────────────────────────────────────────────────────────
    private JPanel buildFinesPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8)); p.setBackground(UITheme.BACKGROUND); p.setBorder(new EmptyBorder(12,12,12,12));
        DefaultTableModel m = new DefaultTableModel(new String[]{"Reason","Amount","Date","Status"},0);
        JTable t = new JTable(m); t.setFont(UITheme.FONT_TABLE); t.setRowHeight(24); t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        for (Fine f : fineDAO.getFinesByGirlId(girl.getGirlId()))
            m.addRow(new Object[]{f.getReason(),String.format("₹%.2f",f.getAmount()),f.getFineDate(),f.getStatus()});
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JButton pdf = UITheme.primaryButton("Download Fines PDF");
        pdf.addActionListener(e -> genPDF(() -> new PDFReportGenerator().generateFinesReport(girl.getGirlId()), p));
        p.add(south(pdf), BorderLayout.SOUTH);
        return p;
    }

    // ── CANTEEN ─────────────────────────────────────────────────────────
    private JPanel buildCanteenPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8)); p.setBackground(UITheme.BACKGROUND); p.setBorder(new EmptyBorder(12,12,12,12));
        DefaultTableModel m = new DefaultTableModel(new String[]{"Date","Status","Remarks"},0);
        JTable t = new JTable(m); t.setFont(UITheme.FONT_TABLE); t.setRowHeight(24); t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        for (CanteenServiceLog log : canteenDAO.getServiceLogs())
            m.addRow(new Object[]{log.getServiceDate(),log.getStatus(),log.getRemarks()});
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    // ── NOTICES ─────────────────────────────────────────────────────────
    private JPanel buildNoticesPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8)); p.setBackground(UITheme.BACKGROUND); p.setBorder(new EmptyBorder(12,12,12,12));
        DefaultTableModel m = new DefaultTableModel(new String[]{"Title","Date"},0);
        JTable t = new JTable(m); t.setFont(UITheme.FONT_TABLE); t.setRowHeight(24); t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        List<Notice> notices = noticeDAO.getNoticesForGirl(girl.getGirlId());
        for (Notice n : notices) m.addRow(new Object[]{n.getTitle(),n.getCreatedDate()});
        p.add(new JScrollPane(t), BorderLayout.CENTER);

        JTextArea detail = new JTextArea(5,40); detail.setEditable(false); detail.setLineWrap(true);
        detail.setFont(UITheme.FONT_LABEL); detail.setBorder(new EmptyBorder(8,8,8,8));
        t.getSelectionModel().addListSelectionListener(e -> {
            int row = t.getSelectedRow();
            if (row >= 0 && row < notices.size()) detail.setText(notices.get(row).getMessage());
        });
        p.add(new JScrollPane(detail), BorderLayout.SOUTH);
        return p;
    }

    // ── COMPLAINTS ──────────────────────────────────────────────────────
    private JPanel buildComplaintsPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8)); p.setBackground(UITheme.BACKGROUND); p.setBorder(new EmptyBorder(12,12,12,12));

        DefaultTableModel m = new DefaultTableModel(new String[]{"Subject","Status","Filed","Admin Remarks"},0);
        JTable t = new JTable(m); t.setFont(UITheme.FONT_TABLE); t.setRowHeight(24); t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        Runnable reload = () -> { m.setRowCount(0); for (Complaint c : cDao.getComplaintsByGirlId(girl.getGirlId()))
            m.addRow(new Object[]{c.getSubject(),c.getStatus(),c.getFiledDate(),c.getAdminRemarks()}); };
        reload.run();
        p.add(new JScrollPane(t), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(UITheme.BACKGROUND);
        form.setBorder(BorderFactory.createTitledBorder("File a New Complaint"));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(6,4,6,4);
        JTextField tfSubject = new JTextField(28); JTextArea tfDesc = new JTextArea(3,40); tfDesc.setLineWrap(true);
        gbc.gridy=0; gbc.gridx=0; form.add(bold("Subject *:"),gbc);
        gbc.gridx=1; gbc.fill=GridBagConstraints.HORIZONTAL; form.add(tfSubject,gbc);
        gbc.gridy=1; gbc.gridx=0; gbc.fill=GridBagConstraints.NONE; form.add(bold("Description *:"),gbc);
        gbc.gridx=1; gbc.fill=GridBagConstraints.HORIZONTAL; form.add(new JScrollPane(tfDesc),gbc);
        JButton sub = UITheme.primaryButton("Submit Complaint");
        gbc.gridy=2; gbc.gridx=0; gbc.gridwidth=2; gbc.fill=GridBagConstraints.NONE; form.add(sub,gbc);
        sub.addActionListener(e -> {
            if (tfSubject.getText().trim().isEmpty()||tfDesc.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(p,"Fill subject and description.","Error",JOptionPane.WARNING_MESSAGE); return;}
            Complaint c=new Complaint(); c.setGirlId(girl.getGirlId()); c.setSubject(tfSubject.getText().trim()); c.setDescription(tfDesc.getText().trim());
            if(cDao.fileComplaint(c)){JOptionPane.showMessageDialog(p,"Complaint filed.","Done",JOptionPane.INFORMATION_MESSAGE);tfSubject.setText("");tfDesc.setText("");reload.run();}
        });
        p.add(form, BorderLayout.SOUTH);
        return p;
    }

    // ── REPORTS ─────────────────────────────────────────────────────────
    private JPanel buildReportsPanel() {
        JPanel p = new JPanel(new GridLayout(0,2,14,14)); p.setBackground(UITheme.BACKGROUND); p.setBorder(new EmptyBorder(20,20,20,20));
        p.add(reportCard("Payment History","All your payments as PDF",e->genPDF(()->new PDFReportGenerator().generatePaymentReport(girl.getGirlId()),p)));
        p.add(reportCard("My Stay Plan","Current plan and fee details",e->genPDF(()->new PDFReportGenerator().generatePlanReport(girl.getGirlId()),p)));
        p.add(reportCard("My Fines","All fines as PDF",e->genPDF(()->new PDFReportGenerator().generateFinesReport(girl.getGirlId()),p)));
        return p;
    }

    // ── Helpers ─────────────────────────────────────────────────────────
    interface PDFTask { String run() throws Exception; }

    private void genPDF(PDFTask task, JPanel parent) {
        new SwingWorker<String,Void>(){
            @Override protected String doInBackground() throws Exception { return task.run(); }
            @Override protected void done() {
                try { JOptionPane.showMessageDialog(parent,"PDF saved:\n"+get(),"Done",JOptionPane.INFORMATION_MESSAGE); }
                catch (Exception ex) { JOptionPane.showMessageDialog(parent,"PDF error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); }
            }
        }.execute();
    }

    private JPanel section(String title, String[][] rows) {
        JPanel p = new JPanel(new GridLayout(0,2,4,4)); p.setBackground(UITheme.CARD_BG);
        p.setBorder(BorderFactory.createTitledBorder(title));
        for (String[] r : rows) {
            JLabel k = new JLabel(r[0]+":"); k.setFont(UITheme.FONT_BOLD_LABEL);
            JLabel v = new JLabel(r[1]!=null?r[1]:"-"); v.setFont(UITheme.FONT_LABEL);
            p.add(k); p.add(v);
        }
        return p;
    }

    private JPanel reportCard(String title, String desc, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(6,6)); card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.BORDER,1),new EmptyBorder(14,16,14,16)));
        JLabel t = new JLabel(title); t.setFont(UITheme.FONT_BOLD_LABEL); t.setForeground(UITheme.PRIMARY_DARK);
        JLabel d = new JLabel("<html>"+desc+"</html>"); d.setFont(UITheme.FONT_LABEL); d.setForeground(Color.GRAY);
        JButton btn = UITheme.primaryButton("Generate PDF"); btn.addActionListener(action);
        card.add(t,BorderLayout.NORTH); card.add(d,BorderLayout.CENTER); card.add(btn,BorderLayout.SOUTH);
        return card;
    }

    private JPanel south(JButton btn) {
        JPanel s = new JPanel(new FlowLayout(FlowLayout.RIGHT)); s.setBackground(UITheme.BACKGROUND); s.add(btn); return s;
    }

    private JScrollPane wrap(JPanel p) { return new JScrollPane(p); }
    private JLabel bold(String t) { JLabel l = new JLabel(t); l.setFont(UITheme.FONT_BOLD_LABEL); return l; }

    private void changePassword() {
        ChangePasswordDialog dlg = new ChangePasswordDialog(this, true);
        dlg.setVisible(true);
        if (dlg.isSuccess()) {
            if (!new GirlDAO().changeGirlPassword(girl.getGirlId(), dlg.getNewPassword())) {
                JOptionPane.showMessageDialog(this,"Password change failed.","Error",JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,"Password changed successfully.","Done",JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void confirmLogout() {
        int c = JOptionPane.showConfirmDialog(this,"Logout?","Confirm",JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            AuditLogger.log("GIRL",girl.getGirlId(),"LOGOUT","Student logged out");
            SessionContext.logout();
            dispose();
            new com.hostel.gui.common.LoginFrame().setVisible(true);
        }
    }
}
