package uk.ac.bris.cs.oxo;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uk.ac.bris.cs.fxkit.BindFXML;
import uk.ac.bris.cs.fxkit.Controller;

@BindFXML(value = "board.fxml", css = "style.css")
public abstract class BoardController implements Controller, Initializable {

	@FXML private StackPane root;
	@FXML private Label status;
	@FXML private ProgressIndicator timer;
	@FXML private GridPane grid;
	@FXML private VBox surface;
	@FXML private StackPane overlay;

	protected final ResourceManager manager;

	private Timeline timeline = new Timeline();

	public BoardController(ResourceManager manager) {
		this.manager = manager;
		Controller.bind(this);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setupSample();
	}

	private void setupSample() {
		int size = 5;
		setupGrid(size);
		for (int row = 0; row < size; row++) {
			for (int column = 0; column < size; column++) {
				getCellAt(row, column)
						.setImage(getCellImage(Math.random() < 0.5 ? Side.NOUGHT : Side.CROSS));
			}
		}
	}

	protected GridPane grid() {
		return grid;
	}

	protected void setTimerVisible(boolean visible) {
		timer.setVisible(visible);
	}

	protected void stopTimer(){
		timeline.stop();
		timeline.getKeyFrames().clear();
	}

	protected void setTimer(Duration duration, Runnable callback) {
		timeline.getKeyFrames().clear();
		this.timer.setProgress(0);
		timeline.getKeyFrames().add(new KeyFrame(new javafx.util.Duration(duration.toMillis()),
				new KeyValue(timer.progressProperty(), 1D)));
		timeline.playFromStart();
		timeline.setOnFinished(e -> callback.run());
	}

	protected void setupGrid(int size) {
		grid.getRowConstraints().clear();
		grid.getColumnConstraints().clear();
		grid.getChildren().retainAll(grid.getChildren().get(0));
		for (int i = 0; i < size; i++) {
			grid.getRowConstraints().add(new RowConstraints(Region.USE_COMPUTED_SIZE,
					Region.USE_COMPUTED_SIZE, Double.MAX_VALUE));
			grid.getColumnConstraints().add(new ColumnConstraints(Region.USE_COMPUTED_SIZE,
					Region.USE_COMPUTED_SIZE, Double.MAX_VALUE));
		}

		for (int row = 0; row < size; row++) {
			for (int column = 0; column < size; column++) {
				ImageView cell = new ImageView();
				cell.setFitWidth(150);
				cell.setFitHeight(150);
				cell.setPickOnBounds(true);
				cell.setSmooth(true);
				cell.setPreserveRatio(true);
				grid.add(cell, column, row);
			}
		}
	}

	protected void showOverlay(Node node) {
		surface.setEffect(new GaussianBlur(20));
		surface.setOpacity(0.3);
		overlay.getChildren().add(node);
		overlay.setVisible(true);
	}

	protected void dismissOverlay() {
		overlay.getChildren().clear();
		overlay.setVisible(false);
		surface.setOpacity(1);
		surface.setEffect(null);
	}

	protected ImageView getCellAt(int row, int column) {
		return (ImageView) grid.getChildren().stream().filter(ImageView.class::isInstance)
				.filter(n -> row == GridPane.getRowIndex(n) && column == GridPane.getColumnIndex(n))
				.findFirst().orElseThrow(AssertionError::new);
	}

	protected List<ImageView> getCells() {
		return grid.getChildren().stream().filter(ImageView.class::isInstance)
				.map(ImageView.class::cast).collect(Collectors.toList());
	}

	protected Image getCellImage(Side side) {
		return manager.of(side);
	}

	protected void setCurrentPlayer(Side side) {
		status.setText((side == Side.CROSS ? "X" : "O") + "'s turn");
		// currentPlayer.setText("Waiting for " + side.name());
	}

	@Override
	public Parent root() {
		return root;
	}

}
