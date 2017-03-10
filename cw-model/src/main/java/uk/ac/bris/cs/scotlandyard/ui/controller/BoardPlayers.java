package uk.ac.bris.cs.scotlandyard.ui.controller;

import java.util.Set;
import java.util.function.Consumer;

import io.atlassian.fugue.Option;
import javafx.application.Platform;
import javafx.util.Duration;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.ui.controller.Board.BoardPlayer;
import uk.ac.bris.cs.scotlandyard.ui.controller.Board.HintedBoard;
import uk.ac.bris.cs.scotlandyard.ui.controller.Notifications.NotificationBuilder;
import uk.ac.bris.cs.scotlandyard.ui.controller.Notifications.NotificationBuilder.TimedNotification;

import static java.lang.String.format;

public class BoardPlayers {

	private BoardPlayers() {}

	public static BoardPlayer resolve(Option<Player> player,
			Option<String> name,
			Runnable timeoutCallback) {
		return player.map(p -> (BoardPlayer) new BoardPlayers.AIBoardPlayer(p, timeoutCallback))
				.getOrElse(new BoardPlayers.HumanBoardPlayer(false, name, timeoutCallback));
	}

	public static class HumanBoardPlayer implements BoardPlayer {

		private static final String MRX_WARN = "mrx_warn";
		private static final String NOTIFY_TIMEOUT = "notify_timeout";
		private static final String NOTIFY_MOVE = "notify_move";
		private final boolean strictTimer;
		private final Option<String> name;
		private final Runnable timeoutCallback;

		public HumanBoardPlayer(boolean strictTimer,
				Option<String> name,
				Runnable timeoutCallback) {
			this.strictTimer = strictTimer;
			this.name = name;
			this.timeoutCallback = timeoutCallback;
		}

		@Override
		public void makeMove(HintedBoard board,
				ScotlandYardView view,
				int location,
				Set<Move> moves,
				Consumer<Move> callback) {

			Duration timeout = Duration
					.millis(board.configuration().timeoutProperty().get().toMillis());
			Notifications notifications = board.notifications();
			Colour colour = moves.iterator().next().colour();

			notifications.dismissAll();

			String name = this.name.getOrElse(colour.name() + " player");

			TimedNotification timed = new NotificationBuilder(
					format("Waiting for %s to make a move", name))
							.create(timeout, () -> {
								notifications.dismissAll();
								timeoutCallback.run();
							});
			timed.apply(n -> notifications.show(NOTIFY_TIMEOUT, n));

			if (colour.isMrX() && strictTimer) {
				NotificationBuilder builder = new NotificationBuilder(
						"Mr.X's turn, detectives please look away")
								.addAction("OK", () -> {
									notifications.dismiss(MRX_WARN);
									showNotificationAndAsk(location,
											name,
											moves,
											board,
											timed,
											callback);
								});
			} else {
				showNotificationAndAsk(location,
						name,
						moves,
						board,
						timed,
						callback);
			}

		}

		private void showNotificationAndAsk(int location,
				String name,
				Set<Move> moves,
				HintedBoard board,
				TimedNotification timed,
				Consumer<Move> consumer) {
			new NotificationBuilder(format("%s, please pick a move", name))
					.addAction("Scroll to player", () -> board.scrollToLocation(location))
					.create()
					.apply(c -> board.notifications().show(NOTIFY_MOVE, c));
			board.showMoveHints(moves, move -> {
				board.hideMoveHints();
				timed.cancel();
				board.notifications().dismiss(NOTIFY_TIMEOUT);
				board.notifications().dismiss(NOTIFY_MOVE);
				consumer.accept(move);
			});
		}
	}

	public static class AIBoardPlayer implements BoardPlayer {

		private static final String WAIT_AI = "wait_ai";
		private final Player player;
		private final Runnable timeoutCallback;

		AIBoardPlayer(Player player, Runnable timeoutCallback) {
			this.player = player;
			this.timeoutCallback = timeoutCallback;
		}

		@Override
		public void makeMove(HintedBoard board, ScotlandYardView view, int location,
				Set<Move> moves,
				Consumer<Move> callback) {
			Duration timeout = Duration
					.millis(board.configuration().timeoutProperty().get().toMillis());
			Notifications notifications = board.notifications();
			TimedNotification timed = new NotificationBuilder(format(
					"Waiting for AI for %s to select a move", moves.iterator().next().colour()))
							.create(timeout, () -> {
								notifications.dismissAll();
								timeoutCallback.run();
							});
			timed.apply(n -> notifications.show(WAIT_AI, n));
			player.makeMove(view, location, moves, move -> {
				Platform.runLater(() -> {
					timed.cancel();
					notifications.dismiss(WAIT_AI);
					callback.accept(move);
				});
			});
		}
	}
}
