import Model.Delivery;
import Model.Order;

import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.pow;


public class TSP {
    int id;
    ArrayList<Order> orders;
    Order depot;
    Order[] routeC;
    Input input;
    Order[] route;
    public TSP(int id, ArrayList<Order> orders, Order depot,Input input){
        this.id = id;
        this.orders = orders;
        for(Order o: this.orders){
            o.served = false;
        }
        this.depot = depot;
        this.input = input;
        routeC = new Order[orders.size()+1];
        route = new Order[orders.size()+1];
        route[0] = depot;
        computeMinDis();
        //dp = new int[orders.size()][1<<orders.size()];
        head = new Delivery(depot);
        tail = new Delivery(depot);
        head.next = tail;
        tail.prev = head;
        current = head;
    }
    double obj=0;
    double result=1000000000.0;
    double min_dis=1000000000.0;
    void computeMinDis(){
        for(Order o1: orders){
            if(min_dis>input.dis[depot.id-1][o1.id-1]){
                min_dis = input.dis[depot.id-1][o1.id-1];
            }
            for(Order o2: orders){
                if(o2==o1) continue;
                else{
                    if(min_dis>input.dis[o1.id-1][o2.id-1]){
                        min_dis = input.dis[o1.id-1][o2.id-1];
                    }
                }
            }
        }
    }
    void backtrack(int k){
        if(k==orders.size()+1){
            if(obj+input.dis[routeC[k-1].id-1][routeC[0].id-1]<result){
                result = obj + input.dis[routeC[k-1].id-1][routeC[0].id-1];
                for(int i=0;i<orders.size()+1;i++){
                    route[i] = routeC[i];
                }
            }
        }
        if(obj+ min_dis*(orders.size()-k+1)>=result){
            return;
        }
        for(Order o: orders){
            if(!o.served){
                routeC[k] = o;
                o.served = true;
                obj+=input.dis[routeC[k-1].id-1][routeC[k].id-1];
                backtrack(k+1);
                obj -= input.dis[routeC[k-1].id-1][routeC[k].id-1];
                o.served = false;
            }
        }
    }
    double solve(){
        routeC[0] = depot;
        if(!orders.isEmpty()){
        backtrack(1);
//        System.out.println("Cost is "+result);
//        for(int i=0;i<orders.size()+1;i++){
//            System.out.print(route[i].id+" ");
//        }
//        System.out.println(route[0].id);
        obj = result;
        return result;
        }
        else return 0;
    }
    Delivery head;
    Delivery tail;
    Delivery current;
    public void nearestNeighbour(){
        for(int i=0;i<orders.size();i++){
            double min_dis = 1000000000.0;
            int orderIndex = -1;
            for(Order o: orders){
                if(!o.served){
                    double tmp = input.dis[current.order.id-1][o.id-1];
                    if(tmp<min_dis){
                        min_dis = tmp;
                        orderIndex = orders.indexOf(o);
                    }
                }
            }
            if(orderIndex!=-1){
                orders.get(orderIndex).served = true;
                Delivery d = new Delivery(orders.get(orderIndex));
                current.next = d;
                d.prev = current;
                tail.prev = d;
                d.next = tail;
                current = d;
            }
        }
        //printSol();
    }
    private int iterationNumber = Settings.swapRepeat;
    //This method attempts to improve an existing route by swapping the nodes
    //At every iteration, we need to find only one swap that will improve the route, it needn't be the best improvement
    public void improveBySwaping(){
        for(int i=0;i<iterationNumber;i++){
            Delivery d1 = head.next;
            while(d1!=tail.prev){
                boolean cont = true;
                Delivery d2 = d1.next;
                while(d2!=tail){
                    double costBefore = distanceOfDelivery(d1)+distanceOfDelivery(d2);
                    swap(d1,d2);
                    double costAfter = distanceOfDelivery(d1)+distanceOfDelivery(d2);
                    if(costBefore>costAfter){
                        cont = false;
                        break;
                    }
                    swap(d1,d2);
                    d2 = d2.next;
                }
                if(!cont){
                    break;
                }
                else{
                    d1=d1.next;
                }
            }
        }
    }
    private double distanceOfDelivery(Delivery d){
        return input.dis[d.prev.order.id-1][d.order.id-1]+input.dis[d.order.id-1][d.next.order.id-1];
    }
    //This method swaps 2 deliveries in the route
    private void swap(Delivery d1, Delivery d2){
        if(d1.next!=d2&&d2.next!=d1) {
            Delivery tmp1 = d1.prev;
            Delivery tmp2 = d1.next;
            d1.prev = d2.prev;
            d2.prev.next = d1;
            d1.next = d2.next;
            d2.next.prev = d1;
            d2.prev = tmp1;
            tmp1.next = d2;
            d2.next = tmp2;
            tmp2.prev = d2;
        }
        else{
            if(d1.next==d2){
            d2.prev = d1.prev;
            d1.next = d2.next;
            d2.next = d1;
            d1.prev = d2;
            }
            if(d2.next==d1){
                d1.prev = d2.prev;
                d2.next = d1.next;
                d1.next = d2;
                d2.prev = d1;
            }
        }
    }
    public void cheapeastInsertion(){
        for(Order o: orders){
            double min_cost = 1000000000.0;
            Delivery d = head;
            Delivery toAdd = null;
            //Compute the cost of inserting an order d into the current route
            while(d!=tail){
                double tmp = input.dis[d.order.id-1][o.id-1]+input.dis[o.id-1][d.next.order.id-1]-input.dis[d.order.id-1][d.next.order.id-1];
                if(tmp<min_cost){
                    toAdd = d;
                }
                d=d.next;
            }
            Delivery delivery = new Delivery(o);
            delivery.prev = toAdd;
            delivery.next = toAdd.next;
            toAdd.next.prev = delivery;
            toAdd.next = delivery;
        }
    }
    public boolean improveRoute() {
       //System.out.println(1);
        Delivery d1 = head;
        while (d1 != tail) {
            Delivery d2 = d1.next;
            while (d2 != tail) {
                if (feasible(d1, d2)) {
                    d1.next.next = d2.next;
                    d2.prev = d1.next;
                    d1.next = d2;
                    d2.prev = d1;
                    return true;
                }
                d2=d2.next;
            }
            d1=d1.next;
        }
        return false;
    }
    boolean feasible(Delivery d1, Delivery d2){
        if(input.dis[d1.order.id-1][d2.order.id-1]+input.dis[d1.next.order.id-1][d2.next.order.id-1]-input.dis[d1.order.id-1][d1.next.order.id-1]-input.dis[d2.order.id-1][d2.next.order.id-1]<0){
            return true;
        }
        return false;
    }
    public void computeCost(){
        Delivery d = head;
        while(d!=tail){
            obj += input.dis[d.order.id-1][d.next.order.id-1];
            d=d.next;
        }
    }
    public void printSol(){
        Delivery d = head;
        while(d!=null){
            System.out.print(d.order.id+" ");
            d=d.next;
        }
        System.out.println();
        System.out.println("Cost is "+obj);
    }
}
