package com.hostel.gui.admin;

import com.hostel.dao.*;
import com.hostel.model.*;
import com.hostel.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class BillPaymentPanel extends JPanel {

    private final Admin admin;
    private final GirlDAO girlDAO = new GirlDAO();
    private final BillDAO billDAO = new BillDAO();

    private final JComboBox<Girl>   cbGirl     = new JComboBox<>();
    private final JComboBox<String> cbBillType = new JComboBox<>(new String[]{
        "MONTHLY_STAY","EMERGENCY_DEPOSIT","ELECTRICITY_DEPOSIT","WIFI_DEPOSIT","KITCHEN","FINE","OTHER"});
    private final JTextField tfAmount  = new JTextField(12);
    private final JTextField tfMonth   = new JTextField(4);
    private final JTextField tfYear    = new JTextField(6);
    private final JTextField tfDueDate = new JTextField(12);
    private final JTextField tfRemarks = new JTextField(22);

    private final DefaultTableModel billModel   = new DefaultTableModel();
    private final JTable            billTable   = new JTable(billModel);
    private final JComboBox<String> cbPayMode   = new JComboBox<>(new String[]{"CASH","CARD","UPI","BANK_TRANSFER","OTHER"});
    private final JTextField        tfPayAmount = new JTextField(12);

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");

    public BillPaymentPanel(Admin admin) {
        this.admin = admin;
        setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        loadGirls();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();

        // ── Tab 1: Generate Bill ──
        tabs.addTab("Generate Bill", buildBillForm());

        // ── Tab 2: Record Payment ──
        tabs.addTab("Record Payment", buildPaymentPanel());

        // ── Tab 3: View All Unpaid Bills ──
        tabs.addTab("All Unpaid Bills", buildAllUnpaid());

        add(UITheme.headerLabel("Bills & Payments"), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildBillForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints g = gbc();
        int r = 0;

        tfDueDate.setText(SDF.format(new java.util.Date()));
        java.util.Calendar cal = java.util.Calendar.getInstance();
        tfMonth.setText(String.valueOf(cal.get(java.util.Calendar.MONTH) + 1));
        tfYear.setText(String.valueOf(cal.get(java.util.Calendar.YEAR)));

        addRow(p, g, r++, "Select Student *:", cbGirl,     "Bill Type *:",  cbBillType);
        addRow(p, g, r++, "Amount (₹) *:",    tfAmount,    "Due Date (dd-MM-yyyy) *:", tfDueDate);
        addRow(p, g, r++, "Month (optional):", tfMonth,    "Year:",         tfYear);
        addRow(p, g, r++, "Remarks:",          tfRemarks,  null,            null);

        JButton btn = UITheme.primaryButton("Generate Bill");
        g.gridy = r; g.gridx = 0; g.gridwidth = 4; g.fill = GridBagConstraints.NONE;
        p.add(btn, g);
        btn.addActionListener(e -> generateBill());
        return p;
    }

    private JPanel buildPaymentPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top: select girl → load her bills
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        top.setBackground(UITheme.BACKGROUND);
        JComboBox<Girl> cbG = new JComboBox<>();
        loadGirlsInto(cbG);
        top.add(new JLabel("Select Student:")); top.add(cbG);
        JButton loadBtn = UITheme.secondaryButton("Load Bills");
        top.add(loadBtn);
        p.add(top, BorderLayout.NORTH);

        // Bill table
        billModel.setColumnIdentifiers(new String[]{"BillID","Type","Month","Amount","Due Date","Status"});
        billTable.setFont(UITheme.FONT_TABLE); billTable.setRowHeight(24);
        billTable.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        billTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(billTable), BorderLayout.CENTER);

        // Bottom: payment form
        JPanel payForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        payForm.setBackground(UITheme.BACKGROUND);
        payForm.setBorder(BorderFactory.createTitledBorder("Record Payment for Selected Bill"));
        payForm.add(new JLabel("Amount (₹):")); payForm.add(tfPayAmount);
        payForm.add(new JLabel("Mode:"));       payForm.add(cbPayMode);
        JButton payBtn = UITheme.primaryButton("Record Payment");
        payForm.add(payBtn);
        p.add(payForm, BorderLayout.SOUTH);

        loadBtn.addActionListener(e -> {
            Girl sel = (Girl) cbG.getSelectedItem(); if (sel == null) return;
            loadBillsForGirl(sel.getGirlId());
        });
        payBtn.addActionListener(e -> recordPayment(cbG));
        return p;
    }

    private JPanel buildAllUnpaid() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        DefaultTableModel m = new DefaultTableModel(
            new String[]{"BillID","Student","Type","Amount","Due Date","Status"}, 0);
        JTable t = new JTable(m); t.setFont(UITheme.FONT_TABLE); t.setRowHeight(24);
        t.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        JButton refreshBtn = UITheme.secondaryButton("Refresh");
        refreshBtn.addActionListener(e -> {
            m.setRowCount(0);
            for (Bill b : billDAO.getAllUnpaidBills()) {
                m.addRow(new Object[]{b.getBillId(),b.getGirlName(),b.getBillType(),
                    String.format("₹%.2f",b.getAmount()), b.getDueDate(), b.getStatus()});
            }
        });
        refreshBtn.doClick();
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT)); top.setBackground(UITheme.BACKGROUND);
        top.add(refreshBtn); p.add(top, BorderLayout.NORTH);
        return p;
    }

    private void generateBill() {
        Girl g = (Girl) cbGirl.getSelectedItem();
        if (g == null) { warn("Select a student."); return; }
        if (!ValidationUtil.isPositiveNumber(tfAmount.getText())) { warn("Enter a valid amount."); return; }
        Date dueDate;
        try { dueDate = new Date(SDF.parse(tfDueDate.getText().trim()).getTime()); }
        catch (Exception e) { warn("Invalid due date. Use dd-MM-yyyy."); return; }

        Bill b = new Bill();
        b.setGirlId(g.getGirlId()); b.setBillType((String) cbBillType.getSelectedItem());
        b.setAmount(Double.parseDouble(tfAmount.getText().trim()));
        b.setDueDate(dueDate);
        b.setGeneratedBy(admin.getAdminId());
        b.setRemarks(tfRemarks.getText().trim());
        if (!tfMonth.getText().trim().isEmpty()) b.setBillMonth(ValidationUtil.parseIntSafe(tfMonth.getText(), 0));
        if (!tfYear.getText().trim().isEmpty())  b.setBillYear(ValidationUtil.parseIntSafe(tfYear.getText(), 0));

        try {
            int id = billDAO.createBill(b);
            AuditLogger.log("ADMIN", admin.getAdminId(), "BILL_CREATED", "Bill ID=" + id + " for " + g.getName());
            JOptionPane.showMessageDialog(this, "Bill created successfully! Bill ID: " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
            tfAmount.setText(""); tfRemarks.setText("");
        } catch (Exception ex) {
            warn("Failed to create bill: " + ex.getMessage());
        }
    }

    private void loadBillsForGirl(int girlId) {
        billModel.setRowCount(0);
        for (Bill b : billDAO.getBillsByGirlId(girlId)) {
            billModel.addRow(new Object[]{b.getBillId(), b.getBillType(),
                b.getBillMonth() != null ? b.getBillMonth()+"/"+b.getBillYear() : "-",
                String.format("₹%.2f", b.getAmount()), b.getDueDate(), b.getStatus()});
        }
    }

    private void recordPayment(JComboBox<Girl> cbG) {
        int row = billTable.getSelectedRow();
        if (row < 0) { warn("Select a bill from the table."); return; }
        if (!ValidationUtil.isPositiveNumber(tfPayAmount.getText())) { warn("Enter valid amount."); return; }
        int billId = (int) billModel.getValueAt(row, 0);
        Girl g = (Girl) cbG.getSelectedItem(); if (g == null) return;

        Payment pay = new Payment();
        pay.setBillId(billId); pay.setGirlId(g.getGirlId());
        pay.setAmountPaid(Double.parseDouble(tfPayAmount.getText().trim()));
        pay.setPaymentDate(new Date(System.currentTimeMillis()));
        pay.setPaymentMode((String) cbPayMode.getSelectedItem());
        pay.setReceivedBy(admin.getAdminId());

        try {
            billDAO.recordPayment(pay);
            AuditLogger.log("ADMIN", admin.getAdminId(), "PAYMENT", "Payment for bill " + billId);
            JOptionPane.showMessageDialog(this, "Payment recorded!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadBillsForGirl(g.getGirlId()); tfPayAmount.setText("");
        } catch (Exception ex) {
            warn("Payment failed: " + ex.getMessage());
        }
    }

    private void loadGirls() { loadGirlsInto(cbGirl); }
    private void loadGirlsInto(JComboBox<Girl> cb) {
        cb.removeAllItems();
        for (Girl g : girlDAO.getAllActive()) cb.addItem(g);
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST; g.insets = new Insets(7, 6, 7, 6); return g;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String l1, Component c1, String l2, Component c2) {
        g.gridy = row; g.gridx = 0; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        JLabel lb1 = new JLabel(l1); lb1.setFont(UITheme.FONT_BOLD_LABEL); p.add(lb1, g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; p.add(c1, g);
        if (l2 != null) {
            g.gridx = 2; g.fill = GridBagConstraints.NONE; g.weightx = 0;
            JLabel lb2 = new JLabel(l2); lb2.setFont(UITheme.FONT_BOLD_LABEL); p.add(lb2, g);
            g.gridx = 3; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; p.add(c2, g);
        }
    }

    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE); }
}
