package com.hostel;

import com.hostel.gui.common.LoginFrame;
import com.hostel.util.AppConstants;
import com.hostel.util.DatabaseInitializer;
import com.hostel.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * ═══════════════════════════════════════════════════════════════
 *  GIRLS HOSTEL MANAGEMENT SYSTEM — Main Entry Point
 *  Final Year MCA Project
 * ═══════════════════════════════════════════════════════════════
 *
 *  HOW TO RUN IN ECLIPSE
 *  ─────────────────────
 *  1. Import this project: File → Import → Existing Projects into Workspace
 *  2. Add both JARs to the build path:
 *       lib/mysql-connector-java-8.0.30.jar
 *       lib/pdfbox-1.8.16.jar
 *     Right-click project → Build Path → Add External Archives
 *  3. Edit db.properties in the project root with your MySQL credentials.
 *  4. Import sql/hostel_management.sql into MySQL Workbench.
 *  5. Run this file (HostelApp.java) as a Java Application.
 *
 *  Default Admin login: admin / Admin@123
 */
public class HostelApp {

    public static void main(String[] args) {
        // ── 1. Ensure reports folder exists ──────────────────────────────
        new File(AppConstants.REPORTS_DIR).mkdirs();

        // ── 2. Apply look and feel on EDT ────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            UITheme.applyGlobalLookAndFeel();
            showSplash();
        });
    }

    private static void showSplash() {
        // Splash screen while DB check runs
        JWindow splash = new JWindow();
        splash.setSize(460, 280);
        splash.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.PRIMARY);
        panel.setBorder(BorderFactory.createLineBorder(UITheme.PRIMARY_DARK, 3));

        JLabel hostelName = new JLabel("Girls Hostel Management System", SwingConstants.CENTER);
        hostelName.setFont(new Font("SansSerif", Font.BOLD, 20));
        hostelName.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Final Year MCA Project", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 13));
        subtitle.setForeground(new Color(255, 220, 240));

        JLabel status = new JLabel("Connecting to database...", SwingConstants.CENTER);
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        status.setForeground(new Color(200, 255, 200));

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setForeground(UITheme.ACCENT);
        bar.setBorderPainted(false);

        JPanel center = new JPanel(new GridLayout(4, 1, 0, 8));
        center.setBackground(UITheme.PRIMARY);
        center.setBorder(BorderFactory.createEmptyBorder(40, 30, 20, 30));
        center.add(hostelName);
        center.add(subtitle);
        center.add(status);
        center.add(bar);

        JLabel version = new JLabel("v" + AppConstants.APP_VERSION + "  |  Java + JDBC + Swing + Sockets + PDFBox",
            SwingConstants.CENTER);
        version.setFont(new Font("SansSerif", Font.PLAIN, 11));
        version.setForeground(new Color(180, 160, 175));
        version.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        panel.add(center, BorderLayout.CENTER);
        panel.add(version, BorderLayout.SOUTH);
        splash.setContentPane(panel);
        splash.setVisible(true);

        // Run DB check in background
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return DatabaseInitializer.runStartupChecks();
            }

            @Override
            protected void done() {
                splash.dispose();
                try {
                    String error = get();
                    if (error != null) {
                        // Show friendly DB error and exit
                        JOptionPane.showMessageDialog(null,
                            error,
                            "Database Connection Failed",
                            JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    } else {
                        // All good — open login screen
                        new LoginFrame().setVisible(true);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                        "Startup error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        };
        worker.execute();
    }
}
