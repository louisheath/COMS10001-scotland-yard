package uk.ac.bris.cs.oxo;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.google.common.base.Throwables;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.ac.bris.cs.oxo.standard.Move;
import uk.ac.bris.cs.oxo.standard.OXOGame;
import uk.ac.bris.cs.oxo.standard.OXOGameFactory;
import uk.ac.bris.cs.oxo.standard.Setup;

public class LocalBoardController extends BoardController implements Spectator {

	private final OXOGameFactory factory;
	private Setup lastSetup;

	public LocalBoardController(ResourceManager manager, OXOGameFactory factory) {
		super(manager);
		this.factory = factory;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);
		showConfiguration();
	}

	private void showConfiguration() {
		LocalBoardSetupController setupController = new LocalBoardSetupController(manager,
				config -> {
					lastSetup = config;
					setup(config);
				});
		showOverlay(setupController.root());
	}

	private void setup(Setup config) {
		dismissOverlay();
		try {
			OXOGame game = factory.createGame(config.size, config.startSide,
					new HumanPlayer(this, config.timeout, Side.NOUGHT),
					new HumanPlayer(this, config.timeout, Side.CROSS));
			game.registerSpectators(this);
			setupGrid(game.board().columnSize());
			game.start();
		} catch (Exception e) {
			handleFatalException(e);
		}
	}

	@Override
	public void moveMade(Side side, Move move) {
		ImageView cell = getCellAt(move.row, move.column);
		cell.setOpacity(1);
		cell.setImage(getCellImage(side));
		cell.setOnMouseEntered(null);
	}

	@Override
	public void gameOver(Outcome outcome) {
		stopTimer();
		Platform.runLater(() -> {
			ButtonType restart = new ButtonType("Restart");
			ButtonType mainMenu = new ButtonType("Main menu");
			ButtonType quit = new ButtonType("Quit", ButtonData.CANCEL_CLOSE);
			Alert alert = new Alert(AlertType.INFORMATION,
					outcome.winningSide().map(s -> s.symbol() + " won!").orElse("It's a tie"),
					restart, mainMenu, quit);
			alert.setTitle("Game over");
			alert.setHeaderText("Game over");
			Optional<ButtonType> buttonType = alert.showAndWait();
			ButtonType type = buttonType.get();
			if (type == restart) {
				setup(lastSetup);
			} else if (type == mainMenu) {
				showConfiguration();
			} else if (type == quit) {
				Platform.exit();
			}
		});
	}

	void timeout(Side side) {
		gameOver(new Outcome(side.other()));
	}

	private void handleFatalException(Throwable throwable) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fatal error");
			alert.setHeaderText("OXO has crashed");
			alert.setContentText("OXO was unable to continue due to a fatal error");
			Label label = new Label("The exception stacktrace was:");
			TextArea textArea = new TextArea(Throwables.getStackTraceAsString(throwable));
			textArea.setEditable(false);
			textArea.setWrapText(true);
			textArea.setMaxWidth(Double.MAX_VALUE);
			textArea.setMaxHeight(Double.MAX_VALUE);
			GridPane.setVgrow(textArea, Priority.ALWAYS);
			GridPane.setHgrow(textArea, Priority.ALWAYS);
			GridPane expContent = new GridPane();
			expContent.setMaxWidth(Double.MAX_VALUE);
			expContent.add(label, 0, 0);
			expContent.add(textArea, 0, 1);
			alert.getDialogPane().setExpandableContent(expContent);
			alert.showAndWait();
			Platform.exit();
		});
	}
}
