package student_player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

import hus.HusBoardState;
import hus.HusMove;
import student_player.mytools.MyTools;

public class MinimaxOptimized implements Callable<HusMove> {
	private HusMove bestMove = null;
	private HusBoardState cloned_board_state;
	private Node mmTreeRoot;
	private int playerNum;
	private int oppPlayerNum;
	private boolean isInterrupted = false;
	private Queue<Node> queue;

	public MinimaxOptimized(HusBoardState board_state, Node mmTreeRoot, Queue<Node> queue){
		cloned_board_state = (HusBoardState) board_state.clone();
		this.mmTreeRoot = mmTreeRoot;
		this.playerNum = board_state.getTurnPlayer();
		this.oppPlayerNum = (this.playerNum + 1) % 2;
		this.queue = queue;
	}
	
	@Override
	public HusMove call() throws Exception {
		List<Node> leaves = new LinkedList<Node>();
		while (!isInterrupted && !queue.isEmpty()){
			if (Thread.interrupted()) break;

			Node curNode = queue.poll();
			List<Node> children = new ArrayList<Node>();
			HusBoardState curState = curNode.boardState;
			
			// Remove this attribute to save space
			curNode.boardState = null;
			
			for (HusMove move : curState.getLegalMoves()){
				HusBoardState newState = (HusBoardState) curState.clone();
				newState.move(move);
				
				// Create a new node instantiated according to max/min
				Node newNode;
				if (curNode.isMax) {
					newNode = new Node(curNode, Float.MAX_VALUE, move, newState, false);
				}
				else{
					newNode = new Node(curNode, Float.MIN_VALUE, move, newState, true);
				}
				
				children.add(newNode);
				queue.add(newNode);
			}
			
			curNode.children = children;			
			if (children.isEmpty()) leaves.add(curNode);			
		}
		
		// Not super sure what this will do
		// TODO check it, hopefully will not remove the nodes
		// This is create a new queue which is a reverse version of the old
		Queue<Node> tempQueue = new LinkedList<Node>();
		Node[] tempArray = (Node[]) queue.toArray();
		for (int i = tempArray.length-1; i >= 0; i--){
			tempQueue.add(tempArray[i]);
		}
		
		// Theoretically could do away with leaves, put tempQueue at the top
		// And add the leaves in there as they appear in BFS
		// But this would be inefficient since the leaves would be processed first
		// By the below, and we don't want that
		tempQueue.addAll(leaves);

		Node lastEntered = null;
		while(!tempQueue.isEmpty()){
			// Could theoretically create a second interrupter to break out of this
			// This will keep hard time constraints
			if (Thread.interrupted()) break;
			
			// Get value of Node
			Node curNode = tempQueue.poll();
			curNode.value = MyTools.seedDifference(curNode.boardState, playerNum, oppPlayerNum);

			// BackPropagation
			Node parent = curNode.parent;
			if ((parent.isMax && curNode.value > parent.value)
					|| (!parent.isMax && curNode.value < parent.value)){
				parent.value = curNode.value;
				if (lastEntered != parent) tempQueue.add(parent);
			}			
		}
		
		
		return getBestMove();
	}
	
	// TODO remove some useless code
	public HusMove getBestMove(){
		float bestValue = Float.MIN_VALUE;
		for (Node node : mmTreeRoot.children){
			// In the event of a tie might want to look at the grandchildren of root
			if (node.value > bestValue){
				bestValue = node.value;
				bestMove = node.move;
			}
		}
		return this.bestMove;
	}
}
