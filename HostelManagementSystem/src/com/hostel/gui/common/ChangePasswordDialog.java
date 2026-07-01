package com.hostel.gui.common;

import com.hostel.util.PasswordUtil;
import com.hostel.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private boolean success = false;
    private String newPassword = null;

    private final JPasswordField tfCurrent = new JPasswordField(20);
    private final JPasswordField tfNew = new JPasswordField(20);
    private final JPasswordField tfConfirm = new JPasswordField(20);
    private final boolean requireCurrent;

    public ChangePasswordDialog(Frame owner, boolean requireCurrent) {
        super(owner, "Change Password", true);
        this.requireCurrent = requireCurrent;
        buildUI();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(UITheme.BACKGROUND);
        main.setBorder(new EmptyBorder(20, 28, 20, 28));

        JLabel title = UITheme.headerLabel("Set New Password");
        main.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 4, 6, 4);
        int row = 0;

        if (requireCurrent) {
            addField(form, gbc, row++, "Current Password:", tfCurrent);
        }
        addField(form, gbc, row++, "New Password:", tfNew);
        addField(form, gbc, row, "Confirm Password:", tfConfirm);

        JLabel hint = new JLabel("(Min 6 chars, include letters and numbers)");
        hint.setFont(UITheme.FONT_LABEL);
        hint.setForeground(Color.GRAY);

        main.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        south.setBackground(UITheme.BACKGROUND);
        JButton btnCancel = UITheme.secondaryButton("Cancel");
        JButton btnSave = UITheme.primaryButton("Save Password");
        south.add(hint);
        south.add(btnCancel);
        south.add(btnSave);
        main.add(south, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        getRootPane().setDefaultButton(btnSave);
        setContentPane(main);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD_LABEL);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private void onSave() {
        String newPwd = new String(tfNew.getPassword());
        String confirmPwd = new String(tfConfirm.getPassword());

        if (!newPwd.equals(confirmPwd)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!PasswordUtil.isStrongEnough(newPwd)) {
            JOptionPane.showMessageDialog(this, "Password too weak. Must be at least 6 characters with letters and numbers.", "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        newPassword = newPwd;
        success = true;
        dispose();
    }

    public boolean isSuccess() { return success; }
    public String getNewPassword() { return newPassword; }
    public String getCurrentPassword() { return new String(tfCurrent.getPassword()); }
}
