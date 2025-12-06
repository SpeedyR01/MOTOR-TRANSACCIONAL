package validator;

import model.Transaction;
import service.ValidationException;

public class BasicTransactionValidator implements TransactionValidator{
    @Override
    public void validate(Transaction tx) throws ValidationException{
        if(tx== null) throw new ValidationException("Transaccion nula");
        if(tx.getId() == null || tx.getId().trim().isEmpty()) throw new ValidationException("Id requerido");
        if(tx.getAmount() <= 0) throw new ValidationException("Amount debe ser mayor a 0.");
        if(tx.getType() == null || tx.getType().trim().isEmpty()) throw new ValidationException("Tipo requerido");
    }
}
