package gwoPsoOptimzer;
import java.util.Random;

public class WolfPack {
	private int numOfWolves;
	private int dimensions;
	Random rand = new Random();	
	
	public WolfPack (int numOfWolves, int dimensions) {
		this.numOfWolves = numOfWolves;
		this.dimensions = dimensions;
	}
}
