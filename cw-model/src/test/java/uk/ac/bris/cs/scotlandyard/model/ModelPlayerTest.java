package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doAnswer;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Blue;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Red;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;

import org.junit.Test;

import uk.ac.bris.cs.scotlandyard.model.PlayerConfiguration.Builder;

/**
 * Tests actual game logic between players for the model
 */
@SuppressWarnings("unchecked")
public class ModelPlayerTest extends ModelTestBase {

	@Test
	public void testDetectivePassMoveDoesNotAffectMrX() {
		PlayerConfiguration mrX = new Builder(Black)
				.using(mocked())
				.with(makeTickets(2, 0, 0, 0, 0))
				.at(45).build();
		PlayerConfiguration red = new Builder(Red)
				.using(mocked())
				.with(makeTickets(2, 0, 0, 0, 0))
				.at(111).build();
		PlayerConfiguration blue = new Builder(Blue)
				.using(mocked())
				.with(noTickets())
				.at(94).build();
		ScotlandYardGame game = createGame(mrX, red, blue);
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Red, Taxi, 112)))
				.when(red.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(pass(Blue)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();

		// red taxi given to black
		assertTickets(game, Black, 2, 0, 0, 0, 0);
		assertTickets(game, Red, 1, 0, 0, 0, 0);
		// blue takes a pass move, nothing happens
		assertTickets(game, Blue, 0, 0, 0, 0, 0);
	}

	@Test
	public void testDetectiveTicketsGivenToMrXOnlyAfterUse() {
		PlayerConfiguration mrX = new Builder(Black)
				.using(mocked())
				.with(makeTickets(1, 1, 1, 0, 0))
				.at(45).build();
		PlayerConfiguration blue = new Builder(Blue)
				.using(mocked())
				.with(makeTickets(2, 0, 0, 0, 0))
				.at(94).build();
		ScotlandYardGame game = createGame(mrX, blue);
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 95)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// blue taxi ticket given to black
		// NOTE: black uses the last taxi ticket but was given another one from
		// blue so the total taxi ticket for MrX is one
		assertTickets(game, Black, 1, 1, 1, 0, 0);
		assertTickets(game, Blue, 1, 0, 0, 0, 0);
	}

	@Test
	public void testMrXMovesToDestinationAfterDoubleMove() {
		PlayerConfiguration mrX = new Builder(Black)
				.using(mocked())
				.with(makeTickets(2, 0, 0, 1, 0))
				.at(45).build();
		PlayerConfiguration blue = blueAt94();
		ScotlandYardGame game = createGame(asList(true, true, true),
				defaultGraph(), mrX, blue);
		doAnswer(tryChoose(x2(Black, Taxi, 46, Taxi, 47)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 95)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// black should move from 45 to 46 to 47
		assertThat(game.getPlayerLocation(Black)).isEqualTo(47);
	}

	@Test
	public void testMrXCorrectTicketDecrementsAfterDoubleMove() {
		PlayerConfiguration mrX = new Builder(Black)
				.using(mocked())
				.with(makeTickets(1, 1, 0, 1, 0))
				.at(45).build();
		ScotlandYardGame game = createGame(mrX, blueAt94());
		doAnswer(tryChoose(x2(Black, Taxi, 46, Bus, 34)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// taxi, bus, and double tickets should decrement by 1
		assertTickets(game, Black, 0, 0, 0, 0, 0);
	}

	@Test
	public void testMrXMovesToDestinationAfterTicketMove() {
		PlayerConfiguration mrX = new Builder(Black)
				.using(mocked())
				.with(makeTickets(1, 0, 0, 0, 0))
				.at(45).build();
		PlayerConfiguration blue = blueAt94();
		// MrX reveals himself for all rounds
		ScotlandYardGame game = createGame(asList(true, true),
				defaultGraph(), mrX, blue);

		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 95)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// black should move from 45 to 46
		assertThat(game.getPlayerLocation(Black)).isEqualTo(46);
	}

	@Test
	public void testMrXCorrectTicketDecrementsByOneAfterTicketMove() {
		PlayerConfiguration mrX = new Builder(Black)
				.using(mocked())
				.with(makeTickets(1, 0, 0, 0, 0))
				.at(45).build();
		ScotlandYardGame game = createGame(mrX, blueAt94());
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// taxi should decrement by 1
		assertTickets(game, Black, 0, 0, 0, 0, 0);
	}

	@Test
	public void testDetectiveMovesToDestinationAfterTicketMove() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration blue = new Builder(Blue)
				.using(mocked())
				.with(makeTickets(1, 2, 3, 0, 0))
				.at(94).build();

		ScotlandYardGame game = createGame(mrX, blue);
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 95)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// blue should move from 94 to 95
		assertThat(game.getPlayerLocation(Blue)).isEqualTo(95);
	}

	@Test
	public void testDetectiveCorrectTicketDecrementsByOneAfterTicketMove() {
		// X 45 Taxi -> 46
		// B 94
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration blue = new Builder(Blue)
				.using(mocked())
				.with(makeTickets(2, 2, 3, 0, 0))
				.at(94).build();

		ScotlandYardGame game = createGame(mrX, blue);

		doAnswer(tryChoose(ticket(Black, Taxi, 46))).when(mrX.player)
				.makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 95))).when(blue.player)
				.makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// taxi should decrement by 1
		assertTickets(game, Blue, 1, 2, 3, 0, 0);
	}

	@Test
	public void testDetectiveLocationHoldsAfterPassMove() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration red = of(Red, 111);
		PlayerConfiguration blue = new Builder(Blue)
				.using(mocked())
				.with(noTickets())
				.at(94).build();

		ScotlandYardGame game = createGame(mrX, red, blue);

		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Red, Taxi, 112)))
				.when(red.player).makeMove(any(), anyInt(), anySet(), any());
		// should contain a pass move
		doAnswer(tryChoose(new PassMove(Blue)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// should be the same as initial location
		assertThat(game.getPlayerLocation(Blue)).isEqualTo(94);
	}

	@Test
	public void testDetectiveTicketCountHoldsAfterPassMove() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration red = of(Red, 111);
		PlayerConfiguration blue = new Builder(Blue)
				.using(mocked())
				.with(noTickets())
				.at(94).build();

		ScotlandYardGame game = createGame(mrX, red, blue);

		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		doAnswer(tryChoose(ticket(Red, Taxi, 112)))
				.when(red.player).makeMove(any(), anyInt(), anySet(), any());
		// should contain a pass move
		doAnswer(tryChoose(pass(Blue)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		game.startRotate();
		// no tickets should be consumed
		assertTickets(game, Blue, 0, 0, 0, 0, 0);
	}

	@Test
	public void testDetectiveLocationAlwaysCorrect() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration blue = blueAt94();
		ScotlandYardGame game = createGame(asList(true, false, true), defaultGraph(), mrX, blue);
		// 45 -> 46 -> 47
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 47)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		// 94 -> 93 -> 92
		doAnswer(tryChoose(ticket(Blue, Taxi, 93)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 92)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		assertThat(game.getPlayerLocation(Blue)).isEqualTo(94);
		game.startRotate();
		assertThat(game.getPlayerLocation(Blue)).isEqualTo(93);
		game.startRotate();
		assertThat(game.getPlayerLocation(Blue)).isEqualTo(92);
	}

	@Test
	public void testMrXLocationCorrectOnRevealRound() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration blue = blueAt94();
		ScotlandYardGame game = createGame(asList(true, true, true), defaultGraph(), mrX, blue);
		// 45 |-> 46 -> 47
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 47)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		// 94 |-> 93 -> 92
		doAnswer(tryChoose(ticket(Blue, Taxi, 93)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 92)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		// initial reveal reveal
		// (0)45 |-> [ 46 ] -> [ 47 ]
		assertThat(game.getPlayerLocation(Black)).isEqualTo(0);
		game.startRotate();
		assertThat(game.getPlayerLocation(Black)).isEqualTo(46);
		game.startRotate();
		assertThat(game.getPlayerLocation(Black)).isEqualTo(47);
	}

	@Test
	public void testMrXLocationIsHisLastRevealLocationOnHiddenRound() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration blue = blueAt94();
		ScotlandYardGame game = createGame(asList(true, false, false, true), defaultGraph(), mrX,
				blue);
		// 45 |-> 46 -> 47 -> 62 -> 79
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 47)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 62)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 79)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		// 94 |-> 93 -> 92 -> 73 -> 57
		doAnswer(tryChoose(ticket(Blue, Taxi, 93)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 92)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 73)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 57)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());

		// initial reveal hidden hidden reveal
		// (0)45 |-> [ 46 ] -> (46)[47] -> (46)[62] -> [ 79 ]
		assertThat(game.getPlayerLocation(Black)).isEqualTo(0);
		game.startRotate();
		assertThat(game.getPlayerLocation(Black)).isEqualTo(46);
		game.startRotate();
		assertThat(game.getPlayerLocation(Black)).isEqualTo(46);
		game.startRotate();
		assertThat(game.getPlayerLocation(Black)).isEqualTo(46);
		game.startRotate();
		assertThat(game.getPlayerLocation(Black)).isEqualTo(79);
	}

	@Test
	public void testMrXLocationCorrectWithOneRevealRound() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration blue = blueAt94();
		ScotlandYardGame game = createGame(singletonList(true), defaultGraph(), mrX, blue);
		// 45 |-> 46
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		// 94 |-> 93
		doAnswer(tryChoose(ticket(Blue, Taxi, 93)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		// initial reveal
		// (0)45 |-> [ 46 ]
		assertThat(game.getPlayerLocation(Black)).isEqualTo(0);
		game.startRotate();
		assertThat(game.getPlayerLocation(Black)).isEqualTo(46);
	}

	@Test
	public void testMrXLocationCorrectWithOneHiddenRound() {
		PlayerConfiguration mrX = mrXAt45();
		PlayerConfiguration blue = blueAt94();
		ScotlandYardGame game = createGame(singletonList(false), defaultGraph(), mrX, blue);
		// 45 |-> 46
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.when(mrX.player).makeMove(any(), anyInt(), anySet(), any());
		// 94 |-> 93
		doAnswer(tryChoose(ticket(Blue, Taxi, 93)))
				.when(blue.player).makeMove(any(), anyInt(), anySet(), any());
		// initial hidden
		// (0)45 |-> (0)[46]
		assertThat(game.getPlayerLocation(Black)).isEqualTo(0);
		game.startRotate();
		assertThat(game.getPlayerLocation(Black)).isEqualTo(0);
	}

}
