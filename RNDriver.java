import java.util.*;
import java.io.*;

public class RNDriver {

	static final int numRuns = 20;
	static final int numIterations = 100000;
	static final int networkSize = 500;
	static int numActions;

	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
//		int[] actions = {2, 5, 10, 20};
		int[] actions = {2,20};
//		int[] distances = {49,37,25,24,21,17};
		int[] distances = {250};
		
		for (int neighborhoodDistance : distances) {
			for(int numActions : actions) {
				int[][] rawMax = new int[4][numRuns];
				RingNetwork network = new RingNetwork(networkSize, numActions, neighborhoodDistance);

				for (int runs = 0; runs < numRuns; runs++) {
					network.reset();
					run: for (int iterations = 0; iterations < numIterations; iterations++) {
						Iterator<Agent> it = network.iterator();
						while(it.hasNext()) {
							Agent curr = it.next();
							Agent opponent = network.getNeighbor(curr);
							curr.play(opponent);
//							System.out.printf("%d Playing %d \n",curr.getId(), opponent.getId());
						}
						int[] dist = network.actionMaxDist();
						int maxLoc = 0;
						for (int i = 1; i<numActions; i++)
							if(dist[i]>dist[maxLoc])
								maxLoc = i;

						double percent = (double) dist[maxLoc]/networkSize;
						if(percent == 1.0) {
							System.out.printf("run %d, numActions %d, window size %d, iteration of convergence %d\n",
									runs, numActions, neighborhoodDistance, iterations);
							rawMax[3][runs] = (iterations+1);
							break run;
						}
						else if (percent >= 0.9 && rawMax[2][runs] == 0)
							rawMax[2][runs] = (iterations+1);
						else if (percent >= 0.8 && rawMax[1][runs] == 0)
							rawMax[1][runs] = (iterations+1);
						else if (percent >= 0.7 && rawMax[0][runs] == 0)
							rawMax[0][runs] = (iterations+1);
					}
				}

				// Average and SD
				int count = 0;
				double[] avg = new double[4];
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


				/*
				 double[] SD = new double[4];
			for(int i = 0; i<numRuns; i++) {
				if (rawMax[0][i] != 0 && rawMax[1][i] != 0 && rawMax[2][i] != 0 && rawMax[3][i] != 0) {
					SD[0]+=Math.pow(rawMax[0][i]-avg[0], 2);
					SD[1]+=Math.pow(rawMax[1][i]-avg[1], 2);
					SD[2]+=Math.pow(rawMax[2][i]-avg[2], 2);
					SD[3]+=Math.pow(rawMax[3][i]-avg[3], 2);
				}
			}
			for(int i = 0; i<4;i++) 
				SD[i] = Math.sqrt(SD[i]/count);
				 */

				System.out.println("numActions "+numActions+", Number of Converging Runs: "+count);

				// Printing Average and SD statistics
				PrintWriter out = new PrintWriter(new FileWriter(neighborhoodDistance+"RNConvergence"+numActions+
				".out"));
				for (int i =0; i<4; i++) {
					out.printf("%d ", (70+10*i));
					out.printf("%5.2f ", avg[i]);
//					out.printf("%5.2f ", SD[i]);
					out.println();
				}
				out.close();
			}
			System.out.println("total time taken: " + (System.currentTimeMillis() - startTime) + " milliseconds");
		}
	}
}
