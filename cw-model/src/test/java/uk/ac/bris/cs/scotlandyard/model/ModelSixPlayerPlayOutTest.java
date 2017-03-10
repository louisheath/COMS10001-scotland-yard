package uk.ac.bris.cs.scotlandyard.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.doAnswer;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Blue;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Green;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Red;
import static uk.ac.bris.cs.scotlandyard.model.Colour.White;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Yellow;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Secret;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Underground;

import org.junit.Test;

/**
 * Test the model with real recorded games that was carried out by hand using a
 * fair dice to decide moves
 */
@SuppressWarnings("unchecked")
public class ModelSixPlayerPlayOutTest extends ModelTestBase {

	@Test
	public void testDetectiveWon() throws Exception {
		PlayerConfiguration black = of(Black, 127);
		PlayerConfiguration blue = of(Blue, 53);
		PlayerConfiguration green = of(Green, 94);
		PlayerConfiguration red = of(Red, 155);
		PlayerConfiguration white = of(White, 29);
		PlayerConfiguration yellow = of(Yellow, 123);
		ScotlandYardGame game = createGame(black, blue, green, red, white, yellow);
		doAnswer(tryChoose(x2(Black, Secret, 116, Secret, 127)))
				.doAnswer(tryChoose(x2(Black, Bus, 133, Secret, 141)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 158)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 142)))
				.doAnswer(tryChoose(ticket(Black, Bus, 157)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 170)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 159)))
				.doAnswer(tryChoose(ticket(Black, Secret, 172)))
				.doAnswer(tryChoose(ticket(Black, Secret, 187)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 172)))
				.doAnswer(tryChoose(ticket(Black, Taxi, 128))).when(black.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());
		doAnswer(tryChoose(ticket(Blue, Taxi, 69))).doAnswer(tryChoose(ticket(Blue, Taxi, 86)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 116)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 127)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 133)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 157)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 133)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 140)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 156)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 140)))
				.doAnswer(tryChoose(ticket(Blue, Underground, 128))).when(blue.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());
		doAnswer(tryChoose(ticket(Green, Bus, 77))).doAnswer(tryChoose(ticket(Green, Taxi, 78)))
				.doAnswer(tryChoose(ticket(Green, Bus, 79)))
				.doAnswer(tryChoose(ticket(Green, Underground, 67)))
				.doAnswer(tryChoose(ticket(Green, Underground, 89)))
				.doAnswer(tryChoose(ticket(Green, Underground, 128)))
				.doAnswer(tryChoose(ticket(Green, Bus, 187)))
				.doAnswer(tryChoose(ticket(Green, Bus, 185)))
				.doAnswer(tryChoose(ticket(Green, Taxi, 170)))
				.doAnswer(tryChoose(ticket(Green, Taxi, 159))).when(green.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());
		doAnswer(tryChoose(ticket(Red, Taxi, 156))).doAnswer(tryChoose(ticket(Red, Taxi, 140)))
				.doAnswer(tryChoose(ticket(Red, Bus, 133)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 141)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 158)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 142)))
				.doAnswer(tryChoose(ticket(Red, Bus, 157)))
				.doAnswer(tryChoose(ticket(Red, Bus, 133)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 141)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 158))).when(red.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());
		doAnswer(tryChoose(ticket(White, Taxi, 42))).doAnswer(tryChoose(ticket(White, Taxi, 72)))
				.doAnswer(tryChoose(ticket(White, Bus, 105)))
				.doAnswer(tryChoose(ticket(White, Taxi, 108)))
				.doAnswer(tryChoose(ticket(White, Taxi, 119)))
				.doAnswer(tryChoose(ticket(White, Taxi, 108)))
				.doAnswer(tryChoose(ticket(White, Taxi, 119)))
				.doAnswer(tryChoose(ticket(White, Taxi, 108)))
				.doAnswer(tryChoose(ticket(White, Taxi, 119)))
				.doAnswer(tryChoose(ticket(White, Taxi, 136))).when(white.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());
		doAnswer(tryChoose(ticket(Yellow, Taxi, 137)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 123)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 124)))
				.doAnswer(tryChoose(ticket(Yellow, Bus, 111)))
				.doAnswer(tryChoose(ticket(Yellow, Underground, 153)))
				.doAnswer(tryChoose(ticket(Yellow, Underground, 185)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 170)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 157)))
				.doAnswer(tryChoose(ticket(Yellow, Bus, 185)))
				.doAnswer(tryChoose(ticket(Yellow, Bus, 187))).when(yellow.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());

		startRoundUntil(game, 11);

		assertThat(game.isGameOver()).isTrue();
		// detectives won at round 11
		assertThat(game.getWinningPlayers()).containsOnly(Green, Yellow, White, Red, Blue);
	}

	@Test
	public void testMrXWon() throws Exception {

		PlayerConfiguration black = of(Black, 45);
		PlayerConfiguration blue = of(Blue, 91);
		PlayerConfiguration green = of(Green, 112);
		PlayerConfiguration red = of(Red, 103);
		PlayerConfiguration white = of(White, 155);
		PlayerConfiguration yellow = of(Yellow, 117);
		ScotlandYardGame game = createGame(black, blue, green, red, white, yellow);
		doAnswer(tryChoose(ticket(Black, Taxi, 46)))
				.doAnswer(tryChoose(x2(Black, Taxi, 45, Secret, 58)))
				.doAnswer(tryChoose(x2(Black, Taxi, 45, Secret, 58)))
				.doAnswer(tryChoose(ticket(Black, Secret, 77)))
				.doAnswer(tryChoose(ticket(Black, Secret, 58)))
				.doAnswer(tryChoose(ticket(Black, Secret, 77)))
				.doAnswer(tryChoose(ticket(Black, Bus, 58)))
				.doAnswer(tryChoose(ticket(Black, Bus, 46)))
				.doAnswer(tryChoose(ticket(Black, Bus, 58)))
				.doAnswer(tryChoose(ticket(Black, Bus, 46)))
				.doAnswer(tryChoose(ticket(Black, Bus, 58)))
				.doAnswer(tryChoose(ticket(Black, Bus, 46)))
				.doAnswer(tryChoose(ticket(Black, Underground, 74)))
				.doAnswer(tryChoose(ticket(Black, Underground, 46)))
				.doAnswer(tryChoose(ticket(Black, Underground, 74)))
				.doAnswer(tryChoose(ticket(Black, Bus, 94)))
				.doAnswer(tryChoose(ticket(Black, Bus, 77)))
				.doAnswer(tryChoose(ticket(Black, Bus, 58)))
				.doAnswer(tryChoose(ticket(Black, Bus, 46))).when(black.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());
		doAnswer(tryChoose(ticket(Blue, Taxi, 90))).doAnswer(tryChoose(ticket(Blue, Taxi, 72)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 90)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 105)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 89)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 71)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 70)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 54)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 70)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 54)))
				.doAnswer(tryChoose(ticket(Blue, Taxi, 41)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 29)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 41)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 87)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 41)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 29)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 15)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 41)))
				.doAnswer(tryChoose(ticket(Blue, Bus, 15))).when(blue.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());
		doAnswer(tryChoose(ticket(Green, Taxi, 125))).doAnswer(tryChoose(ticket(Green, Taxi, 131)))
				.doAnswer(tryChoose(ticket(Green, Taxi, 114)))
				.doAnswer(tryChoose(ticket(Green, Taxi, 131)))
				.doAnswer(tryChoose(ticket(Green, Taxi, 125)))
				.doAnswer(tryChoose(ticket(Green, Taxi, 113)))
				.doAnswer(tryChoose(ticket(Green, Taxi, 100)))
				.doAnswer(tryChoose(ticket(Green, Taxi, 113)))
				.doAnswer(tryChoose(ticket(Green, Taxi, 100)))
				.doAnswer(tryChoose(ticket(Green, Bus, 82)))
				.doAnswer(tryChoose(ticket(Green, Bus, 100)))
				.doAnswer(tryChoose(ticket(Green, Bus, 63)))
				.doAnswer(tryChoose(ticket(Green, Taxi, 64)))
				.doAnswer(tryChoose(ticket(Green, Taxi, 81))).doAnswer(tryChoose(pass(Green)))
				.doAnswer(tryChoose(pass(Green))).doAnswer(tryChoose(pass(Green)))
				.doAnswer(tryChoose(pass(Green))).doAnswer(tryChoose(pass(Green)))
				.when(green.player).makeMove(any(), anyInt(), anySet(), isNotNull());
		doAnswer(tryChoose(ticket(Red, Taxi, 86))).doAnswer(tryChoose(ticket(Red, Taxi, 103)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 86)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 104)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 116)))
				.doAnswer(tryChoose(ticket(Red, Bus, 142)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 141)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 133)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 127)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 126)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 140)))
				.doAnswer(tryChoose(ticket(Red, Taxi, 139))).doAnswer(tryChoose(pass(Red)))
				.doAnswer(tryChoose(pass(Red))).doAnswer(tryChoose(pass(Red)))
				.doAnswer(tryChoose(pass(Red))).doAnswer(tryChoose(pass(Red)))
				.doAnswer(tryChoose(pass(Red))).doAnswer(tryChoose(pass(Red))).when(red.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());
		doAnswer(tryChoose(ticket(White, Taxi, 167))).doAnswer(tryChoose(ticket(White, Taxi, 168)))
				.doAnswer(tryChoose(ticket(White, Taxi, 155)))
				.doAnswer(tryChoose(ticket(White, Taxi, 167)))
				.doAnswer(tryChoose(ticket(White, Taxi, 183)))
				.doAnswer(tryChoose(ticket(White, Taxi, 166)))
				.doAnswer(tryChoose(ticket(White, Taxi, 183)))
				.doAnswer(tryChoose(ticket(White, Taxi, 166)))
				.doAnswer(tryChoose(ticket(White, Taxi, 183)))
				.doAnswer(tryChoose(ticket(White, Taxi, 196)))
				.doAnswer(tryChoose(ticket(White, Taxi, 184)))
				.doAnswer(tryChoose(ticket(White, Bus, 185)))
				.doAnswer(tryChoose(ticket(White, Bus, 187)))
				.doAnswer(tryChoose(ticket(White, Bus, 185)))
				.doAnswer(tryChoose(ticket(White, Bus, 184)))
				.doAnswer(tryChoose(ticket(White, Bus, 153)))
				.doAnswer(tryChoose(ticket(White, Bus, 184)))
				.doAnswer(tryChoose(ticket(White, Bus, 156)))
				.doAnswer(tryChoose(ticket(White, Bus, 157))).when(white.player)
				.makeMove(any(), anyInt(), anySet(), isNotNull());
		doAnswer(tryChoose(ticket(Yellow, Taxi, 129)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 117)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 116)))
				.doAnswer(tryChoose(ticket(Yellow, Bus, 86)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 69)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 52)))
				.doAnswer(tryChoose(ticket(Yellow, Bus, 41)))
				.doAnswer(tryChoose(ticket(Yellow, Bus, 87)))
				.doAnswer(tryChoose(ticket(Yellow, Bus, 41)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 40)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 52)))
				.doAnswer(tryChoose(ticket(Yellow, Bus, 67)))
				.doAnswer(tryChoose(ticket(Yellow, Bus, 82)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 101)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 114)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 131)))
				.doAnswer(tryChoose(ticket(Yellow, Taxi, 114))).doAnswer(tryChoose(pass(Yellow)))
				.when(yellow.player).makeMove(any(), anyInt(), anySet(), isNotNull());

		startRoundUntil(game, 19);

		assertThat(game.isGameOver()).isTrue();
		// black won at round 19
		assertThat(game.getWinningPlayers()).containsOnly(Black);
	}

}
