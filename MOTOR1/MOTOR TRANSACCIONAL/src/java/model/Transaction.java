package model;

/*Contrato basico para una transaccion*/

public interface Transaction {
    String getId();
    String getType();
    double getAmount();
    String getPayLoad();
}
