import Model.Cluster;
import Model.Order;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Solver {
    Input input;
    ArrayList<Cluster> clusters;
    long start;
    long end;
    public Solver(String s) throws FileNotFoundException {
        input = new Input(s,this);
        clusters = new ArrayList<>();
        finalClusters = new ArrayList<>();
    }
    void initialization(){
        clusters.clear();
        Random rand = new Random();
        int index = rand.nextInt(input.dimension-1);
        Cluster c = new Cluster(1,input.orders.get(index).coorX,input.orders.get(index).coorY,0);
        clusters.add(c);
        for(int i=2;i<=input.vehiclesNumber;i++){
            index = -1;
            double max_dis = 0;
            for(int j=0;j<input.dimension-1;j++){
                double min_dis = 1000000000.0;
                for(int k=1;k<i;k++){
                    double tmp = distanceFunction(clusters.get(k-1),input.orders.get(j));
                    if(tmp<min_dis){
                        min_dis = tmp;
                    }
                }
                if(min_dis>max_dis){
                    max_dis = min_dis;
                    index = j;
                }
            }
            Cluster cluster = new Cluster(i,input.orders.get(index).coorX,input.orders.get(index).coorY,0);
            clusters.add(cluster);
        }
    }
    void initializationRandom(){
        Random random = new Random();
        int i=0;
        ArrayList<Integer> selected = new ArrayList<>();
        while(i<input.vehiclesNumber){
            int tmp = random.nextInt(input.dimension-1);
            if(!selected.contains(tmp)){
                selected.add(tmp);
                i++;
            }
        }
        i=0;
        for(Integer j: selected){
            clusters.add(new Cluster(i+1,input.orders.get(j).coorX,input.orders.get(j).coorY,0));
            i++;
        }
    }
    void initializationOri(){
        for(int i=0;i<input.vehiclesNumber;i++){
            Cluster c = new Cluster(i+1,input.orders.get(i).coorX,input.orders.get(i).coorY,0);
            clusters.add(c);
        }
    }
    ArrayList<Double> knownObjects = new ArrayList<>();
    double object = 0;
    double objectBSF = 1000000000.0;
    int served = 0;
    void reset(){
        served = 0;
        object = 0;
        for(Cluster c: clusters){
            c.weight = 0;
            c.orders.clear();
        }
        for(Order o: input.orders){
            o.served = false;
        }
    }
    void calculateClusters(){
        for(Cluster c: clusters) {
            if (!c.orders.isEmpty()) {
                float tmpX = 0;
                float tmpY = 0;
                for (Order o : c.orders) {
                    tmpX += o.coorX;
                    tmpY += o.coorY;
                }
                c.coorX = tmpX / c.orders.size();
                c.coorY = tmpY / c.orders.size();
                for (Order o : c.orders) {
                    object += distanceFunction(c,o);
                }
            served += c.orders.size();
            }
        }
    }
    void printClusters(ArrayList<Cluster> clusters){
        clusters.sort(new Comparator<Cluster>() {
            @Override
            public int compare(Cluster o1, Cluster o2) {
                return Integer.compare(o1.id,o2.id);
            }
        });
        for(Cluster c: clusters){
            System.out.println("Cluster "+ c.id+", Weight: "+c.weight+", served "+c.orders.size()+" orders.");
            for(Order o: c.orders){
                System.out.print(o.id+" ");
            }
            System.out.println();
        }
    }
    public double distanceFunction(Cluster c, Order o){
        return sqrt(pow(c.coorX-o.coorX,2)+pow(c.coorY-o.coorY,2));
    }
    double priorityFunction(Cluster c, Order o){
        return distanceFunction(c,o)/o.demand;
    }
    boolean assignToCluster(Cluster c, Order o ){
        if(c.weight+o.demand<=input.capacity){
            c.weight+=o.demand;
            c.orders.add(o);
            o.served = true;
            return true;
        }
        else return false;
    }
    double total = 0;
    ArrayList<Cluster> finalClusters;
    void solve(){
        start = System.currentTimeMillis();
        initialization();
        int iteration = 0;
        int iterWOImprovement = 0;
        while(iteration<Settings.maxIteration&&(System.currentTimeMillis()-start)<300000){
            //System.out.println(iteration);
            reset();
            for(Order o: input.orders){
                if(!o.served&&o.demand>0){
                    clusters.sort(new Comparator<Cluster>() {
                        @Override
                        public int compare(Cluster c1, Cluster c2) {
                            return Double.compare(distanceFunction(c1,o), distanceFunction(c2,o));
                        }
                    });
                    if(Settings.normalKmeans){
                        int indexC = 0;
                        clusters.sort(new Comparator<Cluster>() {
                            @Override
                            public int compare(Cluster c1, Cluster c2) {
                                return Double.compare(distanceFunction(c1,o),distanceFunction(c2,o));
                            }
                        });
                        while(!o.served&&indexC<input.vehiclesNumber){
                            assignToCluster(clusters.get(indexC),o);
                            indexC++;
                        }
                    }
                    if(!Settings.normalKmeans){
                        int indexC = 0;
                        while(!o.served&&indexC<input.vehiclesNumber) {
                            int id = clusters.get(indexC).id;
                            ArrayList<Integer> acceptableOrders = input.findOrdersWithNearestCentroid(id);
                            if (!acceptableOrders.contains(input.orders.indexOf(o))) {
                                acceptableOrders.add(input.orders.indexOf(o));
                            }
                            if (!acceptableOrders.isEmpty()) {
                                int finalIndexC = indexC;
                                acceptableOrders.sort(new Comparator<Integer>() {
                                    @Override
                                    public int compare(Integer o1, Integer o2) {
                                        return Double.compare(priorityFunction(clusters.get(finalIndexC), input.orders.get(o1)), priorityFunction(clusters.get(finalIndexC), input.orders.get(o2)));
                                    }
                                });
                                int run = 0;
                                while (true) {
                                    assignToCluster(clusters.get(finalIndexC), input.orders.get(acceptableOrders.get(run)));
                                    run++;
                                    if (run > acceptableOrders.size() - 1) {
                                        break;
                                    }
                                }
                            }
                            indexC++;
                        }
                    }
                }
            }
            calculateClusters();
            if(Settings.solveTSPafterClustering) {
            for(Cluster c: clusters){
                TSP tsp = new TSP(c.id,c.orders,input.orders.get(input.dimension-1),input);
                tsp.nearestNeighbour();
                tsp.improveBySwaping();
                tsp.computeCost();
                total += tsp.obj;
            }
            if(total<=objectBSF){
                finalClusters = clusters;
                objectBSF = total;
                iterWOImprovement=0;
            }
            else{
                iterWOImprovement++;
            }
            total = 0;
            }
            else {
                if (object <= objectBSF) {
                    finalClusters = clusters;
                    objectBSF = object;
                    iterWOImprovement = 0;
                } else {
                    iterWOImprovement++;
                }
            }
            if(iterWOImprovement==Settings.numberWOImprovement){
                break;
            }
            iteration++;
        }
        finalClusters.sort(new Comparator<Cluster>() {
            @Override
            public int compare(Cluster o1, Cluster o2) {
                return Integer.compare(o1.id,o2.id);
            }
        });
        end = System.currentTimeMillis();
        //System.out.println("The algorithm finished in "+(end-start)+" ms.");
        int served = 0;
        for(Cluster c: finalClusters){
            if(!c.orders.isEmpty()) {
                served+=c.orders.size();
                TSP tsp = new TSP(c.id, c.orders, input.orders.get(input.dimension - 1), input);
                tsp.nearestNeighbour();
                tsp.improveBySwaping();
                tsp.computeCost();
                //tsp.printSol();
                total += tsp.obj;
            }
            else{
                System.out.println("Cluster "+c.id+" is empty.");
            }
        }
        System.out.println("Total cost is "+total);
        System.out.println("Served "+served+" orders");
        JFrame frame = new JFrame();
        frame.setSize(1280,720);
        frame.setTitle("Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Visualization(finalClusters,input.orders.get(input.dimension-1)));
        frame.setVisible(true);
        if(Settings.solveExact) {
            total = 0;
            for(Cluster c: finalClusters){
                TSP tsp = new TSP(c.id,c.orders, input.orders.get(input.dimension-1),input);
                total+=tsp.solve();
            }
            System.out.println("Solving TSP by exact algorithms, Total cost is "+total);
        }
    }
}