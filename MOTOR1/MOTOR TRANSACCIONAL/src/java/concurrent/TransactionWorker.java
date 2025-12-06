package concurrent;

import model.Transaction;
import service.*;

import java.util.concurrent.BlockingQueue;

public class TransactionWorker implements Runnable{
    private final BlockingQueue<Transaction> queue;
    private final TransactionService service;
    private volatile boolean running = true;
    
    public TransactionWorker(BlockingQueue<Transaction> queue, TransactionService service){
        this.queue = queue;
        this.service = service;
    }
    
    @Override
    public void run(){
        while(running && !Thread.currentThread().isInterrupted()){
            try{
                Transaction tx = queue.take();
                try{
                    service.handle(tx);
                } catch (ValidationException | ProcessingException | RepositoryException e){
                    System.err.println("[TransactionWorker] Error proceando tx " + tx.getId() + ": " + e.getMessage());
                }
            } catch (InterruptedException ie){
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public void stop(){
        running = false;
    }
}
