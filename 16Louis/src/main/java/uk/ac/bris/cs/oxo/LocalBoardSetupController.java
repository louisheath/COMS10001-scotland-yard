package uk.ac.bris.cs.oxo;

import static java.lang.Double.MAX_VALUE;
import static javafx.scene.layout.Priority.SOMETIMES;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import static uk.ac.bris.cs.oxo.Side.CROSS;
import static uk.ac.bris.cs.oxo.Side.NOUGHT;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.fxmisc.easybind.EasyBind;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.util.Callback;
import uk.ac.bris.cs.fxkit.BindFXML;
import uk.ac.bris.cs.fxkit.Controller;
import uk.ac.bris.cs.oxo.standard.Setup;

@BindFXML("localsetup.fxml")
public class LocalBoardSetupController implements Controller, Initializable {

	@FXML private GridPane root;
	@FXML private GridPane grid;
	@FXML private Slider gridSize;
	@FXML private ComboBox<Side> startingSide;
	@FXML private Button start;
	@FXML private Label size;
	@FXML private Slider timeout;

	private final ResourceManager manager;
	private final Consumer<Setup> settingConsumer;

	public LocalBoardSetupController(ResourceManager manager,
			Consumer<Setup> settingConsumer) {
		this.manager = manager;
		this.settingConsumer = settingConsumer;
		Controller.bind(this);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		size.textProperty().bind(gridSize.valueProperty().asString("%1$.0f x %1$.0f"));

		Bindings.min(grid.widthProperty(), grid.hgapProperty());

		// grid.minHeightProperty().bind(gridSize.widthProperty());
		// grid.minWidthProperty().bind(gridSize.heightProperty());

		EasyBind.subscribe(gridSize.valueProperty(), c -> {
			fill(grid.getRowConstraints(), c.intValue(), i -> new RowConstraints(USE_PREF_SIZE,
					USE_PREF_SIZE, MAX_VALUE, SOMETIMES, VPos.CENTER, true));
			fill(grid.getColumnConstraints(), c.intValue(),
					i -> new ColumnConstraints(USE_PREF_SIZE, USE_PREF_SIZE, MAX_VALUE, SOMETIMES,
							HPos.CENTER, true));
		});

		startingSide.setItems(FXCollections.observableArrayList(CROSS, NOUGHT));
		Callback<ListView<Side>, ListCell<Side>> factory = param -> new SideListCell(manager);
		startingSide.setCellFactory(factory);
		startingSide.setButtonCell(factory.call(null));
		startingSide.getSelectionModel().selectFirst();
		start.setOnAction(e -> settingConsumer.accept(new Setup((int) gridSize.getValue(),
				startingSide.getValue(), Duration.ofSeconds((long) timeout.getValue()))));
	}

	private static <E> void fill(List<E> elements, int expected, IntFunction<E> function) {
		int actual = elements.size();
		if (expected == 0) {
			elements.clear();
		} else if (expected < actual) {
			elements.subList(expected, actual).clear();
		} else if (expected > actual) {
			IntStream.range(actual, expected).mapToObj(function)
					.collect(Collectors.toCollection(() -> elements));
		}
	}

	@Override
	public Parent root() {
		return root;
	}

}
