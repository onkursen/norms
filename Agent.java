import java.util.*;

class Agent implements Comparable<Agent> {

	// Generic variables
	private ArrayList<Agent> neighbors;
	private static Random gen;
	private double prob_pi; // probability of selection according to PI probability distribution
	private int numActions;
	private int id;

	// Variables Specific to Q-learning
	private double[] qTable;
	private int action; // action played
	private int lastAction;
	private int itConv;
	private int actionMax; //most profitable action
	private static final double PROB_RANDOM_ACTION_SELECTION = 0.1;
	private static final double LEARNING_RATE = 0.1;
	private static final double epsilon = 0.2;

	public Agent(int actions, int a) {
		gen = new Random();
		neighbors = new ArrayList<Agent>();
		numActions = actions;
		id = a;
		qTable = new double[numActions];
		reset();
	}
	
	public void reset() {
		for (int i = 0; i < numActions; i++)
			qTable[i] = gen.nextDouble()/10.0;
		maxAction();
		if (gen.nextDouble() <= PROB_RANDOM_ACTION_SELECTION)
			action = gen.nextInt(numActions);
		else
			action = actionMax;
		lastAction = actionMax;
		itConv = 0;
	}
	
	public void play(Agent other) {
		int reward = -1;
		if (action == other.getAction())
			reward = 4;
		qTable[action] = (LEARNING_RATE * reward) + ((1-LEARNING_RATE) * qTable[action]);
		maxAction();
		if (gen.nextDouble()<=epsilon)
			action = gen.nextInt(numActions);
		else
			action = actionMax;
		
	}
	
	public void adjustMR() {
		int[] otherActions = new int[numActions];
		for (Agent x : neighbors) {
			int currAction = x.getAction();
			if(currAction != action)
				otherActions[currAction]++;
		}

		int maxLoc  = 0;
		for (int i = 1; i < numActions; i++)
			if (otherActions[i] > otherActions[maxLoc])
				maxLoc = i;
		if (gen.nextDouble()<=epsilon)
			action = gen.nextInt(numActions);
		else
			action = maxLoc;
	}

	public void addLink(Agent n) {
		neighbors.add(n);
		n.neighbors.add(this);
	}

	public void deleteLink(Agent n) {
		neighbors.remove(n);
		n.neighbors.remove(this);
	}
	
	private void maxAction() {
		int maxLoc = 0;
		for (int i = 1; i < numActions; i++)
			if (qTable[i] > qTable[maxLoc])
				maxLoc = i;
//		if (actionMax != maxLoc) {
			lastAction = actionMax;
			actionMax = maxLoc;
//		}
//		return maxLoc;
	}
	
	public double preference() {
		int maxLoc = 0;
		int secondLoc = 1;
		for (int i = 1; i < numActions; i++) {
			if (qTable[i] > qTable[maxLoc]) {
				secondLoc = maxLoc;
				maxLoc = i;
			}
		}
		double maxVal = qTable[maxLoc];
		double secondVal = qTable[secondLoc];
		return (maxVal - secondVal);
	}
	
	public int compareTo(Agent otr) {
		if (degree() < otr.degree())
			return -1;
		if (degree() == otr.degree())
			return 0;
		return 1;
	}
	
	public int getAction() {return action;}
	public int getLastAction() {return lastAction;}
	public int getId() {return id;}
	public int getActionMax() {return actionMax;}
	public Agent getRandomNeighbor() {return neighbors.get(gen.nextInt(degree()));}
	public int degree() {return neighbors.size();}
	public double getProb() {return prob_pi;}
	public void setProb(double aProb) {prob_pi = aProb;}
	public int getConvTime() {return itConv;}
	public void setConvTime(int it) {itConv = it;}
	public boolean isNeighbor(Agent x) {return neighbors.contains(x);}
	public Iterator<Agent> neighborIterator() {return neighbors.iterator();}
}
