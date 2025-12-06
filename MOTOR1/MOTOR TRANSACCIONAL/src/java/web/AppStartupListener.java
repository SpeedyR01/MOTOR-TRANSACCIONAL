package web;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import processor.PaymentProcessor;
import processor.ProcessorFactory;
import processor.RefundProcessor;
import repository.JdbcTransactionRepository;
import service.TransactionService;
import validator.BasicTransactionValidator;
import concurrent.ThreadPoolManager;
import network.SocketServer;

import java.io.IOException;

/**
 * Inicializa processors, repository, service, thread-pool y socket server (Jakarta).
 */
@WebListener
public class AppStartupListener implements ServletContextListener {
    public static final String CTX_TX_SERVICE = "txService";
    public static final String CTX_THREADPOOL = "threadPool";
    public static final String CTX_SOCKET_SERVER = "socketServer";

    private ThreadPoolManager threadPool;
    private SocketServer socketServer;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Registrar processors
        ProcessorFactory.register(new PaymentProcessor());
        ProcessorFactory.register(new RefundProcessor());

        try {
            JdbcTransactionRepository repo = new JdbcTransactionRepository();
            TransactionService txService = new TransactionService(new BasicTransactionValidator(), repo);

            // Thread pool
            threadPool = new ThreadPoolManager(10);

            // Socket server en puerto 9000
            socketServer = new SocketServer(9000, threadPool, txService);

            // Guardar en contexto
            sce.getServletContext().setAttribute(CTX_TX_SERVICE, txService);
            sce.getServletContext().setAttribute(CTX_THREADPOOL, threadPool);
            sce.getServletContext().setAttribute(CTX_SOCKET_SERVER, socketServer);

            // Iniciar servidor de sockets
            socketServer.start();

            System.out.println("[AppStartupListener] Inicialización completa.");
        } catch (IOException ioe) {
            throw new RuntimeException("Error iniciando SocketServer", ioe);
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando la aplicación", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[AppStartupListener] Shutdown: cerrando recursos...");
        if (socketServer != null) {
            socketServer.stop();
        }
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }
}