package uk.ac.bris.cs.oxo;

import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

/**
 * A cell in the OXO game
 */
public class Cell {

	private final Side side;

	/**
	 * Creates a cell that`s occupied with the given side
	 * 
	 * @param side the side to place in this cell
	 */
	public Cell(Side side) {
		this.side = side;
	}

	/**
	 * Creates an empty cell that is unoccupied
	 */
	public Cell() {
		this.side = null;
	}

	/**
	 * Gets the current cell`s side as an optional value
	 * 
	 * @return an optional where an empty cell is {@link Optional#empty()};
	 *         never null
	 */
	public Optional<Side> side() {
		return Optional.ofNullable(side);
	}

	/**
	 * Checks whether the current cell is empty(unoccupied)
	 * 
	 * @return true if the cell is empty, false otherwise
	 */
	public boolean isEmpty() {
		return this.side == null;
	}

	/**
	 * Checks whether the current cell is the same side as the given side
	 * 
	 * @param that the side to check; not null
	 * @return true if the cell has the same side, false if the cell is empty or
	 *         not occupied with the same side
	 */
	public boolean sameSideAs(Side that) {
		return side().map(requireNonNull(that)::equals).orElse(false);
	}

	@Override
	public String toString() {
		return String.format("Cell{%s}", side);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Cell cell = (Cell) o;
		return side == cell.side;
	}

	@Override
	public int hashCode() {
		return hash(side);
	}
}
