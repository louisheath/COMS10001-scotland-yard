package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.List;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 * DataNodeLegacy is an object to be used in the game tree. It stores the locations
 * and tickets of players in the current state. It links to its preceding and 
 * following nodes.
 * 
 * A DataNodeLegacy is a gamestate attached to the move which led to it
 */
public class DataNodeLegacy {
    ////Attributes of MoveNodes
    //
    // links to previous node, and all following nodes
    private DataNodeLegacy previous;
    private final List<DataNodeLegacy> next;
    // the move that led to this node
    private final Move move;
    // the locations and tickets of each player in this current state
    private final List<PlayerData> playerList;
    //for scorng Score of node
    private int score;
    
    //How to create
    public DataNodeLegacy(List<PlayerData> playerList, Move move) {
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
    public List<DataNodeLegacy> next() {
		return next;
	}
    public DataNodeLegacy previous() {
		return previous;
	}
    public void setnext(DataNodeLegacy node) {
            this.next.add(node);
	}
    public void setprevious(DataNodeLegacy node) {
            this.previous=node;
	}
    
}
