package server;

import common.Alert;
import common.AlertType;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AlertServer {

    private static final int PORT = 9090;
    private final List<PrintWriter> subscribers = new CopyOnWriteArrayList<>();

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("[AlertServer] Started on port " + PORT);

        while (true) {
            Socket client = serverSocket.accept();
            new Thread(() -> handleClient(client)).start();
        }
    }

    private void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String role = in.readLine();
            String name = in.readLine();

            if ("SUBSCRIBER".equals(role)) {
                subscribers.add(out);
                System.out.println("[AlertServer] Subscriber joined: " + name + " (total: " + subscribers.size() + ")");
                out.println("CONNECTED: Subscribed to all alerts.");
                while (in.readLine() != null) { /* keep alive */ }
                subscribers.remove(out);
                System.out.println("[AlertServer] Subscriber left: " + name);

            } else if ("PUBLISHER".equals(role)) {
                System.out.println("[AlertServer] Publisher joined: " + name);
                String line;
                while ((line = in.readLine()) != null) {
                    String[] parts = line.split("\\|", 2);
                    if (parts.length == 2) {
                        try {
                            AlertType type = AlertType.valueOf(parts[0]);
                            Alert alert = new Alert(type, parts[1], name);
                            broadcast(alert);
                            out.println("SENT: Broadcasted to " + subscribers.size() + " subscribers.");
                        } catch (IllegalArgumentException e) {
                            out.println("ERROR: Unknown alert type.");
                        }
                    }
                }
            }
        } catch (IOException ignored) {}
    }

    private void broadcast(Alert alert) {
        System.out.println("[AlertServer] Broadcasting: " + alert);
        for (PrintWriter sub : subscribers) {
            sub.println(alert.toString());
        }
    }
}
