package repository;

import model.Transaction;
import service.RepositoryException;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Implementación JDBC simple. Lee resources/db.properties del classpath.
 * Nota: cada llamada abre su propia Connection; no compartir Connection entre hilos.
 */
public class JdbcTransactionRepository implements TransactionRepository {
    private final String url;
    private final String user;
    private final String pass;
    private final String driver;

    public JdbcTransactionRepository() throws RepositoryException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) throw new RepositoryException("No se encontró resources/db.properties", null);
            Properties p = new Properties();
            p.load(in);
            url = p.getProperty("jdbc.url");
            user = p.getProperty("jdbc.user");
            pass = p.getProperty("jdbc.password");
            driver = p.getProperty("jdbc.driver");
            if (driver != null && !driver.isEmpty()) Class.forName(driver);
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception e) {
            throw new RepositoryException("Error cargando propiedades DB", e);
        }
    }

    @Override
    public void save(Transaction tx) throws RepositoryException {
        String sql = "INSERT INTO transactions (id, type, amount, payload) VALUES (?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tx.getId());
            ps.setString(2, tx.getType());
            ps.setDouble(3, tx.getAmount());
            ps.setString(4, tx.getPayLoad());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Error guardando transacción", e);
        }
    }
}