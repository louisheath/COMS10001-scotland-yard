package uk.ac.bris.cs.scotlandyard.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Blue;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Red;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYardView.NOT_STARTED;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;

import org.junit.Test;
import org.mockito.InOrder;

/**
 * Test {@link Player} related callbacks for the model
 */
@SuppressWarnings("unchecked")
public class ModelRoundTest extends ModelTestBase {

	@Test
	public void testPlayerNotified() {
		PlayerConfiguration black = validMrX();
		createGame(black, validBlue()).startRotate();
		// player should be notified when it is their turn to make a move
		verify(black.player, only()).makeMove(any(), anyInt(), anySet(), any());
	}

	@Test
	public void testCallbackIsNotNull() {
		PlayerConfiguration black = validMrX();
		createGame(black, validBlue()).startRotate();
		// the callback supplied cannot be null
		verify(black.player).makeMove(any(), anyInt(), anySet(), notNull());
	}

	@Test
	public void testInitialPositionMatchFirstRound() {
		PlayerConfiguration black = validMrX();
		PlayerConfiguration blue = validBlue();
		doAnswer(chooseFirst())
				.doNothing().when(black.player)
				.makeMove(any(), anyInt(), anySet(), any());
		doNothing().when(blue.player)
				.makeMove(any(), anyInt(), anySet(), any());
		createGame(black, blue).startRotate();
		// all locations should match the initial given location for all players
		verify(black.player).makeMove(any(), eq(black.location), anySet(), any());
		verify(blue.player).makeMove(any(), eq(blue.location), anySet(), any());
	}

	@Test
	public void testCallbackPositionMatchPlayerLocationDuringRevealRound() {
		PlayerConfiguration black = mrXAt45();
		PlayerConfiguration blue = blueAt94();
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(black.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 95)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		createGame(rounds(true, true), black, blue).startRotate();
		// reveal round should match
		verify(black.player).makeMove(any(), eq(black.location), anySet(), any());
		verify(blue.player).makeMove(any(), eq(blue.location), anySet(), any());
	}

	@Test
	public void testCallbackPositionMatchPlayerLocationDuringHiddenRound() {
		PlayerConfiguration black = mrXAt45();
		PlayerConfiguration blue = blueAt94();
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(black.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 95)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		createGame(rounds(false, false), black, blue).startRotate();
		// hidden round should match
		verify(black.player).makeMove(any(), eq(black.location), anySet(), any());
		verify(blue.player).makeMove(any(), eq(blue.location), anySet(), any());
	}

	@Test
	public void testRoundIncrementsAfterAllPlayersHavePlayed() {
		PlayerConfiguration black = of(Black, 35);
		PlayerConfiguration blue = of(Blue, 50);
		doAnswer(tryChoose(ticket(Black, Taxi, 65)))
				.doAnswer(tryChoose(ticket(Black, Bus, 82)))
				.doNothing()
				.when(black.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 49)))
				.doNothing()
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());

		ScotlandYardGame game = createGame(black, blue);
		game.startRotate();
		game.startRotate();
		// after two rotation(without double move), round should 2
		verify(black.player, times(2)).makeMove(any(), anyInt(), anySet(), any());
		verify(blue.player, times(2)).makeMove(any(), anyInt(), anySet(), any());
		assertThat(game.getCurrentRound()).isEqualTo(2);
	}

	@Test
	public void testRoundIncrementsCorrectlyForDoubleMove() throws Exception {
		PlayerConfiguration mrX = of(Black, 45);
		ScotlandYardGame game = createGame(mrX, of(Blue, 94));
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		DoubleMove expected = x2(Black, Taxi, 32, Taxi, 19);

		doAnswer(tryChoose(expected)).when(mrX.player)
				.makeMove(any(), anyInt(), anySet(), any());

		// MrX consumes a double move, we get notified but round should should
		// not change as DoubleMove is actually 2 moves
		doAnswer(forSpectator(view -> assertThat(view.getCurrentRound()).isEqualTo(NOT_STARTED)))
				.when(spectator).onMoveMade(any(), eq(expected));
		// MrX consumes the first ticket of the DoubleMove, expect round to
		// increment by one
		doAnswer(forSpectator(view -> assertThat(view.getCurrentRound()).isEqualTo(1)))
				.when(spectator).onMoveMade(any(), eq(ticket(Black, Taxi, 32)));
		// MrX consumes the second ticket of the DoubleMove, expect round to
		// increment again
		doAnswer(forSpectator(view -> assertThat(view.getCurrentRound()).isEqualTo(2)))
				.when(spectator).onMoveMade(any(), eq(ticket(Black, Taxi, 19)));

		game.startRotate();
	}

	@Test
	public void testMrXIsTheFirstToPlay() {
		PlayerConfiguration black = validMrX();
		PlayerConfiguration blue = validBlue();
		doAnswer(chooseFirst()).doNothing().when(black.player).makeMove(any(), anyInt(),
				anySet(), any());
		doNothing().when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		ScotlandYardGame game = createGame(black, blue);
		game.startRotate();
		// black always starts first
		InOrder order = inOrder(black.player, blue.player);
		order.verify(black.player).makeMove(any(), anyInt(), anySet(), any());
		order.verify(blue.player).makeMove(any(), anyInt(), anySet(), any());
	}

	@Test
	public void testRoundWaitsForPlayerWhoDoesNotRespond() {
		PlayerConfiguration black = validMrX();
		PlayerConfiguration blue = validBlue();
		ScotlandYardGame game = createGame(black, blue);
		// black player does nothing, preventing the game from rotating
		doNothing().when(black.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		verify(black.player, only()).makeMove(any(), anyInt(), anySet(), any());
		// blue should not receive any move request at all because black stalled
		// the game
		verify(blue.player, never()).makeMove(any(), anyInt(), anySet(), any());
	}

	@Test
	public void testRoundRotationNotifiesAllPlayer() {
		PlayerConfiguration black = of(Black, 35);
		PlayerConfiguration blue = of(Blue, 50);
		PlayerConfiguration red = of(Red, 26);
		ScotlandYardGame game = createGame(black, blue, red);
		doAnswer(tryChoose(ticket(Black, Taxi, 22)))
				.doNothing()
				.when(black.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 37)))
				.doNothing()
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Red, Taxi, 15)))
				.doNothing()
				.when(red.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// everyone should be notified only once
		verify(black.player, only()).makeMove(any(), anyInt(), anySet(), any());
		verify(blue.player, only()).makeMove(any(), anyInt(), anySet(), any());
		verify(red.player, only()).makeMove(any(), anyInt(), anySet(), any());
	}

	@Test(expected = NullPointerException.class)
	public void testCallbackWithNullWillThrow() {
		PlayerConfiguration black = validMrX();
		ScotlandYardGame game = createGame(black, validBlue());
		// supplying a null to the given consumer should not be allowed
		doAnswer(choose((ms, c) -> c.accept(null))).when(black.player).makeMove(any(), anyInt(),
				anySet(), any());
		game.startRotate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCallbackWithIllegalMoveNotInGivenMovesWillThrow() {
		PlayerConfiguration black = validMrX();
		ScotlandYardGame game = createGame(black, validBlue());
		// supplying a illegal tickets to the given consumer should not be
		// allowed in this case, Bus ticket with destination 20 is not included
		// in the given list
		doAnswer(choose((ms, c) -> c.accept(new TicketMove(Black, Bus, 20))))
				.when(black.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
	}

}
