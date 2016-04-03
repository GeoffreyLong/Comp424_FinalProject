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
public class OpponentPlayer extends HusPlayer {
	private Node mmTreeRoot = new Node(null, Float.MIN_VALUE, null, true);
	private float avgLeaf = 0;
	private float avgCount = 0;
	private int count = 0;
	
    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public OpponentPlayer() { super("MinimaxSimple"); }

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class hus.RandomHusPlayer
     * for another example agent. */
    public HusMove chooseMove(HusBoardState board_state){
        // Use executor to handle the timing
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // MiniMax mm = new MiniMax(board_state,mmTreeRoot);
        MiniMax mm = new MiniMax(board_state);
        Future<HusMove> future = executor.submit(mm);
        HusMove move = null;
        
        
        try {
        	move = future.get(1250, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			System.out.println("interrupt");
		} catch (ExecutionException e) {
			System.out.println("interrupt1");
			StringWriter writer = new StringWriter();
			e.printStackTrace( new PrintWriter(writer,true ));
			System.out.println("exeption stack is :\n"+writer.toString());
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			// System.out.println("Timeout");
			future.cancel(true);
		} finally {
			
		}
        
        
        // Just in case
        future.cancel(true);
        while(!future.isDone());
        move = mm.getBestMove();
                
        //System.out.println("Hello" + move.toPrettyString());
        executor.shutdownNow();
        
        // But since this is a placeholder algorithm, we won't act on that information.
        return move;
    }
}