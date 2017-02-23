package uk.ac.bris.cs.oxo;

import java.time.Duration;
import java.util.Set;
import java.util.function.Consumer;

import javafx.scene.image.ImageView;
import uk.ac.bris.cs.oxo.standard.Move;
import uk.ac.bris.cs.oxo.standard.OXOView;

public class HumanPlayer implements Player {

	private final LocalBoardController controller;
	private final Duration timeout;
	private final Side side;

	public HumanPlayer(LocalBoardController controller, Duration timeout, Side side) {
		this.controller = controller;
		this.timeout = timeout;
		this.side = side;
	}

	@Override
	public Side side() {
		return side;
	}

	@Override
	public void makeMove(OXOView view, Set<Move> moves, Consumer<Move> callback) {
		controller.setCurrentPlayer(side);
		controller.setTimer(timeout, () -> controller.timeout(side));
		moves.forEach(move -> {
			ImageView cell = controller.getCellAt(move.row, move.column);
			cell.setOnMouseEntered(event -> {
				cell.setImage(controller.getCellImage(side));
				cell.setOpacity(0.3);
				event.consume();
			});
			cell.setOnMouseExited(event -> {
				cell.setImage(null);
				cell.setOpacity(1);
				event.consume();
			});
			cell.setOnMouseClicked(event -> {
				controller.getCells().forEach(c -> {
					c.setOnMouseEntered(null);
					c.setOnMouseExited(null);
					c.setOnMouseClicked(null);
				});
				callback.accept(move);
				event.consume();
			});
		});
	}
}
