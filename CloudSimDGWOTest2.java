package gwoPsoOptimzer;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

import java.text.DecimalFormat;
import java.util.*;

public class CloudSimDGWOTest2 {
	protected static List<Cloudlet> cloudletList;
    protected static List<Vm> vmList;
    protected static final int WOLF_POPULATION = 30;
    protected static final int MAX_ITERATIONS = 100;
    protected static final int NUM_ISLANDS = 3;
    protected static final int MIGRATION_FREQUENCY = 10;
    protected static final double MIGRATION_RATE = 0.1;
    protected static double[] dgwoTransmisCost;

    // GWO parameters
    protected static double a;
    protected static final int SEARCH_AGENTS_PER_ISLAND = 10;
    protected static final int DIMENSION = 50;
    
    protected static class Island {
        double[][] positions;
        double[][] alphaPos;
        double[][] betaPos;
        double[][] deltaPos;
        double alphaScore;
        double betaScore;
        double deltaScore;
        
        public Island() {
            positions = new double[SEARCH_AGENTS_PER_ISLAND][DIMENSION];
            alphaPos = new double[1][DIMENSION];
            betaPos = new double[1][DIMENSION];
            deltaPos = new double[1][DIMENSION];
            alphaScore = Double.POSITIVE_INFINITY;
            betaScore = Double.POSITIVE_INFINITY;
            deltaScore = Double.POSITIVE_INFINITY;
        }
        
        public void initializePositions(Random rand) {
            for (int i = 0; i < SEARCH_AGENTS_PER_ISLAND; i++) {
                for (int j = 0; j < DIMENSION; j++) {
                    positions[i][j] = rand.nextInt(vmList.size());
                }
            }
        }
    }
  
    public CloudSimDGWOTest2 (List<Cloudlet> cloudletList, List<Vm> vmList) {
    	this.cloudletList = cloudletList;
		this.vmList = vmList;
		dgwoTransmisCost = new double[cloudletList.size()];
		applyDGWO2();
    }
    /*
    public static void main(String[] args) {
        try {
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            
            // Initialize CloudSim
            CloudSim.init(num_user, calendar, trace_flag);
            
            // Create Datacenter
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            
            // Create Broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();
            
            // Create VMs and Cloudlets
            vmList = createVMs(brokerId);
            cloudletList = createCloudlets(brokerId);
            
            // Submit VM list to broker
            broker.submitVmList(vmList);
            
            // Apply DGWO Algorithm
            List<Cloudlet> optimizedCloudlets = applyDGWO2();
            
            // Submit cloudlet list to broker
            broker.submitCloudletList(optimizedCloudlets);
            
            // Start simulation
            CloudSim.startSimulation();
            
            // Stop simulation
            CloudSim.stopSimulation();
            
            // Print results
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            printCloudletList(newList);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     */
    public static List<Cloudlet> applyDGWO2() {
        Island[] islands = new Island[NUM_ISLANDS];
        double[][] globalBestPosition = new double[1][DIMENSION];
        double globalBestScore = Double.POSITIVE_INFINITY;
        
        // Initialize islands
        Random rand = new Random();
        for (int i = 0; i < NUM_ISLANDS; i++) {
            islands[i] = new Island();
            islands[i].initializePositions(rand);
        }
        
        // Main loop
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // Update a
            a = 2 - iteration * (2.0 / MAX_ITERATIONS);
            
            // Process each island
            for (int islandIndex = 0; islandIndex < NUM_ISLANDS; islandIndex++) {
                Island island = islands[islandIndex];
                
                // Evaluate each search agent in the island
                for (int i = 0; i < SEARCH_AGENTS_PER_ISLAND; i++) {
                    double fitness = calculateFitness(island.positions[i]);
                    
                    // Update Alpha, Beta, and Delta
                    if (fitness < island.alphaScore) {
                        island.alphaScore = fitness;
                        System.arraycopy(island.positions[i], 0, island.alphaPos[0], 0, DIMENSION);
                        
                        // Update global best
                        if (fitness < globalBestScore) {
                            globalBestScore = fitness;
                            System.arraycopy(island.positions[i], 0, globalBestPosition[0], 0, DIMENSION);
                        }
                    } else if (fitness > island.alphaScore && fitness < island.betaScore) {
                        island.betaScore = fitness;
                        System.arraycopy(island.positions[i], 0, island.betaPos[0], 0, DIMENSION);
                    } else if (fitness > island.alphaScore && fitness > island.betaScore && fitness < island.deltaScore) {
                        island.deltaScore = fitness;
                        System.arraycopy(island.positions[i], 0, island.deltaPos[0], 0, DIMENSION);
                    }
                }
                
                // Update positions within island
                updatePositions(island, rand);
            }
            
            // Perform migration if needed
            if (a <= 1.0 && iteration > 0 && iteration % MIGRATION_FREQUENCY == 0) {
                performMigration(islands);
            }
        }
        
        // Apply the global best solution
        return applySchedulingSolution(globalBestPosition[0]);
    }
    
    protected static void updatePositions(Island island, Random rand) {
        for (int i = 0; i < SEARCH_AGENTS_PER_ISLAND; i++) {
            for (int j = 0; j < DIMENSION; j++) {
                double r1 = rand.nextDouble();
                double r2 = rand.nextDouble();
                
                double A1 = 2 * a * r1 - a;
                double C1 = 2 * r2;
                
                double D_alpha = Math.abs(C1 * island.alphaPos[0][j] - island.positions[i][j]);
                double X1 = island.alphaPos[0][j] - A1 * D_alpha;
                
                r1 = rand.nextDouble();
                r2 = rand.nextDouble();
                
                double A2 = 2 * a * r1 - a;
                double C2 = 2 * r2;
                
                double D_beta = Math.abs(C2 * island.betaPos[0][j] - island.positions[i][j]);
                double X2 = island.betaPos[0][j] - A2 * D_beta;
                
                r1 = rand.nextDouble();
                r2 = rand.nextDouble();
                
                double A3 = 2 * a * r1 - a;
                double C3 = 2 * r2;
                
                double D_delta = Math.abs(C3 * island.deltaPos[0][j] - island.positions[i][j]);
                double X3 = island.deltaPos[0][j] - A3 * D_delta;
                
                island.positions[i][j] = (X1 + X2 + X3) / 3;
                
                // Ensure position is within bounds
                island.positions[i][j] = Math.max(0, Math.min(vmList.size() - 1, Math.round(island.positions[i][j])));
            }
        }
    }
    
    protected static void performMigration(Island[] islands) {
    	int numMigrants = (int) (SEARCH_AGENTS_PER_ISLAND * MIGRATION_RATE);
        
        for (int i = 0; i < NUM_ISLANDS; i++) {
            int nextIsland = (i + 1) % NUM_ISLANDS;
            Island currentIsland = islands[i];
            Island targetIsland = islands[nextIsland];
            
            // Select best solutions from current island
            for (int j = 0; j < numMigrants; j++) {
                // Replace worst solutions in target island with best solutions from current island
                System.arraycopy(currentIsland.positions[j], 0, 
                    targetIsland.positions[SEARCH_AGENTS_PER_ISLAND - 1 - j], 0, DIMENSION);
            }
        }
    }
    
    protected static double calculateFitness(double[] position) {
        double totalExecutionTime = 0;
        double[] vmTime = new double[vmList.size()];
        
        for (int i = 0; i < position.length; i++) {
            int vmid = (int) position[i];
            Cloudlet cloudlet = cloudletList.get(i);
            Vm vm = vmList.get(vmid);
            
            // Calculate execution time for this cloudlet on the assigned VM
            double executionTime = cloudlet.getCloudletLength() / vm.getMips();
            vmTime[vmid] += executionTime;
            totalExecutionTime = Math.max(totalExecutionTime, vmTime[vmid]);
        }
        
        return totalExecutionTime;
    }

    // [Previous methods remain the same: createDatacenter, createBroker, createVMs, createCloudlets, printCloudletList]
    static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();
        
        int mips = 1000;
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));
        
        int hostId = 0;
        int ram = 2048;
        long storage = 1000000;
        int bw = 10000;
        
        hostList.add(new Host(
            hostId,
            new RamProvisionerSimple(ram),
            new BwProvisionerSimple(bw),
            storage,
            peList,
            new VmSchedulerTimeShared(peList)
        ));
        
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;
        
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw
        );
        
        try {
            return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }
    
    static List<Vm> createVMs(int brokerId) {
        List<Vm> vms = new ArrayList<>();
        int mips = 1000;
        long size = 10000;
        int ram = 512;
        long bw = 1000;
        int pesNumber = 1;
        String vmm = "Xen";
        
        for (int i = 0; i < 5; i++) {
            Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vms.add(vm);
        }
        return vms;
    }
    
    static List<Cloudlet> createCloudlets(int brokerId) {
        List<Cloudlet> cloudlets = new ArrayList<>();
        long length = 1000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();
        
        for (int i = 0; i < DIMENSION; i++) {
            Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudlets.add(cloudlet);
        }
        return cloudlets;
    }
    
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;
        
        String indent = "    ";
        System.out.println();
        System.out.println("========== OUTPUT ==========");
        System.out.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");
        
        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            System.out.printf(indent + cloudlet.getCloudletId() + indent + indent);
            
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                System.out.printf("SUCCESS");
                System.out.printf(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                    indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) +
                    indent + indent + dft.format(cloudlet.getFinishTime()) + "\n");
            }
        }
    }
    
    protected static List<Cloudlet> applySchedulingSolution(double[] solution) {
        for (int i = 0; i < solution.length; i++) {
            Cloudlet cloudlet = cloudletList.get(i);
            int vmIndex = (int) solution[i];
            cloudlet.setVmId(vmList.get(vmIndex).getId());
            dgwoTransmisCost[i] = calculateTransmissionCost(cloudlet);
        }
        return cloudletList;
    }
    
	protected static double calculateTransmissionCost (Cloudlet cloudlet) {
		double transmissionCost = 0.0;
		double costPerSecond = 0.01;
		double bandwidthBytesPerSec = (1000 * 1024 * 1024) / 8.0;
		transmissionCost = cloudlet.getCloudletLength() / bandwidthBytesPerSec;
		transmissionCost = transmissionCost*costPerSecond;
	    System.out.println(transmissionCost);

		return transmissionCost;
	}
	
	public double[] getTransmissionCost() {
		return dgwoTransmisCost;
	}
    // [Include all the other methods from the original code unchanged]
    
}