package uk.ac.bris.cs.oxo;

import java.util.Set;
import java.util.function.Consumer;

import uk.ac.bris.cs.oxo.standard.Move;
import uk.ac.bris.cs.oxo.standard.OXOView;

/**
 * A player of the OXO game
 */
public interface Player {

	/**
	 * Used to identify the player's side
	 * 
	 * @return the current player's side; never null
	 */
	Side side();

	/**
	 * Called when a move is required for this player
	 * 
	 * @param view the view of the current game; not null
	 * @param moves the available moves to choose from in this move
	 * @param callback the callback to invoke when a move has been chosen
	 */
	void makeMove(OXOView view, Set<Move> moves, Consumer<Move> callback);

}
