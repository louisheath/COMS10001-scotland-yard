package uk.ac.bris.cs.scotlandyard.ai;

import java.util.ArrayList;
import java.util.Random;

import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Player;

/**
 * An AI that randomly picks a move
 */
@ManagedAI(value = "Random")
public class RandomAI implements PlayerFactory {

	private final Random random = new Random();

	@Override
	public Player createPlayer(Colour colour) {
		return (view, location, moves, callback) -> {
			callback.accept(new ArrayList<>(moves).get(random.nextInt(moves.size())));
		};
	}

}
