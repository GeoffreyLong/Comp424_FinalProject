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
public class MiniMax implements Callable<HusMove> {
	// Will want this to be iterative deepening
	private HusMove bestMove = null;
	private HusBoardState cloned_board_state;
	private Node mmTreeRoot = new Node(null, Float.MIN_VALUE, null, true);
	
	public MiniMax(HusBoardState board_state){
		cloned_board_state = (HusBoardState) board_state.clone();
	}
	
	// super basic for now
	private float estimateNodeValue(HusBoardState state){
		return state.getLegalMoves().size();
	}

	@Override
	public HusMove call() throws Exception {
		// TODO Auto-generated method stub
		int depth = 0;
		
		// Performs useless calculations when the tree isn't deep
		// Not a really bad problem though?
		while (true){
			depth += 1;
			//System.out.println(depth);
			// Recloning will also be slow
			// Would be best if we could just continue on from where we left off
			// This "in-between" is a simple backpropagation of values
			minimax(cloned_board_state, mmTreeRoot, depth, true);

			float bestValue = Float.MIN_VALUE;
			for (Node node : mmTreeRoot.children){
				// In the event of a tie might want to look at the grandchildren of root
				if (node.value > bestValue){
					bestValue = node.value;
					bestMove = node.move;
				}
			}
		}
	}
	
	// Adapted from pseudocode found on https://en.wikipedia.org/wiki/Minimax
	// TODO use the fact that we have already iterated down once
	//		Might be able to save previous values? Don't think so though
	// TODO I pass a clone each time
	//		Do I need to do this?
	//			This seems expensive, might want to optimize
	//			Might be able to simply apply the moves to the board each time?
	//		If I do, do I need to delete the clone to save memory?
	private float minimax(HusBoardState state, Node parent, int depth, boolean isMax){
		ArrayList<HusMove> moves = state.getLegalMoves();
		List<Node> nodeList = new ArrayList<Node>();
		if (depth == 0 || moves.size() == 0){
			// If we are at a leaf or the max depth 
			// (which is basically like a leaf for our purposes)
			// Estimate the value of the node 
			// This value will be based on the heuristics
			//		that are based on the board state
			return estimateNodeValue(state);
		}
		
		// Maximizing player
		if (isMax){
			// Set best value to negative infinity
			float bestValue = Float.MIN_VALUE;
			
			// Iterate through all the moves
			for (HusMove move : moves){
				Node node = new Node(parent,move,isMax);
				
				HusBoardState tempState = (HusBoardState) state.clone();
				
				// TODO Add if statement to check for infinite moves
				tempState.move(move);

				float value = minimax(tempState, node, depth-1, false);
				
				if (value >= bestValue){
					bestValue = value;
				}
				node.value = value;
				nodeList.add(node);
				
			}
			
			parent.children = nodeList;
			return bestValue;
		}
		// Minimizing player
		else{
			// Set best value to positive infinity
			float bestValue = Float.MAX_VALUE;
			
			// Iterate through all the moves
			for (HusMove move : moves){
				Node node = new Node(parent,move,isMax);
				
				HusBoardState tempState = (HusBoardState) state.clone();
				tempState.move(move);

				float value = minimax(tempState, node, depth-1, true);
				
				if (value <= bestValue){
					bestValue = value;
				}
				node.value = value;
				nodeList.add(node);
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
