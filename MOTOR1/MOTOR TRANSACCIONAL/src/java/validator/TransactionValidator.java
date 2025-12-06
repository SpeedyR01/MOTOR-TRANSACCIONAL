package validator;

import model.Transaction;
import service.ValidationException;

public interface TransactionValidator {
    void validate(Transaction tx) throws ValidationException;
}
