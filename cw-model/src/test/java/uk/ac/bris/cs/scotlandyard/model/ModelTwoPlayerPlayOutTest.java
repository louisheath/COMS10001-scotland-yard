package uk.ac.bris.cs.scotlandyard.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.doAnswer;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Blue;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Secret;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;

import org.junit.Test;

/**
 * Test the model with real recorded games that was carried out by hand using a
 * fair dice to decide moves
 */
@SuppressWarnings("unchecked")
public class ModelTwoPlayerPlayOutTest extends ModelTestBase {

	@Test
	public void testMrXWon() throws Exception {
		PlayerConfiguration black = of(Black, 172);
		PlayerConfiguration blue = of(Blue, 141);
		ScotlandYardGame game = createGame(black, blue);
		doAnswer(tryChoose(x2(Black, Secret, 187, Secret, 188)))
				.doAnswer(tryChoose(x2(Black, Taxi, 128, Bus, 199)))
				.doAnswer(tryChoose(ticket(Black, Secret, 161)))
				.doAnswer(tryChoose(ticket(Black, Secret, 135)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 136)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 162)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 175)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 174)))
				.doAnswer(tryChoose(ticket(Black, Secret, 175)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 174)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 173)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 171)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 173))).when(black.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());
		doAnswer(tryChoose(ticket(Blue, Taxi, 142))).doAnswer(tryChoose(ticket(Blue, Taxi, 128)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 188)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 128)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 135)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 136)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 119)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 136)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 119)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 107)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 161)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 174)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 175))).when(blue.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());

		startRoundUntil(game, 13);

		assertThat(game.isGameOver()).isTrue();
		// black won at round 13
		assertThat(game.getWinningPlayers()).containsOnly(Black);
	}

	@Test
	public void testDetectiveWon() throws Exception {
		PlayerConfiguration black = of(Black, 104);
		PlayerConfiguration blue = of(Blue, 53);
		ScotlandYardGame game = createGame(black, blue);
		doAnswer(tryChoose(x2(Black, Taxi, 116, Bus, 108)))
				.doAnswer(tryChoose(x2(Black, Bus, 116, Secret, 104)))
				.doAnswer(tryChoose(ticket(Black, Secret, 116))).when(black.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());
		doAnswer(tryChoose(ticket(Blue, Taxi, 69))).doAnswer(tryChoose(ticket(Blue, Taxi, 86)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 116))).when(blue.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());

		startRoundUntil(game, 3);

		assertThat(game.isGameOver()).isTrue();
		// detectives won at round 3
		assertThat(game.getWinningPlayers()).containsOnly(Blue);
	}

}
