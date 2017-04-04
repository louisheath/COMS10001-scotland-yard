package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.List;
import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 *
 * @author Will
 */
public class MoveNode {
    private MoveNode previous;
    private Move move;
    private List<MoveNode> next;
    
    public MoveNode(Move move) {
		this.move = move;
                this.next = new ArrayList<>();
                this.previous = this;
	}
    
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
