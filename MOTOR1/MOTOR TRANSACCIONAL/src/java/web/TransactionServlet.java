package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.PaymentTransaction;
import model.RefundTransaction;
import service.ProcessingException;
import service.RepositoryException;
import service.TransactionService;
import service.ValidationException;
import util.IdGenerator;

import java.io.IOException;

/**
 * Servlet para recibir transacciones vía HTTP (usa Jakarta).
 * Asegúrate de que en ServletContext esté TransactionService (AppStartupListener lo pone).
 */
@WebServlet("/transactions")
public class TransactionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        TransactionService txService = (TransactionService) getServletContext().getAttribute(AppStartupListener.CTX_TX_SERVICE);
        if (txService == null) {
            resp.setStatus(500);
            resp.getWriter().write("Servicio no inicializado");
            return;
        }

        String type = req.getParameter("type");
        String amountStr = req.getParameter("amount");
        String payload = req.getParameter("payload");

        try {
            double amount = Double.parseDouble(amountStr);
            String id = IdGenerator.generate();

            if ("PAYMENT".equalsIgnoreCase(type)) {
                PaymentTransaction tx = new PaymentTransaction(id, amount, payload);
                txService.handle(tx);
            } else if ("REFUND".equalsIgnoreCase(type)) {
                RefundTransaction tx = new RefundTransaction(id, amount, payload);
                txService.handle(tx);
            } else {
                resp.setStatus(400);
                resp.getWriter().write("Tipo no soportado");
                return;
            }

            resp.setStatus(200);
            resp.getWriter().write("Transacción procesada: id=" + id);
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write("Amount inválido");
        } catch (ValidationException | ProcessingException | RepositoryException e) {
            resp.setStatus(500);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }
}