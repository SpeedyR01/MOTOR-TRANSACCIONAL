package processor;

import model.Transaction;
import service.ProcessingException;

/*Interfaz para pocesadores de transaccion.*/

public interface TransactionProcessor {
    void process(Transaction tx) throws ProcessingException;
    String supportedType();
}
