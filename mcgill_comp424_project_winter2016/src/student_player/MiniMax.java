package student_player;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MiniMax implements Callable<Integer> {
	// Will want this to be iterative deepening
	
	
	public MiniMax(){
		
	}
	
	private float estimateNodeValue(){
		
		return 0;
	}

	@Override
	public Integer call() throws Exception {
		// TODO Auto-generated method stub
		Thread.sleep(100000);
		return 0;
	}
}
