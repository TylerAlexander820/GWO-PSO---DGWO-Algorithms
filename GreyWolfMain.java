/**
 * 
 */
package gwoPsoOptimzer;
//import java.util.Random;
import java.util.ArrayList;
import java.lang.StringBuilder;
import java.util.List;
import java.util.Random;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Calendar;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.Host;
//import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.Storage;
//import org.cloudbus.cloudsim.Log;
/**
 * This class manages the initialization of the Cloud environment through Cloudsim.
 * This information will then be passed onto the GreyWolfOptimizer program for optimization
 * Will implement a UI to select specific Cloud Parameters 
 */
public class GreyWolfMain {
	//public static final int DEFAULT_MAX_ITERATIONS = 100;
    //public static final int DEFAULT_NUM_OF_WOLVES = 50;
    //public static final int DEFAULT_DIMENSIONS = 5;
    //private double iniPosition = Double.MAX_VALUE;
	private static List<Vm> VmList;
	private static List<Cloudlet> cloudletList;
	public static double[] gwoAlphaPosition = null;
	public List<Host> hostList = new ArrayList<Host>();;
	public static List<Datacenter> datacenters = new ArrayList<>();
	private static double[] gwoTransmisCost;
	private static double[] psoTransmisCost;
	private static double[] gwopsoTransmisCost;
	
	public static void main(String[] args) {
	
		System.out.println("Starting Simulation...");
		Random rand = new Random();
		int iterationMAX = 100;
		int userDC;
		int userBroker;
		int userCloud = 500;
		int userVM = 7;
		int userInput = 4;
		int d = 1;
		long fileSize = 1200;
		long outputSize = 400;
		int mips = 1000;
		// dgwo = new CloudSimDGWOTest1();
		
		//int maxIteration = parseInput("Please enter the number of iterations you would like for the GWO process: ", DEFAULT_MAX_ITERATIONS, scan);
		try {

			Scanner scan = new Scanner(System.in);
			//int userInput = 1;		
			
			double[] alphaPosition;
			//System.out.print("How many Virtual Machines?: ");
			//userVM = 3;
		    for (int iteration = 0; iteration <= iterationMAX; iteration++) {
		    	CloudSim sim = new CloudSim();
				Calendar cal = Calendar.getInstance();
				CloudSim.init(1, cal, false);
				//System.out.print("How many Datacentres?: ");
				//userDC = scan.nextInt();
				Datacenter datacentre = createDataCenter("Datacenter", d);
	/*
				for (int i = 0; i < userDC; i++) {
					String dcName = "Datacenter" + d;
					System.out.println(dcName);
					Datacenter datacentre = createDataCenter(dcName, d);
					System.out.println("Created " + dcName + " with ID: " + datacentre.getId());
					datacenters.add(datacentre);
					d++;
				*/
				
				DatacenterBroker broker = createBroker();
				int brokerID = broker.getId();
				VmList = new ArrayList<Vm>();
				
		        System.out.println("Starting iteration " + (iteration + 1));
		
		        /*
		        if (iteration % 20 == 0 && iteration > 0) { 
		        	 userCloud += 100; 
		        	 if (userCloud > 1000) {
		        		 userCloud = 100;
		        	 }
		        } 
		      
		        if (iteration % 200 == 0 && iteration > 0) {
		        	userInput++;
		        	if (userInput > 7) {
		        		userInput = 1;
		        	}
		        }
		       
		        if (iteration % 200 == 0 && iteration > 0) {
		        	userVM += 2;
		            if (userVM > 9) {
		            	userVM = 3;  
		            }
		         }
 
		        if (iteration % 400 == 0 && iteration > 0) {
		            fileSize += 400;
		            if (fileSize > 1200) fileSize = 400;  // Cycle between 400, 800, 1200
		        }

		        if (iteration % 800 == 0 && iteration > 0) {
		            outputSize += 400;
		            if (outputSize > 1200) outputSize = 400;  // Cycle between 400, 800, 1200
		        }
		     */
			int vmID = 0;
			long size = 1000;
			int ram = 1024;
			long bw = 1000;
			int pesNumber = 1;
			String vmm = "Xen";
			int multiplier = 1;
			
			for (int i = 0; i < userVM; i++) {
				VmList.add(new Vm(vmID, brokerID, mips*multiplier, pesNumber* multiplier, ram* multiplier, bw* multiplier, size*multiplier, vmm, new CloudletSchedulerTimeShared()));
				vmID++;
				multiplier++;
				if (multiplier > 4) {
					multiplier = 1;
					ram = 512;
					bw = 500;
					pesNumber = 1;
					size = 500;
					mips = 500;
				}	
			} 
			
			broker.submitVmList(VmList);
			
			cloudletList = new ArrayList<Cloudlet>();
			//System.out.print("How many cloudlets? (Please enter a value under 500): ");
			Cloudlet cloudlet = null;
			
			int cid = 0;
			long length = 4000;
			
			//long fileSize = 400;
			//long outputSize = 400;
			//int userCloud = scan.nextInt();
			
		/*	while (userCloud > 500 || userCloud < 0) {
				System.out.println("You have entered a value that exceeds the limitation. Please re-enter here:");
				userCloud = scan.nextInt();
			}*/
			
			UtilizationModel utilmod = new UtilizationModelFull();
		
			for (int i = 0; i < userCloud; i++) {
				cloudlet = new Cloudlet(cid, length, pesNumber, fileSize, outputSize, utilmod, utilmod, utilmod);
				cloudlet.setUserId(brokerID);
				cloudletList.add(cloudlet);
				cid++;
			}
			/*
			System.out.println("Please enter the following to perform one of the algorithmic operations:\n"
					+ "1: Grey Wolf Optimization (GWO)\n2: Particle Swarm Optimization (PSO)\n3: "
					+ "Hybrid Grey Wolf Optimization and Particle Swarm Optimization (GWO-PSO)\n4: " 
					+ "Distributed Grey Wolf Optimization (DGWO)\n5: " 
					+ "CloudSimGWOTest1T \n6:" + "CloudSimDGWOTest1 \n7:" +
					"GWO-PSO V2 (FOR TESTING,  DO NOT USE)" + "\n8:" + "Sequential: GWO -> PSO -> Hybrid GWO-PSO -> DGWO (BROKEN! DO NOT USE)");
			*/
			//userInput = scan.nextInt();
			/*
			while (userInput > 5 || userInput < 1) {
				System.out.println("You have entered an invalid value. Please re-enter here:");
				userInput = scan.nextInt();
			}*/
			
			ParticleSwarmOptimizer pso;
			Boolean hybridCheck = false;
			List<Cloudlet> updatedList;
			int i = 0;
			List<Cloudlet> gwoCloudletList;
			List<Cloudlet> psoCloudletList;
			gwopsoTransmisCost = new double[(int) (cloudletList.size() + 1.0)];
			
			switch(userInput) {
				case 1: 
					i = 0;
					System.out.println("You have chosen Grey Wolf Optimization\n");
					System.out.println("Starting Grey Wolf Optimization\n");
					GreyWolfOptimizer gwo = new GreyWolfOptimizer(cloudletList, VmList);
					List<Cloudlet> greyWolf = gwo.greyWolfOptimizer();
					System.out.println("GWO Optimization Complete");
					
					alphaPosition = gwo.getAlphaPosition();
					broker.submitCloudletList(greyWolf);
					
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					
					updatedList = broker.getCloudletReceivedList();
					System.out.println("Modified List: " + updatedList);
					gwoTransmisCost = gwo.getTransmissionCost();
					
					printCloudletList(updatedList, i, userVM, gwoTransmisCost, iteration, fileSize, outputSize);
					break;
				case 2:
					i = 1;
					System.out.println("You have chosen Particle Swarm Optimization\n");
					System.out.println("Starting Particle Swarm Optimization\n");
					pso = new ParticleSwarmOptimizer(cloudletList, VmList, hybridCheck, null);
		            List<Cloudlet> particleSwarm = pso.particleSwarmOptimizer();
					System.out.println("PSO Optimization Complete");
				/*	
					for (int a = 0; a < cloudletList.size(); a++) {
						alphaPosition = pso.getGlobalBestPosition();
						vmID = (int) alphaPosition[a];
						cloudletList.get(a).setVmId(vmID);
					}*/
					broker.submitCloudletList(particleSwarm);
					
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					
					updatedList = broker.getCloudletReceivedList();
					psoTransmisCost = pso.getTransmissionCost();
					printCloudletList(updatedList, i, userVM, psoTransmisCost, iteration, fileSize, outputSize);
					break;
				case 3:
					i = 2;
					System.out.println("You have chosen Hybrid Grey Wolf and Particle Swarm Optimization\n");
					System.out.println("Starting Hybrid Grey Wolf and Particle Swarm Optimization");
					System.out.println("-----------------------------------------------------------");
					hybridCheck = true;
					GreyWolfOptimizer gwopso = new GreyWolfOptimizer(cloudletList, VmList);

					gwoCloudletList = gwopso.greyWolfOptimizer();
					gwoAlphaPosition = gwopso.getAlphaPosition();
					System.out.println("-----------------------------------------------------------");
					System.out.println("\nGWO Optimization Complete");
					
					System.out.println("Starting PSO Optimization");
					System.out.println("-----------------------------------------------------------");
					ParticleSwarmOptimizer hpso = new ParticleSwarmOptimizer(gwoCloudletList, VmList, hybridCheck, gwoAlphaPosition);
					List<Cloudlet> hybridGWOPSO = hpso.particleSwarmOptimizer();
		            System.out.println("PSO Optimization Complete");
		            System.out.println("Hybrid GWO-PSO Optimization Complete");
		            
		            broker.submitCloudletList(hybridGWOPSO);
					
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					
					updatedList = broker.getCloudletReceivedList();
					gwoTransmisCost = gwopso.getTransmissionCost();
					psoTransmisCost = hpso.getTransmissionCost();
					
					for (int j = 0; j < gwoTransmisCost.length; j++) {
						gwopsoTransmisCost[j] = gwoTransmisCost[j] + psoTransmisCost[j];
					}
					
					printCloudletList(updatedList, i, userVM, gwopsoTransmisCost, iteration, fileSize, outputSize);
					break;
				case 4:
					i = 3;
					System.out.println("You have chosen Hybrid Grey Wolf and Particle Swarm Optimization V2\n");
					System.out.println("Starting Hybrid Grey Wolf and Particle Swarm Optimization V2");
					System.out.println("-----------------------------------------------------------");
					hybridCheck = true;
					GreyWolfOptimizerV2 gwopsov2 = new GreyWolfOptimizerV2(cloudletList, VmList);

					gwoCloudletList = gwopsov2.greyWolfOptimizer();
					gwoAlphaPosition = gwopsov2.getAlphaPosition();
					System.out.println("-----------------------------------------------------------");
					System.out.println("\nGWO Optimization Complete");
					
					System.out.println("Starting PSO Optimization");
					System.out.println("-----------------------------------------------------------");
					ParticleSwarmOptimizer hpsov2 = new ParticleSwarmOptimizer(gwoCloudletList, VmList, hybridCheck, gwoAlphaPosition);
					List<Cloudlet> hybridGWOPSOv2 = hpsov2.particleSwarmOptimizer();
		            System.out.println("PSO Optimization Complete");
		            System.out.println("Hybrid GWO-PSO Optimization Complete");
		            
		            broker.submitCloudletList(hybridGWOPSOv2);
					
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					
					updatedList = broker.getCloudletReceivedList();

					gwoTransmisCost = gwopsov2.getTransmissionCost();
					psoTransmisCost = hpsov2.getTransmissionCost();
					
					for (int j = 0; j < gwoTransmisCost.length; j++) {
						gwopsoTransmisCost[j] = gwoTransmisCost[j] + psoTransmisCost[j];
					}
					
					printCloudletList(updatedList, i, userVM, gwopsoTransmisCost, iteration, fileSize, outputSize);
					break;
				case 5:
					i = 4;
					System.out.println("You have chosen Hybrid Grey Wolf and Particle Swarm Optimization V3\n");
					System.out.println("Starting Hybrid Grey Wolf and Particle Swarm Optimization V3");
					System.out.println("-----------------------------------------------------------");
					hybridCheck = true;
					GreyWolfOptimizerV3 gwopsov3 = new GreyWolfOptimizerV3(cloudletList, VmList);

					gwoCloudletList = gwopsov3.greyWolfOptimizer();
					gwoAlphaPosition = gwopsov3.getAlphaPosition();
					System.out.println("-----------------------------------------------------------");
					System.out.println("\nGWO Optimization Complete");
					
					System.out.println("Starting PSO Optimization");
					System.out.println("-----------------------------------------------------------");
					ParticleSwarmOptimizer hpsov3 = new ParticleSwarmOptimizer(gwoCloudletList, VmList, hybridCheck, gwoAlphaPosition);
					List<Cloudlet> hybridGWOPSOv3 = hpsov3.particleSwarmOptimizer();
		            System.out.println("PSO Optimization Complete");
		            System.out.println("Hybrid GWO-PSO Optimization Complete");
		            
		            broker.submitCloudletList(hybridGWOPSOv3);
					
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					
					updatedList = broker.getCloudletReceivedList();
				
					gwoTransmisCost = gwopsov3.getTransmissionCost();
					psoTransmisCost = hpsov3.getTransmissionCost();
					
					for (int j = 0; j < gwoTransmisCost.length; j++) {
						gwopsoTransmisCost[j] = gwoTransmisCost[j] + psoTransmisCost[j];
					}
					
					printCloudletList(updatedList, i, userVM, gwopsoTransmisCost, iteration, fileSize, outputSize);
					break;
				case 6:
					i = 5;
					System.out.println("You have chosen Distributed Grey Wolf Optimization\n");
					System.out.println("Starting Distributed Grey Wolf Optimization");
					CloudSimDGWOTest3T dgwo = new CloudSimDGWOTest3T(cloudletList, VmList);
					List <Cloudlet> DistribGWO = dgwo.applyDGWO3();
					System.out.println("DGWO Optimization Complete");
					broker.submitCloudletList(DistribGWO);
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					double[] dgwoTransmisCost = dgwo.getTransmissionCost();

					updatedList = broker.getCloudletReceivedList();
					printCloudletList(updatedList, i, userVM, dgwoTransmisCost, iteration, fileSize, outputSize);
					break;
				case 7:
					i = 6;
					System.out.println("You have chosen CloudSimGWOTest1T Optimization\n");
					System.out.println("Starting CloudSimGWOTest1T Optimization");
					CloudSimGWOTest1T gwoC = new CloudSimGWOTest1T(cloudletList, VmList);
					List <Cloudlet> GWOC = gwoC.applyGWO();
					System.out.println("DGWO Optimization Complete");
					broker.submitCloudletList(GWOC);
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					double[] cgwoTransmisCost = gwoC.getTransmissionCost();

					
					updatedList = broker.getCloudletReceivedList();
					printCloudletList(updatedList, i, userVM, cgwoTransmisCost, iteration, fileSize, outputSize);
					break;
				/*		case 6:
					i = 5;
					System.out.println("You have chosen CloudSimDGWOTest1 Optimization\n");
					System.out.println("Starting CloudSimDGWOTest1 Optimization");
					CloudSimDGWOTest1 dgwo1 = new CloudSimDGWOTest1(cloudletList, VmList);
					List <Cloudlet> DistribDGWO = dgwo1.applyDGWO();
					System.out.println("DGWO Optimization Complete");
					broker.submitCloudletList(DistribDGWO);
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					
					updatedList = broker.getCloudletReceivedList();
					printCloudletList(updatedList, userInput, userVM,gwoTransmisCost);
					break;
				case 7:
					i = 6;
					System.out.println("You have chosen Hybrid Grey Wolf and Particle Swarm Optimization V2\n");
					System.out.println("Starting Hybrid Grey Wolf and Particle Swarm Optimization");
					System.out.println("-----------------------------------------------------------");
					hybridCheck = true;
					GreyWolfOptimizer gwopso2 = new GreyWolfOptimizer(cloudletList, VmList, hybridCheck);

					gwoCloudletList = gwopso2.greyWolfOptimizer();
		            
		            broker.submitCloudletList(gwoCloudletList);
					
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					
					updatedList = broker.getCloudletReceivedList();
					printCloudletList(updatedList, i, userVM, gwoTransmisCost);
					break;
				case 7:
					i = 6;
					System.out.println("All 4 will be performed and evaluated sequentially");
					System.out.println("Starting Grey Wolf Optimization\n");
					gwoCloudletList = gwo.greyWolfOptimizer();
					System.out.println("GWO Optimization Complete");
					
					broker.submitCloudletList(gwoCloudletList);
					
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					
					updatedList = broker.getCloudletReceivedList();
					printCloudletList(updatedList, i, userVM);
					updatedList = null;
					i++;
					
					broker.getCloudletList().clear();
					CloudSim.init(1, cal, false);
					
					System.out.println("-----------------------------------------------------------");
					System.out.println("Starting PSO Optimization");
					System.out.println("-----------------------------------------------------------");
					pso = new ParticleSwarmOptimizer(cloudletList, VmList, hybridCheck, gwoAlphaPosition);
					psoCloudletList = pso.particleSwarmOptimizer();
		            System.out.println("PSO Optimization Complete");
		            System.out.println(psoCloudletList);
		            broker.submitCloudletList(psoCloudletList);
		            
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					updatedList = broker.getCloudletReceivedList();
					printCloudletList(updatedList, i, userVM);
					updatedList = null;
					i++;
					
					broker.getCloudletList().clear();
					CloudSim.init(1, cal, false);
					
					System.out.println("-----------------------------------------------------------");
					System.out.println("Starting Hybrid Grey Wolf and Particle Swarm Optimization");
					System.out.println("-----------------------------------------------------------");
					hybridCheck = true;
					gwo.greyWolfOptimizer();
					gwoAlphaPosition = gwo.getAlphaPosition();
					pso = new ParticleSwarmOptimizer(cloudletList, VmList, hybridCheck, gwoAlphaPosition);
					List<Cloudlet> psoCloudletList1 = pso.particleSwarmOptimizer();
		            System.out.println("Hybrid GWO-PSO Optimization Complete");
		           
		            broker.submitCloudletList(psoCloudletList1);
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					updatedList = broker.getCloudletReceivedList();
					printCloudletList(updatedList, i, userVM);
					updatedList = null;
					i++;     
					
					broker.getCloudletList().clear();
					CloudSim.init(1, cal, false);
					
					System.out.println("-----------------------------------------------------------");    
					System.out.println("Starting Distributed Grey Wolf Optimization");
					System.out.println("-----------------------------------------------------------");
					CloudSimDGWOTest2 dgwo2 = new CloudSimDGWOTest2(cloudletList, VmList);
					List <Cloudlet> DistribGWO1 = dgwo2.applyDGWO2();
					System.out.println("DGWO Optimization Complete");
					
					broker.submitCloudletList(DistribGWO1);
					
					CloudSim.startSimulation();
					CloudSim.stopSimulation();
					
					updatedList = broker.getCloudletReceivedList();
					printCloudletList(updatedList, i, userVM);
					System.out.println("Simulations fully completed. Check folder for results");
					break;	*/
					
			}
			
			broker.getCloudletList().clear();
	        VmList.clear();
	        cloudletList.clear();

	        System.out.println("Simulation iteration complete: " + iteration);
	        System.out.println("Broker Cleared =======================================================================================================================================================");
		    }
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("An error has occured");
		}
	}

	private static Datacenter createDataCenter(String name, int d) {
		Scanner scanner = new Scanner(System.in);
		int hostID = 0;
		int ram = 1600000000;
		int storage = 1000000000;
		int bw = 1000000000;
		int mips = 1000000000;
		int hostNum = 0;
		int resourceScaler = 1;
		List<Host> hostList = new ArrayList<Host>();
		
		//System.out.print("How many host machines for the Datacentre?: ");
		hostNum = 2;
/*
		while (hostNum < 1 || hostNum > 20) {
		    System.out.println("You have entered a value that exceeds the limitation (1-20). Please re-enter here:");
		    hostNum = scanner.nextInt();
		}*/
		
		for (int i = 0; i < hostNum; i++) {
		    List<Pe> peList = new ArrayList<>();
		    peList.add(new Pe(0, new PeProvisionerSimple(mips * resourceScaler)));

		    hostList.add(new Host(hostID, new RamProvisionerSimple(ram * resourceScaler), new BwProvisionerSimple(bw * resourceScaler), storage * resourceScaler, peList, new VmSchedulerTimeShared(peList)));
		    hostID++;
		    resourceScaler++;
		}
		
		System.out.println(hostList);
		String arch = "X_86";
		String os = "Linux";
		String vmm = "Xen";
		double timeZone = 10.0;
		double cost = 3.00;
		double costPerMem = 0.05;
		double costPerStorage = 0.001;
		double costPerBw = 0.1;
		
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		
		DatacenterCharacteristics charac = new DatacenterCharacteristics(arch, os, vmm, hostList, timeZone, cost, costPerMem, costPerStorage, costPerBw);
		Datacenter datacentre = null;
		
		try {
			datacentre = new Datacenter(name, charac, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datacentre;
	}
	
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		
		try {
			broker = new DatacenterBroker("Broker");	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return broker;
	}
	
	private static void printCloudletList(List<Cloudlet> list, int i, int vmNum, double[] transmissionCost, int iteration, long fileSize, long outputSize) throws FileNotFoundException {
		double executionTime = 0.0;
		double submissionTime = 0.0;
		double makespan = 0.0;
		double makespanTotal = 0.0;
		double executionTimeTotal = 0.0;
		double responseTimeTotal = 0.0;
		double finishTime = 0.0; 
		double startTime = 0.0;
		double waitingTime = 0.0;
		double responseTime = 0.0;
		int iter = (iteration - 1) % 20 + 1;
		if (iter< 1) iter =1;
		int userVM = vmNum;
		int listLen = list.size();
		String[] type = {"GWO", "PSO", "GWO-PSO", "GWO-PSO V2", "GWO-PSO V3", "DGWO", "CloudSimGWOTest1T", "CloudSimDGWOTest1T"};
		
		//For exporting
		File csvFile = new File ("CloudletInfo - " + type[i] + " - " + " File Size -  " + fileSize + " - Output - " + outputSize + " Number of Virual Machines - "+ userVM + " Number of Cloudlets - " + listLen + " Iteration - " + iter + ".csv");
		 try (PrintWriter out = new PrintWriter(csvFile)) {
		String header = ("Cloudlet ID" + "," + "Virtual Machine ID" + "," + "Makespan"+ "," + "Start Time"+ "," +
						"Execution Time"+ "," + "Submission Time" + "," + "Finish Time" + "," + "Waiting Time" + "," + 
						"Response Time"+ "," + "Transmission Cost" + "," + "Cumulative Makespan" + "," + "Cumulative Response Time" + "," + "Cumulative Execution Time" + "\n");
		out.write(header);
		System.out.println("\n---------------------------Output--------------------------------");
		
		for (Cloudlet cloudlet1: list) {
			int x = 0;
			submissionTime = cloudlet1.getSubmissionTime();
			finishTime = cloudlet1.getFinishTime();
			startTime = cloudlet1.getExecStartTime();
			executionTime = finishTime - startTime;
			waitingTime = startTime - submissionTime;
			responseTime = waitingTime + submissionTime;
			responseTimeTotal += responseTime;
			executionTimeTotal += executionTime;
			makespan =  executionTime + responseTime;
			makespanTotal += makespan;
			//This prints out the Cloudlet process information
			System.out.println("Cloudlet: " + cloudlet1.getCloudletId() + "\nOn Virtual Machine: " + cloudlet1.getVmId() + "\nMakespan: "
					+ makespan + "\nStart Time: " + startTime + "\nExecution time: " + executionTime + "\nSubmission time: " + submissionTime + "\nFinish Time: " + finishTime 
					+ "\nWaiting time: " + waitingTime + "\nResponse time: " + responseTime + "\nCulmulative Makespan: " + makespanTotal + "\nCumulative Response Time: " + 
					responseTimeTotal + "\nCulmulative Execution Time: " + executionTimeTotal + "\nTransmission Cost: " + transmissionCost[x] + "\nStatus: " + cloudlet1.getStatus());
			//outputs the data needed for comparisons
			out.printf(cloudlet1.getCloudletId() + "," + cloudlet1.getVmId() + "," + makespan + "," + startTime + "," + executionTime + "," + 
					submissionTime + "," + finishTime + "," + waitingTime + "," + responseTime + "," + transmissionCost[x] + "," + makespanTotal + 
					"," + responseTimeTotal + "," + executionTimeTotal + "\n");
			System.out.println("----------------------------------------------------------------------------------------------------------------");
		x++;
		}
		
		//out.close();
	    System.out.println("Output written to CSV: " + csvFile.getAbsolutePath());
		System.out.println("Optimization Algorithm Completed");
		 }	
	}
	
	//used for testing during allocation errors, not invokes as 
	public static boolean canAllocateResources(Host host, Vm vm, int i, int j) {
		double availableMips = host.getAvailableMips();
		long availableRam = host.getRamProvisioner().getAvailableRam();
		long availableBw = host.getBwProvisioner().getAvailableBw();
		
		double requiredMips = vm.getMips();
		long requiredRam = vm.getRam();
		long requiredBw = vm.getBw();
		
		if(availableMips >= requiredMips && availableRam >= requiredRam && availableBw >= requiredBw) {
			System.out.println("VM #" + j + " can be allocated to Host #" + i);
			System.out.println("Available Mips: " + availableMips);
			System.out.println("Requested Mips: " + requiredMips);
			System.out.println("Available Ram: " + availableRam);
			System.out.println("Requested Ram: " + requiredRam);
			return true;
		} else {
			System.out.println("Not enough resources to allocate VM #" + j + " to Host #" + i);
			System.out.println("Available Mips: " + availableMips);
			System.out.println("Requested Mips: " + requiredMips);
			System.out.println("Available Ram: " + availableRam);
			System.out.println("Requested Ram: " + requiredRam);
			return false;
		}
	}
	
}
