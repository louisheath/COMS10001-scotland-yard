package uk.ac.bris.cs.oxo;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uk.ac.bris.cs.oxo.standard.OXO;

public class Main extends Application {


	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		ResourceManager manager = new ResourceManager();
		LocalBoardController controller = new LocalBoardController(manager, OXO::new);
		primaryStage.setScene(new Scene(controller.root(), 500, 500));
		primaryStage.getIcons().add(manager.of(Side.NOUGHT));
		primaryStage.setTitle("OXO");
		primaryStage.show();
	}
}
