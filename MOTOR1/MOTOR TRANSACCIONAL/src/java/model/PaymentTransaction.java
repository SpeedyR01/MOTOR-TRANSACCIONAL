package model;

public class PaymentTransaction implements Transaction {
    private final String id;
    private final double amount;
    private final String payLoad;
    
    public PaymentTransaction(String id, double amount, String payLoad){
        this.id = id;
        this.amount = amount;
        this.payLoad = payLoad;
    }
    
    @Override
    public String getId() {return id;}
    @Override
    public String getType() {return "PAYMENT";}
    @Override
    public double getAmount() {return amount;}
    @Override
    public String getPayLoad() {return payLoad;}
}