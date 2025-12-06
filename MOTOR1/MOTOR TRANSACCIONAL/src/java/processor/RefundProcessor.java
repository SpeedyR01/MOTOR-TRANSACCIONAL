package processor;

import model.Transaction;
import service.ProcessingException;

public class RefundProcessor implements TransactionProcessor{
    
    @Override
    public void process(Transaction tx) throws ProcessingException{
        System.out.println("[RefundProcessor] Procesando reembolso id=" + tx.getId() + " amount=" + tx.getAmount());
    }
    
    @Override
    public String supportedType(){
        return "REFUND";
    }
}
