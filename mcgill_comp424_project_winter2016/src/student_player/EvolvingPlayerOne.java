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
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import student_player.mytools.MyTools;

/** A Hus player submitted by a student. */
public class EvolvingPlayerOne extends HusPlayer {
	private Node mmTreeRoot = new Node(null, Float.MIN_VALUE, null, true);
	private float avgLeaf = 0;
	private float avgCount = 0;
	private int count = 0;
	private static int[] weights = new int[13];
	static Random rand;
	private static int start = Integer.MAX_VALUE;
	
    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. 
     * @throws IOException */
    public EvolvingPlayerOne() throws IOException {     	
    	super(getWeights()); 
    }
    
    // The weights can be from -8 to 8
    public static String getWeights() throws IOException{
    	List<String> results = new ArrayList<String>();
    	List<String> evolution = new ArrayList<String>();	
    	for (String line : Files.readAllLines(Paths.get("logs/outcomes.txt"))) {
        	results.add(line.trim());
        }
    	for (String line : Files.readAllLines(Paths.get("evolve.txt"))) {
        	evolution.add(line.trim());
        }
    	
    	// The starting point for the evolution
    	if (results.size() < start) start = results.size();
    	
    	if (evolution.size() < 10){
    		for (int j = 0; j < 10; j++){
    			String evolveString = "";
	    		for (int i = 0; i < 13; i++){
	    			evolveString += ThreadLocalRandom.current().nextInt(-8, 8 + 1) + " ";
	    		}
	    		evolution.add(evolveString);    	
    		}	
    		Files.write(Paths.get("evolve.txt"), evolution);
    	}

    	// This will be all the evolution logic
		// Do rounds of 10 each, then mutate and choose a new player
		int index = (results.size() - start) / 10;
		if (results.size() - start % 100 == 0){
			// Find the best performing of the bunch
			int[] victories = new int[10]; 
			for (int i = 0; i < 100; i++){
				String[] tokens = results.get(start + i).split(",");
				victories[Integer.valueOf(tokens[5])] ++;
			}
			int mostVictoryIdx = 0;
			int mostVictories = 0;
			for (int i = 0; i < 10; i++){
				if (victories[i] > mostVictories){
					mostVictoryIdx = i;
					mostVictories = victories[i];
				}
			}
			
			// Use the best performing in crossover
			// Also mutate the individuals
			String bestEvolved = evolution.get(mostVictoryIdx);
			String[] bestTokens = bestEvolved.split(" ");
			for (int i = 0; i < 10; i++){
				// Don't mutate the best
				if (i == mostVictoryIdx) continue;
				
				String[] curEvolved = evolution.get(i).split(" ");
				String newEvolved = "";
				for (int j = 0; j < curEvolved.length; j++){
					// 50% probability of crossover average with best
					if (Math.random() > 0.5){
						curEvolved[j] = String.valueOf((Integer.valueOf(curEvolved[j]) 
								+ Integer.valueOf(bestTokens[j])) / 2);
						
					}
					// Turn off an index with 20% probability
					if (Math.random() > 0.8){
						curEvolved[j] = String.valueOf(0);
					}
					newEvolved = curEvolved[j] + " ";
				}
				evolution.set(i, newEvolved);
			}
			Files.write(Paths.get("evolve.txt"), evolution);
		}

		String[] tokens = evolution.get(index).split(" ");
		for (int i = 0; i < tokens.length; i++){
			weights[i] = Integer.valueOf(tokens[i]);
		}

    	System.out.println("Evolving Player One Set");
    	return Integer.toString(index) + "," + evolution.get(index);
    }

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class hus.RandomHusPlayer
     * for another example agent. */
    public HusMove chooseMove(HusBoardState board_state){
    	
    	
        // Use executor to handle the timing
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // MiniMax mm = new MiniMax(board_state,mmTreeRoot);
        AlphaBeta mm = new AlphaBeta(board_state, weights);
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