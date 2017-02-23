package uk.ac.bris.cs.oxo.standard;

import uk.ac.bris.cs.oxo.Player;
import uk.ac.bris.cs.oxo.Side;

/**
 * A factory that creates new OXO games
 */
public interface OXOGameFactory {
	OXOGame createGame(int size, Side startSide, Player noughtSide, Player crossSide);
}
