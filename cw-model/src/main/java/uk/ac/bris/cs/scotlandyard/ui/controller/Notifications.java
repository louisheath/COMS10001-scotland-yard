package uk.ac.bris.cs.scotlandyard.ui.controller;

import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Sets;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import uk.ac.bris.cs.fxkit.BindFXML;
import uk.ac.bris.cs.fxkit.Controller;
import uk.ac.bris.cs.scotlandyard.ResourceManager;
import uk.ac.bris.cs.scotlandyard.ui.GameControl;
import uk.ac.bris.cs.scotlandyard.ui.model.BoardProperty;

/**
 * Controller for stackable notifications
 */
@BindFXML("layout/Notification.fxml")
public final class Notifications implements Controller, GameControl {

	@FXML private VBox root;

	Notifications(ResourceManager resourceManager, BoardProperty config) {
		Controller.bind(this);
	}

	void show(String key, Node node) {
		Platform.runLater(() -> {
			StackPane container = new StackPane(node);
			container.setId(key);
			container.getStyleClass().add("notification");
			root.getChildren().add(container);
		});
	}

	void dismiss(String... keys) {
		Platform.runLater(() -> {
			Set<String> set = Sets.newHashSet(keys);
			root.getChildren().removeIf(p -> set.contains(p.getId()));
		});
	}

	void dismissAll() {
		Platform.runLater(() -> {
			root.getChildren().clear();
		});
	}

	public static class NotificationBuilder {

		private final VBox root = new VBox();
		private final Label title = new Label();
		private final HBox actions = new HBox();
		private final ProgressBar timer = new ProgressBar();
		private Timeline timeline;

		NotificationBuilder(String titleText) {
			root.setMinWidth(300);
			root.setSpacing(8);
			root.setAlignment(Pos.CENTER);
			title.setContentDisplay(ContentDisplay.RIGHT);
			title.setGraphicTextGap(12);
			title.setGraphic(actions);
			title.setText(titleText);
			timer.setManaged(false);
			timer.setMaxWidth(Double.MAX_VALUE);
			root.getChildren().addAll(title, timer);
		}

		NotificationBuilder addAction(String text, Runnable callback) {
			Button action = new Button(text);
			action.setOnAction(e -> callback.run());
			actions.getChildren().add(action);
			return this;
		}

		Notification create() {
			return new Notification(root);
		}

		TimedNotification create(Duration duration, Runnable callback) {
			timer.setManaged(true);
			timeline = new Timeline(
					new KeyFrame(Duration.ZERO, new KeyValue(timer.progressProperty(), 1)),
					new KeyFrame(duration, new KeyValue(timer.progressProperty(), 0)));
			timeline.setOnFinished(e -> callback.run());
			timeline.play();
			return new TimedNotification(root, timeline);
		}

		class Notification {

			private final Node root;

			private Notification(Node root) {
				this.root = root;
			}

			void apply(Consumer<Node> consumer) {
				consumer.accept(root);
			}

			Node node() {
				return root;
			}
		}

		class TimedNotification extends Notification {

			private final Timeline timeline;

			private TimedNotification(Node root, Timeline timeline) {
				super(root);
				this.timeline = timeline;
			}

			void cancel() {
				timeline.stop();
			}

		}

	}

	@Override
	public Parent root() {
		return root;
	}
}
