import Model.Cluster;
import Model.Order;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Visualization extends JPanel {
    ArrayList<Cluster> clusters;
    Order depot;
    private int radius = 5;
    private int offX;
    private int offY;
    private double ratio = 3 ;
    public Visualization(ArrayList<Cluster> a, Order order){
        depot = order;
        clusters = a;
        offX = 640-(int)(depot.coorX*ratio)-200;
        offY = 360-(int)(depot.coorY*ratio);
        this.setBackground(Color.WHITE);
    }
    public void paintComponent(Graphics g){
        super.paintComponents(g);
        g.setColor(Color.BLACK);
        g.drawOval((int)((depot.coorX-radius)*ratio)+offX,(int)((depot.coorY-radius)*ratio)+offY,(int)(2*radius*ratio),(int)(2*radius*ratio));
        g.drawString("Depot",(int)((depot.coorX-radius)*ratio)+offX,(int)((depot.coorY)*ratio)+offY);
        Random random = new Random();
        for(Cluster c: clusters){
            g.setColor(new Color(random.nextInt(255),random.nextInt(255),random.nextInt(255)));
            for(Order o: c.orders){
                g.drawOval((int)((o.coorX-radius)*ratio)+offX,(int)((o.coorY-radius)*ratio)+offY,(int)(2*radius*ratio),(int)(2*radius*ratio));
                g.drawString(String.valueOf(c.id),(int)((o.coorX-radius*3/4)*ratio)+offX,(int)((o.coorY)*ratio)+offY);
            }
        }
    }
}
