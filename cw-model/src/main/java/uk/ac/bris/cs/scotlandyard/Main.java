package uk.ac.bris.cs.scotlandyard;

import javafx.application.Application;
import javafx.stage.Stage;
import uk.ac.bris.cs.scotlandyard.ui.Utils;
import uk.ac.bris.cs.scotlandyard.ui.controller.LocalGame;

/**
 * Main entry point
 */
public final class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		LocalGame.newGame(Utils.setupResources(), primaryStage);
	}

}
