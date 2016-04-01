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

    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public StudentPlayer() { super("260403840"); }

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
    	//		[15][14][13][12][11][10][9]	[8]
    	//  	[0]	[1]	[2]	[3]	[4]	[5]	[6]	[7]
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

        // Use executor to handle the timing
        ExecutorService executor = Executors.newSingleThreadExecutor();
        MiniMax mm = new MiniMax(board_state);
        Future<HusMove> future = executor.submit(mm);
        HusMove move = null;
        try {
        	move = future.get(1900, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			System.out.println("interrupt");
		} catch (ExecutionException e) {
			System.out.println("interrupt1");
			StringWriter writer = new StringWriter();
			e.printStackTrace( new PrintWriter(writer,true ));
			System.out.println("exeption stack is :\n"+writer.toString());
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			System.out.println("interrupt2");
			move = mm.getBestMove();
		} finally {
			future.cancel(true);
		}
        
        //System.out.println("Hello" + move.toPrettyString());
        executor.shutdownNow();
        
        // But since this is a placeholder algorithm, we won't act on that information.
        return move;
    }
}