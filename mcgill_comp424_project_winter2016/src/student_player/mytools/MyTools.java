package student_player.mytools;

import hus.HusBoardState;

public class MyTools {
	public final static double weighter = 8.0;
	

    public static double getRandom(){
        return Math.random();
    }
    
    // In this method, all the smaller submethods in the loop will be pulled out
    // This makes it so that the for loop does not need to be run multiple times
    // Not sure if I want to do this or just put them into a super method...
    public static float opt_evaluator(HusBoardState state, int playerNum, int oppPlayerNum,
    		int[] weights){
    	int[][] pits = state.getPits();
		
		int[] playerPits = pits[playerNum];
        int[] oppPits = pits[oppPlayerNum];
        
        int value = 0;
        for (int i = 0; i < playerPits.length; i++){
        	// Seed difference between player and opponent
        	value += weights[0]/weighter * opt_seedDifference(playerPits[i], oppPits[i]);     	
        	// Seed difference between opponent and player
        	value += weights[1]/weighter * opt_seedDifference(oppPits[i], playerPits[i]);
        	// Pits that the opponent could capture (simple estimate)
        	value += weights[2]/weighter * opt_simpleCapturablePits(playerPits[i], playerPits[31-i], oppPits[i], i);
        	// Pits that the player could capture (simple estimate)
        	value += weights[3]/weighter * opt_simpleCapturablePits(oppPits[i], oppPits[31-i], playerPits[i], i);
        	// Pits that the opponent could capture (oneTurn)
        	value += weights[4]/weighter * opt_complexCapturablePits(playerPits, oppPits, i);
        	// Pits that the player could capture (oneTurn)
        	value += weights[5]/weighter * opt_complexCapturablePits(oppPits, playerPits, i);
        	// Number of player pits with more than 12 seeds
        	value += weights[6]/weighter * opt_largePits(playerPits[i]);
        	// Number of opponent pits with more than 12 seeds
        	value += weights[7]/weighter * opt_largePits(oppPits[i]);
        	// Number of player pits with exactly 1 seed
        	value += weights[8]/weighter * opt_singletonPits(playerPits[i]);
        	// Number of opponent pits with exactly 1 seed
        	value += weights[9]/weighter * opt_singletonPits(oppPits[i]);
        	// Number of player pits with exactly 0 seeds
        	value += weights[10]/weighter * opt_zeroPits(playerPits[i]);
        	// Number of opponent pits with exactly 0 seeds
        	value += weights[11]/weighter * opt_zeroPits(oppPits[i]);
        }

        value += weights[12]/weighter * opt_numMoves(state);
        
        return value;
    	
    }
    // This will return the number of seeds the player is ahead
    private static float opt_seedDifference(int playerPit, int oppPit){
    	return playerPit-oppPit;
    }
    // This will return a simple measure of which pits may be captured
    private static float opt_simpleCapturablePits(int innerPit, int outerPit, int innerPit2, int i){
    	if (innerPit != 0 && innerPit2 != 0 && i > 15) return innerPit + outerPit;
    	return 0;
    }
    // Slightly more involved measurement of the number of pits that can be captured
    private static float opt_complexCapturablePits(int[] onePits, int[] twoPits, int i){
    	// onePit is the player that is making the move
    	// If onePit plays pit i the last seed will end on landingPit%32
    	// If landingPit/32 >= 1 then there has been a full lap
    	// 		so, all the pits will be filled
    	// Then calculate the number of opponent pits captured
    	int landingPit = (i + onePits[i]);
    	int landingIdx = landingPit % 32;
    	if (onePits[landingIdx] != 0 || landingPit/32 >= 1) 
    			return opt_simpleCapturablePits(onePits[landingIdx], onePits[31-landingIdx],
    					twoPits[landingIdx], landingIdx);
    	return 0;
    }
    // This returns 1 if a pit is greater than or equal to 12, 0 if not
    private static float opt_largePits(int pit){
    	if (pit >= 12) return 1;
    	return 0;
    }    
    // This returns 1 if a pit is equal to 1, 0 if not
    private static float opt_singletonPits(int pit){
    	if (pit == 1) return 1;
    	return 0;
    }    
    // This returns 1 if a pit is equal to 0, 0 if not
    private static float opt_zeroPits(int pit){
    	if (pit == 0) return 1;
    	return 0;
    }    
    // This will return the number of moves the player can make
    private static float opt_numMoves(HusBoardState state){
    	return state.getLegalMoves().size();
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
	
	public static float simpleCapturablePits(HusBoardState state, int playerNum, int oppPlayerNum){
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
