package network;

import concurrent.ThreadPoolManager;
import service.TransactionService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketServer {
    private final int port;
    private final ThreadPoolManager pool; // puede ser null (se usará hilo por conexión)
    private final TransactionService txService;

    private ServerSocket serverSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread acceptThread;

    public SocketServer(int port, ThreadPoolManager pool, TransactionService txService) {
        if (port <= 0 || port > 65535) throw new IllegalArgumentException("Puerto inválido: " + port);
        if (txService == null) throw new IllegalArgumentException("TransactionService no puede ser null");
        this.port = port;
        this.pool = pool;
        this.txService = txService;
    }

    /**
     * Inicia el servidor de sockets y devuelve inmediatamente.
     * Lanza IOException si no puede abrir el socket.
     */
    public synchronized void start() throws IOException {
        if (running.get()) throw new IllegalStateException("SocketServer ya está en ejecución");
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        running.set(true);

        acceptThread = new Thread(this::acceptLoop, "socket-accept-thread");
        acceptThread.setDaemon(false);
        acceptThread.start();
        System.out.println("[SocketServer] Iniciado en puerto " + port);
    }

    private void acceptLoop() {
        try {
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    Socket client = serverSocket.accept();
                    if (!running.get()) {
                        try { client.close(); } catch (IOException ignore) {}
                        break;
                    }

                    // Crear handler por conexión. Usamos protocolo basado en líneas (SocketClientHandler)
                    SocketClientHandler handler = new SocketClientHandler(client, txService, true);

                    if (pool != null) {
                        pool.submit(handler); // delega ejecución al pool
                    } else {
                        Thread t = new Thread(handler, "socket-client-" + client.getPort());
                        t.setDaemon(false);
                        t.start();
                    }
                } catch (IOException ioe) {
                    // Si estamos en shutdown, es normal recibir excepciones al cerrar el socket
                    if (running.get()) {
                        System.err.println("[SocketServer] Error aceptando conexión: " + ioe.getMessage());
                    } else {
                        // esperado al cerrar serverSocket
                        break;
                    }
                }
            }
        } finally {
            // Asegurar que el servidor quede en estado detenido si salimos del loop por cualquier motivo
            running.set(false);
            try {
                if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            } catch (IOException ignore) {}
            System.out.println("[SocketServer] Loop de aceptación finalizado.");
        }
    }

    /**
     * Detiene el servidor de forma ordenada: marca stop, cierra el ServerSocket e interrumpe el hilo de aceptación.
     */
    public synchronized void stop() {
        if (!running.get()) return;
        System.out.println("[SocketServer] Deteniendo servidor...");
        running.set(false);

        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            System.err.println("[SocketServer] Error cerrando ServerSocket: " + e.getMessage());
        }

        if (acceptThread != null) {
            acceptThread.interrupt();
            try {
                acceptThread.join(2000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("[SocketServer] Detenido.");
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getPort() {
        return port;
    }
}