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

public class GreyWolfOptimizerV3 {
	

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
		public double[] transmissionCost;
		
		public GreyWolfOptimizerV3(List<Cloudlet> cloudletList, List<Vm> vmList) {
			this.cloudletList = cloudletList;
			this.vmList = vmList;
			wolfPack = new Wolf[NUM_OF_WOLVES];
			int cloudletSize = cloudletList.size();
			transmissionCost = new double[cloudletSize];
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
	                    
	                    totalMakespan = calculateET(wolf.position);
	                  
	                    double S_alpha = Math.abs((y * alphaWolf.position[d]) - totalMakespan);
	                    double S_beta = Math.abs((y * betaWolf.position[d]) - totalMakespan);
	                    double S_delta = Math.abs((y * deltaWolf.position[d]) - totalMakespan);
	                              
	                    double E1 = Math.abs(alphaWolf.position[d] - (x * S_alpha));
	                    double E2 = Math.abs(betaWolf.position[d] - (x * S_beta));
	                    double E3 = Math.abs(deltaWolf.position[d] - (x * S_delta));
	                    
	                    wolf.position[d] = ((E1 + E2 + E3) / 3.0);
	                    wolf.position[d] = Math.max(0, Math.min(vmList.size() - 1, wolf.position[d]));
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
		
		public double calculateET(double[] position) {
				double makespan = 0.0;
				double[] vmExecutionTime = new double[vmList.size()];  
				double transmisCost = 0;
		        for (int i = 0; i < position.length; i++) {
		            int vmId = (int) position[i];  

		            Cloudlet cloudlet = cloudletList.get(i);
		            Vm vm = vmList.get(vmId);
		            
		            double executionTime = cloudlet.getCloudletLength() / vm.getMips();
		            vmExecutionTime[vmId] += executionTime;  
		            makespan = Math.max(makespan, vmExecutionTime[vmId]);
		            transmisCost += calculateTransmissionCost(cloudlet);
		            transmissionCost[i] = transmisCost;
		        }
		        return makespan + transmisCost;  
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
		
	    public void dynamicLoadBalancing() {
	        if (vmList.size() <= 5) {  
	            for (int i = 0; i < cloudletList.size(); i++) {
	                Cloudlet cloudlet = cloudletList.get(i);
	                Vm vm = OptimalVm();
	                cloudlet.setVmId(vm.getId());
	            }
	        }
	    }

	    private Vm OptimalVm() {
	        Vm leastLoadedVm = vmList.get(0);
	        double minLoad = calculateVmLoad(leastLoadedVm);
	        
	        for (Vm vm : vmList) {
	            double load = calculateVmLoad(vm);
	            if (load < minLoad) {
	                leastLoadedVm = vm;
	                minLoad = load;
	            }
	        }
	        return leastLoadedVm;
	    }

	    private double calculateVmLoad(Vm vm) {
	        double load = 0.0;
	        for (Cloudlet cloudlet : cloudletList) {
	            if (cloudlet.getVmId() == vm.getId()) {
	                load += cloudlet.getCloudletLength() / vm.getMips();
	            }
	        }
	        return load;
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
			return transmissionCost;
		}
}


