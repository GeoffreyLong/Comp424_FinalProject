package student_player;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

import hus.HusBoardState;
import hus.HusMove;
import student_player.mytools.MyTools;

public class MinimaxOptimized implements Callable<Node> {
	private Node bestNode = null;
	private HusBoardState cloned_board_state;
	private Node mmTreeRoot;
	private int playerNum;
	private int oppPlayerNum;
	private Queue<Node> queue;

	public MinimaxOptimized(HusBoardState board_state, Node mmTreeRoot, Queue<Node> queue){
		cloned_board_state = (HusBoardState) board_state.clone();
		this.mmTreeRoot = mmTreeRoot;
		this.playerNum = board_state.getTurnPlayer();
		this.oppPlayerNum = (this.playerNum + 1) % 2;
		this.queue = queue;
	}
	
	@Override
	public Node call() throws Exception {
		// This is create a new queue which is a reverse version of the old
		Deque<Node> tempQueue = new LinkedList<Node>();
		
		while (!queue.isEmpty()){
			if (Thread.interrupted()) break;

			Node curNode = queue.poll();
			List<Node> children = new ArrayList<Node>();
			HusBoardState curState = curNode.boardState;
			
			// Remove this attribute to save space
			// The values at these nodes will be backpropagated, 
			// So we never need to calculate them
			// Save some space by setting the boardState to null
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
			
			// Leaf node
			if (children.isEmpty()){
				curNode.boardState = curState;
				tempQueue.add(curNode);			
			}
		}
		
		tempQueue.addAll(queue);
		
		while(!tempQueue.isEmpty()){
			// Could theoretically create a second interrupter to break out of this
			// This will keep hard time constraints
			// Actually, can't do this, if I do then values are wrong!
			// if (Thread.interrupted()) break;
			
			// Get value of Node
			Node curNode = tempQueue.removeLast();
			if ((curNode.value == Float.MAX_VALUE || curNode.value == Float.MIN_VALUE)
					&& curNode.boardState != null){
				curNode.value = MyTools.seedDifference(curNode.boardState, playerNum, oppPlayerNum);
			}
			
			// BackPropagation
			Node parent = curNode.parent;
			if (parent != null){
				if (((parent.isMax && curNode.value > parent.value)
						|| (!parent.isMax && curNode.value < parent.value))
						&& curNode.value != Float.MAX_VALUE && curNode.value != Float.MIN_VALUE){
					parent.value = curNode.value;
				}			
	
				if (tempQueue.peekFirst() != parent) tempQueue.addFirst(parent);
			}
		}
		
		
		return getBestNode();
	}
	
	public Node getBestNode(){
		float bestValue = Float.MIN_VALUE;
		for (Node node : mmTreeRoot.children){
			if (node.value > bestValue && node.value != Float.MAX_VALUE){
				bestValue = node.value;
				bestNode = node;
			}
		}
		return this.bestNode;
	}
}
