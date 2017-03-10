package uk.ac.bris.cs.scotlandyard.model;

import java.util.Objects;

/**
 * Represents a pass move in the Scotland Yard game
 */
public class PassMove extends Move {

	/**
	 * Create a new pass move
	 *
	 * @param colour the colour of the player playing this move
	 */
	public PassMove(Colour colour) {
		super(colour);
	}

	@Override
	public void visit(MoveVisitor visitor) {
		Objects.requireNonNull(visitor).visit(this);
	}

	@Override
	public String toString() {
		return "Pass[" + colour() + "]";
	}

}
