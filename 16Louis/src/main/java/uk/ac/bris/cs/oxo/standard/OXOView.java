package uk.ac.bris.cs.oxo.standard;

import java.util.function.Consumer;

import uk.ac.bris.cs.gamekit.matrix.Matrix;
import uk.ac.bris.cs.oxo.Cell;
import uk.ac.bris.cs.oxo.Side;

/**
 * A view to the OXO game that will be exposed to the player in
 * {@link uk.ac.bris.cs.oxo.Player#makeMove(OXOView, java.util.Set, Consumer)}
 */
public interface OXOView {

	/**
	 * An immutable view of the board
	 * @return an immutable matrix; never null
	 */
	Matrix<Cell> board();

	/**
	 * The current playing side
	 * @return the side
	 */
	Side currentSide();

}
