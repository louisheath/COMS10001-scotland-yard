package uk.ac.bris.cs.oxo.standard;

import static java.util.Objects.requireNonNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.matrix.ImmutableMatrix;
import uk.ac.bris.cs.gamekit.matrix.Matrix;
import uk.ac.bris.cs.gamekit.matrix.SquareMatrix;
import uk.ac.bris.cs.oxo.Cell;
import uk.ac.bris.cs.oxo.Outcome;
import uk.ac.bris.cs.oxo.Player;
import uk.ac.bris.cs.oxo.Side;
import uk.ac.bris.cs.oxo.Spectator;

public class OXO implements OXOGame {

	//make variables just for this object
	private Player noughtSide, crossSide;
	private Side currentSide;
	private int size;
	private final SquareMatrix<Cell> matrix;

	public OXO(int size, Side startSide, Player noughtSide, Player crossSide) {


		//If size is less than or equal to zero throw exception.
		if(size <= 0) throw new IllegalArgumentException("size invalid");
		//sets current object size to given size
		this.size = size;
		//Some Magic built in thing that throws when null
		this.noughtSide = requireNonNull(noughtSide);
		this.crossSide = requireNonNull(crossSide);
		this.currentSide = requireNonNull(startSide);

		this.matrix = new SquareMatrix<Cell>(size, new Cell());

			// TODO
	}

	@Override
	public void registerSpectators(Spectator... spectators) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void unregisterSpectators(Spectator... spectators) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	private Set<Move> validMoves() {
	  Set<Move> moves = new HashSet<>();
	  for (int row = 0; row < matrix.rowSize(); row++) {
	    for (int col = 0; col < matrix.columnSize(); col++) {
				if(matrix.get(row,col) == null ) moves.add(new Move(row, col));
	      //add moves here via moves.add(new Move(row, col)) if the matrix is empty at this location
	  } }
	  return moves;
	}

	@Override
	public void start() {
		//this bit is broken as dunno what it is meant to do
		Consumer<Move> callback;
		Set<Move> validMoves = validMoves();
		Player player = (currentSide == Side.CROSS) ? crossSide : noughtSide;
		player.makeMove(this,validMoves,callback);
		// TODO
	}

	@Override
	public SquareMatrix<Cell> board() {
		return this.matrix;
	}

	@Override
	public Side currentSide() {
		return this.currentSide;
	}

}
