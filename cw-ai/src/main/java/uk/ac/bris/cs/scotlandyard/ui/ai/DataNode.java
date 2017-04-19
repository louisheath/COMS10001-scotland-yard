package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.List;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;

/**
 * DataNode is an object to be used in the game tree. It stores a playerconfigs
 * and links to the ones before and after it.
 */
public class DataNode {
    //Attributes of MoveNodes
    private DataNode previous;
    private final List<PlayerData> playerList;
    private final Move move;
    private final List<DataNode> next;
    //How to create
    public DataNode(List<PlayerData> playerList, Move move) {
                this.move = move;
		this.playerList = playerList;
                this.next = new ArrayList<>();
                this.previous = this;
	}
    //Simple functions to manipulate Nodes
    public List<PlayerData> playerList() {
		return playerList;
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
    
}
