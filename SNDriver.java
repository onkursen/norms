import java.util.*;
import java.io.*;

public class SNDriver {
	static final int numInitialNodes = 10;
	static final int numChangePerStep = 3;
	static final double p = 0.4;
	static final double q = 0.4;
	static final int numRuns = 1;
	static final int numIterations = 1000;

	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
//		int[] actions = {2, 5, 10, 20};
		int[] actions = {5};
		int[] networkSizes = {100};
//		int[] networkSizes = {100,200,300,400,500,600,700,800,900,1000};
//		int[] networkSizes = {250,500,750,1000};
		
		for (int networkSize : networkSizes) {
			for(int numActions : actions) {
				int[][] rawMax = new int[4][numRuns];
				ArrayList<ArrayList<Integer>> conv = new ArrayList<ArrayList<Integer>>();
				for (int i = 0; i <networkSize; i++)
					conv.add(new ArrayList<Integer>());

				for (int runs = 0; runs < numRuns; runs++) {
					ScaleFreeNetwork network = new ScaleFreeNetwork(numInitialNodes, numChangePerStep, p, q, 
							numActions, networkSize);
					
					/*int num0 = 0;
					Iterator<Agent> i1 = network.iterator();
					while (i1.hasNext()) {
						Agent curr = i1.next();
						if (curr.degree()==0) num0++;
					}
					System.out.println("Number of nodes with 0 neighbors: "+num0);*/

					// Begin degree distribution ---------------------------
					/*int[] degrees = new int[networkSize];
					Iterator<Agent> it2 = network.iterator();
					while (it2.hasNext())
						degrees[it2.next().degree()]++;
					//				System.out.println("0 "+degrees[0]);
					PrintWriter deg = new PrintWriter(new FileWriter(networkSize+".out"));
					for (int i = 0; i<networkSize; i++)
						if (degrees[i]>0)
//							System.out.printf("%d %d \n", i, degrees[i]);
							deg.printf("%d %d \n", i, degrees[i]);
					deg.close();*/
					// End degree distribution ---------------------------


					// Determining the number of hubs -------------------------
					/*int numHubs = 0;
					Iterator<Agent> it0 = network.iterator();
					ArrayList<Agent> nodeDist = network.nodeDist();
					while (it0.hasNext()) {
						Agent curr = it0.next();
						boolean an = network.isHub(curr);
						//					System.out.printf("Agent Id: %d, Degree: %d, network size: %d, isHub: %b \n", curr.getId(), 
						//							curr.degree(), networkSize, an);
						if (an) {
							//						System.out.println("got a hub");
							numHubs++;
						}
					}*/
//					int numHubs = network.numHubs();
					// end hub determination -----------------------------------

//					double[][][] hubHistory = new double[numHubs][numIterations][8];
					int iterations;
					PrintWriter act = new PrintWriter(new FileWriter("actions.out"));
					run: for (iterations = 0; iterations < numIterations; iterations++) {
						Iterator<Agent> it = network.iterator();
						while(it.hasNext()) {
							Agent curr = it.next();
							curr.play(curr.getRandomNeighbor());
							if (curr.getLastAction()!=curr.getActionMax())
								curr.setConvTime(iterations);
						}
						
						Iterator<Agent> actIt = network.iterator();
						while (actIt.hasNext()) {
							act.print(actIt.next().getActionMax() + " ");
						}
						act.println();
						
						// Hub-branch statistics
						/*for (int i = 0; i<numHubs; i++) {
							//	for (int i = 0; i <1; i++) {
							//	System.out.println("hub found");
							Agent hub = network.nodeAt(i);
							double[] neighborPrefs = network.neighborPreference(hub);
							for (int j = 0; j < 8; j++)
								hubHistory[i][iterations][j] = neighborPrefs[j];
						}*/

						// Begin Convergence statistics------------------------------
						int[] dist = network.actionDist();
						int maxLoc = 0;
						for (int i = 1; i<numActions; i++)
							if(dist[i]>dist[maxLoc])
								maxLoc = i;

						double percent = (double) dist[maxLoc]/networkSize;
						if(percent == 1.0) {
							System.out.printf("run %d, numActions %d, populationSize %d, " +
									"iteration of convergence %d \n",
									runs, numActions,networkSize, iterations);
							rawMax[3][runs] = (iterations+1);
							act.close();
							break run;
						}
						else if (percent >= 0.9 && rawMax[2][runs] == 0)
							rawMax[2][runs] = (iterations+1);
						else if (percent >= 0.8 && rawMax[1][runs] == 0)
							rawMax[1][runs] = (iterations+1);
						else if (percent >= 0.7 && rawMax[0][runs] == 0)
							rawMax[0][runs] = (iterations+1);
						// End Convergence Statistics--------------------------------
					}
					
					Iterator<Agent> it = network.iterator();
					while (it.hasNext()) {
						Agent curr = it.next();
						conv.get(curr.degree()).add(curr.getConvTime());
					}
					

					// Begin hub data printing ------------------------------------------------------------------
					/*for (int i = 0; i < numHubs; i++) {
						//					out1.println("Hub " +(i+1) +", Neighbors: " +degrees[i]);
						PrintWriter out1 = new PrintWriter(new FileWriter((i+1)+"SNHub"+numActions+".out"));
						for (int j = 0; j<iterations; j++) {
							out1.printf("%d %d %5.2f %d %5.2f %d %5.2f %d %d \n", j, (int)hubHistory[i][j][0], 
									hubHistory[i][j][1], (int)hubHistory[i][j][2], hubHistory[i][j][3], 
									(int)hubHistory[i][j][4], hubHistory[i][j][5], (int)hubHistory[i][j][6], 
									(int)hubHistory[i][j][7]);
						}
						out1.close();
					}*/
					// End hub data printing ------------------------------------------------------------------
					//				System.out.println("**");
					//				System.out.println("Number of hubs:" + numHubs);
				}

				// Average convergence time of nodes of various degrees
				double[] avgConvTimes = new double[networkSize];
				for (int i = 0; i < networkSize; i++) {
					ArrayList<Integer> curr = conv.get(i);
					if (!curr.isEmpty()) {
						int total = 0;
						for (int x : curr)
							total += x;
						avgConvTimes[i] = (double) (total / curr.size());
					}
					else
						avgConvTimes[i] = -1;
				}
				double[] SDConvTimes = new double[networkSize];
				for (int i = 0; i < networkSize; i++) {
					ArrayList<Integer> curr = conv.get(i);
					if (!curr.isEmpty()) {
						for (int x : curr)
							SDConvTimes[i] += Math.pow((double)x-avgConvTimes[i],2); 
						SDConvTimes[i] = Math.sqrt((double)SDConvTimes[i]/curr.size());
					}
					else
						SDConvTimes[i] = -1;
				}
				/*int total = 0;
				for (ArrayList<Integer> a : conv)
					total+=a.size();
				System.out.println("total "+total);*/

				// Begin printing data of when nodes converge
				PrintWriter convP = new PrintWriter(new FileWriter("convNodes.out"));
				for (int i = 0; i < networkSize; i++) {
					if (avgConvTimes[i] != -1)
						convP.printf("%d %5.2f %5.2f %d \n", i, avgConvTimes[i], SDConvTimes[i],
								conv.get(i).size());
				}
				convP.close();
				// End printing data of when nodes converge

				// Average
				int count = 0;
				double[] avg = new double[4];
				//			double[] SD = new double[4];
				for(int i = 0; i<numRuns; i++) {
					if (rawMax[0][i] != 0 && rawMax[1][i] != 0 && rawMax[2][i] != 0 && rawMax[3][i] != 0) {
						avg[0] += rawMax[0][i];
						avg[1] += rawMax[1][i];
						avg[2] += rawMax[2][i];
						avg[3] += rawMax[3][i];
						count++;
					}
				}
				for(int i = 0; i<4;i++)
					avg[i] /= count;

				// Standard Deviation
				/*for(int i = 0; i<numRuns; i++) {
				if (rawMax[0][i] != 0 && rawMax[1][i] != 0 && rawMax[2][i] != 0 && rawMax[3][i] != 0) {
					SD[0]+=Math.pow(rawMax[0][i]-avg[0], 2);
					SD[1]+=Math.pow(rawMax[1][i]-avg[1], 2);
					SD[2]+=Math.pow(rawMax[2][i]-avg[2], 2);
					SD[3]+=Math.pow(rawMax[3][i ]-avg[3], 2);
				}
			}
			for(int i = 0; i<4;i++) 
				SD[i] = Math.sqrt(SD[i]/count);*/

				System.out.println("numActions "+numActions+", Number of Converging Runs: "+count);

				// Printing Average and SD statistics
				PrintWriter out = new PrintWriter(new FileWriter(networkSize+"SNConvergence"+numActions+".out"));
				for (int i =0; i<4; i++) {
					out.printf("%d ", (70+10*i));
					out.printf("%5.2f ", avg[i]);
					//				out.printf("%5.2f ", SD[i]);
					out.println();
				}
				out.close();

			}
			System.out.println("total time taken: " + (System.currentTimeMillis() - startTime) + " milliseconds");
		}
	}
}
