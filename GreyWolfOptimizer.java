package gwoPsoOptimzer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.FileSystemNotFoundException;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import java.util.concurrent.TimeUnit;

public class GreyWolfOptimizer {
	public static final int MAX_ITERATIONS = 100;
	public static final int NUM_OF_WOLVES = 50;
	private List<Vm> vmList;
	private List<Cloudlet> cloudletList;
	
	double[] wolfPosition;
	private Wolf alphaWolf;
	private Wolf betaWolf;
	private Wolf deltaWolf;
	private Wolf[] wolfPack;
	Random rand = new Random();
	ParticleSwarmOptimizer pso;
	double makespan;
	double totalMakespan;
	private boolean hybridCheck;
	public double[] transmisCost;
	
	public GreyWolfOptimizer(List<Cloudlet> cloudletList, List<Vm> vmList) {
		this.cloudletList = cloudletList;
		this.vmList = vmList;
		wolfPack = new Wolf[NUM_OF_WOLVES];
		int cloudletSize = cloudletList.size();
		transmisCost = new double[cloudletSize];
		initializeWolves();
	}
	
	public GreyWolfOptimizer(List<Cloudlet> cloudletList, List<Vm> vmList, boolean hybridCheck) {
		this.cloudletList = cloudletList;
		this.vmList = vmList;
		wolfPack = new Wolf[NUM_OF_WOLVES];
		initializeWolves();
	}
	
	//initializes wolves position randomly relative to the cloudlet and VM problem space
	public void initializeWolves() {
		for (int i = 0; i < NUM_OF_WOLVES; i++) {
			wolfPosition = new double[cloudletList.size()];;
			for (int j = 0; j < cloudletList.size(); j++) {
				wolfPosition[j] = rand.nextInt(vmList.size());
			}		
			double fitness = fitnessEvaluation(wolfPosition, wolfPosition);
			wolfPack[i] = new Wolf(wolfPosition, fitness);
		}
		updateWolfPosition();
	}
	
	public List<Cloudlet> greyWolfOptimizer() {
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double a = 2.0 * (1.0 - ((double) i / MAX_ITERATIONS));
            for (Wolf wolf : wolfPack) {
                for (int d = 0; d < cloudletList.size(); d++) {
                	//Fitness calculations
                    double r1 = rand.nextDouble();
                    double r2 = rand.nextDouble();
                    double x = (2 * a * r1) - a;
                    double y = 2 * r2;
                    
                    totalMakespan = calculateET(cloudletList);
                  
                    double S_alpha = Math.abs((y * alphaWolf.position[d]) - totalMakespan);
                    double S_beta = Math.abs((y * betaWolf.position[d]) - totalMakespan);
                    double S_delta = Math.abs((y * deltaWolf.position[d]) - totalMakespan);
                              
                    double E1 = Math.abs(alphaWolf.position[d] - (x * S_alpha));
                    double E2 = Math.abs(betaWolf.position[d] - (x * S_beta));
                    double E3 = Math.abs(deltaWolf.position[d] - (x * S_delta));
                    
                    wolf.position[d] = ((E1 + E2 + E3) / 3.0);
                    wolf.position[d] = Math.max(0, Math.min(vmList.size() - 1, Math.round(wolf.position[d])));
                }
                wolf.fitness = fitnessEvaluation(alphaWolf.position, wolf.position);        
            }
           updateWolfPosition();
         //For testing not invoked
           //printOutput();
        }
        List <Cloudlet> GWO = gwoOptimization(getAlphaPosition());
        return GWO;
	}
	
	public List<Cloudlet> gwoOptimization(double[] optimized){
		for (int i = 0; i < cloudletList.size(); i++) {	
			Cloudlet cloudlet = cloudletList.get(i);	
			//used to rebind vmID into the problem space (will throw index OOB error if not)
			int vmID = (int) optimized[i] % vmList.size();
			cloudlet.setVmId(vmList.get(vmID).getId());
			transmisCost[i] = calculateTransmissionCost(cloudlet);
		}
        return cloudletList;
	}
	
	public double fitnessEvaluation(double[] alphaPosition, double[] preyPosition) {
		double fitness = 0.0;
		for (int i = 0; i < preyPosition.length; i++) {
            fitness += Math.abs(alphaPosition[i] - preyPosition[i]);  
        }
		return fitness;
	}
	
	public double calculateET(List<Cloudlet> cloudletList) {
	        double minET = cloudletList.size();
	        for (Cloudlet cloudlet : cloudletList) {
	            double RTj = getResponseTime(cloudlet);
	            double ETj = getExecutionTime(cloudlet);
	            
	            makespan += getMakespan(RTj, ETj);
	            minET = Math.max(minET, Math.max(RTj, ETj));
	        }
	        return minET;
	    }
	
	public double getMakespan (double responseTime, double executionTime) {
		return responseTime + executionTime;
	}
	
	public double getResponseTime(Cloudlet cloudlet) {
		double responseTime = cloudlet.getFinishTime() - cloudlet.getExecStartTime();
		return responseTime;
	}
	
	public double getExecutionTime(Cloudlet cloudlet) {
		double executionTime = cloudlet.getExecStartTime();
		return executionTime;
	}
	
	private void updateWolfPosition() {
		alphaWolf = wolfPack[0];
        betaWolf = wolfPack[1];
        deltaWolf = wolfPack[2];
        
		for (Wolf wolf : wolfPack) {
            if (wolf.fitness < alphaWolf.fitness) {
                deltaWolf = betaWolf;
                betaWolf = alphaWolf;
                alphaWolf = wolf;
            } else if (wolf.fitness < betaWolf.fitness) {
                deltaWolf = betaWolf;
                betaWolf = wolf;
            } else if (wolf.fitness < deltaWolf.fitness) {
                deltaWolf = wolf;
            }
        }
	}
	
	//For testing
	private void printOutput() {
        System.out.println("Best solution found for Alpha: ");
        for (double x : alphaWolf.position) {
            System.out.println("Alpha Wolf Position: " + x);
        }
        System.out.println("Alpha Wolf Fitness: " + alphaWolf.fitness);
        System.out.println("------------------------------------------------");
       
        System.out.println("Best solution found for Beta: ");
        for (double x : betaWolf.position) {
            System.out.println("Beta Wolf Position: " + x);
        }
        System.out.println("Beta Wolf Fitness: " + alphaWolf.fitness);
        System.out.println("------------------------------------------------");
        
        System.out.println("Best solution found for Delta: ");
        for (double x : alphaWolf.position) {
            System.out.println("Delta Wolf Position: " + x);
        }
        System.out.println("Delta Wolf Fitness: " + alphaWolf.fitness);
        System.out.println("------------------------------------------------");
    }
	
	public double[] getAlphaPosition() {
		return alphaWolf.position.clone();
	}
	
	public double getAlphaFitness() {
		return alphaWolf.fitness;
	}
	
	public double[] getBetaPosition() {
		return betaWolf.position.clone();
	}
	
	public double[] getDeltaPosition() {
		return deltaWolf.position.clone();
	}
	
	protected static double calculateTransmissionCost(Cloudlet cloudlet) {
        // Calculate the transmission cost based on data size and network load
        
	 double bandwidthBytesPerSec = (1000 * 1024 * 1024) / 8.0; //1000Mbps(bits per second) ---->  bandwidth in bytes per second.
	 double dataSizeBytes = cloudlet.getCloudletFileSize();
     double transmissionTime = dataSizeBytes / bandwidthBytesPerSec;
     //double transmissionTime = cloudlet.getCloudletLength() / bandwidthBytesPerSec;
     //double costPerSecond = 0.01;
     return transmissionTime; //* costPerSecond;
	}
	
	public double[] getTransmissionCost() {
		return transmisCost;
	}
}
