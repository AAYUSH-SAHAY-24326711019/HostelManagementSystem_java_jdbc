package com.hostel.socket;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    private Socket socket;
    private ObjectOutputStream out;
    private volatile boolean running = false;
    private final String host;
    private final int port;
    private final ChatClientListener listener;

    public interface ChatClientListener {
        void onMessageReceived(ChatMessage msg);
        void onConnected();
        void onDisconnected(String reason);
    }

    public ChatClient(String host, int port, ChatClientListener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        running = true;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        new Thread(this::receiveLoop, "ChatClientReceive").start();
        if (listener != null) listener.onConnected();
    }

    public void sendMessage(ChatMessage msg) {
        if (!running || out == null) return;
        try { out.writeObject(msg); out.flush(); }
        catch (IOException e) { disconnect("Send failed: " + e.getMessage()); }
    }

    public void disconnect(String reason) {
        running = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        if (listener != null) listener.onDisconnected(reason);
    }

    private void receiveLoop() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (running) {
                ChatMessage msg = (ChatMessage) in.readObject();
                if (listener != null && msg != null) listener.onMessageReceived(msg);
            }
        } catch (EOFException | java.net.SocketException ignored) {
        } catch (Exception e) {
            if (running && listener != null) listener.onDisconnected("Connection lost: " + e.getMessage());
        }
    }

    public boolean isConnected() { return running && socket != null && !socket.isClosed(); }
}
