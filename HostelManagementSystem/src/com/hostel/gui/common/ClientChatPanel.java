package com.hostel.gui.common;

import com.hostel.socket.ChatClient;
import com.hostel.socket.ChatMessage;
import com.hostel.util.AppConstants;
import com.hostel.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Reusable chat panel embedded in Girl, Parent, and Admin dashboards.
 * Connects to the chat server as a client.
 */
public class ClientChatPanel extends JPanel implements ChatClient.ChatClientListener {

    private final JTextArea chatArea = new JTextArea();
    private final JTextField messageField = new JTextField();
    private final JButton sendBtn;
    private final JButton connectBtn;
    private final JLabel statusLabel = new JLabel("Disconnected");

    private ChatClient client;
    private final String userName;
    private final String userRole;
    private final int userId;

    public ClientChatPanel(String userName, String userRole, int userId) {
        this.userName = userName;
        this.userRole = userRole;
        this.userId = userId;
        sendBtn = UITheme.primaryButton("Send");
        connectBtn = UITheme.secondaryButton("Connect");
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PRIMARY);
        topBar.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel title = new JLabel("Hostel Chat");
        title.setFont(UITheme.FONT_HEADER); title.setForeground(Color.WHITE);
        statusLabel.setFont(UITheme.FONT_LABEL); statusLabel.setForeground(new Color(200, 255, 200));
        topBar.add(title, BorderLayout.WEST);
        topBar.add(statusLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Chat area
        chatArea.setEditable(false);
        chatArea.setFont(UITheme.FONT_TABLE);
        chatArea.setLineWrap(true); chatArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        add(scroll, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(6, 0));
        inputPanel.setBackground(UITheme.BACKGROUND);
        messageField.setFont(UITheme.FONT_LABEL);
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(4, 8, 4, 8)));
        inputPanel.add(messageField, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnPanel.setBackground(UITheme.BACKGROUND);
        btnPanel.add(sendBtn); btnPanel.add(connectBtn);
        inputPanel.add(btnPanel, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Events
        sendBtn.addActionListener(e -> sendMessage());
        connectBtn.addActionListener(e -> toggleConnection());
        messageField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) sendMessage();
            }
        });
        sendBtn.setEnabled(false);
        messageField.setEnabled(false);
    }

    private void toggleConnection() {
        if (client != null && client.isConnected()) {
            client.disconnect("User disconnected");
        } else {
            connectToServer();
        }
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                client = new ChatClient(AppConstants.CHAT_SERVER_HOST, AppConstants.CHAT_SERVER_PORT, this);
                client.connect();
                // Send JOIN message
                ChatMessage join = new ChatMessage(ChatMessage.Type.JOIN, userName, userRole, userId, 0, "ALL", userName + " joined the chat.");
                client.sendMessage(join);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    appendChat("[System] Could not connect to chat server. Is the Admin running the server?");
                    statusLabel.setText("Disconnected");
                });
            }
        }, "ChatConnect").start();
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty() || client == null || !client.isConnected()) return;
        ChatMessage msg = new ChatMessage(ChatMessage.Type.TEXT, userName, userRole, userId, 0, "ALL", text);
        client.sendMessage(msg);
        messageField.setText("");
    }

    @Override
    public void onMessageReceived(ChatMessage msg) {
        SwingUtilities.invokeLater(() -> appendChat(msg.toString()));
    }

    @Override
    public void onConnected() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Connected");
            connectBtn.setText("Disconnect");
            sendBtn.setEnabled(true);
            messageField.setEnabled(true);
            appendChat("[System] Connected to hostel chat.");
        });
    }

    @Override
    public void onDisconnected(String reason) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Disconnected");
            connectBtn.setText("Connect");
            sendBtn.setEnabled(false);
            messageField.setEnabled(false);
            appendChat("[System] Disconnected: " + reason);
        });
    }

    private void appendChat(String text) {
        chatArea.append(text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
