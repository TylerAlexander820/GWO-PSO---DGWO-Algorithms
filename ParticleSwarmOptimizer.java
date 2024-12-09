package gwoPsoOptimzer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import java.util.ArrayList;
import java.util.List;

public class ParticleSwarmOptimizer {
    private List<Particle> swarm;
    private double[] globalBestPosition;
    private double globalBestFitness;
    private List<Cloudlet> cloudletListA;
    private List<Vm> vmLista;
    private double[] alphaPositionA;
    private double[] psoTransmisCost;
    private static final int SWARM_SIZE = 50;
    private static final int MAX_ITERATIONS = 100;
    private static final double INERTIA_WEIGHT = 0.5;
    private static final double C1 = 1.5;
    private static final double C2 = 1.5;
    
    public ParticleSwarmOptimizer(List<Cloudlet> cloudletList, List<Vm> vmList, boolean hybridCheck, double[] alphaPosition) {
        this.cloudletListA = cloudletList;
        this.vmLista = vmList;
        this.alphaPositionA = alphaPosition;
        this.swarm = new ArrayList<>();
        this.globalBestFitness = Double.MAX_VALUE;
        this.globalBestPosition = new double[cloudletList.size()];
		psoTransmisCost = new double[cloudletListA.size()];
        initializeSwarm(cloudletListA.size(), vmLista, hybridCheck);
    }

    private void initializeSwarm(int cloudletSize, List<Vm> vmList, boolean hybridCheck) {
        Random rand = new Random();
        for (int i = 0; i < SWARM_SIZE; i++) {
            Particle particle = new Particle(cloudletSize);

            if (hybridCheck) {
                for (int j = 0; j < cloudletSize; j++) {
                    particle.position[j] = alphaPositionA[j];  
                    particle.velocity[j] = rand.nextDouble() * 2 - 1;  
                }
            } else {
                for (int j = 0; j < cloudletSize; j++) {
                    particle.position[j] = rand.nextInt(vmList.size());  
                    particle.velocity[j] = rand.nextDouble() * 2 - 1;  
                }
            }
             
            particle.pBestPosition = particle.position.clone();
            particle.pBestFitness = fitnessEvaluation(particle.position); 
            
            swarm.add(particle);
        }
    }
    
	public void updateSwarm() {
        Random rand = new Random();
        for (Particle particle : swarm) {
            for (int i = 0; i < particle.position.length; i++) {
                double r1 = rand.nextDouble();
                double r2 = rand.nextDouble();
                
                //Velocity calculations
                particle.velocity[i] = INERTIA_WEIGHT * particle.velocity[i]
                        + C1 * r1 * (particle.pBestPosition[i] - particle.position[i])
                        + C2 * r2 * (globalBestPosition[i] - particle.position[i]);

                particle.position[i] += particle.velocity[i];

                if (particle.position[i] < 0) {
                    particle.position[i] = 0;
                } else if (particle.position[i] >= vmLista.size()) {
                    particle.position[i] = vmLista.size() - 1;
                }
            }

            double fitness = fitnessEvaluation(particle.position);

            if (fitness < particle.pBestFitness) {
                particle.pBestFitness = fitness;
                particle.pBestPosition = particle.position.clone();
            }

            if (fitness < globalBestFitness) {
                globalBestFitness = fitness;
                globalBestPosition = particle.position.clone();
            }
        }
    }

    private double fitnessEvaluation(double[] position) {
       double makespan = 0.0;
       double[] vmExecutionTime = new double[vmLista.size()];  
        
        for (int i = 0; i < position.length; i++) {
            int vmId = (int) position[i];  

            Cloudlet cloudlet = cloudletListA.get(i);
            Vm vm = vmLista.get(vmId);
            
            double executionTime = cloudlet.getCloudletLength() / vm.getMips();
            vmExecutionTime[vmId] += executionTime;  
            makespan = Math.max(makespan, vmExecutionTime[vmId]);
            psoTransmisCost[i] = calculateTransmissionCost(cloudlet);
        }
        return makespan;  
    }

   
    public List<Cloudlet> psoOptimization() {
        for (int i = 0; i < cloudletListA.size(); i++) {
            Cloudlet cloudlet = cloudletListA.get(i);
            int vmID = (int) globalBestPosition[i] % vmLista.size();  
            cloudlet.setVmId(vmLista.get(vmID).getId());  
            System.out.println("Cloudlet " + cloudlet.getUserId() + " assigned to VM " + vmID);
            
        }
        return cloudletListA;
    }

    public List<Cloudlet> particleSwarmOptimizer() {
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            updateSwarm();
            System.out.println("Iteration " + iter + " - Global Best Fitness: " + globalBestFitness);
        }
        return psoOptimization();
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
    	return psoTransmisCost;
    }
}


