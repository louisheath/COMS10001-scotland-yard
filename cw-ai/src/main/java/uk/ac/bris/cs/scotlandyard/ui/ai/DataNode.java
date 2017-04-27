package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.List;
import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 * DataNode is an object to be used in the game tree. It stores the locations
 * and tickets of players in the current state. It links to its preceding and 
 * following nodes.
 * 
 * A DataNode is a gamestate attached to the move which led to it
 */
public class DataNode {
    ////Attributes of MoveNodes
    //
    // links to previous node, and all following nodes
    private DataNode previous;
    private final List<DataNode> next;
    // the move that led to this node
    private final Move move;
    // the locations and tickets of each player in this current state
    private final List<PlayerData> playerList;
    //for scorng Score of node
    private int score;
    
    private List<Integer> mrXLocations;
    
    //How to create
    public DataNode(List<PlayerData> playerList, Move move) {
                this.move = move;
		this.playerList = playerList;
                this.next = new ArrayList<>();
                this.previous = this;
                this.score = 1;
	}
    //Simple functions to manipulate Nodes
    public List<PlayerData> playerList() {
		return playerList;
	}
	public void score(int score) {
		this.score = score;
	}
	public int score() {
		return score;
	}
    public Move move() {
		return move;
	}
    public List<DataNode> next() {
		return next;
	}
    public DataNode previous() {
		return previous;
	}
    public void setnext(DataNode node) {
            this.next.add(node);
	}
    public void setprevious(DataNode node) {
            this.previous=node;
	}
    public void setMrXLocations(List<Integer> locations) {
        this.mrXLocations = locations;
    }
    public List<Integer> mrXLocations() {
        return mrXLocations;
    }
}
