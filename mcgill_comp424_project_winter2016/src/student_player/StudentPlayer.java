package student_player;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import autoplay.Autoplay;
import student_player.mytools.AlphaBeta;
import student_player.mytools.MyTools;

/** A Hus player submitted by a student. */
public class StudentPlayer extends HusPlayer {
	private static int[] weights = new int[13];
	private final int TIME_LIMIT = 1800;
	
	
    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public StudentPlayer() { 
    	super("260403840");
    }
    	
    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class hus.RandomHusPlayer
     * for another example agent. */
    public HusMove chooseMove(HusBoardState board_state){
		if (board_state.getTurnPlayer() == 0){
	    	weights[0] = 8;
			weights[1] = 0;
			weights[2] = 2;
			weights[3] = 0;
			weights[4] = 2;
			weights[5] = 0;
			weights[6] = 2;
			weights[7] = 0;
			weights[8] = 2;
			weights[9] = 0;
			weights[10] = 2;
			weights[11] = 0;
			weights[12] = 2;			
		}
		else{
	    	weights[0] = 8;
			weights[1] = 0;
			weights[2] = 2;
			weights[3] = -3;
			weights[4] = 2;
			weights[5] = 0;
			weights[6] = 0;
			weights[7] = 0;
			weights[8] = 2;
			weights[9] = 0;
			weights[10] = 0;
			weights[11] = 0;
			weights[12] = 2;			
		}
    	
    	return timed(board_state);
    	//return threaded(board_state);
    }

    // This is the threaded version
    // Not sure if it will play nicely with the system
	private HusMove threaded(HusBoardState board_state) {
        // Use executor to handle the timing
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Instantiate the Alpha Beta Pruning method
        // Pass in the current board state as well as the weighting used
        AlphaBeta mm = new AlphaBeta(board_state, weights, System.currentTimeMillis());
        
		// If it is threaded, add another 300 ms to the timer
		// Don't want to time out prematurely
		// But will want to time out if, say, there is an error
        mm.timeLimit = TIME_LIMIT + 300;
        
        // Submit the instance to the executor
        Future<HusMove> future = executor.submit(mm);

        // Initialize HusMove to null
        // If the method finds no moves, a null move will be random
        HusMove move = null;
        

        // Spawn a thread to perform the work 
        try {
        	// This will allow the system to run for 1900 ms
        	// Ease off a bit for the trials though
        	move = future.get(TIME_LIMIT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			//System.out.println("interrupt");
		} catch (ExecutionException e) {
			// Print stack trace if there is an error
			//System.out.println("interrupt1");
			//StringWriter writer = new StringWriter();
			//e.printStackTrace( new PrintWriter(writer,true ));
			//System.out.println("exeption stack is :\n"+writer.toString());
		} catch (TimeoutException e) {
			// On a timeout, the thread will end up here
			// This will throw an interrupt to the thread
			future.cancel(true);
		} catch (Exception e){
			
    	}finally {
			
		}
        
        // Pass the thread another interrupt just in case
        // This sets the isInterrupted flag on the thread
        future.cancel(true);
        
        // Wait for the back-propagation to complete, then get the best move
        while(!future.isDone());
        move = mm.getBestMove();
        
        // Shut down the executor
        executor.shutdownNow();
        
        // Return the chosen move
        return move;
	}

	// Timed Version
	private HusMove timed(HusBoardState board_state) {
		// Instantiate the Alpha Beta Pruning method
        // Pass in the current board state as well as the weighting used
        AlphaBeta mm = new AlphaBeta(board_state, weights, System.currentTimeMillis());
        mm.timeLimit = TIME_LIMIT;
        
        // Will throw an exception if timeout
        try {
			mm.runAB();
		} catch (Exception e) {
			
		}
        
        // Return the chosen move
        return mm.getBestMove();
	}
}