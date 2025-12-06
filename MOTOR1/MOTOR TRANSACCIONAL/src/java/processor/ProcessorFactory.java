package processor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*Aqui se implementan los nuevos tipos de transacciones*/

public final class ProcessorFactory {
    private static final Map<String, TransactionProcessor> registry = new ConcurrentHashMap<>();
    
    private ProcessorFactory(){}
    
    public static void register(TransactionProcessor processor){
        if(processor == null) return;
        registry.put(processor.supportedType(), processor);
    }
    
    public static TransactionProcessor getProcessor(String type){
        return registry.get(type);
    }
}
