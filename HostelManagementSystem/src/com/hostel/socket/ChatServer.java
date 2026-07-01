package com.hostel.socket;

import com.hostel.util.AppConstants;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Multi-threaded chat server.
 * Run inside AdminDashboard. Handles broadcast and private messages.
 */
public class ChatServer {

    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    private ChatServerListener listener;
    private int port;

    public interface ChatServerListener {
        void onMessageReceived(ChatMessage msg);
        void onClientConnected(String name);
        void onClientDisconnected(String name);
        void onServerLog(String log);
    }

    public ChatServer(int port, ChatServerListener listener) {
        this.port = port;
        this.listener = listener;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        if (listener != null) listener.onServerLog("Chat server started on port " + port);
        new Thread(this::acceptLoop, "ChatServer-Accept").start();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler, "ChatClient-" + clientSocket.getPort()).start();
            } catch (IOException e) {
                if (running) { if (listener != null) listener.onServerLog("Accept error: " + e.getMessage()); }
            }
        }
    }

    public void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
    }

    public void broadcastFromServer(String message) {
        ChatMessage msg = new ChatMessage(ChatMessage.Type.BROADCAST, "Server", "SERVER", 0, 0, "ALL", message);
        broadcast(msg);
    }

    private void broadcast(ChatMessage msg) {
        for (ClientHandler ch : clients.values()) ch.send(msg);
        if (listener != null) listener.onMessageReceived(msg);
    }

    private void sendPrivate(ChatMessage msg) {
        String targetKey = msg.getReceiverName();
        ClientHandler target = clients.get(targetKey);
        if (target != null) {
            target.send(msg);
        }
        // Also echo back to sender
        ClientHandler sender = clients.get(msg.getSenderName());
        if (sender != null) sender.send(msg);
        if (listener != null) listener.onMessageReceived(msg);
    }

    // ---- Inner class: handles one connected client -----
    class ClientHandler implements Runnable {
        private final Socket socket;
        private ObjectOutputStream out;
        private String clientName = "Unknown";

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        void send(ChatMessage msg) {
            try { if (out != null) { out.writeObject(msg); out.flush(); } }
            catch (IOException e) { disconnect(); }
        }

        private void disconnect() {
            clients.remove(clientName);
            try { socket.close(); } catch (IOException ignored) {}
            ChatMessage leaveMsg = new ChatMessage(ChatMessage.Type.LEAVE, clientName, "SYSTEM", 0, 0, "ALL", clientName + " left the chat.");
            broadcast(leaveMsg);
            if (listener != null) listener.onClientDisconnected(clientName);
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                ChatMessage msg;
                while ((msg = (ChatMessage) in.readObject()) != null) {
                    if (msg.getType() == ChatMessage.Type.JOIN) {
                        clientName = msg.getSenderName();
                        clients.put(clientName, this);
                        if (listener != null) listener.onClientConnected(clientName);
                        broadcast(msg);
                    } else if (msg.getType() == ChatMessage.Type.TEXT && msg.getReceiverId() == 0) {
                        broadcast(msg);
                    } else if (msg.getType() == ChatMessage.Type.PRIVATE) {
                        sendPrivate(msg);
                    }
                }
            } catch (EOFException | SocketException ignored) {
            } catch (Exception e) {
                if (listener != null) listener.onServerLog("Handler error: " + e.getMessage());
            } finally {
                disconnect();
            }
        }
    }

    public Set<String> getConnectedClients() {
        return clients.keySet();
    }

    public boolean isRunning() { return running; }
}
