package uk.ac.bris.cs.scotlandyard.ui.controller;

import java.util.List;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import uk.ac.bris.cs.fxkit.BindFXML;
import uk.ac.bris.cs.fxkit.Controller;
import uk.ac.bris.cs.scotlandyard.ResourceManager;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.ui.GameControl;
import uk.ac.bris.cs.scotlandyard.ui.model.BoardProperty;

/**
 * Controller for status bar
 */
@BindFXML("layout/Status.fxml")
public final class Status implements Controller, GameControl {

	@FXML private ToolBar root;
	@FXML private Label round;
	@FXML private Label player;
	@FXML private Label validMoves;
	@FXML private Label time;
	@FXML private Label status;
	@FXML private Slider volume;

	private final ResourceManager manager;

	Status(ResourceManager manager, BoardProperty config) {
		Controller.bind(this);
		this.manager = manager;
	}

	@Override
	public void onRoundStarted(ScotlandYardView view, int round) {
		this.round.setText((round + 1) + " of " + view.getRounds().size());
	}

	@Override
	public void onMoveMade(ScotlandYardView view, Move move) {
		Colour nextPlayer = nextPlayer(view);
		this.player.setText(nextPlayer.toString());
		status.setText(String.format("Waiting move(%s)", nextPlayer.name()));
	}

	private Colour nextPlayer(ScotlandYardView view) {
		List<Colour> players = view.getPlayers();
		int currentIndex = players.indexOf(view.getCurrentPlayer());
		return players.get(currentIndex >= players.size() ? 0 : currentIndex);
	}

	@Override
	public void onGameOver(ScotlandYardView view, Set<Colour> winningPlayers) {
		status.setText("Game completed, winning player:" + view.getWinningPlayers());
	}

	@Override
	public Parent root() {
		return root;
	}
}
