package com.hostel.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Centralised colors/fonts/helpers so every form in the app looks consistent
 * without copy-pasting styling code everywhere.
 */
public final class UITheme {

    private UITheme() { }

    public static final Color PRIMARY = new Color(123, 31, 91);      // deep pink/maroon - hostel theme
    public static final Color PRIMARY_DARK = new Color(90, 20, 66);
    public static final Color ACCENT = new Color(255, 183, 94);
    public static final Color BACKGROUND = new Color(250, 247, 249);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(45, 45, 45);
    public static final Color SUCCESS = new Color(46, 139, 87);
    public static final Color DANGER = new Color(196, 50, 50);
    public static final Color BORDER = new Color(225, 215, 220);

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_LABEL = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BOLD_LABEL = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_TABLE = new Font("SansSerif", Font.PLAIN, 12);

    public static void applyGlobalLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back silently to the default cross-platform L&F.
        }
        UIManager.put("control", BACKGROUND);
        UIManager.put("Button.font", FONT_BUTTON);
        UIManager.put("Label.font", FONT_LABEL);
        UIManager.put("Table.font", FONT_TABLE);
        UIManager.put("TableHeader.font", FONT_BOLD_LABEL);
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, PRIMARY, Color.WHITE);
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DANGER, Color.WHITE);
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, Color.WHITE, PRIMARY);
        b.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(FONT_BUTTON);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
    }

    public static JLabel titleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(PRIMARY_DARK);
        return l;
    }

    public static JLabel headerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADER);
        l.setForeground(PRIMARY_DARK);
        return l;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)));
        return p;
    }
}
