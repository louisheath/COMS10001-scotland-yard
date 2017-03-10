package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Blue;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Green;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Red;
import static uk.ac.bris.cs.scotlandyard.model.Colour.White;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Yellow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.gamekit.graph.UndirectedGraph;
import uk.ac.bris.cs.scotlandyard.model.PlayerConfiguration.Builder;

/**
 * Tests model initialisation and constructor
 *
 * Note that exception message testing is intentionally left out as throwing the
 * correct exception type is enough to proof the given model is designed to spec
 */

public class ModelCreationTest extends ModelTestBase {

	@Test(expected = NullPointerException.class)
	public void testNullMrXShouldThrow() {
		createGame(
				null,
				of(Red, 1));
	}

	@Test(expected = NullPointerException.class)
	public void testNullDetectiveShouldThrow() {
		createGame(
				of(Black, 1),
				null);
	}

	@Test(expected = NullPointerException.class)
	public void testAnyNullDetectiveShouldThrow() {
		createGame(
				of(Black, 1),
				of(Blue, 2),
				(PlayerConfiguration) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoMrXShouldThrow() {
		createGame(
				of(Blue, 1),
				of(Red, 2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMoreThanOneMrXShouldThrow() {
		createGame(
				of(Black, 1),
				of(Black, 2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSwappedMrXShouldThrow() {
		createGame(
				of(Blue, 1),
				of(Black, 2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDuplicateDetectivesShouldThrow() {
		createGame(
				of(Black, 1),
				of(Blue, 2),
				of(Blue, 2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLocationOverlapBetweenMrXAndDetectiveShouldThrow() {
		createGame(
				of(Black, 1),
				of(Blue, 1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLocationOverlapBetweenDetectivesShouldThrow() {
		createGame(
				of(Black, 1),
				of(Blue, 2),
				of(Blue, 2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDetectiveHaveSecretTicketShouldThrow() {
		PlayerConfiguration blue = new Builder(Blue).using(mocked())
				.with(makeTickets(1, 1, 1, 0, 1))
				.at(2).build();
		createGame(validMrX(), blue);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDetectiveHaveDoubleTicketShouldThrow() {
		PlayerConfiguration blue = new Builder(Blue).using(mocked())
				.with(makeTickets(1, 1, 0, 1, 1))
				.at(2).build();
		createGame(validMrX(), blue);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMrXMissingAnyTicketsShouldThrow() {
		Map<Ticket, Integer> tickets = new HashMap<>();
		tickets.put(Ticket.Taxi, 1);
		PlayerConfiguration blue = new Builder(Blue).using(mocked())
				.with(tickets).at(2).build();
		createGame(validMrX(), blue);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDetectiveMissingAnyTicketsShouldTrow() {
		Map<Ticket, Integer> tickets = new HashMap<>();
		tickets.put(Ticket.Secret, 1);
		tickets.put(Ticket.Double, 1);
		PlayerConfiguration black = new Builder(Black).using(mocked())
				.with(tickets).at(2).build();
		createGame(black, validBlue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyRoundsShouldThrow() {
		createGame(
				emptyList(),
				defaultGraph(),
				of(Black, 1),
				of(Blue, 1));
	}

	@Test(expected = NullPointerException.class)
	public void testNullRoundsShouldThrow() {
		createGame(
				null,
				defaultGraph(),
				of(Black, 1),
				of(Blue, 1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyMapShouldThrow() {
		createGame(
				emptyList(),
				new UndirectedGraph<>(),
				of(Black, 1),
				of(Blue, 1));
	}

	@Test(expected = NullPointerException.class)
	public void testNullMapShouldThrow() {
		createGame(
				rounds(true),
				(Graph<Integer, Transport>) null,
				of(Black, 1),
				of(Blue, 1));
	}

	@Test
	public void testTwoPlayer() {
		createGame(
				validMrX(),
				validBlue());
	}

	@Test
	public void testFivePlayer() {
		createGame(
				validMrX(),
				validRed(),
				validGreen(),
				validBlue(),
				validWhite(),
				validYellow());
	}

	@Test
	public void testGetRoundsMatchesSupplied() throws Exception {
		ScotlandYardGame game = createGame(
				asList(true, false, true, true),
				defaultGraph(),
				validMrX(),
				validBlue());
		assertThat(game.getRounds()).containsExactly(true, false, true, true);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetRoundsIsImmutable() throws Exception {
		ScotlandYardGame game = createGame(
				new ArrayList<>(asList(true, false)),
				defaultGraph(),
				validMrX(),
				validBlue());
		game.getRounds().add(true);
	}

	@Test
	public void testGetGraphMatchesSupplied() throws Exception {
		ScotlandYardGame game = createGame(
				asList(true, false),
				defaultGraph(),
				validMrX(),
				validBlue());
		assertThat(game.getGraph()).isEqualTo(defaultGraph());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetGraphIsImmutable() throws Exception {
		ScotlandYardGame game = createGame(
				asList(true, false),
				new UndirectedGraph<>(defaultGraph()),
				validMrX(),
				validBlue());
		game.getGraph().addNode(new Node<>(500));
	}

	@Test
	public void testGetPlayersMatchesSupplied() throws Exception {
		ScotlandYardGame game = createGame(
				asList(true, false, true, false),
				defaultGraph(),
				validMrX(),
				validRed(),
				validGreen(),
				validBlue());
		assertThat(game.getPlayers()).containsExactly(Black, Red, Green, Blue);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetPlayersIsImmutable() throws Exception {
		createValidGame().getPlayers().add(Red);
	}

	@Test
	public void testGameIsNotOverInitially() throws Exception {
		assertThat(createValidGame().isGameOver()).isFalse();
	}

	@Test
	public void testWinningPlayerIsEmptyInitially() throws Exception {
		assertThat(createValidGame().getWinningPlayers()).isEmpty();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testWinningPlayerIsImmutable() throws Exception {
		createValidGame().getWinningPlayers().add(Black);
	}

	@Test
	public void testGetRoundIsNOT_STARTEDInitially() {
		assertThat(createValidGame().getCurrentRound()).isEqualTo(ScotlandYardGame.NOT_STARTED);
	}

	@Test
	public void testGetPlayerIsMrXInitially() {
		assertThat(createValidGame().getCurrentPlayer()).isEqualTo(Black);
	}

	@Test
	public void testGetPlayersStartsWithBlack() throws Exception {
		ScotlandYardGame game = createGame(
				asList(true, false, true, false),
				defaultGraph(),
				validMrX(),
				validRed(),
				validGreen(),
				validBlue());
		assertThat(game.getPlayers()).startsWith(Black);
	}

	@Test
	public void testGetDetectiveLocationMatchesSupplied() {
		ScotlandYardGame game = createGame(
				asList(false, false, false),
				defaultGraph(),
				of(Black, 35),
				of(Red, 26),
				of(Blue, 50),
				of(Green, 94),
				of(White, 155),
				of(Yellow, 174));
		assertThat(game.getPlayerLocation(Red)).isEqualTo(26);
		assertThat(game.getPlayerLocation(Blue)).isEqualTo(50);
		assertThat(game.getPlayerLocation(Green)).isEqualTo(94);
		assertThat(game.getPlayerLocation(White)).isEqualTo(155);
		assertThat(game.getPlayerLocation(Yellow)).isEqualTo(174);
	}

	@Test
	public void testGetPlayerLocationConcealsMrXLocationInitially() throws Exception {
		ScotlandYardGame game = createGame(
				asList(false, false, false),
				defaultGraph(),
				of(Black, 35),
				of(Red, 26));
		assertThat(game.getPlayerLocation(Black)).isEqualTo(0);
	}

	@Test
	public void testGetPlayerTicketsMatchesSupplied() {
		PlayerConfiguration mrX = new Builder(Black).using(mocked())
				.with(makeTickets(1, 2, 3, 4, 5)).at(1).build();

		PlayerConfiguration blue = new Builder(Blue).using(mocked())
				.with(makeTickets(5, 4, 3, 0, 0)).at(2).build();
		ScotlandYardGame game = createGame(mrX, blue);
		assertTickets(game, Black, 1, 2, 3, 4, 5);
		assertTickets(game, Blue, 5, 4, 3, 0, 0);
	}

}
