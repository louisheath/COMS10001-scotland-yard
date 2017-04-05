package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.List;
import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 * MoveNode is an object to be used in the game tree. It stores a move 
 * the move leading up to it and all the possible moves after it. 
 */
public class MoveNode {
    //Attributes of MoveNodes
    private MoveNode previous;
    private final Move move;
    private List<MoveNode> next;
    //How to create
    public MoveNode(Move move) {
		this.move = move;
                this.next = new ArrayList<>();
                this.previous = this;
	}
    //Simple functions to manipulate Nodes
    public Move move() {
		return move;
	}
    public List<MoveNode> next() {
		return next;
	}
    public MoveNode previous() {
		return previous;
	}
    public void setnext(MoveNode node) {
            this.next.add(node);
	}
    public void setprevious(MoveNode node) {
            this.previous=node;
	}
    
}
