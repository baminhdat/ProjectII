import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);
        String instance = scanner.nextLine();
        double start = System.nanoTime();
        for(int i=0;i<1;i++) {
            Solver solver = new Solver(instance);
            solver.solve();
        }
        double end = System.nanoTime();
        System.out.println("The algorithm finished in approximately "+(end-start)+" nanoseconds.");
    }
}