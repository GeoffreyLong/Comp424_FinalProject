package student_player;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a Node class, specifically for use with the MiniMax algorithm and its variants.
 * Note that I am making the attributes public. Although the execution time is the same, 
 * the getters/setters for private variables might consume more bytes of bytecode data.
 * 
 * @author geoffrey
 *
 */
// TODO might actually want to make the attrs private
public class Node {
	public float value = 0;
	public Node parent = null;
	public List<Node> children = new ArrayList<Node>();
	
	// Questionable call for this... might be better with an enum
	public boolean isMax;
	
	// Set the attributes of the node
	public Node(Node parent, float value, boolean isMax){
		this.parent = parent;
		this.value = value;
		this.isMax = isMax;
	}	
}
