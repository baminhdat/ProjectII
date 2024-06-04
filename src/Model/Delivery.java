package Model;

public class Delivery {
    public Order order;
    public Delivery prev;
    public Delivery next;
    public Delivery(Order o ){
        this.order = o;
        prev = null;
        next = null;
    }
}
