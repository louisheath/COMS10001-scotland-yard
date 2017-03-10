package uk.ac.bris.cs.scotlandyard.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Blue;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Green;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Red;
import static uk.ac.bris.cs.scotlandyard.model.Colour.White;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import uk.ac.bris.cs.scotlandyard.model.PlayerConfiguration.Builder;

/**
 * Tests related to whether the model reports game over correctly
 */
public class ModelGameOverTest extends ModelTestBase {

	@Test(expected = IllegalStateException.class)
	public void testStartRoundShouldThrowIfGameAlreadyOver() {
		PlayerConfiguration black = of(Black, 86);
		PlayerConfiguration blue = new Builder(Blue).using(mocked())
				.with(makeTickets(0, 0, 0, 0, 0))
				.at(105).build();
		ScotlandYardGame game = createGame(black, blue);
		assertThat(game.isGameOver()).isTrue();
		// should throw because game is over
		game.startRotate();
	}

	@Test
	public void testGameNotOverWhenThereIsStillRoundsLeft() {
		PlayerConfiguration black = of(Black, 86);
		PlayerConfiguration blue = of(Blue, 85);

		ScotlandYardGame game = createGame(rounds(true, false, false), black, blue);
		// 86 -> 103 -> 102
		doAnswer(tryChoose(ticket(Black, Taxi, 103)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 102)))
				.when(black.player).makeMove(any(), anyInt(), any(), any());
		// 85 -> 68 -> 51
		doAnswer(tryChoose(ticket(Blue, Taxi, 68)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 51)))
				.when(blue.player).makeMove(any(), anyInt(), any(), any());
		// first round, should not finish
		game.startRotate();
		assertThat(game.isGameOver()).isFalse();
		// we still have one round left
		game.startRotate();
		assertThat(game.isGameOver()).isFalse();
	}

	@Test
	public void testGameOverAfterAllRoundsUsed() {
		PlayerConfiguration black = of(Black, 86);
		PlayerConfiguration blue = of(Blue, 85);

		ScotlandYardGame game = createGame(rounds(true), black, blue);
		doAnswer(tryChoose(ticket(Black, Taxi, 103)))
				.when(black.player).makeMove(any(), anyInt(), any(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 68)))
				.when(blue.player).makeMove(any(), anyInt(), any(), any());
		// all round used
		game.startRotate();
		assertThat(game.isGameOver()).isTrue();
	}

	@Test
	public void testGameOverIfAllDetectivesStuck() {
		PlayerConfiguration black = of(Black, 86);
		PlayerConfiguration blue = new Builder(Blue).using(mocked())
				.with(makeTickets(1, 0, 0, 0, 0))
				.at(105).build();
		PlayerConfiguration red = new Builder(Red).using(mocked())
				.with(makeTickets(1, 0, 0, 0, 0))
				.at(70).build();

		ScotlandYardGame game = createGame(black, blue, red);
		doAnswer(tryChoose(ticket(Black, Taxi, 104)))
				.when(black.player).makeMove(any(), anyInt(), any(), any());
		// Blue runs out of ticket at this point
		doAnswer(tryChoose(ticket(Blue, Taxi, 106)))
				.when(blue.player).makeMove(any(), anyInt(), any(), any());
		// Red runs out of ticket at this point
		doAnswer(tryChoose(ticket(Red, Taxi, 71)))
				.when(red.player).makeMove(any(), anyInt(), any(), any());
		game.startRotate();
		// All detectives ran out of tickets, they are stuck
		assertThat(game.isGameOver()).isTrue();
	}

	@Test
	public void testGameOverIfMrXStuck() {
		PlayerConfiguration black = new Builder(Black).using(mocked())
				.with(makeTickets(1, 1, 1, 0, 0))
				.at(86).build();
		PlayerConfiguration blue = of(Blue, 108);
		ScotlandYardGame game = createGame(black, blue);
		// MrX picks uses the last taxi ticket and lands on a spot where there
		// is no other method of transport, he can no longer move
		doAnswer(tryChoose(ticket(Black, Taxi, 104)))
				.when(black.player).makeMove(any(), anyInt(), any(), any());
		// MrX will receive an extra bus ticket but he is still stuck
		doAnswer(tryChoose(ticket(Blue, Bus, 105)))
				.when(blue.player).makeMove(any(), anyInt(), any(), any());
		game.startRotate();
		// game is over because MrX is stuck and cannot make a move (no other
		// detectives can move because they are waiting for MrX, thus game
		// cannot proceed because no one can move at this point; in this case,
		// MrX lose the game by foolishly walking into a bad spot
		assertThat(game.isGameOver()).isTrue();
	}

	@Test
	public void testGameNotOverIfMrXWasFreedBeforeNextRotation() {
		PlayerConfiguration black = new Builder(Black).using(mocked())
				.with(makeTickets(1, 1, 1, 0, 0))
				.at(86).build();
		PlayerConfiguration blue = of(Blue, 108);
		PlayerConfiguration red = of(Red, 134);
		ScotlandYardGame game = createGame(black, blue, red);
		// MrX uses the last taxi ticket and lands on a spot where there is no
		// other
		// method of transport, he can no longer move
		doAnswer(tryChoose(ticket(Black, Taxi, 104)))
				.when(black.player).makeMove(any(), anyInt(), any(), any());
		// MrX will receive an extra bus ticket but he is still stuck
		doAnswer(tryChoose(ticket(Blue, Bus, 105)))
				.when(blue.player).makeMove(any(), anyInt(), any(), any());
		// MrX will receive an extra Taxi ticket, he is now freed
		doAnswer(tryChoose(ticket(Red, Taxi, 118)))
				.when(red.player).makeMove(any(), anyInt(), any(), any());
		game.startRotate();
		// game is not over because MrX still has a spare Taxi ticket that he
		// can use
		assertThat(game.isGameOver()).isFalse();
	}

	@Test
	public void testGameOverIfMrXCaptured() {
		PlayerConfiguration black = of(Black, 86);
		PlayerConfiguration blue = of(Blue, 85);

		ScotlandYardGame game = createGame(black, blue);
		doAnswer(tryChoose(ticket(Black, Taxi, 103)))
				.when(black.player).makeMove(any(), anyInt(), any(), any());
		// MrX captured at 103
		doAnswer(tryChoose(ticket(Blue, Taxi, 103)))
				.when(blue.player).makeMove(any(), anyInt(), any(), any());
		game.startRotate();
		assertThat(game.isGameOver()).isTrue();
	}

	@Test
	public void testDetectiveWinsIfMrXCornered() {
		PlayerConfiguration black = of(Black, 103);
		PlayerConfiguration blue = new Builder(Blue).using(mocked())
				.with(noTickets())
				.at(68).build();
		PlayerConfiguration red = new Builder(Red).using(mocked())
				.with(noTickets())
				.at(84).build();
		PlayerConfiguration green = of(Green, 102);
		ScotlandYardGame game = createGame(black, blue, red, green);
		// MrX moves to 85, of which 2 of the 3 connecting nodes are occupied by
		// blue(68) and red(84)
		doAnswer(tryChoose(ticket(Black, Taxi, 85)))
				.when(black.player).makeMove(any(), anyInt(), any(), any());
		doAnswer(tryChoose(pass(Blue)))
				.when(blue.player).makeMove(any(), anyInt(), any(), any());
		doAnswer(tryChoose(pass(Red)))
				.when(red.player).makeMove(any(), anyInt(), any(), any());
		// blue then cuts MrX off by moving to 103, MrX is now cornered and
		// cannot move
		doAnswer(tryChoose(ticket(Green, Taxi, 103)))
				.when(green.player).makeMove(any(), anyInt(), any(), any());
		game.startRotate();
		assertThat(game.isGameOver()).isTrue();
	}

	@Test
	public void testGameNotOverIfMrXCorneredButCanStillEscape() {
		PlayerConfiguration black = of(Black, 40);
		PlayerConfiguration blue = new Builder(Blue).using(mocked())
				.with(noTickets())
				.at(39).build();
		PlayerConfiguration red = new Builder(Red).using(mocked())
				.with(noTickets())
				.at(51).build();
		PlayerConfiguration white = new Builder(White).using(mocked())
				.with(noTickets())
				.at(69).build();
		PlayerConfiguration green = of(Green, 41);
		ScotlandYardGame game = createGame(black, blue, red, white, green);
		// MrX moves to 85, of which 3 of the 4 connecting nodes are occupied by
		// blue(39), red(51), and white(69)
		doAnswer(tryChoose(ticket(Black, Taxi, 52)))
				.when(black.player).makeMove(any(), anyInt(), any(), any());
		doAnswer(tryChoose(pass(Blue)))
				.when(blue.player).makeMove(any(), anyInt(), any(), any());
		doAnswer(tryChoose(pass(Red)))
				.when(red.player).makeMove(any(), anyInt(), any(), any());
		doAnswer(tryChoose(pass(White)))
				.when(white.player).makeMove(any(), anyInt(), any(), any());
		// blue then cuts MrX off by moving to 40 but MrX can still escape by
		// taking a bus/secret to 41/13/67/86 or even a double move
		doAnswer(tryChoose(ticket(Green, Taxi, 40)))
				.when(green.player).makeMove(any(), anyInt(), any(), any());
		game.startRotate();
		assertThat(game.isGameOver()).isFalse();
	}

	@Test
	public void testGameNotOverBeforeAnyRoundWithNonTerminatingConfiguration() {
		ScotlandYardGame game = createGame(of(Black, 86), of(Blue, 108));
		// game is not over with initial non-terminating setup
		assertThat(game.isGameOver()).isFalse();
	}

	@Test
	public void testGameOverBeforeAnyRoundWithTerminatingConfiguration() {
		// blue cannot move and is the only detective, the game is over by
		// default
		PlayerConfiguration blue = new Builder(Blue).using(mocked())
				.with(noTickets())
				.at(108).build();
		ScotlandYardGame game = createGame(of(Black, 86), blue);
		// game is over with initial condition terminating setup
		assertThat(game.isGameOver()).isTrue();
	}

	@Test
	public void testWinningPlayerIsEmptyBeforeGameOver() {
		PlayerConfiguration black = of(Black, 86);
		PlayerConfiguration blue = of(Blue, 108);
		ScotlandYardGame game = createGame(black, blue);
		doAnswer(tryChoose(ticket(Black, Taxi, 104)))
				.when(black.player).makeMove(any(), anyInt(), any(), any());
		doAnswer(tryChoose(ticket(Blue, Bus, 105)))
				.when(blue.player).makeMove(any(), anyInt(), any(), any());
		game.startRotate();
		assertThat(game.isGameOver()).isFalse();
		// game is not over, no winning players
		assertThat(game.getWinningPlayers()).isEmpty();
	}

	@Test
	public void testWinningPlayerOnlyContainsBlackIfMrXWins() {
		PlayerConfiguration black = of(Black, 86);
		PlayerConfiguration blue = new Builder(Blue).using(mocked())
				.with(makeTickets(1, 0, 0, 0, 0))
				.at(108).build();

		ScotlandYardGame game = createGame(black, blue);
		doAnswer(tryChoose(ticket(Black, Taxi, 104)))
				.when(black.player).makeMove(any(), anyInt(), any(), any());
		doAnswer(tryChoose(ticket(Blue, Taxi, 105)))
				.when(blue.player).makeMove(any(), anyInt(), any(), any());
		game.startRotate();

		assertThat(game.isGameOver()).isTrue();
		// MrX won, should contain black only
		assertThat(game.getWinningPlayers()).containsOnly(Black);
	}

	@Test
	public void testWinningPlayerOnlyContainAllDetectivesIfDetectiveWins() {
		PlayerConfiguration black = of(Black, 86);
		PlayerConfiguration blue = of(Blue, 85);
		PlayerConfiguration red = of(Red, 111);
		ScotlandYardGame game = createGame(black, red, blue);
		doAnswer(tryChoose(ticket(Black, Taxi, 103)))
				.when(black.player).makeMove(any(), anyInt(), any(), any());
		// MrX captured at 103
		doAnswer(tryChoose(ticket(Blue, Taxi, 103)))
				.when(blue.player).makeMove(any(), anyInt(), any(), any());
		doAnswer(tryChoose(ticket(Red, Taxi, 112)))
				.when(red.player).makeMove(any(), anyInt(), any(), any());
		game.startRotate();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(game.isGameOver()).isTrue();
		// detectives won, should contain red and blue only
		softly.assertThat(game.getWinningPlayers()).containsOnly(Blue, Red);
		softly.assertAll();
	}

}
