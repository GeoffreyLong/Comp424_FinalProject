package student_player;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import student_player.mytools.MyTools;

/** A Hus player submitted by a student. */
public class OptPlayer extends HusPlayer {
	private Node mmTreeRoot = new Node(null, Float.MIN_VALUE, null, true);
	private Queue<Node> queue = new LinkedList<Node>();

	
    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public OptPlayer() { super("MinimaxOpt"); }

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class hus.RandomHusPlayer
     * for another example agent. */
    public HusMove chooseMove(HusBoardState board_state){
        // Use executor to handle the timing
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        //System.out.println(board_state.getTurnNumber());
        if (board_state.getTurnNumber() == 0){
        	mmTreeRoot.boardState = (HusBoardState) board_state.clone();
            queue.add(mmTreeRoot);
        }
        else{
        	int count = 0;
        	Node nodeSet = null;
        	float eval = MyTools.seedDifference(board_state, board_state.getTurnPlayer(), (board_state.getTurnPlayer() + 1) % 2);
        	//System.out.println("new eval: " + eval);
        	if (mmTreeRoot != null){
	        	for (Node node : mmTreeRoot.children){
	        		//System.out.println(node.value);
	        		if (node.value == eval){
	        			nodeSet = node;
	        			count ++;
	        		}
	        	}
        	}
	        if (count == 1){
        		mmTreeRoot = nodeSet;
        		System.out.println("Found next");
        	}
        	else{
        		// Couldn't find next node corresponding to opp's move
        		//		Or found two...
        		// Need to clear the queue and the root node
        		// Very unfortunate
        		System.out.println("Couldn't find next, clearing... :(");
        		queue.clear();
        		mmTreeRoot = new Node(null, Float.MIN_VALUE, null, true);
        		mmTreeRoot.boardState = (HusBoardState) board_state.clone();
        		queue.add(mmTreeRoot);
        	}
        }

        // clear mmTreeRoot parent
        mmTreeRoot.parent = null;
        MinimaxOptimized mm = new MinimaxOptimized(board_state, mmTreeRoot, queue);
        Future<Node> future = executor.submit(mm);
        HusMove move = null;
        
        try {
        	mmTreeRoot = future.get(800, TimeUnit.MILLISECONDS);
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
        
        /*
        System.out.println(future.isCancelled());
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("Error in Sleep");
		}
        */
        
		future.cancel(true);
		
        // Might not want this line
		// Spinwait
		while(!future.isDone());
		// System.out.println(future.isDone());
		mmTreeRoot = mm.getBestNode();
		move = mmTreeRoot.move;
		System.out.println("MOVE VALUE: " + mmTreeRoot.value);
		
		
        //System.out.println("Hello" + move.toPrettyString());
        executor.shutdownNow();
        
        // But since this is a placeholder algorithm, we won't act on that information.
        return move;
    }
}