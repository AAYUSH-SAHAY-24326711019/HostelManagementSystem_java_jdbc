package com.hostel.gui.admin;

import com.hostel.dao.*;
import com.hostel.model.*;
import com.hostel.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class GirlListPanel extends JPanel {

    private final GirlDAO girlDAO = new GirlDAO();
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel();
    private final JComboBox<String> filterBox = new JComboBox<>(new String[]{"Active Girls", "Left Girls", "All Girls"});
    private final JTextField searchField = new JTextField(18);
    private List<Girl> currentList;

    public GirlListPanel() {
        setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // ── Top bar ──
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        top.setBackground(UITheme.BACKGROUND);
        top.add(new JLabel("Show:")); top.add(filterBox);
        top.add(new JLabel("Search:")); top.add(searchField);
        JButton searchBtn  = UITheme.primaryButton("Search");
        JButton refreshBtn = UITheme.secondaryButton("Refresh");
        top.add(searchBtn); top.add(refreshBtn);
        filterBox.addActionListener(e -> loadData());
        searchBtn.addActionListener(e -> filterTable(searchField.getText()));
        refreshBtn.addActionListener(e -> loadData());
        add(top, BorderLayout.NORTH);

        // ── Table ──
        String[] cols = {"ID","Name","Mobile","Aadhar","Room","Plan","Admission","Status"};
        model.setColumnIdentifiers(cols);
        table.setModel(model);
        table.setFont(UITheme.FONT_TABLE);
        table.setRowHeight(26);
        table.getTableHeader().setFont(UITheme.FONT_BOLD_LABEL);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        add(scroll, BorderLayout.CENTER);

        // ── Action buttons ──
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        south.setBackground(UITheme.BACKGROUND);
        JButton viewBtn   = UITheme.primaryButton("View Details");
        JButton editBtn   = UITheme.secondaryButton("Edit");
        JButton leftBtn   = UITheme.dangerButton("Mark As Left");
        south.add(viewBtn); south.add(editBtn); south.add(leftBtn);
        add(south, BorderLayout.SOUTH);

        viewBtn.addActionListener(e -> viewSelected());
        editBtn.addActionListener(e -> editSelected());
        leftBtn.addActionListener(e -> markLeft());
    }

    private void loadData() {
        model.setRowCount(0);
        String filter = (String) filterBox.getSelectedItem();
        if ("Active Girls".equals(filter))    currentList = girlDAO.getAllActive();
        else if ("Left Girls".equals(filter)) currentList = girlDAO.getAllLeft();
        else                                  currentList = girlDAO.getAll();
        for (Girl g : currentList) addRow(g);
    }

    private void addRow(Girl g) {
        model.addRow(new Object[]{
            g.getGirlId(), g.getName(), g.getMobile(), g.getAadharNumber(),
            g.getRoomNumber() != null ? g.getRoomNumber() : "-",
            g.getPlanName() != null ? g.getPlanName() : "-",
            g.getAdmissionDate(), g.getStatus()
        });
    }

    private void filterTable(String txt) {
        model.setRowCount(0);
        String q = txt.trim().toLowerCase();
        for (Girl g : currentList) {
            if (g.getName().toLowerCase().contains(q) || g.getMobile().contains(q) || g.getAadharNumber().contains(q))
                addRow(g);
        }
    }

    private Girl selectedGirl() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this,"Please select a girl first."); return null; }
        int id = (int) model.getValueAt(row, 0);
        return currentList.stream().filter(g -> g.getGirlId() == id).findFirst().orElse(null);
    }

    private void viewSelected() {
        Girl g = selectedGirl(); if (g == null) return;
        new GirlDetailDialog(SwingUtilities.getWindowAncestor(this), g).setVisible(true);
    }

    private void editSelected() {
        Girl g = selectedGirl(); if (g == null) return;
        new GirlEditDialog(SwingUtilities.getWindowAncestor(this), g, this::loadData).setVisible(true);
    }

    private void markLeft() {
        Girl g = selectedGirl(); if (g == null) return;
        if (!"ACTIVE".equals(g.getStatus())) { JOptionPane.showMessageDialog(this,"Girl has already left."); return; }
        int c = JOptionPane.showConfirmDialog(this,"Mark " + g.getName() + " as LEFT?","Confirm",JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        girlDAO.markGirlLeft(g.getGirlId(), new Date(System.currentTimeMillis()));
        new RoomDAO().decrementOccupancy(g.getRoomId());
        AuditLogger.log("ADMIN", SessionContext.getUserId(), "MARK_LEFT", "Girl marked left: " + g.getName());
        loadData();
        JOptionPane.showMessageDialog(this,"Marked as left successfully.");
    }
}
