package gwoPsoOptimzer;

public class Particle {
	public double pBestFitness;
	public double[] velocity;
	public double[] pBestPosition;
	double[] position;
	
	public Particle(int cloudletSize) {
		this.position = new double[cloudletSize];
		this.pBestFitness = Double.MAX_VALUE;
		this.velocity = new double[cloudletSize];
		this.pBestPosition = new double[cloudletSize];
	}
}
