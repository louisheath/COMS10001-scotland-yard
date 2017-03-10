package uk.ac.bris.cs.scotlandyard.ui.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.base.Strings;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import uk.ac.bris.cs.fxkit.BindFXML;
import uk.ac.bris.cs.fxkit.Controller;
import uk.ac.bris.cs.scotlandyard.ResourceManager;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.StandardGame;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.ui.GameControl;
import uk.ac.bris.cs.scotlandyard.ui.model.BoardProperty;
import uk.ac.bris.cs.scotlandyard.ui.ModelConfiguration;

/**
 * Controller for the ticket counter
 */
@BindFXML("layout/Players.fxml")
public final class TicketsCounter implements Controller, GameControl {

	@FXML private VBox root;
	@FXML private VBox playerContainer;

	private final ResourceManager manager;
	private final HashMap<Colour, PlayerView> controllers = new HashMap<>();

	TicketsCounter(ResourceManager manager, BoardProperty config) {
		this.manager = manager;
		Controller.bind(this);
		root.managedProperty().bind(root.visibleProperty());
	}

	@Override
	public void onGameAttach(ScotlandYardView view, ModelConfiguration configuration) {
		configuration.players().forEach(player -> {
			controllers.computeIfAbsent(player.colour(),
					c -> {
						PlayerView v = new PlayerView(manager, c, player.name().orElse(""));
						playerContainer.getChildren().add(v.root());
						VBox.setVgrow(v.root(), Priority.ALWAYS);
						return v;
					}).update(view);
		});
	}

	@Override
	public void onGameDetached() {
		controllers.clear();
		playerContainer.getChildren().clear();
	}

	@Override
	public void onMoveMade(ScotlandYardView view, Move move) {
		controllers.values().forEach(c -> c.update(view));
	}

	@Override
	public Parent root() {
		return root;
	}

	@BindFXML("layout/Ticket.fxml")
	static class TicketView implements Controller {

		@FXML private HBox root;
		@FXML private ImageView ticket;
		@FXML private Label count;
		@FXML private HBox bar;

		private final ResourceManager manager;

		TicketView(Ticket ticket, ResourceManager manager) {
			this.manager = manager;
			Controller.bind(this);
			this.ticket.setImage(this.manager.getTicket(ticket));
		}

		void updateCount(int count) {
			this.count.setText(String.format("%3d", count));
			if (this.bar.getChildren().size() != count) {
				this.bar.getChildren().clear();
				IntStream.range(0, count)
						.mapToObj(i -> new Rectangle(1, 12, Color.WHITE))
						.forEachOrdered(bar.getChildren()::add);
			}
		}

		@Override
		public Parent root() {
			return root;
		}
	}

	@BindFXML("layout/Player.fxml")
	static class PlayerView implements Controller {

		@FXML private VBox root;
		@FXML private Label label;
		@FXML private Pane tickets;

		private final Colour colour;
		private final String name;

		private final Map<Ticket, TicketView> ticketMap = new HashMap<>();
		private final ResourceManager manager;

		PlayerView(ResourceManager manager, Colour colour, String name) {
			this.manager = manager;
			Controller.bind(this);
			this.colour = colour;
			this.name = name;
			root.setBackground(new Background(
					new BackgroundFill(Color.valueOf(colour.name()).darker(), null, null)));
		}

		void update(ScotlandYardView view) {
			label.setText(Strings.isNullOrEmpty(name) ? colour.name() : name);
			Stream.of(Ticket.values()).filter(t -> hasTicket(colour, t)).forEachOrdered(ticket -> {
				ticketMap.computeIfAbsent(ticket, (t) -> {
					TicketView controller = new TicketView(t, manager);
					this.tickets.getChildren().add(controller.root());
					return controller;
				}).updateCount(view.getPlayerTickets(colour, ticket));
			});
		}

		private static boolean hasTicket(Colour colour, Ticket ticket) {
			return colour == Colour.Black
					? StandardGame.MRX_TICKETS.contains(ticket)
					: StandardGame.DETECTIVE_TICKETS.contains(ticket);
		}

		@Override
		public Parent root() {
			return root;
		}
	}

}
