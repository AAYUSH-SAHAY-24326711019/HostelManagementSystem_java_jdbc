package com.hostel.gui.admin;

import com.hostel.dao.GirlDAO;
import com.hostel.model.Girl;
import com.hostel.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GirlEditDialog extends JDialog {

    private final Girl girl;
    private final Runnable onSaved;

    private final JTextField tfName   = tf(), tfMobile = tf(), tfEmail  = tf();
    private final JTextField tfAadhar = tf(), tfCollege= tf(), tfAddr   = tf();
    private final JTextField tfAge    = new JTextField(6);

    public GirlEditDialog(Window owner, Girl g, Runnable onSaved) {
        super(owner, "Edit Student – " + g.getName(), ModalityType.APPLICATION_MODAL);
        this.girl = g;
        this.onSaved = onSaved;
        setSize(500, 380);
        setLocationRelativeTo(owner);
        setResizable(false);
        buildUI();
        populate();
    }

    private JTextField tf() { return new JTextField(22); }

    private void populate() {
        tfName.setText(girl.getName());   tfMobile.setText(girl.getMobile());
        tfEmail.setText(girl.getEmail()); tfAadhar.setText(girl.getAadharNumber());
        tfCollege.setText(girl.getCollegeAddress()); tfAddr.setText(girl.getAddress());
        tfAge.setText(String.valueOf(girl.getAge()));
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBackground(UITheme.BACKGROUND);
        main.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(6, 4, 6, 4);

        int r = 0;
        addRow(form, gbc, r++, "Full Name *",    tfName,   "Age *", tfAge);
        addRow(form, gbc, r++, "Mobile No. *",   tfMobile, "Email", tfEmail);
        addRow(form, gbc, r++, "Aadhar No. *",   tfAadhar, "College", tfCollege);
        addRow(form, gbc, r,   "Address",         tfAddr,   null, null);

        main.add(UITheme.headerLabel("Edit Student Details"), BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        south.setBackground(UITheme.BACKGROUND);
        JButton cancel = UITheme.secondaryButton("Cancel");
        JButton save   = UITheme.primaryButton("Save Changes");
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> saveChanges());
        south.add(cancel); south.add(save);
        main.add(south, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String l1, JComponent c1, String l2, JComponent c2) {
        g.gridy = row;
        g.gridx = 0; p.add(bold(l1), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; p.add(c1, g);
        if (l2 != null) {
            g.gridx = 2; g.fill = GridBagConstraints.NONE; p.add(bold(l2), g);
            g.gridx = 3; g.fill = GridBagConstraints.HORIZONTAL; p.add(c2, g);
        }
    }

    private JLabel bold(String t) { JLabel l = new JLabel(t); l.setFont(UITheme.FONT_BOLD_LABEL); return l; }

    private void saveChanges() {
        if (tfName.getText().trim().isEmpty()) { warn("Name is required."); return; }
        if (!ValidationUtil.isValidMobile(tfMobile.getText().trim())) { warn("Invalid mobile number."); return; }
        if (!ValidationUtil.isValidAge(tfAge.getText().trim())) { warn("Age must be 16–60."); return; }

        girl.setName(tfName.getText().trim());   girl.setMobile(tfMobile.getText().trim());
        girl.setEmail(tfEmail.getText().trim());  girl.setAadharNumber(tfAadhar.getText().trim());
        girl.setCollegeAddress(tfCollege.getText().trim()); girl.setAddress(tfAddr.getText().trim());
        girl.setAge(Integer.parseInt(tfAge.getText().trim()));

        if (new GirlDAO().updateGirl(girl)) {
            AuditLogger.log("ADMIN", SessionContext.getUserId(), "EDIT_GIRL", "Edited girl ID=" + girl.getGirlId());
            JOptionPane.showMessageDialog(this, "Details updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            onSaved.run();
            dispose();
        } else {
            warn("Update failed. Please try again.");
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }
}
