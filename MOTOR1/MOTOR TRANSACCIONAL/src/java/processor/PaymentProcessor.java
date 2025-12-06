package processor;

import model.Transaction;
import service.ProcessingException;

public class PaymentProcessor implements TransactionProcessor{
    
    @Override
    public void process(Transaction tx) throws ProcessingException{
        //Llama a un gateway, validaciones adicionales.
        System.out.println("[PaymentProcessor] Procesando pago id=" + tx.getId()+" amount=" + tx.getAmount());
        //Manda un error si ocurre algo inesperado.
        
    }
    
    @Override
    public String supportedType(){
        return "PAYMENT";
    }
}
