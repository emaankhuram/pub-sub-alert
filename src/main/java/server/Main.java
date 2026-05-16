package server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.InputStream;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        System.out.println("=== PUB-SUB-ALERT Starting on port " + port + " ===");

        // Start AlertServer internally
        new Thread(() -> {
            try { new AlertServer().start(); }
            catch (Exception e) { e.printStackTrace(); }
        }).start();
        Thread.sleep(500);

        // Jetty server — handles both HTTP and WebSocket on same port
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.setHost("0.0.0.0");
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Serve dashboard.html at root
        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws java.io.IOException {
                InputStream html = Main.class.getResourceAsStream("/dashboard.html");
                if (html == null) { resp.sendError(404, "dashboard.html not found"); return; }
                resp.setContentType("text/html; charset=UTF-8");
                html.transferTo(resp.getOutputStream());
            }
        }), "/");

        // WebSocket endpoint at /ws
        JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) -> {
            wsContainer.setMaxTextMessageSize(65536);
            wsContainer.addMapping("/ws", (req, resp) -> new WebSocketBridge());
        });

        server.setHandler(context);
        server.start();
        System.out.println("=== Server running on port " + port + " ===");
        server.join();
    }
}
