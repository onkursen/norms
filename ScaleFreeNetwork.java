import java.util.*;

class ScaleFreeNetwork {
	private ArrayList<Agent> nodes;
	private static Random gen;
	private int m0; // Initial number of nodes
	private int m; // Number of edges to add or rewire at each step
	private double p; // Probability of adding links
	private double q; // Probability of rewiring links
	private int numActions;
	private static double thresholdHub = 0.25;
//	private static int thresholdHub = 4.5*Math.sqrt()
	private int numHubs;

	public ScaleFreeNetwork(int m0Val, int mVal, double pVal, double qVal, int actions, int goalSize) {
		m0 = m0Val;
		m = mVal;
		p = pVal;
		q = qVal;
		numActions = actions;
		gen = new Random();
		nodes = new ArrayList<Agent>();
		for (int i = 0; i < m0; i++)
			nodes.add(new Agent(numActions, i));
//		int countOp = 0;
		while (size() < goalSize) {
			double x = gen.nextDouble();
			if (x <=p){
//				System.out.println("starting addlinks");
				addLinks();
			}
			else if (x<=p+q){
//				System.out.println("starting rewirelinks");
				rewireLinks();
			}
			else{
//				System.out.println("starting addnode");
				addNode();
			}
			/*else {
				x = gen.nextDouble();
				if (x<= q/(1-p))
					rewireLinks();
				else
					addNode();
			}*/
//			countOp++;
		}
//		System.out.println("Network generation completed, numOps: "+countOp);
		Collections.sort(nodes);
		Collections.reverse(nodes);
		numHubs = 0;
		for (Agent a : nodes)
			if (isHub(a))
				numHubs++;
	}

	// addLinks: step 1 of Albert-Barabasi algorithm
	private void addLinks() {
		for (int i = 0; i < m; i++) {
			Agent start = nodes.get(gen.nextInt(nodes.size()));
			PD();
			double probOfSelection = gen.nextDouble();
			double total = 0;
			search: for (Agent curr : nodes) {
				if (!curr.equals(start)) { 
					total += curr.getProb();
					if (probOfSelection <= total && !start.isNeighbor(curr)) {
						start.addLink(curr);
						break search;
					}
				}
			}
		}
	}

	// rewireLinks: step 2 of Albert-Barabasi algorithm
	private void rewireLinks() {
//		System.out.println("rewire start starting");
		Iterator<Agent> it = nodes.iterator();
		ArrayList<Agent> valid = new ArrayList<Agent>();
		while(it.hasNext()) {
			Agent now = it.next();
			if (now.degree()>0)
				valid.add(now);
		}
//		System.out.println("rewire start generated");
		
		rewire: if (!valid.isEmpty()) {
			for (int i = 0; i < m; i++) {
				Agent start = nodes.get(gen.nextInt(nodes.size()));
				boolean found = false;
				int loc = gen.nextInt(valid.size());
				int count = 0;
//				System.out.println("Starting search for place to delete");
				while (!found) {
					start = valid.get((loc + count++) % valid.size());
					if (count == valid.size())
						break rewire;
					Iterator<Agent> ni = start.neighborIterator();
					ArrayList<Agent> candidates = new ArrayList<Agent>();
//					System.out.println("starting compiling candidates");
					while (ni.hasNext()) {
						Agent curr = ni.next();
						if (curr.degree()>1)
							candidates.add(curr);
					}
//					System.out.println("finished compiling candidates");
					if (!candidates.isEmpty()) {
						Agent end = candidates.get(gen.nextInt(candidates.size()));
						start.deleteLink(end);
						found = true;
//						System.out.println("found place for deletion");
					}
				}
				//System.out.printf("node chosen: id:%d,degree:%d \n",start.getId(), start.degree());
				PD();
				double probOfSelection = gen.nextDouble();
				double total = 0;
				search: for (Agent curr : nodes) {
					total += curr.getProb();
					if (probOfSelection <= total && !curr.equals(start) && !start.isNeighbor(curr)) {
						start.addLink(curr);
//						System.out.println("found place to rewire");
						if (curr.degree()==1)
							valid.add(curr);
						break search;
					}
				}
			}
		}
	}

	// addNode: step3 of Albert-Barabasi algorithm
	private void addNode() {
		// Add a new node
		Agent start = new Agent(numActions, size());
		nodes.add(start);
		// Add m links according to PI PD
		for (int i = 0; i < m; i++) {
			PD();
			double probOfSelection = gen.nextDouble();
			double total = 0;
			search: for (Agent curr : nodes) {
				if (!curr.equals(start)) { 
					total += curr.getProb();
					if (probOfSelection <= total && !start.isNeighbor(curr)) {
						start.addLink(curr);
						break search;
					}
				}
			}
		}
	}
	
	public int[] actionDist() {
		int[] actionValues = new int[numActions];
		for (Agent curr : nodes)
			actionValues[curr.getActionMax()]++;
		return actionValues;
	}

	// 1. Sets each agent's prob_pi variable according to the PI probability distribution
	// 2. Sorts the nodes by prob_pi, as detailed in Agent's compareTo method
	private void PD() {
		float total = 0;
		for (Agent x : nodes)
			total += (float)(x.degree() + 1);
		for (Agent x : nodes) {
			float indiv = (float)(x.degree() + 1);
			x.setProb(indiv/total);
		}
		Collections.sort(nodes);
		Collections.reverse(nodes);
	}
	
	public double[] neighborPreference(Agent curr) {
		double hubCount = 0;
		double nonHubCount = 0;
		double hubPrefs = 0;
		double nonHubPrefs = 0;
		int[] hubs = new int[numActions];
		int[] nonHubs = new int[numActions];
		Iterator<Agent> it = curr.neighborIterator();
		int currAction = curr.getActionMax();
		int popAction = 0;
		
		while (it.hasNext()) {
			Agent x = it.next();
			int xAction = x.getActionMax();
			if (isHub(x)) {
//				if (x.getId()== curr.getId())
//					System.out.printf("%d connected to %d \n", curr.getId(),x.getId());
				hubs[xAction]++;
				if (currAction == xAction) {
					hubCount++;
					hubPrefs += x.preference();
				}
			}
			else {
				nonHubs[xAction]++;
				if (currAction == xAction) {
					nonHubCount++;
					nonHubPrefs += x.preference();
				}
			}
		}
		
		if (hubCount>numHubs) {
			System.out.printf("HC: %d,Id %d:" , (int)hubCount, curr.getId());
			Iterator<Agent> it2 = curr.neighborIterator();
			while (it2.hasNext()) {
				Agent y = it2.next();
				if (isHub(y)) {
					System.out.printf("(%d,%d)", y.getId(), y.degree());
				}
			}
			System.out.println();
		}
		
		for (int i = 1; i < numActions; i++)
			if (hubs[i] + nonHubs[i] > hubs[popAction] + nonHubs[popAction])
				popAction = i;

		// 1. Action preferred by hub (int)
		// 2. How much preferred compared to 2nd-best action (double)
		// 3. How many hub children prefer the hub's action (int)
		// 4. By how much the hub children prefer that action (on average) (double)
		// 5. How many non-hub children prefer the hub's action (int)
		// 6. By how much the non-hub children prefer that action (on average) (double)
		// 7. Most followed action among hub's neighbors (int)
		// 8. # of neighbors following that action (int)
		double[] output = {currAction, curr.preference(), hubCount, hubPrefs/hubCount, nonHubCount, 
				nonHubPrefs/nonHubCount, popAction, hubs[popAction]+nonHubs[popAction]};
		return output;
	}
	
	public boolean isHub(Agent x) {return ((double)x.degree() / size()) >= thresholdHub;}
	public int numHubs() {return numHubs;}
	public int size() {return nodes.size();}
	public Iterator<Agent> iterator() {return nodes.iterator();}
	public Agent nodeAt(int i) {return nodes.get(i);}
}
