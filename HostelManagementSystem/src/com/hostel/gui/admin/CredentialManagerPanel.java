package com.hostel.gui.admin;

import com.hostel.dao.*;
import com.hostel.model.*;
import com.hostel.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class CredentialManagerPanel extends JPanel {

    private final GirlDAO   girlDAO   = new GirlDAO();
    private final ParentDAO parentDAO = new ParentDAO();

    private final JComboBox<Girl>   cbGirl   = new JComboBox<>();
    private final JComboBox<Parent> cbParent = new JComboBox<>();
    private final JTextField tfGirlUsername   = new JTextField(20);
    private final JTextField tfParentUsername = new JTextField(20);
    private final JTextArea  logArea          = new JTextArea(8, 50);

    public CredentialManagerPanel() {
        setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        buildUI();
        loadGirls();
    }

    private void buildUI() {
        add(UITheme.headerLabel("Generate / Reset Login Credentials"), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(UITheme.BACKGROUND);

        // ── Girl credentials card ──
        JPanel girlCard = card("Student Login Account");
        girlCard.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();
        addRow(girlCard, g, 0, "Select Student:", cbGirl);
        addRow(girlCard, g, 1, "Username:", tfGirlUsername);
        JButton genGirlBtn = UITheme.primaryButton("Generate / Reset Student Credentials");
        g.gridy = 2; g.gridx = 0; g.gridwidth = 2; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.WEST;
        girlCard.add(genGirlBtn, g);
        genGirlBtn.addActionListener(e -> generateGirlCreds());
        cbGirl.addActionListener(e -> prefillGirlUsername());
        center.add(girlCard);
        center.add(Box.createVerticalStrut(14));

        // ── Parent credentials card ──
        JPanel parentCard = card("Parent / Guardian Login Account");
        parentCard.setLayout(new GridBagLayout());
        GridBagConstraints gp = gbc();
        addRow(parentCard, gp, 0, "Select Student:", cbGirl);  // same combo – no need to duplicate
        JComboBox<Girl> cbGirl2 = new JComboBox<>();
        loadGirlsInto(cbGirl2);
        addRow(parentCard, gp, 0, "Select Student:", cbGirl2);
        addRow(parentCard, gp, 1, "Select Parent:", cbParent);
        addRow(parentCard, gp, 2, "Username:", tfParentUsername);
        JButton genParentBtn = UITheme.primaryButton("Generate / Reset Parent Credentials");
        gp.gridy = 3; gp.gridx = 0; gp.gridwidth = 2; gp.fill = GridBagConstraints.NONE;
        parentCard.add(genParentBtn, gp);
        cbGirl2.addActionListener(e -> loadParents(cbGirl2, cbParent));
        genParentBtn.addActionListener(e -> generateParentCreds(cbGirl2, cbParent));
        center.add(parentCard);
        center.add(Box.createVerticalStrut(14));

        // ── Log area ──
        logArea.setEditable(false); logArea.setFont(UITheme.FONT_TABLE);
        logArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Credential Log"));
        center.add(scroll);

        add(center, BorderLayout.CENTER);
    }

    private void loadGirls() { loadGirlsInto(cbGirl); }

    private void loadGirlsInto(JComboBox<Girl> cb) {
        cb.removeAllItems();
        for (Girl g : girlDAO.getAllActive()) cb.addItem(g);
    }

    private void prefillGirlUsername() {
        Girl g = (Girl) cbGirl.getSelectedItem();
        if (g == null) return;
        tfGirlUsername.setText(g.getName().toLowerCase().replaceAll("\\s+","_") + "_" + g.getGirlId());
    }

    private void loadParents(JComboBox<Girl> cbG, JComboBox<Parent> cbP) {
        cbP.removeAllItems();
        Girl g = (Girl) cbG.getSelectedItem();
        if (g == null) return;
        for (Parent p : parentDAO.getByGirlId(g.getGirlId())) cbP.addItem(p);
    }

    private void generateGirlCreds() {
        Girl g = (Girl) cbGirl.getSelectedItem();
        if (g == null) { warn("Select a student."); return; }
        String username = tfGirlUsername.getText().trim();
        if (!ValidationUtil.isValidUsername(username)) { warn("Username must be 4-30 chars, letters/digits/_/."); return; }
        try {
            String tempPwd = girlDAO.createGirlCredentials(g.getGirlId(), username);
            logArea.append("=== STUDENT CREDENTIALS ===\n");
            logArea.append("Name    : " + g.getName() + "\n");
            logArea.append("Username: " + username + "\n");
            logArea.append("Temp Pwd: " + tempPwd + "  (student must change on first login)\n\n");
            AuditLogger.log("ADMIN", SessionContext.getUserId(), "GEN_CREDS", "Generated student creds for " + g.getName());
            JOptionPane.showMessageDialog(this,
                "Student credentials created!\n\nUsername: " + username + "\nTemp Password: " + tempPwd +
                "\n\nPlease share these with the student. She must change her password on first login.",
                "Credentials", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            warn("Error: " + ex.getMessage());
        }
    }

    private void generateParentCreds(JComboBox<Girl> cbG, JComboBox<Parent> cbP) {
        Girl g = (Girl) cbG.getSelectedItem();
        Parent p = (Parent) cbP.getSelectedItem();
        if (g == null || p == null) { warn("Select student and parent."); return; }
        String username = tfParentUsername.getText().trim();
        if (!ValidationUtil.isValidUsername(username)) { warn("Invalid username format."); return; }
        try {
            String tempPwd = parentDAO.createParentCredentials(p.getParentId(), g.getGirlId(), username);
            logArea.append("=== PARENT CREDENTIALS ===\n");
            logArea.append("Parent  : " + p.getName() + " (" + p.getRelationType() + ")\n");
            logArea.append("Student : " + g.getName() + "\n");
            logArea.append("Username: " + username + "\n");
            logArea.append("Temp Pwd: " + tempPwd + "\n\n");
            AuditLogger.log("ADMIN", SessionContext.getUserId(), "GEN_PARENT_CREDS", "Generated parent creds for " + p.getName());
            JOptionPane.showMessageDialog(this,
                "Parent credentials created!\n\nUsername: " + username + "\nTemp Password: " + tempPwd,
                "Credentials", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            warn("Error: " + ex.getMessage());
        }
    }

    private JPanel card(String title) {
        JPanel p = new JPanel();
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UITheme.BORDER), title,
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                UITheme.FONT_BOLD_LABEL, UITheme.PRIMARY_DARK),
            new EmptyBorder(8, 12, 10, 12)));
        return p;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST; g.insets = new Insets(6, 4, 6, 4); return g;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent comp) {
        g.gridy = row; g.gridx = 0; g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label); lbl.setFont(UITheme.FONT_BOLD_LABEL); p.add(lbl, g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; p.add(comp, g);
    }

    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE); }
}
