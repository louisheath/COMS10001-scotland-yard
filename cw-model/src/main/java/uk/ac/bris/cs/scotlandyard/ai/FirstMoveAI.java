package uk.ac.bris.cs.scotlandyard.ai;

import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Player;

/**
 * An AI that always picks the first given move
 */
@ManagedAI("First move")
public class FirstMoveAI implements PlayerFactory {

	@Override
	public Player createPlayer(Colour colour) {
		return (view, location, moves, callback) -> callback.accept(moves.iterator().next());
	}

}
