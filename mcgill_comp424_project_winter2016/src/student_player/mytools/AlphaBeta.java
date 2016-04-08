package student_player.mytools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import hus.HusBoardState;
import hus.HusMove;

// Technically could just do runnable
// This will be iterative
public class AlphaBeta implements Callable<HusMove> {
	// Will want this to be iterative deepening
	private HusMove bestMove = null;
	private HusBoardState cloned_board_state;
	private int playerNum;
	private int oppPlayerNum;
	private boolean isInterrupted = false;
	private int[] weights;
	private long startTime = Long.MAX_VALUE;
	public int timeLimit = 1800;
	
	public AlphaBeta(HusBoardState board_state, int[] weights, long startTime){
		cloned_board_state = (HusBoardState) board_state.clone();
		this.playerNum = board_state.getTurnPlayer();
		this.oppPlayerNum = (this.playerNum + 1) % 2;
		this.weights = weights;
		this.startTime = startTime;
	}
	
	@Override
	public HusMove call() throws Exception {
		return runAB();
	}

	public HusMove runAB() throws Exception{
		// Start at depth three
		// Expected to, at minimum, reach depth 4
		// This should offer a slight speedup
		int depth = 4;
		

		// This while statement handles the iterative deepening
		// The system will run alpha beta pruning with one extra level each iteration
		while (true){
			// If the thread has been interrupted, break the loop
			if (isInterrupted) break;
			
			int systime = (int) System.currentTimeMillis();

			Node mmTreeRoot = new Node(null, null, true);
			
			// Run Alpha Beta pruning
			alphabeta(cloned_board_state, mmTreeRoot, depth, Float.MIN_VALUE, Float.MAX_VALUE, true);

			// Make sure to break before the backpropagation
			if (isInterrupted) break;
			
			// Find the best possible move based on the children
			// Store  the best move in a class variable
			float bestValue = Float.MIN_VALUE;
			for (Node node : mmTreeRoot.children){
				// In the event of a tie might want to look at the grandchildren of root
				if (node.value > bestValue){
					bestValue = node.value;
					bestMove = node.move;
				}
			}
			
			// Simple way of easily increasing the search
			// Increase the depth of the search
			int endTime = (int) System.currentTimeMillis() - systime;
			int timeLeft = (int) System.currentTimeMillis() - (int) startTime;
			
			if (endTime == 0){
				depth += 1;
			}
			else{
				long multiplicity = timeLeft / endTime;
				if (multiplicity > 3) multiplicity = 3;
				if (multiplicity < 1) multiplicity = 1;
				depth += multiplicity;
			}
		}
		return bestMove;
	}
	
	// Adapted from pseudocode found on https://en.wikipedia.org/wiki/alpha-beta_pruning
	private float alphabeta(HusBoardState state, Node parent, int depth, float alpha, float beta, boolean isMax) throws Exception{
		// Return if the timer runs out
		if (System.currentTimeMillis() - startTime >= timeLimit) {
			isInterrupted = true;
		}

		
		// Necessary to set this flag since Thread.interrupted() resets when called
		if (Thread.interrupted()) isInterrupted = true;

		if (isInterrupted) {
			throw new Exception("Timeout");
		}
		
		
		ArrayList<HusMove> moves = state.getLegalMoves();

		// Simple sort to give preference to those
		// Slightly more likely to land on an inner pit
		// But on an earlier inner pit rather than a later one
		Collections.sort(moves, new Comparator<HusMove>() {
			public int compare(HusMove x, HusMove y){
				int xVal = Math.abs(x.getPit() - 15);
				int yVal = Math.abs(y.getPit() - 15);
				
				return xVal - yVal;
			}
		});
		
		// If max depth reached or if thread was interrupted
		if (depth == 0 || moves.size() == 0){
			// If we are at a leaf or the max depth 
			// (which is basically like a leaf for our purposes)
			// Estimate the value of the node 
			// This value will be based on the heuristics
			//		that are based on the board state
			return MyTools.opt_evaluator(state, playerNum, oppPlayerNum, weights);
		}

		// Initialize the empty children list
		List<Node> children = new ArrayList<Node>();
		
		// Maximizing player
		if (isMax){
			// Set best value to negative infinity
			float bestValue = alpha;
			
			// Iterate through all the moves
			// Each move will be a child value
			for (HusMove move : moves){
				Node node = new Node(parent,move,isMax);

				// Clone the state to avoid mutating the parent's or sibling's state
				// Relic of the pass by reference nature
				HusBoardState tempState = (HusBoardState) state.clone();
				
				// The move is infinite if false
				if (!tempState.move(move)) continue;

				// Perform recursive alpha beta
				float value = alphabeta(tempState, node, depth-1, alpha, beta, false);
				
				// Update the value of bestValue and alpha
				if (value > bestValue) bestValue = value;
				if (value > alpha) alpha = value;

				// Set the node's value and add to children list
				node.value = value;
				children.add(node);

				// Beta cutoff
				if (beta <= alpha) break;
			}
			
			// Add the children list to the parent
			parent.children = children;
			
			// Return the best performing child
			return bestValue;
		}
		// Minimizing player
		else{
			// Set best value to positive infinity
			float bestValue = beta;
			
			// Iterate through all the moves
			for (HusMove move : moves){
				Node node = new Node(parent,move,isMax);

				// Clone the state to avoid mutating the parent's or sibling's state
				// Relic of the pass by reference nature
				HusBoardState tempState = (HusBoardState) state.clone();

				// The move is infinite if false
				if (!tempState.move(move)) continue;
				
				// Recursively compute the value by backpropagation
				float value = alphabeta(tempState, node, depth-1, alpha, beta, true);

				// update bestValue and beta
				if (value < bestValue) bestValue = value;
				if (value < beta) beta = value;
				
				// Set the node value
				node.value = value;
				
				// Add the child to the list of children
				children.add(node);

				// Alpha cutoff
				if (beta <= alpha) break;				
			}
			
			// Add the children list to the parent
			parent.children = children;
			
			// Return the best performing child (for minimizing)
			return bestValue;
		}
	}
	
	// Could have this do the recursion the update the best Value
	// Might be smart to do at the end if we can estimate the time required to calc
	// Else just throw the static one 
	// Might be able to do this with a callable as well
	// i.e. have another timed method, if that method times out then
	// just return the statically generated value
	public HusMove getBestMove(){
		return this.bestMove;
	}
}
