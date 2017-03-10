package uk.ac.bris.cs.scotlandyard.ui.controller;

import com.google.common.collect.ImmutableSet;

import static io.atlassian.fugue.Option.fromOptional;
import static java.util.Arrays.*;
import static java.util.stream.Collectors.toList;
import static uk.ac.bris.cs.scotlandyard.ui.Utils.handleFatalException;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import uk.ac.bris.cs.scotlandyard.ResourceManager;
import uk.ac.bris.cs.scotlandyard.ResourceManager.ImageResource;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.PlayerConfiguration;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardModel;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Spectator;
import uk.ac.bris.cs.scotlandyard.ui.GameControl;
import uk.ac.bris.cs.scotlandyard.ui.Utils;
import uk.ac.bris.cs.scotlandyard.ui.controller.Notifications.NotificationBuilder;
import uk.ac.bris.cs.scotlandyard.ai.AIPool;
import uk.ac.bris.cs.scotlandyard.ui.model.ModelProperty;
import uk.ac.bris.cs.scotlandyard.ui.model.PlayerProperty;
import uk.ac.bris.cs.scotlandyard.ui.model.Side;

public final class LocalGame extends BaseGame implements Spectator {

	public static void newGame(ResourceManager manager, Stage stage) {
		BaseGame controller = new LocalGame(manager, stage);
		stage.setTitle("ScotlandYard");
		stage.setScene(new Scene(controller.root()));
		stage.getIcons().add(manager.getImage(ImageResource.ICON));
		stage.show();
	}

	private LocalGame(ResourceManager manager, Stage stage) {
		super(manager, stage);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		MenuItem newGame = new MenuItem("New game");
		newGame.setOnAction(e -> LocalGame.newGame(resourceManager, new Stage()));
		addMenuItem(newGame);
		setupGame();
	}

	private void setupGame() {
		StartScreen startScreen = new StartScreen(resourceManager, config, this::createGame);
		showOverlay(startScreen.root());
	}

	private void createGame(ModelProperty setup) {
		hideOverlay();
		try {
			Game game = new Game(setup);
		} catch (Exception e) {
			e.printStackTrace();
			handleFatalException(e);
		}

	}

	private class Game implements GameControl {

		private static final String NOTIFY_GAMEOVER = "notify_gameover";
		private final ModelProperty setup;
		private final ScotlandYardModel model;
		private final List<GameControl> controls;
		private final AIPool<Side> pool = new AIPool<>(
				createVisualiserSurface(),
				Utils::handleFatalException);

		Game(ModelProperty setup) throws Exception {
			this.setup = setup;

			List<PlayerProperty> joining = setup.players();

			// Add players to AI pool
			for (PlayerProperty property : joining) {
				property.ai().ifPresent(ai -> pool.addToGroup(
						property.side(),
						property.colour(),
						ai));
			}

			List<PlayerConfiguration> configs = joining.stream()
					.map(p -> new PlayerConfiguration.Builder(p.colour())
							.at(p.location())
							.with(p.ticketsAsMap())
							.using(board)
							.build())
					.collect(Collectors.toList());

			PlayerConfiguration mrX = configs.stream()
					.filter(p -> p.colour.isMrX())
					.findFirst()
					.orElseThrow(AssertionError::new);

			List<PlayerConfiguration> detectives = configs.stream()
					.filter(p -> p.colour.isDetective())
					.collect(toList());

			model = new ScotlandYardModel(
					setup.revealRounds(),
					setup.graphProperty().get(),
					mrX,
					detectives.get(0),
					detectives.stream().skip(1).toArray(PlayerConfiguration[]::new));

			controls = asList(
					board,
					travelLog,
					ticketsCounter,
					status,
					this);

			pool.initialise(resourceManager, model);
			// Add all players to board
			for (PlayerProperty property : joining) {
				board.setBoardPlayer(property.colour(),
						BoardPlayers.resolve(
								fromOptional(pool.createPlayer(property.colour())),
								fromOptional(property.name()), () -> {
									onGameOver(model, model.getCurrentPlayer().isDetective()
											? ImmutableSet.of(Colour.Black)
											: ImmutableSet.copyOf(stream(Colour.values())
													.filter(Colour::isDetective)
													.collect(toList())));
								}));
			}

			controls.forEach(model::registerSpectator);
			controls.forEach(l -> l.onGameAttach(model, setup));
			model.startRotate();
		}

		void terminate() {
			controls.forEach(model::unregisterSpectator);
			controls.forEach(GameControl::onGameDetached);
			pool.terminate();
		}

		@Override
		public void onRotationComplete(ScotlandYardView view) {
			if (!view.isGameOver()) model.startRotate();
		}

		@Override
		public void onGameOver(ScotlandYardView view, Set<Colour> winningPlayers) {
			Platform.runLater(() -> {
				board.lock();
				notifications.dismissAll();
				new NotificationBuilder("Game over, winner is " + winningPlayers)
						.addAction("Start again(same location)", () -> {
							notifications.dismissAll();
							terminate();
							createGame(setup);
						})
						.addAction("Main menu", () -> {
							notifications.dismissAll();
							terminate();
							setupGame();
						}).create().apply(n -> notifications.show(NOTIFY_GAMEOVER, n));
			});
		}

	}

}
