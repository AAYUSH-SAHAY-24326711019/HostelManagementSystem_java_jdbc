package com.hostel.gui.admin;

import com.hostel.dao.*;
import com.hostel.model.*;
import com.hostel.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/** Read-only detail view of a girl and her parents. */
public class GirlDetailDialog extends JDialog {

    public GirlDetailDialog(Window owner, Girl g) {
        super(owner, "Student Details – " + g.getName(), ModalityType.APPLICATION_MODAL);
        setSize(560, 520);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(UITheme.BACKGROUND);
        main.setBorder(new EmptyBorder(16, 20, 16, 20));

        main.add(section("Student Info", new String[][]{
            {"Name",g.getName()},{"Gender",g.getGender()},{"Age",String.valueOf(g.getAge())},
            {"Mobile",g.getMobile()},{"Email",g.getEmail()},
            {"Aadhar",g.getAadharNumber()},{"College",g.getCollegeAddress()},
            {"Address",g.getAddress()},{"Room",g.getRoomNumber()},
            {"Plan",g.getPlanName()},{"Kitchen",g.getKitchenPlanName()},
            {"Admission",String.valueOf(g.getAdmissionDate())},{"Status",g.getStatus()}
        }));
        main.add(Box.createVerticalStrut(10));

        List<Parent> parents = new ParentDAO().getByGirlId(g.getGirlId());
        for (Parent p : parents) {
            main.add(section(p.getRelationType() + " Details", new String[][]{
                {"Name",p.getName()},{"Mobile",p.getMobile()},
                {"Email",p.getEmail()},{"Aadhar",p.getAadharNumber()},
                {"Occupation",p.getOccupation()},{"Address",p.getAddress()}
            }));
            main.add(Box.createVerticalStrut(8));
        }

        FeeStructure fs = new FeeStructureDAO().getByGirlId(g.getGirlId());
        if (fs != null) {
            main.add(section("Fee Structure", new String[][]{
                {"Monthly Stay","₹ "+fs.getMonthlyStayBill()},
                {"Emergency Dep.","₹ "+fs.getEmergencyDeposit()},
                {"Electricity Dep.","₹ "+fs.getElectricityDeposit()},
                {"WiFi Deposit","₹ "+fs.getWifiDeposit()},
                {"Plan Extra","₹ "+fs.getPlanExtraCharge()},
                {"Kitchen","₹ "+fs.getKitchenCharge()}
            }));
        }

        JButton close = UITheme.primaryButton("Close");
        close.addActionListener(e -> dispose());
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btn.setBackground(UITheme.BACKGROUND);
        btn.add(close);
        main.add(btn);

        JScrollPane scroll = new JScrollPane(main);
        scroll.setBorder(null);
        setContentPane(scroll);
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
}
