package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Blue;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Red;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.mockito.InOrder;

import uk.ac.bris.cs.scotlandyard.model.PlayerConfiguration.Builder;

/**
 * Tests spectator related features of the model
 */
@SuppressWarnings("unchecked")
public class ModelSpectatorTest extends ModelTestBase {

	@Test(expected = NullPointerException.class)
	public void testRegisterNullSpectatorShouldThrow() {
		createValidGame().registerSpectator(null);
	}

	@Test(expected = NullPointerException.class)
	public void testUnregisterNullSpectatorShouldThrow() {
		createValidGame().unregisterSpectator(null);
	}

	@Test
	public void testIsEmptyByDefault() {
		assertTrue(createValidGame().getSpectators().isEmpty());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetSpectatorIsImmutable() {
		Collection<Spectator> spectators = createValidGame().getSpectators();
		spectators.add(mock(Spectator.class));
	}

	@Test
	public void testRegisterAndUnregisterSpectator() {
		ScotlandYardGame game = createValidGame();
		Spectator mock = mock(Spectator.class);
		game.registerSpectator(mock);
		assertThat(game.getSpectators()).containsExactly(mock);
		game.unregisterSpectator(mock);
		assertThat(game.getSpectators()).isEmpty();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRegisterSameSpectatorTwiceShouldThrow() {
		ScotlandYardGame game = createValidGame();
		Spectator mock = mock(Spectator.class);
		// can't register the same spectator
		game.registerSpectator(mock);
		game.registerSpectator(mock);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnregisterIllegalSpectatorShouldThrow() {
		ScotlandYardGame game = createValidGame();
		// can't unregister a spectator that has never been registered before
		game.unregisterSpectator(mock(Spectator.class));
	}

	@Test
	public void testDoubleMoveShouldNotifyRoundStartTwoTimesInOrder() {
		PlayerConfiguration mrX = mrXAt45();
		ScotlandYardGame game = createGame(mrX, blueAt94());
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(x2(Black, Taxi, 46, Bus, 34)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		List<Integer> currentRounds = new ArrayList<>();
		doAnswer(invocation -> {
			currentRounds.add(invocation.<ScotlandYardView> getArgument(0).getCurrentRound());
			return null;
		}).when(spectator).onMoveMade(any(), any());
		game.startRotate();

		InOrder order = inOrder(spectator);
		// spectator should have the following methods called in order during a
		// double move
		order.verify(spectator).onMoveMade(any(), isA(DoubleMove.class));
		order.verify(spectator).onRoundStarted(notNull(), eq(1));
		order.verify(spectator).onMoveMade(any(), isA(TicketMove.class));
		order.verify(spectator).onRoundStarted(notNull(), eq(2));
		order.verify(spectator).onMoveMade(any(), isA(TicketMove.class));
		order.verifyNoMoreInteractions();

		// calls to ScotlandYard#getCurrentRound within Spectator#onMoveMade
		// should be exactly this:
		assertThat(currentRounds).containsExactly(0, 1, 2);
	}

	@Test
	public void testDoubleMoveShouldNotifyMoveMadeThreeTimesInOrder() {
		PlayerConfiguration mrX = mrXAt45();
		ScotlandYardGame game = createGame(mrX, blueAt94());
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(x2(Black, Taxi, 46, Bus, 34)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		InOrder order = inOrder(spectator);
		// first notified about the double
		order.verify(spectator).onMoveMade(any(), isA(DoubleMove.class));
		// and then the 2 tickets in the double move
		order.verify(spectator, times(2)).onMoveMade(any(), isA(TicketMove.class));
	}

	@Test
	public void testDoubleMoveShouldNotifyWithCorrectTicketAndLastDestinationDuringHiddenRound() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration blue = blueAt94();
		ScotlandYardGame game = createGame(rounds(false, false, false, false), mrX, blue);
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(x2(Black, Taxi, 46, Bus, 34)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 95)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// during hidden rounds, double move never reveals the actual location
		verify(spectator).onMoveMade(any(), eq(x2(Black, Taxi, 0, Bus, 0)));
		verify(spectator).onMoveMade(any(), eq(ticket(Black, Taxi, 0)));
		verify(spectator).onMoveMade(any(), eq(ticket(Black, Bus, 0)));
	}

	@Test
	public void testDoubleMoveShouldNotifyWithCorrectTicketAndDestinationDuringRevealRound() {
		PlayerConfiguration mrX = mrXAt45();
		ScotlandYardGame game = createGame(rounds(true, true, true, true), mrX, blueAt94());
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(x2(Black, Taxi, 46, Bus, 34)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// during reveal rounds, double move always reveal the actual location
		verify(spectator).onMoveMade(any(), eq(x2(Black, Taxi, 46, Bus, 34)));
		verify(spectator).onMoveMade(any(), eq(ticket(Black, Taxi, 46)));
		verify(spectator).onMoveMade(any(), eq(ticket(Black, Bus, 34)));
	}

	@Test
	public void
			testDoubleMoveShouldNotifyWithCorrectTicketAndDestinationWhenGoingFromRevealToHiddenRound() {
		PlayerConfiguration mrX = mrXAt45();
		ScotlandYardGame game = createGame(rounds(true, false, false), mrX, blueAt94());
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(x2(Black, Taxi, 46, Bus, 34)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// if the first move of a double move happens during a reveal round and
		// the next move is not, reveal actual location for the first ticket and
		// then the last revealed location for the second ticket
		verify(spectator).onMoveMade(any(), eq(x2(Black, Taxi, 46, Bus, 46)));
		verify(spectator).onMoveMade(any(), eq(ticket(Black, Taxi, 46)));
		verify(spectator).onMoveMade(any(), eq(ticket(Black, Bus, 46)));
	}

	@Test
	public void
			testDoubleMoveShouldNotifyWithCorrectTicketAndDestinationWhenGoingFromHiddenToRevealRound() {
		PlayerConfiguration mrX = mrXAt45();
		ScotlandYardGame game = createGame(rounds(false, true, true), mrX, blueAt94());
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(x2(Black, Taxi, 46, Bus, 34)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// if the first move of a double move happens during a hidden round and
		// the next move is not, reveal last revealed location for the first
		// ticket and then the actual location for the second ticket
		verify(spectator).onMoveMade(any(), eq(x2(Black, Taxi, 0, Bus, 34)));
		verify(spectator).onMoveMade(any(), eq(ticket(Black, Taxi, 0)));
		verify(spectator).onMoveMade(any(), eq(ticket(Black, Bus, 34)));
	}

	@Test
	public void
			testDetectiveTicketMoveShouldNotifyWithCorrectTicketAndDestinationDuringHiddenRound() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration blue = blueAt94();
		ScotlandYardGame game = createGame(rounds(false, false, false, false), mrX, blue);
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(x2(Black, Taxi, 46, Bus, 34)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 95)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// detective should not have their ticket destination changed in anyway
		verify(spectator).onMoveMade(any(), eq(ticket(Blue, Taxi, 95)));
	}

	@Test
	public void
			testDetectiveTicketMoveShouldNotifyWithCorrectTicketAndDestinationDuringRevealRound() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration blue = blueAt94();
		ScotlandYardGame game = createGame(rounds(true, true, true, true), mrX, blue);
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(x2(Black, Taxi, 46, Bus, 34)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 95)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// detective should not have their ticket destination changed in anyway
		verify(spectator).onMoveMade(any(), eq(ticket(Blue, Taxi, 95)));
	}

	@Test
	public void testPassMoveShouldNotifyOnce() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration red = redAt111();
		PlayerConfiguration blue = new Builder(Blue).using(mocked())
				.with(noTickets())
				.at(94).build();
		ScotlandYardGame game = createGame(mrX, red, blue);
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Red, Taxi, 112)))
				.when(red.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(pass(Blue)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// pass move should notify once
		verify(spectator).onMoveMade(any(), eq(pass(Blue)));
	}

	@Test
	public void testAllPlayersPlayedShouldNotifyRotationCompletedOnce() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration blue = blueAt94();
		PlayerConfiguration red = redAt111();
		ScotlandYardGame game = createGame(mrX, red, blue);
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Red, Taxi, 112)))
				.when(red.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 95)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		verify(spectator).onRotationComplete(any());

	}

	@Test
	public void testAllPlayersPlayedWithOneDoubleMoveShouldNotifyRotationCompletedOnce() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration blue = blueAt94();
		PlayerConfiguration red = redAt111();
		ScotlandYardGame game = createGame(mrX, red, blue);
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(x2(Black, Taxi, 46, Taxi, 47)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Red, Taxi, 112)))
				.when(red.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 95)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		verify(spectator).onRotationComplete(any());
	}

	@Test
	public void testStartRotateShouldNotifyRoundStartedOnce() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration red = redAt111();
		ScotlandYardGame game = createGame(mrX, red);
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// MrX is always the first player to play so:
		InOrder order = inOrder(spectator, mrX.player);
		order.verify(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		order.verify(spectator).onRoundStarted(any(), eq(1));
	}

	@Test
	public void testGameOverShouldNotifyGameOverOnce() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration red = of(Red, 47);
		ScotlandYardGame game = createGame(mrX, red);
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Red, Taxi, 46)))
				.when(red.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		verify(spectator).onGameOver(notNull(), eq(players(Red)));
	}

	@Test
	public void testMrXCaptureShouldNotifyMoveAndThenNotifyGameOver() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration red = of(Red, 47);
		ScotlandYardGame game = createGame(mrX, red);
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Red, Taxi, 46)))
				.when(red.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();

		InOrder order = inOrder(spectator);
		order.verify(spectator).onMoveMade(notNull(), eq(ticket(Red, Taxi, 46)));
		order.verify(spectator).onGameOver(notNull(), eq(players(Red)));
		order.verifyNoMoreInteractions();
	}

	@Test
	public void testFinalMoveShouldNotifyMoveAndThenNotifyGameOver() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration red = redAt111();
		ScotlandYardGame game = createGame(rounds(true), mrX, red);
		Spectator spectator = mock(Spectator.class);
		game.registerSpectator(spectator);
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Red, Taxi, 112)))
				.when(red.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		InOrder order = inOrder(spectator);
		order.verify(spectator).onMoveMade(notNull(), eq(ticket(Red, Taxi, 112)));
		order.verify(spectator).onGameOver(notNull(), eq(players(Black)));
		order.verifyNoMoreInteractions();
	}

	// not a test method
	private static Set<Colour> players(Colour... colours) {
		return new HashSet<>(asList(colours));
	}

}
