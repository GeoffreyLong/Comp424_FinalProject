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
	private Node mmTreeRoot = new Node(null, Float.MIN_VALUE, null, true);
	private float avgLeaf = 0;
	private float avgCount = 0;
	private int count = 0;
	private static int[] weights = new int[13];
	
	
    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public StudentPlayer() { 
    	super(getIndex()); 
    	//super("260403840");
    }

    public static String getIndex(){
    	List<String> weightSet = new ArrayList<String>();	
    	try {
			for (String line : Files.readAllLines(Paths.get("weightSet.txt"))) {
				weightSet.add(line.trim());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	// Easiest way to increment the index
    	List<String> results = new ArrayList<String>();
    	try {
			for (String line : Files.readAllLines(Paths.get("logs/outcomes.txt"))) {
				results.add(line.trim());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	int index = 3;
    	//int index = (results.size() / 10) % weightSet.size();
    	if (index == 0){
    		try{
	    		String newIndividual = "";
	    		for (int i = 0; i < 13; i ++){
	    			int weight = 0;
	    			if (Math.random() > 0.9){
	    				weight = ThreadLocalRandom.current().nextInt(-8, 8 + 1);
	    			}
	    			else if (Math.random() > 0.7){
	    				// Top individuals are (theoretically) the best ones
	    				int ind = ThreadLocalRandom.current().nextInt(0, 5);
	    				String individual = weightSet.get(ind);
	    				String[] tokens = individual.split(" ");
	    				weight = Integer.valueOf(tokens[i]);
	    			}
	    			else if (Math.random() > 0.5){
	    				// Select from any individual
	    				int ind = ThreadLocalRandom.current().nextInt(0, weightSet.size());
	    				String individual = weightSet.get(ind);
	    				String[] tokens = individual.split(" ");
	    				// Change the value a bit
	    				weight = (Integer.valueOf(tokens[i]) + ThreadLocalRandom.current().nextInt(-2, 2)) % 9;
	    			}
	    			else{
	    				weight = 0;
	    			}
	    			
	    			newIndividual += String.valueOf(weight) + " ";
	    		}
	
	    		weightSet.add(newIndividual);
	    		try {
					Files.write(Paths.get("weightSet.txt"), weightSet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		} catch(Exception e){
	    		
	    	}
    	}
    	
    	
    	String[] tokens = weightSet.get(index).split(" ");
		for (int i = 0; i < tokens.length; i++){
			weights[i] = Integer.valueOf(tokens[i]);
		}
    	
		return "TESTING("+String.valueOf(index)+")";
    }
    	
    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class hus.RandomHusPlayer
     * for another example agent. */
    public HusMove chooseMove(HusBoardState board_state){
    	//return timed(board_state);
    	return threaded(board_state);
    }

    // This is the threaded version
    // Not sure if it will play nicely with the system
	private HusMove threaded(HusBoardState board_state) {
        // Use executor to handle the timing
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Instantiate the Alpha Beta Pruning method
        // Pass in the current board state as well as the weighting used
        AlphaBeta mm = new AlphaBeta(board_state, weights, System.currentTimeMillis());
        
        // Submit the instance to the executor
        Future<HusMove> future = executor.submit(mm);

        // Initialize HusMove to null
        // If the method finds no moves, a null move will be random
        HusMove move = null;
        

        // Spawn a thread to perform the work 
        try {
        	if (board_state.getTurnNumber() == 0){
           		mm.timerLimit = 29000;
        		// Give the system 29000 ms if first turn
        		move = future.get(29000, TimeUnit.MILLISECONDS);
 
        	}
        	else{
        		mm.timerLimit = 1900;
        		// This will allow the system to run for 1900 ms
            	move = future.get(1900, TimeUnit.MILLISECONDS);
        	}
		} catch (InterruptedException e) {
			System.out.println("interrupt");
		} catch (ExecutionException e) {
			// Print stack trace if there is an error
			System.out.println("interrupt1");
			StringWriter writer = new StringWriter();
			e.printStackTrace( new PrintWriter(writer,true ));
			System.out.println("exeption stack is :\n"+writer.toString());
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

       	if (board_state.getTurnNumber() == 0){
    		// Give the system 29000 ms if first turn
       		mm.timerLimit = 29000;
    	}
    	else{
    		// This will allow the system to run for 1900 ms
    		mm.timerLimit = 1900;
    	}
        
        // Will throw an exception if timeout
        try {
			mm.runAB();
		} catch (Exception e) {
			
		}
        
        // Return the chosen move
        return mm.getBestMove();
	}
}