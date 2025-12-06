package repository;

import model.Transaction;
import service.RepositoryException;

public interface TransactionRepository {
    void save(Transaction tx) throws RepositoryException;
}
