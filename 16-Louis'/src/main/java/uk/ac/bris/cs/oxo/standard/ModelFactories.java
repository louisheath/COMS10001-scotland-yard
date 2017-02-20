package uk.ac.bris.cs.oxo.standard;

import java.util.Collections;
import java.util.List;

import uk.ac.bris.cs.oxo.Player;
import uk.ac.bris.cs.oxo.Side;

public class ModelFactories {

	static List<Class<? extends OXOGameFactory>> factories() {
		return Collections.singletonList(ModelFactory.class);
	}

	public static class ModelFactory implements OXOGameFactory {
		@Override
		public OXOGame createGame(int size, Side startSide, Player noughtSide, Player crossSide) {
			return new OXO(size, startSide, noughtSide, crossSide);
		}

		@Override
		public String toString() {
			return "OXO";
		}
	}
}
