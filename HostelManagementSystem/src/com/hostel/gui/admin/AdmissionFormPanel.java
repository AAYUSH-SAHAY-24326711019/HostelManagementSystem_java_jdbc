package com.hostel.gui.admin;

import com.hostel.dao.*;
import com.hostel.model.*;
import com.hostel.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class AdmissionFormPanel extends JPanel {

    // ── Girl fields ──────────────────────────────────────────────────────
    private final JTextField tfName     = tf(), tfMobile  = tf(), tfEmail   = tf();
    private final JTextField tfAadhar   = tf(), tfCollege = tf(), tfAddr    = tf();
    private final JTextField tfAge      = tf(4), tfDob    = tf(12);
    private final JTextField tfAdmDate  = tf(12);
    private final JComboBox<String> cbGender = new JComboBox<>(new String[]{"Female","Other"});

    // ── Room / Plan ──────────────────────────────────────────────────────
    private final JComboBox<RoomPlan>    cbPlan    = new JComboBox<>();
    private final JComboBox<Room>        cbRoom    = new JComboBox<>();
    private final JComboBox<KitchenPlan> cbKitchen = new JComboBox<>();

    // ── Fee structure ────────────────────────────────────────────────────
    private final JTextField tfStayBill = tf(10), tfEmer = tf(10), tfElec = tf(10);
    private final JTextField tfWifi     = tf(10), tfExtra = tf(10), tfKitFee = tf(10);

    // ── Father fields ────────────────────────────────────────────────────
    private final JTextField tfFName = tf(), tfFMob = tf(), tfFEmail = tf(), tfFAad = tf(), tfFOcc = tf(), tfFAddr = tf();

    // ── Mother fields ────────────────────────────────────────────────────
    private final JTextField tfMName = tf(), tfMMob = tf(), tfMEmail = tf(), tfMAad = tf(), tfMOcc = tf(), tfMAddr = tf();

    // ── Guardian (optional) ──────────────────────────────────────────────
    private final JCheckBox  cbHasGuardian = new JCheckBox("Has Guardian / Additional Contact");
    private final JTextField tfGName = tf(), tfGMob = tf(), tfGEmail = tf(), tfGAad = tf(), tfGOcc = tf(), tfGAddr = tf();

    private final RoomDAO    roomDAO = new RoomDAO();
    private final GirlDAO    girlDAO = new GirlDAO();
    private final ParentDAO  parentDAO = new ParentDAO();
    private final FeeStructureDAO fsDAO = new FeeStructureDAO();

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");

    public AdmissionFormPanel() {
        setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());
        buildUI();
        loadCombos();
    }

    private JTextField tf()     { JTextField t = new JTextField(22); t.setFont(UITheme.FONT_LABEL); return t; }
    private JTextField tf(int w){ JTextField t = new JTextField(w);  t.setFont(UITheme.FONT_LABEL); return t; }

    private void buildUI() {
        JLabel title = UITheme.headerLabel("New Student Admission");
        title.setBorder(new EmptyBorder(14, 16, 8, 16));
        add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UITheme.BACKGROUND);
        body.setBorder(new EmptyBorder(0, 14, 14, 14));

        body.add(buildGirlSection());
        body.add(Box.createVerticalStrut(10));
        body.add(buildPlanSection());
        body.add(Box.createVerticalStrut(10));
        body.add(buildFeeSection());
        body.add(Box.createVerticalStrut(10));
        body.add(buildParentSection("Father Details", tfFName, tfFMob, tfFEmail, tfFAad, tfFOcc, tfFAddr));
        body.add(Box.createVerticalStrut(10));
        body.add(buildParentSection("Mother Details", tfMName, tfMMob, tfMEmail, tfMAad, tfMOcc, tfMAddr));
        body.add(Box.createVerticalStrut(10));
        body.add(buildGuardianSection());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        south.setBackground(UITheme.BACKGROUND);
        JButton resetBtn = UITheme.secondaryButton("Reset Form");
        JButton saveBtn  = UITheme.primaryButton("Save Admission");
        resetBtn.addActionListener(e -> resetForm());
        saveBtn.addActionListener(e -> saveAdmission());
        south.add(resetBtn); south.add(saveBtn);
        add(south, BorderLayout.SOUTH);
    }

    private JPanel buildGirlSection() {
        JPanel p = section("Student Personal Details");
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();
        int r = 0;
        addRow(p, g, r++, "Full Name *",    tfName,   "Age *",        tfAge);
        addRow(p, g, r++, "Date of Birth (dd-MM-yyyy)", tfDob, "Gender", cbGender);
        addRow(p, g, r++, "Mobile No. *",   tfMobile, "Email",        tfEmail);
        addRow(p, g, r++, "Aadhar No. *",   tfAadhar, "College Name", tfCollege);
        addRow(p, g, r++, "Address",        tfAddr,   "Admission Date (dd-MM-yyyy) *", tfAdmDate);
        // prefill today
        tfAdmDate.setText(SDF.format(new java.util.Date()));
        return p;
    }

    private JPanel buildPlanSection() {
        JPanel p = section("Room & Canteen Plan");
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();
        addRow(p, g, 0, "Room Plan *", cbPlan, "Room *", cbRoom);
        addRow(p, g, 1, "Kitchen/Canteen Plan", cbKitchen, null, null);
        cbPlan.addActionListener(e -> refreshRooms());
        return p;
    }

    private JPanel buildFeeSection() {
        JPanel p = section("Fee Structure (set at admission)");
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();
        addRow(p, g, 0, "Monthly Stay Bill ₹ *", tfStayBill, "Emergency Deposit ₹ *", tfEmer);
        addRow(p, g, 1, "Electricity Deposit ₹ *", tfElec, "WiFi Deposit ₹",         tfWifi);
        addRow(p, g, 2, "Plan Extra Charge ₹",    tfExtra, "Kitchen Monthly ₹",       tfKitFee);
        return p;
    }

    private JPanel buildParentSection(String title, JTextField nm, JTextField mob, JTextField em,
                                      JTextField aad, JTextField occ, JTextField addr) {
        JPanel p = section(title);
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();
        addRow(p, g, 0, "Full Name *", nm,  "Mobile No. *", mob);
        addRow(p, g, 1, "Email",       em,  "Aadhar No.",   aad);
        addRow(p, g, 2, "Occupation",  occ, "Address",      addr);
        return p;
    }

    private JPanel buildGuardianSection() {
        JPanel p = section("Guardian Details (Optional)");
        p.setLayout(new BorderLayout(0, 8));
        cbHasGuardian.setFont(UITheme.FONT_BOLD_LABEL);
        cbHasGuardian.setBackground(UITheme.CARD_BG);
        p.add(cbHasGuardian, BorderLayout.NORTH);

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(UITheme.CARD_BG);
        GridBagConstraints g = gbc();
        addRow(inner, g, 0, "Full Name", tfGName, "Mobile No.", tfGMob);
        addRow(inner, g, 1, "Email",     tfGEmail,"Aadhar No.", tfGAad);
        addRow(inner, g, 2, "Occupation",tfGOcc,  "Address",    tfGAddr);
        inner.setVisible(false);
        p.add(inner, BorderLayout.CENTER);
        cbHasGuardian.addActionListener(e -> inner.setVisible(cbHasGuardian.isSelected()));
        return p;
    }

    private void loadCombos() {
        cbPlan.removeAllItems();
        for (RoomPlan rp : roomDAO.getAllPlans()) cbPlan.addItem(rp);
        cbKitchen.removeAllItems();
        cbKitchen.addItem(null);
        for (KitchenPlan kp : roomDAO.getAllKitchenPlans()) cbKitchen.addItem(kp);
        refreshRooms();
    }

    private void refreshRooms() {
        cbRoom.removeAllItems();
        RoomPlan selected = (RoomPlan) cbPlan.getSelectedItem();
        if (selected == null) return;
        List<Room> rooms = roomDAO.getAvailableRooms(selected.getPlanId());
        if (rooms.isEmpty()) cbRoom.addItem(null);
        for (Room r : rooms) cbRoom.addItem(r);
    }

    private void saveAdmission() {
        if (!validateForm()) return;
        try {
            // Build girl object
            Girl g = new Girl();
            g.setName(tfName.getText().trim());
            g.setGender((String) cbGender.getSelectedItem());
            g.setAge(Integer.parseInt(tfAge.getText().trim()));
            try { g.setDob(new Date(SDF.parse(tfDob.getText().trim()).getTime())); } catch (Exception ignored) {}
            g.setMobile(tfMobile.getText().trim()); g.setEmail(tfEmail.getText().trim());
            g.setAadharNumber(tfAadhar.getText().trim()); g.setCollegeAddress(tfCollege.getText().trim());
            g.setAddress(tfAddr.getText().trim());
            g.setAdmissionDate(new Date(SDF.parse(tfAdmDate.getText().trim()).getTime()));
            RoomPlan rp = (RoomPlan) cbPlan.getSelectedItem();
            Room room = (Room) cbRoom.getSelectedItem();
            KitchenPlan kp = (KitchenPlan) cbKitchen.getSelectedItem();
            if (rp != null) g.setPlanId(rp.getPlanId());
            if (room != null) g.setRoomId(room.getRoomId());
            if (kp != null) g.setKitchenPlanId(kp.getKitchenPlanId());

            int girlId = girlDAO.insertGirl(g);

            // Increment room occupancy
            if (room != null) roomDAO.incrementOccupancy(room.getRoomId());

            // Fee structure
            FeeStructure fs = new FeeStructure();
            fs.setGirlId(girlId);
            fs.setMonthlyStayBill(Double.parseDouble(tfStayBill.getText().trim()));
            fs.setEmergencyDeposit(Double.parseDouble(tfEmer.getText().trim()));
            fs.setElectricityDeposit(Double.parseDouble(tfElec.getText().trim()));
            fs.setWifiDeposit(ValidationUtil.parseDoubleSafe(tfWifi.getText(), 0));
            fs.setPlanExtraCharge(ValidationUtil.parseDoubleSafe(tfExtra.getText(), 0));
            fs.setKitchenCharge(ValidationUtil.parseDoubleSafe(tfKitFee.getText(), 0));
            fs.setEffectiveDate(g.getAdmissionDate());
            fsDAO.saveOrUpdate(fs);

            // Parents
            saveParent(girlId, "FATHER", tfFName, tfFMob, tfFEmail, tfFAad, tfFOcc, tfFAddr);
            saveParent(girlId, "MOTHER", tfMName, tfMMob, tfMEmail, tfMAad, tfMOcc, tfMAddr);
            if (cbHasGuardian.isSelected()) {
                saveParent(girlId, "GUARDIAN", tfGName, tfGMob, tfGEmail, tfGAad, tfGOcc, tfGAddr);
            }

            AuditLogger.log("ADMIN", SessionContext.getUserId(), "ADMISSION", "New girl admitted: " + g.getName() + " ID=" + girlId);
            JOptionPane.showMessageDialog(this,
                "Admission saved successfully!\nStudent ID: " + girlId +
                "\n\nPlease go to 'Credentials' tab to create the login account.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving admission:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveParent(int girlId, String rel, JTextField nm, JTextField mob, JTextField em,
                            JTextField aad, JTextField occ, JTextField addr) throws Exception {
        Parent p = new Parent();
        p.setGirlId(girlId); p.setRelationType(rel);
        p.setName(nm.getText().trim()); p.setMobile(mob.getText().trim());
        p.setEmail(em.getText().trim()); p.setAadharNumber(aad.getText().trim());
        p.setOccupation(occ.getText().trim()); p.setAddress(addr.getText().trim());
        parentDAO.insertParent(p);
    }

    private boolean validateForm() {
        if (tfName.getText().trim().isEmpty()) { err("Student name is required."); return false; }
        if (!ValidationUtil.isValidMobile(tfMobile.getText().trim())) { err("Invalid student mobile number."); return false; }
        if (!ValidationUtil.isValidAadhar(tfAadhar.getText().trim())) { err("Invalid student Aadhar (12 digits)."); return false; }
        if (!ValidationUtil.isValidAge(tfAge.getText().trim())) { err("Age must be between 16 and 60."); return false; }
        try { SDF.parse(tfAdmDate.getText().trim()); } catch (Exception e) { err("Invalid admission date. Use dd-MM-yyyy."); return false; }
        if (!ValidationUtil.isPositiveNumber(tfStayBill.getText())) { err("Monthly stay bill must be a positive number."); return false; }
        if (!ValidationUtil.isPositiveNumber(tfEmer.getText()))  { err("Emergency deposit must be a positive number."); return false; }
        if (!ValidationUtil.isPositiveNumber(tfElec.getText()))  { err("Electricity deposit must be a positive number."); return false; }
        if (tfFName.getText().trim().isEmpty()) { err("Father's name is required."); return false; }
        if (!ValidationUtil.isValidMobile(tfFMob.getText().trim())) { err("Invalid Father mobile number."); return false; }
        if (tfMName.getText().trim().isEmpty()) { err("Mother's name is required."); return false; }
        if (!ValidationUtil.isValidMobile(tfMMob.getText().trim())) { err("Invalid Mother mobile number."); return false; }
        if (cbRoom.getSelectedItem() == null) { err("No room available for selected plan. Please add rooms."); return false; }
        return true;
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void resetForm() {
        for (JTextField tf : new JTextField[]{tfName,tfMobile,tfEmail,tfAadhar,tfCollege,tfAddr,tfAge,tfDob,
                tfFName,tfFMob,tfFEmail,tfFAad,tfFOcc,tfFAddr,
                tfMName,tfMMob,tfMEmail,tfMAad,tfMOcc,tfMAddr,
                tfGName,tfGMob,tfGEmail,tfGAad,tfGOcc,tfGAddr,
                tfStayBill,tfEmer,tfElec,tfWifi,tfExtra,tfKitFee}) tf.setText("");
        tfAdmDate.setText(SDF.format(new java.util.Date()));
        cbHasGuardian.setSelected(false);
        loadCombos();
    }

    // ── Layout helpers ──────────────────────────────────────────────────
    private JPanel section(String title) {
        JPanel p = new JPanel();
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UITheme.BORDER), title,
                TitledBorder.LEFT, TitledBorder.TOP, UITheme.FONT_BOLD_LABEL, UITheme.PRIMARY_DARK),
            new EmptyBorder(8, 12, 10, 12)));
        return p;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST; g.insets = new Insets(5, 6, 5, 6);
        return g;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String l1, Component c1, String l2, Component c2) {
        g.gridy = row;
        g.gridx = 0; g.fill = GridBagConstraints.NONE;
        JLabel lbl1 = new JLabel(l1); lbl1.setFont(UITheme.FONT_BOLD_LABEL);
        p.add(lbl1, g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        p.add(c1, g);
        if (l2 != null && c2 != null) {
            g.gridx = 2; g.fill = GridBagConstraints.NONE; g.weightx = 0;
            JLabel lbl2 = new JLabel(l2); lbl2.setFont(UITheme.FONT_BOLD_LABEL);
            p.add(lbl2, g);
            g.gridx = 3; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
            p.add(c2, g);
        }
    }
}
