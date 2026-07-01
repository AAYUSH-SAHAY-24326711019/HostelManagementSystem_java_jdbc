package com.hostel.gui.admin;

import com.hostel.dao.*;
import com.hostel.model.Admin;
import com.hostel.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminHomePanel extends JPanel {
    private final Admin admin;

    public AdminHomePanel(Admin admin) {
        this.admin = admin;
        setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel welcome = UITheme.titleLabel("Welcome, " + admin.getFullName() + "!");
        welcome.setBorder(new EmptyBorder(0, 0, 16, 0));
        add(welcome, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 3, 16, 16));
        cards.setBackground(UITheme.BACKGROUND);

        GirlDAO gDao = new GirlDAO();
        BillDAO bDao = new BillDAO();
        ComplaintDAO cDao = new ComplaintDAO();

        int activeGirls = gDao.getAllActive().size();
        int leftGirls = gDao.getAllLeft().size();
        int pendingDues = bDao.getAllPendingDues().size();
        int openComplaints = (int) cDao.getAllComplaints().stream().filter(c -> "OPEN".equals(c.getStatus())).count();
        int dueFees = gDao.getGirlsDueNextMonth().size();
        int paidThisMonth = gDao.getGirlsPaidThisMonth().size();

        cards.add(statCard("Currently Staying", String.valueOf(activeGirls), UITheme.PRIMARY, "Girls"));
        cards.add(statCard("Left Girls", String.valueOf(leftGirls), new Color(100, 100, 130), "Total"));
        cards.add(statCard("Pending Dues", String.valueOf(pendingDues), UITheme.DANGER, "Bills"));
        cards.add(statCard("Open Complaints", String.valueOf(openComplaints), new Color(200, 100, 0), "Issues"));
        cards.add(statCard("Due Next Month", String.valueOf(dueFees), new Color(60, 120, 180), "Girls"));
        cards.add(statCard("Paid This Month", String.valueOf(paidThisMonth), UITheme.SUCCESS, "Girls"));

        add(cards, BorderLayout.CENTER);

        JLabel hint = new JLabel("<html><i>Use the left navigation tabs to manage all hostel operations.</i></html>");
        hint.setFont(UITheme.FONT_LABEL); hint.setForeground(Color.GRAY);
        hint.setBorder(new EmptyBorder(16, 0, 0, 0));
        add(hint, BorderLayout.SOUTH);
    }

    private JPanel statCard(String title, String value, Color accent, String unit) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2),
                new EmptyBorder(18, 20, 18, 20)));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
        valLbl.setForeground(accent);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_BOLD_LABEL);
        titleLbl.setForeground(UITheme.TEXT_DARK);

        JLabel unitLbl = new JLabel(unit);
        unitLbl.setFont(UITheme.FONT_LABEL);
        unitLbl.setForeground(Color.GRAY);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(unitLbl, BorderLayout.SOUTH);
        return card;
    }
}
