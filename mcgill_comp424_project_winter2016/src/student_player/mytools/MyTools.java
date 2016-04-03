package student_player.mytools;

import hus.HusBoardState;

public class MyTools {

    public static double getSomething(){
        return Math.random();
    }
    
    // In this method, all the smaller submethods in the loop will be pulled out
    // This makes it so that the for loop does not need to be run multiple times
    // Not sure if I want to do this or just put them into a super method...
    public static float opt_evaluator(HusBoardState state, int playerNum, int oppPlayerNum){
    	int[][] pits = state.getPits();
		
		int[] playerPits = pits[playerNum];
        int[] oppPits = pits[oppPlayerNum];
        
        int value = 0;
        for (int i = 0; i < playerPits.length; i++){
        	value += playerPits[i] - oppPits[i];
        }
        
        return value;
    	
    }
    public static float opt_seedDifference(int playerPit, int oppPit){
    	return playerPit-oppPit;
    }
    public static float opt_simpleCapturablePits(int playerPit, int oppPit, int i){
    	if (playerPit != 0 && oppPit != 0 && i > 15) return -playerPit;
    	return 0;
    }
    

    public static float seedDifference(HusBoardState state, int playerNum, int oppPlayerNum){
		// state.getLegalMoves().size();
    	int[][] pits = state.getPits();
		
		int[] playerPits = pits[playerNum];
        int[] oppPits = pits[oppPlayerNum];
        
        int value = 0;
        for (int i = 0; i < playerPits.length; i++){
        	value += playerPits[i] - oppPits[i];
        }
        
        return value;
	}
	
	private float simpleCapturablePits(HusBoardState state, int playerNum, int oppPlayerNum){
		// This is a negative value
		// It is a rough guesstimate on the number of pits capturable by opp
		// Simply sees which of the opp's inner pits are filled, and which of ours are
		// Counts up the number of seeds in these pits
		
		
		// state.getLegalMoves().size();
		int[][] pits = state.getPits();
		
		int[] playerPits = pits[playerNum];
        int[] oppPits = pits[oppPlayerNum];
        
        int badPits = 0;
        for (int i = 0; i < playerPits.length; i++){
        	// My capturable pits
        	if (playerPits[i] != 0 && oppPits[i] != 0 && i > 15) badPits -= playerPits[i];
        }
        
        return badPits;
	}
}
