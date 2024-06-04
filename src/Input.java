import Model.Cluster;
import Model.Order;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Input {
    //String problemName;
    String s;
    final String filePath = "Instances/";
    int dimension;
    int vehiclesNumber;
    int capacity;
    ArrayList<Order> orders;
    public double[][] dis;
    double min_dis=1000000000.0;
    Solver solver;
    public Input(String s,Solver solver) throws FileNotFoundException {
        this.solver = solver;
        this.s = s;
        s= filePath+s+".txt";
        Scanner scanner = new Scanner(new File(s));
        dimension = scanner.nextInt();
        vehiclesNumber = scanner.nextInt();
        capacity = scanner.nextInt();
        orders = new ArrayList<>();
        for(int i=0;i<dimension;i++){
            Order o = new Order();
            o.id = scanner.nextInt();
            o.coorX = scanner.nextInt();
            o.coorY = scanner.nextInt();
            orders.add(o);
        }
        int tmp;
        for(int i=0;i<dimension;i++){
            tmp = scanner.nextInt();
            orders.get(tmp-1).demand = scanner.nextInt();
        }
        scanner.close();
        dis = new double[dimension][dimension];
        for(int i=0;i<dimension;i++){
            for(int j=0;j<dimension;j++){
                dis[i][j] = computeDis(orders.get(i),orders.get(j));
                if(dis[i][j]<min_dis&&dis[i][j]!=0){
                    min_dis=dis[i][j];
                }
            }
        }
        sortByDemand();
    }
    double computeDis(Order o1, Order o2){
        return sqrt(pow(o1.coorX-o2.coorX,2)+pow(o1.coorY-o2.coorY,2));
    }
    void sortByDemand(){
        orders.sort(new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return Integer.compare(o2.demand,o1.demand);
            }
        });
    }
    ArrayList<Integer> findOrdersWithNearestCentroid(int id){
        ArrayList<Integer> res = new ArrayList<>();
        ArrayList<Cluster> tmp = new ArrayList<>(solver.clusters);
        for(int i=0;i<orders.size()-1;i++){
            if(!orders.get(i).served) {
                int finalI = i;
                tmp.sort(new Comparator<Cluster>() {
                    @Override
                    public int compare(Cluster c1, Cluster c2) {
                        return Double.compare(solver.distanceFunction(c1, orders.get(finalI)), solver.distanceFunction(c2, orders.get(finalI)));
                    }
                });
                if (tmp.get(0).id == id) {
                    res.add(i);
                }
            }
        }
        return res;
    }
}
