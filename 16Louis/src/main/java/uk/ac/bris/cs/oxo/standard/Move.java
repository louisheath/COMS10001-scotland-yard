package uk.ac.bris.cs.oxo.standard;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.io.Serializable;
import java.util.Objects;

/**
 * A move for the OXO game
 */
public class Move implements Serializable {

	/**
	 * The row and column of the current move
	 */
	public final int row, column;

	/**
	 * Create a new move with the given coordinates
	 * 
	 * @param row the row of the move
	 * @param column the column of the move
	 */
	public Move(int row, int column) {
		this.row = row;
		this.column = column;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Move move = (Move) o;
		return row == move.row && column == move.column;
	}

	@Override
	public int hashCode() {
		return Objects.hash(row, column);
	}

	@Override
	public String toString() {
		return toStringHelper(this).add("row", row).add("column", column).toString();
	}
}
