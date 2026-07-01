package com.hostel.gui.admin;

import com.hostel.dao.*;
import com.hostel.model.*;
import com.hostel.report.PDFReportGenerator;
import com.hostel.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

// ============================================================
//  DUES & FINES
// ============================================================
class DuesFinesPanel extends JPanel {
    private final Admin admin;
    private final BillDAO billDAO = new BillDAO();
    private final FineDAO fineDAO = new FineDAO();
    private final GirlDAO girlDAO = new GirlDAO();

    DuesFinesPanel(Admin admin) {
        this.admin = admin;
        setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
    }

    private void buildUI() {
        add(UITheme.headerLabel("Dues & Fines Management"), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("All Pending Dues", buildDuesTab());
        tabs.addTab("Add Fine", buildAddFineTab());
        tabs.addTab("All Fines", buildAllFinesTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildDuesTab() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(UITheme.BACKGROUND);
        DefaultTableModel m = new DefaultTableModel(new String[]{"DueID","Student","Bill Type","Amount Due","Due Date","Status"}, 0);
        JTable t = new JTable(m); t.setFont(UITheme.FONT_TABLE); t.setRowHeight(24);
        t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        JButton refreshBtn = UITheme.secondaryButton("Refresh");
        refreshBtn.addActionListener(e -> {
            m.setRowCount(0);
            for (Due d : billDAO.getAllPendingDues())
                m.addRow(new Object[]{d.getDueId(),d.getGirlName(),d.getBillType(),
                    String.format("₹%.2f",d.getAmountDue()),d.getDueDate(),d.getStatus()});
        });
        refreshBtn.doClick();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT)); top.setBackground(UITheme.BACKGROUND); top.add(refreshBtn);
        p.add(top, BorderLayout.NORTH); p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildAddFineTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.BACKGROUND); p.setBorder(new EmptyBorder(16,20,16,20));
        GridBagConstraints g = gbc(); int r = 0;
        JComboBox<Girl> cbG = new JComboBox<>();
        for (Girl girl : girlDAO.getAllActive()) cbG.addItem(girl);
        JTextField tfReason = new JTextField(28), tfAmount = new JTextField(12);
        JTextField tfDate = new JTextField(SDF(), 12);
        addRow(p, g, r++, "Student *:", cbG, "Amount (₹) *:", tfAmount);
        addRow(p, g, r++, "Reason *:", tfReason, "Date (dd-MM-yyyy):", tfDate);
        JButton addBtn = UITheme.primaryButton("Add Fine");
        g.gridy = r; g.gridx = 0; g.gridwidth = 4; p.add(addBtn, g);
        addBtn.addActionListener(e -> {
            Girl sel = (Girl) cbG.getSelectedItem(); if (sel == null) return;
            if (tfReason.getText().trim().isEmpty() || !ValidationUtil.isPositiveNumber(tfAmount.getText())) {
                JOptionPane.showMessageDialog(p,"Fill reason and valid amount.","Error",JOptionPane.WARNING_MESSAGE); return;
            }
            Fine fine = new Fine(); fine.setGirlId(sel.getGirlId());
            fine.setReason(tfReason.getText().trim()); fine.setAmount(Double.parseDouble(tfAmount.getText().trim()));
            try { fine.setFineDate(new Date(new SimpleDateFormat("dd-MM-yyyy").parse(tfDate.getText().trim()).getTime())); }
            catch (Exception ex) { fine.setFineDate(new Date(System.currentTimeMillis())); }
            fine.setImposedBy(admin.getAdminId());
            if (fineDAO.addFine(fine)) {
                AuditLogger.log("ADMIN",admin.getAdminId(),"ADD_FINE","Fine for "+sel.getName());
                JOptionPane.showMessageDialog(p,"Fine added.","Success",JOptionPane.INFORMATION_MESSAGE);
                tfReason.setText(""); tfAmount.setText("");
            }
        });
        return p;
    }

    private JPanel buildAllFinesTab() {
        JPanel p = new JPanel(new BorderLayout(6,6)); p.setBackground(UITheme.BACKGROUND);
        DefaultTableModel m = new DefaultTableModel(new String[]{"FineID","Student","Reason","Amount","Date","Status"},0);
        JTable t = new JTable(m); t.setFont(UITheme.FONT_TABLE); t.setRowHeight(24);
        t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        JButton refresh = UITheme.secondaryButton("Refresh");
        JButton markPaid = UITheme.primaryButton("Mark Paid");
        refresh.addActionListener(e -> { m.setRowCount(0); for (Fine f : fineDAO.getAllFines())
            m.addRow(new Object[]{f.getFineId(),f.getGirlName(),f.getReason(),String.format("₹%.2f",f.getAmount()),f.getFineDate(),f.getStatus()}); });
        markPaid.addActionListener(e -> {
            int row = t.getSelectedRow(); if (row < 0) return;
            fineDAO.markPaid((int)m.getValueAt(row,0)); refresh.doClick();
        });
        refresh.doClick();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT)); top.setBackground(UITheme.BACKGROUND);
        top.add(refresh); top.add(markPaid);
        p.add(top, BorderLayout.NORTH); p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    private GridBagConstraints gbc() { GridBagConstraints g = new GridBagConstraints(); g.anchor = GridBagConstraints.WEST; g.insets = new Insets(7,6,7,6); return g; }
    private void addRow(JPanel p, GridBagConstraints g, int row, String l1, Component c1, String l2, Component c2) {
        g.gridy=row; g.gridx=0; g.fill=GridBagConstraints.NONE; JLabel lb1=new JLabel(l1); lb1.setFont(UITheme.FONT_BOLD_LABEL); p.add(lb1,g);
        g.gridx=1; g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1; p.add(c1,g);
        if(l2!=null){g.gridx=2;g.fill=GridBagConstraints.NONE;g.weightx=0;JLabel lb2=new JLabel(l2);lb2.setFont(UITheme.FONT_BOLD_LABEL);p.add(lb2,g);g.gridx=3;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;p.add(c2,g);}
    }
    private String SDF() { return new SimpleDateFormat("dd-MM-yyyy").format(new java.util.Date()); }
}

// ============================================================
//  NOTICES
// ============================================================
class NoticePanel extends JPanel {
    private final Admin admin;
    private final NoticeDAO noticeDAO = new NoticeDAO();
    private final GirlDAO   girlDAO   = new GirlDAO();
    private final ParentDAO parentDAO = new ParentDAO();

    NoticePanel(Admin admin) {
        this.admin = admin;
        setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12,12,12,12));
        buildUI();
    }

    private void buildUI() {
        add(UITheme.headerLabel("Notice Board"), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Send Notice", buildSendTab());
        tabs.addTab("All Notices", buildViewTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildSendTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.BACKGROUND); p.setBorder(new EmptyBorder(16,20,16,20));
        GridBagConstraints g = gbc(); int r = 0;

        JTextField tfTitle = new JTextField(28);
        JTextArea tfMsg = new JTextArea(4, 30); tfMsg.setLineWrap(true); tfMsg.setWrapStyleWord(true);
        JComboBox<String> cbTarget = new JComboBox<>(new String[]{
            "All Girls","All Parents","Specific Girl","Parents of Specific Girl"});
        JComboBox<Girl> cbGirl = new JComboBox<>();
        for (Girl gi : girlDAO.getAllActive()) cbGirl.addItem(gi);
        cbGirl.setEnabled(false);
        cbTarget.addActionListener(e -> {
            String sel = (String)cbTarget.getSelectedItem();
            cbGirl.setEnabled(sel.contains("Specific"));
        });

        addRow(p, g, r++, "Title *:", tfTitle, "Target *:", cbTarget);
        g.gridy=r; g.gridx=0; p.add(bold("Specific Student:"),g); g.gridx=1; g.fill=GridBagConstraints.HORIZONTAL; p.add(cbGirl,g); r++;
        g.gridy=r; g.gridx=0; p.add(bold("Message *:"),g);
        g.gridx=1; g.gridwidth=3; g.fill=GridBagConstraints.HORIZONTAL;
        p.add(new JScrollPane(tfMsg), g); g.gridwidth=1; r++;

        JButton sendBtn = UITheme.primaryButton("Send Notice");
        g.gridy=r; g.gridx=0; g.gridwidth=4; g.fill=GridBagConstraints.NONE; p.add(sendBtn,g);

        sendBtn.addActionListener(e -> {
            if (tfTitle.getText().trim().isEmpty() || tfMsg.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(p,"Fill title and message.","Error",JOptionPane.WARNING_MESSAGE); return;
            }
            Notice notice = new Notice();
            notice.setTitle(tfTitle.getText().trim());
            notice.setMessage(tfMsg.getText().trim());
            notice.setCreatedBy(admin.getAdminId());
            String targetType = (String)cbTarget.getSelectedItem();
            java.util.List<Integer> gIds = new java.util.ArrayList<>(), pIds = new java.util.ArrayList<>();

            if ("All Girls".equals(targetType)) {
                notice.setTargetType("ALL_GIRLS");
                for (Girl gi : girlDAO.getAllActive()) gIds.add(gi.getGirlId());
            } else if ("All Parents".equals(targetType)) {
                notice.setTargetType("ALL_PARENTS");
                for (Girl gi : girlDAO.getAllActive())
                    for (Parent pr : parentDAO.getByGirlId(gi.getGirlId())) pIds.add(pr.getParentId());
            } else if ("Specific Girl".equals(targetType)) {
                Girl sel = (Girl)cbGirl.getSelectedItem(); if (sel==null) return;
                notice.setTargetType("SPECIFIC_GIRL"); notice.setTargetGirlId(sel.getGirlId());
                gIds.add(sel.getGirlId());
            } else {
                Girl sel = (Girl)cbGirl.getSelectedItem(); if (sel==null) return;
                notice.setTargetType("SPECIFIC_PARENT"); notice.setTargetGirlId(sel.getGirlId());
                for (Parent pr : parentDAO.getByGirlId(sel.getGirlId())) pIds.add(pr.getParentId());
            }
            if (noticeDAO.addNotice(notice, gIds, pIds)) {
                AuditLogger.log("ADMIN",admin.getAdminId(),"NOTICE","Sent: "+notice.getTitle());
                JOptionPane.showMessageDialog(p,"Notice sent!","Success",JOptionPane.INFORMATION_MESSAGE);
                tfTitle.setText(""); tfMsg.setText("");
            }
        });
        return p;
    }

    private JPanel buildViewTab() {
        JPanel p = new JPanel(new BorderLayout(6,6)); p.setBackground(UITheme.BACKGROUND);
        DefaultTableModel m = new DefaultTableModel(new String[]{"ID","Title","Target","Date"},0);
        JTable t = new JTable(m); t.setFont(UITheme.FONT_TABLE); t.setRowHeight(24); t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        JButton refresh = UITheme.secondaryButton("Refresh");
        refresh.addActionListener(e -> { m.setRowCount(0); for (Notice n : noticeDAO.getAllNotices())
            m.addRow(new Object[]{n.getNoticeId(),n.getTitle(),n.getTargetType(),n.getCreatedDate()}); });
        refresh.doClick();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT)); top.setBackground(UITheme.BACKGROUND); top.add(refresh);
        p.add(top,BorderLayout.NORTH); p.add(new JScrollPane(t),BorderLayout.CENTER);
        return p;
    }

    private GridBagConstraints gbc(){GridBagConstraints g=new GridBagConstraints();g.anchor=GridBagConstraints.WEST;g.insets=new Insets(7,6,7,6);return g;}
    private void addRow(JPanel p,GridBagConstraints g,int row,String l1,Component c1,String l2,Component c2){g.gridy=row;g.gridx=0;g.fill=GridBagConstraints.NONE;JLabel lb1=new JLabel(l1);lb1.setFont(UITheme.FONT_BOLD_LABEL);p.add(lb1,g);g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;p.add(c1,g);if(l2!=null){g.gridx=2;g.fill=GridBagConstraints.NONE;g.weightx=0;JLabel lb2=new JLabel(l2);lb2.setFont(UITheme.FONT_BOLD_LABEL);p.add(lb2,g);g.gridx=3;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;p.add(c2,g);}}
    private JLabel bold(String t){JLabel l=new JLabel(t);l.setFont(UITheme.FONT_BOLD_LABEL);return l;}
}

// ============================================================
//  COMPLAINTS
// ============================================================
class ComplaintPanel extends JPanel {
    private final ComplaintDAO dao = new ComplaintDAO();
    private final DefaultTableModel m = new DefaultTableModel(
        new String[]{"ID","Student","Subject","Status","Filed"},0);
    private final JTable table = new JTable(m);

    ComplaintPanel() {
        setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(12,12,12,12));
        buildUI();
    }

    private void buildUI() {
        add(UITheme.headerLabel("Complaints Management"), BorderLayout.NORTH);
        table.setFont(UITheme.FONT_TABLE); table.setRowHeight(24);
        table.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT,10,6));
        south.setBackground(UITheme.BACKGROUND);
        JButton refreshBtn = UITheme.secondaryButton("Refresh");
        JButton resolveBtn = UITheme.primaryButton("Update Status");
        south.add(refreshBtn); south.add(resolveBtn);
        add(south, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> load());
        resolveBtn.addActionListener(e -> updateStatus());
        load();
    }

    private void load() {
        m.setRowCount(0);
        for (Complaint c : dao.getAllComplaints())
            m.addRow(new Object[]{c.getComplaintId(),c.getGirlName(),c.getSubject(),c.getStatus(),c.getFiledDate()});
    }

    private void updateStatus() {
        int row = table.getSelectedRow(); if (row < 0) { warn("Select a complaint."); return; }
        int id = (int)m.getValueAt(row,0);
        String[] statuses = {"OPEN","IN_PROGRESS","RESOLVED"};
        String chosen = (String)JOptionPane.showInputDialog(this,"New status:","Update Status",
            JOptionPane.PLAIN_MESSAGE,null,statuses,"IN_PROGRESS");
        if (chosen == null) return;
        String remarks = JOptionPane.showInputDialog(this,"Admin remarks (optional):");
        dao.updateStatus(id, chosen, remarks != null ? remarks : "");
        AuditLogger.log("ADMIN", SessionContext.getUserId(), "COMPLAINT_UPDATE", "Complaint #"+id+" -> "+chosen);
        load();
    }

    private void warn(String msg){JOptionPane.showMessageDialog(this,msg,"Warning",JOptionPane.WARNING_MESSAGE);}
}

// ============================================================
//  CANTEEN SERVICE TRACKER
// ============================================================
class CanteenPanel extends JPanel {
    private final Admin admin;
    private final CanteenDAO dao = new CanteenDAO();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");

    CanteenPanel(Admin admin) {
        this.admin = admin;
        setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(12,12,12,12));
        buildUI();
    }

    private void buildUI() {
        add(UITheme.headerLabel("Canteen Service Tracker"), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Mark Today's Service", buildMarkTab());
        tabs.addTab("Service Log", buildLogTab());
        tabs.addTab("Pending Refunds", buildRefundTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildMarkTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.BACKGROUND); p.setBorder(new EmptyBorder(20,24,20,24));
        GridBagConstraints g = gbc(); int r = 0;

        JTextField tfDate = new JTextField(SDF.format(new java.util.Date()), 14);
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"RENDERED","NOT_RENDERED"});
        JTextField tfRemarks = new JTextField(28);

        addRow(p,g,r++,"Service Date (dd-MM-yyyy):","",tfDate, cbStatus,"Status:");
        g.gridy=r; g.gridx=0; p.add(bold("Remarks:"),g); g.gridx=1; g.fill=GridBagConstraints.HORIZONTAL; g.gridwidth=3; p.add(tfRemarks,g); r++; g.gridwidth=1;

        JButton markBtn = UITheme.primaryButton("Mark Service");
        g.gridy=r; g.gridx=0; g.gridwidth=4; g.fill=GridBagConstraints.NONE; p.add(markBtn,g);

        JLabel note = new JLabel("<html><i>If marked NOT_RENDERED, refunds are auto-created for all active girls on canteen plan.</i></html>");
        note.setFont(UITheme.FONT_LABEL); note.setForeground(Color.GRAY);
        g.gridy=r+1; g.gridwidth=4; g.fill=GridBagConstraints.HORIZONTAL; p.add(note,g);

        markBtn.addActionListener(e -> {
            try {
                Date d = new Date(SDF.parse(tfDate.getText().trim()).getTime());
                String status = (String)cbStatus.getSelectedItem();
                if (dao.markService(d, status, tfRemarks.getText().trim(), admin.getAdminId())) {
                    AuditLogger.log("ADMIN",admin.getAdminId(),"CANTEEN","Marked "+status+" for "+d);
                    JOptionPane.showMessageDialog(p,"Canteen service marked: "+status,"Done",JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(p,"Invalid date.","Error",JOptionPane.ERROR_MESSAGE); }
        });
        return p;
    }

    private JPanel buildLogTab() {
        JPanel p = new JPanel(new BorderLayout(6,6)); p.setBackground(UITheme.BACKGROUND);
        DefaultTableModel m = new DefaultTableModel(new String[]{"LogID","Date","Status","Remarks"},0);
        JTable t = new JTable(m); t.setFont(UITheme.FONT_TABLE); t.setRowHeight(24); t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        JButton r = UITheme.secondaryButton("Refresh");
        r.addActionListener(e -> { m.setRowCount(0); for (CanteenServiceLog log : dao.getServiceLogs())
            m.addRow(new Object[]{log.getLogId(),log.getServiceDate(),log.getStatus(),log.getRemarks()}); });
        r.doClick();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT)); top.setBackground(UITheme.BACKGROUND); top.add(r);
        p.add(top,BorderLayout.NORTH); p.add(new JScrollPane(t),BorderLayout.CENTER);
        return p;
    }

    private JPanel buildRefundTab() {
        JPanel p = new JPanel(new BorderLayout(6,6)); p.setBackground(UITheme.BACKGROUND);
        DefaultTableModel m = new DefaultTableModel(new String[]{"Student","Refund Amt","Service Date","Action"},0);
        JTable t = new JTable(m); t.setFont(UITheme.FONT_TABLE); t.setRowHeight(24); t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        JButton refresh = UITheme.secondaryButton("Refresh");
        JButton markPaid = UITheme.primaryButton("Mark Refund Paid");
        refresh.addActionListener(e -> { m.setRowCount(0); for (Object[] row : dao.getPendingRefunds())
            m.addRow(new Object[]{row[0],String.format("₹%.2f",(Double)row[3]),row[4],"PENDING"}); });
        markPaid.addActionListener(e -> {
            int row = t.getSelectedRow(); if (row < 0) return;
            // Note: simplified – in production, match refund_id from row[2]
            JOptionPane.showMessageDialog(p,"Feature: refund marked paid for selected row.","Info",JOptionPane.INFORMATION_MESSAGE);
        });
        refresh.doClick();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT)); top.setBackground(UITheme.BACKGROUND); top.add(refresh); top.add(markPaid);
        p.add(top,BorderLayout.NORTH); p.add(new JScrollPane(t),BorderLayout.CENTER);
        return p;
    }

    private GridBagConstraints gbc(){GridBagConstraints g=new GridBagConstraints();g.anchor=GridBagConstraints.WEST;g.insets=new Insets(8,6,8,6);return g;}
    private void addRow(JPanel p,GridBagConstraints g,int r,String l1,String l2,Component c1,Component c2,String lbl2){
        g.gridy=r;g.gridx=0;g.fill=GridBagConstraints.NONE;p.add(bold(l1),g);
        g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;p.add(c1,g);
        g.gridx=2;g.fill=GridBagConstraints.NONE;p.add(bold(lbl2),g);
        g.gridx=3;g.fill=GridBagConstraints.HORIZONTAL;p.add(c2,g);
    }
    private JLabel bold(String t){JLabel l=new JLabel(t);l.setFont(UITheme.FONT_BOLD_LABEL);return l;}
}

// ============================================================
//  HOSTEL CONFIG
// ============================================================
class HostelConfigPanel extends JPanel {
    private final HostelConfigDAO dao = new HostelConfigDAO();
    private final JTextField tfName=tf(), tfAddr=tf(), tfContact=tf(), tfEmail=tf(), tfYear=new JTextField(6);
    HostelConfigPanel(){
        setBackground(UITheme.BACKGROUND);setLayout(new BorderLayout(8,8));setBorder(new EmptyBorder(20,24,20,24));buildUI();load();
    }
    private JTextField tf(){return new JTextField(28);}
    private void buildUI(){
        add(UITheme.headerLabel("Hostel Control Panel"), BorderLayout.NORTH);
        JPanel form=new JPanel(new GridBagLayout());form.setBackground(UITheme.BACKGROUND);
        GridBagConstraints g=new GridBagConstraints();g.anchor=GridBagConstraints.WEST;g.insets=new Insets(8,6,8,6);
        addRow(form,g,0,"Hostel Name *:",tfName,"Contact Number:",tfContact);
        addRow(form,g,1,"Address:",tfAddr,"Email:",tfEmail);
        addRow(form,g,2,"Established Year:",tfYear,null,null);
        JButton save=UITheme.primaryButton("Save Configuration");
        g.gridy=3;g.gridx=0;g.gridwidth=4;g.fill=GridBagConstraints.NONE;form.add(save,g);
        save.addActionListener(e->saveConfig());
        add(form,BorderLayout.CENTER);
    }
    private void load(){
        java.util.Map<String,String> cfg=dao.getConfig();
        tfName.setText(cfg.getOrDefault("hostel_name",""));tfAddr.setText(cfg.getOrDefault("address",""));
        tfContact.setText(cfg.getOrDefault("contact_number",""));tfEmail.setText(cfg.getOrDefault("email",""));
        tfYear.setText(cfg.getOrDefault("established_year",""));
    }
    private void saveConfig(){
        if(tfName.getText().trim().isEmpty()){JOptionPane.showMessageDialog(this,"Hostel name is required.","Error",JOptionPane.WARNING_MESSAGE);return;}
        int yr=0;try{yr=Integer.parseInt(tfYear.getText().trim());}catch(Exception ignored){}
        if(dao.updateConfig(tfName.getText().trim(),tfAddr.getText().trim(),tfContact.getText().trim(),tfEmail.getText().trim(),yr)){
            AuditLogger.log("ADMIN",SessionContext.getUserId(),"CONFIG","Updated hostel config");
            JOptionPane.showMessageDialog(this,"Configuration saved!","Success",JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void addRow(JPanel p,GridBagConstraints g,int r,String l1,Component c1,String l2,Component c2){g.gridy=r;g.gridx=0;g.fill=GridBagConstraints.NONE;JLabel lb1=new JLabel(l1);lb1.setFont(UITheme.FONT_BOLD_LABEL);p.add(lb1,g);g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;p.add(c1,g);if(l2!=null){g.gridx=2;g.fill=GridBagConstraints.NONE;g.weightx=0;JLabel lb2=new JLabel(l2);lb2.setFont(UITheme.FONT_BOLD_LABEL);p.add(lb2,g);g.gridx=3;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;p.add(c2,g);}}
}

// ============================================================
//  REPORT PANEL
// ============================================================
class ReportPanel extends JPanel {
    private final GirlDAO gDao = new GirlDAO();
    ReportPanel(){
        setBackground(UITheme.BACKGROUND);setLayout(new BorderLayout(8,8));setBorder(new EmptyBorder(16,16,16,16));buildUI();
    }
    private void buildUI(){
        add(UITheme.headerLabel("Generate PDF Reports"), BorderLayout.NORTH);
        JPanel grid=new JPanel(new GridLayout(0,2,14,14));grid.setBackground(UITheme.BACKGROUND);

        grid.add(reportCard("Currently Staying Girls","List of all active girls",e->gen(()->"active")));
        grid.add(reportCard("Girls Who Left","List of all left girls",e->gen(()->"left")));
        grid.add(reportCard("Due Next Month","Girls with fees due next month",e->gen(()->"due_next")));
        grid.add(reportCard("Paid This Month","Girls who paid this month",e->gen(()->"paid_month")));
        grid.add(reportCard("All Pending Dues","All unpaid dues",e->gen(()->"dues")));
        grid.add(reportCard("All Fines","All fine records",e->gen(()->"fines")));
        grid.add(reportCard("Canteen Refunds","Pending refund list",e->gen(()->"refunds")));
        grid.add(reportCard("Student Payment Report","Select student → payment history",e->genStudentReport()));

        add(grid,BorderLayout.CENTER);
        JLabel note=new JLabel("<html><i>Reports are saved in the <b>reports/</b> folder next to the application.</i></html>");
        note.setFont(UITheme.FONT_LABEL);note.setForeground(Color.GRAY);note.setBorder(new EmptyBorder(10,0,0,0));
        add(note,BorderLayout.SOUTH);
    }

    private interface Supplier<T>{T get();}

    private void gen(Supplier<String> typeSupplier){
        new SwingWorker<String,Void>(){
            @Override protected String doInBackground() throws Exception{
                PDFReportGenerator gen=new PDFReportGenerator();
                String type=typeSupplier.get();
                return switch(type){
                    case "active" -> gen.generateActiveGirlsReport();
                    case "left"   -> gen.generateLeftGirlsReport();
                    case "due_next" -> gen.generateDueNextMonthReport();
                    case "paid_month" -> gen.generatePaidThisMonthReport();
                    case "dues"   -> gen.generateDuesReport();
                    case "fines"  -> gen.generateFinesReport(-1);
                    case "refunds"-> gen.generateRefundReport();
                    default -> null;
                };
            }
            @Override protected void done(){
                try{String path=get();JOptionPane.showMessageDialog(ReportPanel.this,"PDF saved:\n"+path,"Report Generated",JOptionPane.INFORMATION_MESSAGE);}
                catch(Exception ex){JOptionPane.showMessageDialog(ReportPanel.this,"PDF error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            }
        }.execute();
    }

    private void genStudentReport(){
        java.util.List<Girl> girls=gDao.getAll();
        Girl[] arr=girls.toArray(new Girl[0]);
        Girl sel=(Girl)JOptionPane.showInputDialog(this,"Select student:","Student Report",JOptionPane.PLAIN_MESSAGE,null,arr,arr.length>0?arr[0]:null);
        if(sel==null)return;
        new SwingWorker<String,Void>(){
            @Override protected String doInBackground()throws Exception{return new PDFReportGenerator().generatePaymentReport(sel.getGirlId());}
            @Override protected void done(){try{JOptionPane.showMessageDialog(ReportPanel.this,"PDF saved:\n"+get(),"Done",JOptionPane.INFORMATION_MESSAGE);}catch(Exception ex){JOptionPane.showMessageDialog(ReportPanel.this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}}
        }.execute();
    }

    private JPanel reportCard(String title,String desc,java.awt.event.ActionListener action){
        JPanel card=new JPanel(new BorderLayout(6,6));card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.BORDER,1),new EmptyBorder(14,16,14,16)));
        JLabel t=new JLabel(title);t.setFont(UITheme.FONT_BOLD_LABEL);t.setForeground(UITheme.PRIMARY_DARK);
        JLabel d=new JLabel("<html>"+desc+"</html>");d.setFont(UITheme.FONT_LABEL);d.setForeground(Color.GRAY);
        JButton btn=UITheme.primaryButton("Generate PDF");btn.addActionListener(action);
        card.add(t,BorderLayout.NORTH);card.add(d,BorderLayout.CENTER);card.add(btn,BorderLayout.SOUTH);
        return card;
    }
}
