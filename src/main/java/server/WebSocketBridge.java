package server;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketBridge {

    private static final Map<Session, Socket> alertSockets = new ConcurrentHashMap<>();
    private static final Map<Session, Object[]> publisherStreams = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("[Bridge] Connected: " + session.getRemoteAddress());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            Socket alertSocket = alertSockets.get(session);

            if (alertSocket == null) {
                String[] parts = message.split("\\|", 2);
                String role = parts[0];
                String name = parts.length > 1 ? parts[1] : "Anonymous";
                System.out.println("[Bridge] " + role + ": " + name);

                alertSocket = new Socket("localhost", 9090);
                alertSockets.put(session, alertSocket);

                PrintWriter alertOut = new PrintWriter(alertSocket.getOutputStream(), true);
                BufferedReader alertIn = new BufferedReader(new InputStreamReader(alertSocket.getInputStream()));

                alertOut.println(role);
                alertOut.println(name);

                if ("SUBSCRIBER".equals(role)) {
                    String confirmation = alertIn.readLine();
                    session.getRemote().sendString("STATUS|" + confirmation);
                    final Session s = session;
                    new Thread(() -> {
                        try {
                            String line;
                            while ((line = alertIn.readLine()) != null && s.isOpen()) {
                                s.getRemote().sendString("ALERT|" + line);
                            }
                        } catch (IOException ignored) {}
                    }).start();
                } else {
                    publisherStreams.put(session, new Object[]{alertOut, alertIn});
                }

            } else {
                Object[] streams = publisherStreams.get(session);
                if (streams != null) {
                    PrintWriter alertOut = (PrintWriter) streams[0];
                    BufferedReader alertIn = (BufferedReader) streams[1];
                    alertOut.println(message);
                    String response = alertIn.readLine();
                    if (response != null) session.getRemote().sendString("STATUS|" + response);
                }
            }
        } catch (Exception e) {
            System.out.println("[Bridge] Error: " + e.getMessage());
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int code, String reason) {
        Socket s = alertSockets.remove(session);
        publisherStreams.remove(session);
        if (s != null) try { s.close(); } catch (IOException ignored) {}
        System.out.println("[Bridge] Disconnected");
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.out.println("[Bridge] WS Error: " + error.getMessage());
    }
}
