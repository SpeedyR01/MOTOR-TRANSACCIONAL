package concurrent;

import java.util.concurrent.*;

/*Encapsula el ExcutorService reusable y seguro para la app.*/

public class ThreadPoolManager {
    private final ExecutorService executor;
    
    public ThreadPoolManager(int poolSize) {
        this.executor = new ThreadPoolExecutor(
                poolSize, poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
                    private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
                    @Override public Thread newThread(Runnable r) {
                        Thread t = defaultFactory.newThread(r);
                        t.setDaemon(false);
                        t.setName("tx-worker-" + t.getId());
                        return t;
                    }
                });
    }
    
    public void submit(Runnable r){
        executor.submit(r);
    }
    
    public void shutdown(){
        executor.shutdown();
        try{
            if(!executor.awaitTermination(10, TimeUnit.SECONDS)){
                executor.shutdownNow();
            }
        } catch (InterruptedException e){
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
