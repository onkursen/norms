import java.util.*;

public class RingNetwork {
	private ArrayList<Agent> nodes;
	private int numActions;
	private int neighborDistance;
	private static Random gen;
	
	public RingNetwork(int size, int actions, int distance) {
		numActions = actions;
		neighborDistance = distance;
		gen = new Random();
		nodes = new ArrayList<Agent>();
		for (int i = 0; i < size; i++)
			nodes.add(new Agent(numActions, i));
	}
	
	public void reset() {
		for (Agent x: nodes)
			x.reset();
	}
	
	public int[] actionMaxDist() {
		int[] actionValues = new int[numActions];
		for (Agent curr : nodes)
			actionValues[curr.getActionMax()]++;
		return actionValues;
	}
	
	public Agent getNeighbor(Agent curr) {
		int j = nodes.indexOf(curr) +gen.nextInt(2*neighborDistance) - neighborDistance;
		int size = nodes.size();
		if (j >= size) 
			j -= size;
		else if (j < 0)
			j += size;
		return nodes.get(j);
	}
	
	public Iterator<Agent> iterator() {return nodes.iterator();}
}
