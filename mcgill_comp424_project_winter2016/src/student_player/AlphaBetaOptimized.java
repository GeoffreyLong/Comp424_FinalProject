//TODO
//	Consider sorting the nodes so that it is easy to prune + get max/min value


package student_player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import hus.HusBoardState;
import hus.HusMove;

// Technically could just do runnable
// This will be iterative
public class AlphaBetaOptimized implements Callable<HusMove> {
	// Will want this to be iterative deepening
	private HusMove bestMove = null;
	private HusBoardState cloned_board_state;
	private Node mmTreeRoot;
	private int playerNum;
	private int oppPlayerNum;
	private boolean isInterrupted = false;
	//public int count = 0;
	//public int leafCount = 0;
	
	public AlphaBetaOptimized(HusBoardState board_state, Node mmTreeRoot){
		cloned_board_state = (HusBoardState) board_state.clone();
		this.mmTreeRoot = mmTreeRoot;
		this.playerNum = board_state.getTurnPlayer();
		this.oppPlayerNum = (this.playerNum + 1) % 2;
	}
		
	// super basic for now
	private float estimateNodeValue(HusBoardState state){
		// state.getLegalMoves().size();
		int[][] pits = state.getPits();
		
		int[] player_pits = pits[playerNum];
        int[] opp_pits = pits[oppPlayerNum];
        
        int value = 0;
        for (int i = 0; i < player_pits.length; i++){
        	value += player_pits[i] - opp_pits[i];
        }
        
        return value;
	}

	@Override
	public HusMove call() throws Exception {
		// TODO Auto-generated method stub
		int depth = 3;
		
		// Performs useless calculations when the tree isn't deep
		// Not a really bad problem though?
		while (true){
			depth += 1;
			// System.out.println(depth);

			// Recloning will also be slow
			// Would be best if we could just continue on from where we left off
			// This "in-between" is a simple backpropagation of values
			alphabeta(cloned_board_state, mmTreeRoot, depth, Float.MIN_VALUE, Float.MAX_VALUE, true);

			float bestValue = Float.MIN_VALUE;
			for (Node node : mmTreeRoot.children){
				// In the event of a tie might want to look at the grandchildren of root
				if (node.value > bestValue){
					bestValue = node.value;
					bestMove = node.move;
				}
			}
			
			if (isInterrupted) break;
		}
		return bestMove;
	}
	
	// Adapted from pseudocode found on https://en.wikipedia.org/wiki/alpha-beta_pruning
	private float alphabeta(HusBoardState state, Node parent, int depth, float alpha, float beta, boolean isMax){
		ArrayList<HusMove> moves = state.getLegalMoves();

		// Necessary to set this flag since Thread.interrupted() resets when called
		if (Thread.interrupted()) isInterrupted = true;

		// If max depth reached or if thread was interrupted
		if (depth == 0 || moves.size() == 0 || isInterrupted){
			//leafCount++;
			// If we are at a leaf or the max depth 
			// (which is basically like a leaf for our purposes)
			// Estimate the value of the node 
			// This value will be based on the heuristics
			//		that are based on the board state
			return estimateNodeValue(state);
		}
		
		List<Node> nodeList = new ArrayList<Node>();
		
		// Maximizing player
		if (isMax){
			// Set best value to negative infinity
			float bestValue = alpha;
			
			// Iterate through all the moves
			for (HusMove move : moves){
				Node node = new Node(parent,move,isMax);
				
				HusBoardState tempState = (HusBoardState) state.clone();
				// TODO Add if statement to check for infinite moves
				tempState.move(move);

				float value = alphabeta(tempState, node, depth-1, alpha, beta, false);
				
				if (value > bestValue) bestValue = value;
				if (value > alpha) alpha = value;

				node.value = value;
				nodeList.add(node);

				// Beta cutoff
				if (beta <= alpha) break;
				
			}
			
			parent.children = nodeList;
			return bestValue;
		}
		// Minimizing player
		else{
			// Set best value to positive infinity
			float bestValue = beta;
			
			// Iterate through all the moves
			for (HusMove move : moves){
				Node node = new Node(parent,move,isMax);
				
				HusBoardState tempState = (HusBoardState) state.clone();
				tempState.move(move);

				float value = alphabeta(tempState, node, depth-1, alpha, beta, true);
				
				if (value < bestValue) bestValue = value;
				if (value < beta) beta = value;
				
				node.value = value;
				nodeList.add(node);

				// Alpha cutoff
				if (beta <= alpha) break;				
			}
			
			parent.children = nodeList;
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
