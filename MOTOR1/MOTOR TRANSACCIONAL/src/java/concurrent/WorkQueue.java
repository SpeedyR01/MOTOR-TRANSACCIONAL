package concurrent;

import model.Transaction;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class WorkQueue {
    private final BlockingQueue<Transaction> queue;
    
    public WorkQueue(int capacity){
        if (capacity > 0) this.queue = new LinkedBlockingQueue<>(capacity);
        else this.queue = new LinkedBlockingQueue<>();
    }
    
    public boolean offer(Transaction tx){
        try{
            return queue.offer(tx, 2, TimeUnit.SECONDS);
            
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    public BlockingQueue<Transaction> getQueue(){
        return queue;
    }
}
