package network;

import model.Transaction;
import service.TransactionService;
import util.JsonUtils;

import java.io.*;
import java.net.Socket;

/**
 * Handler por conexión; cada línea es un JSON con la transacción.
 * Usa JsonUtils (Gson) para parsear y delega al TransactionService (puede ser síncrono o asíncrono).
 */
public class SocketClientHandler implements Runnable {
    private final Socket socket;
    private final TransactionService txService;
    private final boolean async; // si true, el servidor debe usar ThreadPoolManager para ejecutar handlers

    public SocketClientHandler(Socket socket, TransactionService txService, boolean async) {
        this.socket = socket;
        this.txService = txService;
        this.async = async;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String line;
            while ((line = in.readLine()) != null) {
                Transaction tx = JsonUtils.parseTransaction(line);
                if (tx == null) {
                    out.write(SocketProtocol.MSG_ERROR + " invalid payload\n");
                    out.flush();
                    continue;
                }

                try {
                    // Procesamiento síncrono
                    txService.handle(tx);
                    out.write(SocketProtocol.MSG_OK + " id=" + tx.getId() + "\n");
                } catch (Exception e) {
                    out.write(SocketProtocol.MSG_ERROR + " " + e.getMessage() + "\n");
                }
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("[SocketClientHandler] IO error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignore) {}
        }
    }
}