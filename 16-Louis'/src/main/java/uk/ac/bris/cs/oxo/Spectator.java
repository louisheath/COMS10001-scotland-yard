package uk.ac.bris.cs.oxo;

import java.util.function.Consumer;

import uk.ac.bris.cs.oxo.standard.Move;
import uk.ac.bris.cs.oxo.standard.OXOView;

/**
 * Receive events in the OXO game
 */
public interface Spectator {

	/**
	 * Called when one of the players have won or that the board is filled and
	 * no winner can be decided(a tie)
	 * 
	 * @param outcome the outcome of the game; not null
	 */
	default void gameOver(Outcome outcome) {}

	/**
	 * Called when a move is made by a player,
	 * i.e. when {@link Player#makeMove(OXOView, java.util.Set, Consumer)} is
	 * done selecting a move
	 * 
	 * @param side the side that just made a move; not null
	 * @param move the move that was made; not null
	 */
	default void moveMade(Side side, Move move) {}

}
