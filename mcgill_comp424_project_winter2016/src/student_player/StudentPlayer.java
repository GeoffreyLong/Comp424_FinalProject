package student_player;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import student_player.mytools.MyTools;

/** A Hus player submitted by a student. */
public class StudentPlayer extends HusPlayer {
	private Node mmTreeRoot = new Node(null, Float.MIN_VALUE, null, true);
	private float avgLeaf = 0;
	private float avgCount = 0;
	private int count = 0;
	
    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public StudentPlayer() { super("AlphaBetaSimple"); }

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class hus.RandomHusPlayer
     * for another example agent. */
    public HusMove chooseMove(HusBoardState board_state){
    	// TODO is there a game clock?
    	
    	////////// PROJECT RULES //////////
    	// A draw is declared if neither wins after 5000 turns
    	// 30 seconds for initial move, 2 seconds for subsequent moves
    	// Code must be less than 10mb
    	// Can only use 500mb of ram
    	//		Run JVM with “-Xms520m -Xmx520m” to ensure this
    	// Can multithread, but only one processor 
    	//		Must end threads on turn completion
    	// Allowed to read files, not allowed to write them
    	// Possible to have infinite moves
    	//		This is where you pick up and sow seeds indefinitely within a turn
    	//		Can only have up to 200 of these relays
    	//		If 3 times of more than 200, automatic game loss
    	//		Apparently can check this by running move on copy of board state
    	//			HusBoardState has invalid move checkers
    	// Cannot use external libraries (must make all my own code)
    	//		TODO check to see if tree libraries are acceptable 
    	//		All these: https://docs.oracle.com/javase/7/docs/api/overview-summary.html
    	// Make sure I document well
    	
    	////////// GAMEPLAY NOTES //////////
    	
    	
    	////////// INITIAL THOUGHTS //////////
    	// Should have each gameplay type as a separate package
    	// i.e. if I use minimax, should have a minimax package
    	
    	
    	////////// CODE NOTES //////////    	
    	// In order to change the faceoff, change the 51st line of autoplay.Autoplay
    	// pits -> Each player has an array of pits
    	//		The pits are indexed counter clockwise, starting from bottom left
    	//		[32][31] ... [18][17]
    	//  	[0]	[1]	 ... [15][16]
    	// board_state.getLegalMoves() -> All of the moves that I can do
    	// HusMove.getPit() -> will return the pit number of the move
    	
    	// Can do work on the cloned board state to check game tree
    	//		I'm not sure if this is the most efficient way to go about things
    	//		See how efficient it is vs another routine I come up with for furthering the tree
    	
    	///////////////////////////////////////////////////////////////////////
        // Get the contents of the pits so we can use it to make decisions.
        // int[][] pits = board_state.getPits();

        // Use ``player_id`` and ``opponent_id`` to get my pits and opponent pits.
        //int[] my_pits = pits[player_id];
        //int[] op_pits = pits[opponent_id];

        // Use code stored in ``mytools`` package.
        //MyTools.getSomething();

        // Get the legal moves for the current board state.
    	//ArrayList<HusMove> moves = board_state.getLegalMoves();
        //HusMove move = moves.get(0);
        
        // We can see the effects of a move like this...
    	//HusBoardState cloned_board_state = (HusBoardState) board_state.clone();
        //cloned_board_state.move(move);

    	
    	int count = 0;
    	int count1 = 0;
    	int count2 = 0;
    	int count3 = 0;
    	int count4 = 0;
    	for (HusMove move : board_state.getLegalMoves()){
    		HusBoardState state = (HusBoardState) board_state.clone();
    		state.move(move);
    		count ++;
    		for (HusMove move2 : state.getLegalMoves()){
    			HusBoardState state2 = (HusBoardState) state.clone();
    			state2.move(move2);
    			count1 ++;
    			for (HusMove move3 : state2.getLegalMoves()){
    				HusBoardState state3 = (HusBoardState) state2.clone();
    				state3.move(move3);
    				count2 ++;
    				for (HusMove move4 : state3.getLegalMoves()){
    					HusBoardState state4 = (HusBoardState) state3.clone();
        				state4.move(move4);
        				count3 ++;   
        				for (HusMove move5 : state4.getLegalMoves()){
        					count4 ++;
        				}
    				}
    			}
    		}
    	}
        System.out.println("count: " + count);
        System.out.println("count1: " + count1);
        System.out.println("count2: " + count2);
        System.out.println("count3: " + count3);
        System.out.println("count4: " + count4);
    	
    	
        // Use executor to handle the timing
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // MiniMax mm = new MiniMax(board_state,mmTreeRoot);
        AlphaBeta mm = new AlphaBeta(board_state);
        Future<HusMove> future = executor.submit(mm);
        HusMove move = null;
        
        try {
        	move = future.get(1300, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			System.out.println("interrupt");
		} catch (ExecutionException e) {
			// Print stack trace if there is an error
			System.out.println("interrupt1");
			StringWriter writer = new StringWriter();
			e.printStackTrace( new PrintWriter(writer,true ));
			System.out.println("exeption stack is :\n"+writer.toString());
		} catch (TimeoutException e) {
			// System.out.println("Timeout");
			future.cancel(true);
		} finally {
			
		}
        
        
        // Throw another cancel just in case
        future.cancel(true);
        
        // Wait for back-propagation to complete, then get the best move
        while(!future.isDone());
        move = mm.getBestMove();
        
        // Shut down the executor
        executor.shutdownNow();
        
        return move;
    }
}