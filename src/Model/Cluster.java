package Model;

import java.util.ArrayList;

public class Cluster {
    public int id;
    public float coorX;
    public float coorY;
    public int weight;
    public ArrayList<Order> orders;
    public Cluster(int id, float coorX,float coorY, int weight){
        this.id = id;
        this.coorY = coorY;
        this.coorX = coorX;
        this.weight = weight;
        orders = new ArrayList<>();
    }
}
