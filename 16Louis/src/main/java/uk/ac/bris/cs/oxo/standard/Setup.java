package uk.ac.bris.cs.oxo.standard;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

import uk.ac.bris.cs.oxo.Side;

/**
 * A configuration value class that stores OXO game setups
 */
public class Setup implements Serializable {

	public final int size;
	public final Side startSide;
	public final Duration timeout;

	public Setup(int size, Side startSide, Duration timeout) {
		this.size = size;
		this.startSide = startSide;
		this.timeout = timeout;
	}

	public Setup() {
		this(3, Side.CROSS, Duration.ZERO);
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Setup that = (Setup) o;
		return size == that.size &&
				       startSide == that.startSide &&
				       Objects.equals(timeout, that.timeout);
	}

	@Override
	public int hashCode() {
		return Objects.hash(size, startSide, timeout);
	}
}
