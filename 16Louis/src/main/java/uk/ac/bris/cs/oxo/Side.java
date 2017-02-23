package uk.ac.bris.cs.oxo;

/**
 * A side of the OXO game
 */
public enum Side {
	NOUGHT("O"), CROSS("X");

	private final String symbol;

	Side(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * The ASCII representation of the side
	 * @return the symbol; never null
	 */
	public String symbol() {
		return symbol;
	}

	public Side other() {
		return this == NOUGHT ? CROSS : NOUGHT;
	}

}
