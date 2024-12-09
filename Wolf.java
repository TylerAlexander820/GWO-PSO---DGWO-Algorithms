package gwoPsoOptimzer;
import java.util.Random;

public class Wolf {
	 public double[] position;
	 public double fitness;

	 public Wolf(double[] position, double fitness) {
		 this.position = position.clone();
	     this.fitness = fitness;
	 }
}
