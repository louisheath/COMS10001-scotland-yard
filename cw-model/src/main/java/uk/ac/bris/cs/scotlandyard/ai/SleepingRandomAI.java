package uk.ac.bris.cs.scotlandyard.ai;

import java.util.ArrayList;
import java.util.Random;

import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Player;

/**
 * An AI that sleeps for 100ms and then picks a random move
 */
@ManagedAI(value = "Sleep 100ms")
public class SleepingRandomAI implements PlayerFactory {

	private final Random random = new Random();

	@Override
	public Player createPlayer(Colour colour) {
		return (view, location, moves, callback) -> {
			try {
				Thread.sleep(100);
				callback.accept(new ArrayList<>(moves).get(random.nextInt(moves.size())));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				e.printStackTrace();
			}
		};
	}

}
