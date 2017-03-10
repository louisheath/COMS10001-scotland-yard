package uk.ac.bris.cs.scotlandyard.model;

import java.util.List;

import uk.ac.bris.cs.gamekit.graph.Graph;

public interface ScotlandYardGameFactory {

	/**
	 * Creates a {@link ScotlandYardGame} from the given parameters
	 *
	 * @param rounds reveal and hidden rounds of the game
	 * @param graph the map the game will use
	 * @param mrX MrX's player configuration
	 * @param firstDetective the first detective's player configuration
	 * @param restOfTheDetectives the rest of the detective's player
	 *        configuration
	 * @return the created model; not null
	 */
	ScotlandYardGame createGame(
			List<Boolean> rounds,
			Graph<Integer, Transport> graph,
			PlayerConfiguration mrX,
			PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives);

}
