package uk.ac.bris.cs.oxo.standard;

import static java.util.Objects.requireNonNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.matrix.*;
import uk.ac.bris.cs.oxo.Cell;
import uk.ac.bris.cs.oxo.Outcome;
import uk.ac.bris.cs.oxo.Player;
import uk.ac.bris.cs.oxo.Side;
import uk.ac.bris.cs.oxo.Spectator;

public class OXO implements OXOGame, Consumer<Move> {

	private Player noughtSide, crossSide;
	private Side currentSide;
	private int size;
	private SquareMatrix<Cell> matrix;
	private final List<Spectator> spectators = new CopyOnWriteArrayList<>();

	public OXO(int size, Side startSide, Player noughtSide, Player crossSide) {

		if(size <= 0) throw new IllegalArgumentException("size invalid");

		this.size = size;
		this.noughtSide = requireNonNull(noughtSide);
		this.crossSide = requireNonNull(crossSide);
		this.currentSide = requireNonNull(startSide);

		this.matrix = new SquareMatrix<Cell>(size, new Cell());

		}

	@Override
	public void registerSpectators(Spectator... spectators) {
		this.spectators.addAll(Arrays.asList(spectators));
	}

	@Override
	public void unregisterSpectators(Spectator... spectators) {
		this.spectators.removeAll(Arrays.asList(spectators));
	}

	private Set<Move> validMoves() {
	  Set<Move> moves = new HashSet<>();
	  for (int row = 0; row < matrix.rowSize(); row++) {
	    for (int col = 0; col < matrix.columnSize(); col++) {
	      if(matrix.get(row,col).isEmpty()) moves.add(new Move(row,col));
	  } }

		return moves;
	}

	private boolean won() {
		int rows = matrix.rowSize();
		int cols = matrix.columnSize();

		boolean test = true;
		for (int row = 0; row < rows; row++) {
			for (int col = 1; col < cols; col++) {
			  if (matrix.get(row,col)!=matrix.get(row,col-1)) test = false;
		  }
		}
		if (test) return true;

		test = true;
		for (int col = 0; col < cols; col++) {
			for (int row = 1; row < rows; row++) {
				if (matrix.get(row,col)!=matrix.get(row-1,col)) test = false;
			}
		}
		if (test) return true;

		test = true;
		//List<Cell> mainDiag = matrix.mainDiagonal()
		for (int i = 1; i < matrix.mainDiagonal().size(); i++) {
			if (matrix.mainDiagonal().get(i)!=matrix.mainDiagonal().get(i-1)) test = false;
		}
		if (test) return true;

		test = true;
		for (int i = 1; i < matrix.antiDiagonal().size(); i++) {
			if (matrix.antiDiagonal().get(i)!=matrix.antiDiagonal().get(i-1)) test = false;
		}
		if (test) return true;

		return false;
	}

	private boolean draw() {
		for (int row = 0; row < matrix.rowSize(); row++) {
			for (int col = 0; col < matrix.columnSize(); col++) {
				if(matrix.get(row,col).isEmpty()) return false;
		} }
		return true;
	}

	@Override
	public void accept(Move move) {
		boolean validMove = validMoves().contains(move);

		if (validMove) {
			Cell side = new Cell(currentSide);

			matrix.put(move.row,move.column,side);

			for (Spectator s : spectators) s.moveMade(currentSide,move);

			if (won()) {
				Outcome o = new Outcome(currentSide);
				for (Spectator s : spectators) s.gameOver(o);
			}
			else if (draw()) {
				Outcome o = new Outcome();
				for (Spectator s : spectators) s.gameOver(o);
			}
			else {
				this.currentSide = (currentSide == Side.CROSS) ? Side.NOUGHT : Side.CROSS;
				start();
			}
		}
		else throw new IllegalArgumentException("Invalid Move!");
	}

	@Override
	public void start() {

		Player player = (currentSide == Side.CROSS) ? crossSide : noughtSide;

		player.makeMove(this, validMoves(), this);

	}

	@Override
	public ImmutableMatrix<Cell> board() {
		return new ImmutableMatrix<>(matrix);
	}

	@Override
	public Side currentSide() {
		return currentSide;
	}
}
