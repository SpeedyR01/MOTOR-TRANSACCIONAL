package service;

import model.Transaction;
import processor.ProcessorFactory;
import processor.TransactionProcessor;
import repository.TransactionRepository;
import validator.TransactionValidator;

public class TransactionService {
    private final TransactionValidator validator;
    private final TransactionRepository repository;
    
    public TransactionService(TransactionValidator validator, TransactionRepository repository){
        this.validator = validator;
        this.repository = repository;
    }
    
    public void handle(Transaction tx) throws ValidationException, ProcessingException, RepositoryException{
        validator.validate(tx);
        
        TransactionProcessor processor = ProcessorFactory.getProcessor(tx.getType());
        if(processor == null){
            throw new ProcessingException("No processor for type: " + tx.getType());
        }
        
        processor.process(tx);
        repository.save(tx);
    }
}
