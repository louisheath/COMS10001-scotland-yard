package uk.ac.bris.cs.oxo.standard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.bris.cs.oxo.Side.CROSS;
import static uk.ac.bris.cs.oxo.Side.NOUGHT;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import uk.ac.bris.cs.gamekit.matrix.SquareMatrix;
import uk.ac.bris.cs.oxo.Cell;
import uk.ac.bris.cs.oxo.Outcome;
import uk.ac.bris.cs.oxo.Player;
import uk.ac.bris.cs.oxo.Side;
import uk.ac.bris.cs.oxo.Spectator;

// a parameterised test that could test different implementations of the OXOGame
@RunWith(Parameterized.class)
public class OXOTest {

	// list of factories to use in this test
	private static final List<Class<? extends OXOGameFactory>> FACTORIES = ModelFactories
			.factories();

	@Parameter public OXOGameFactory factory;

	// instantiates implementations of the game
	@Parameters(name = "{0}")
	public static OXOGameFactory[] data() {
		List<OXOGameFactory> factories = new ArrayList<>();
		for (Class<? extends OXOGameFactory> clazz : FACTORIES) {
			try {
				factories.add(clazz.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return factories.toArray(new OXOGameFactory[factories.size()]);
	}

	// creation tests

	@Test(expected = IllegalArgumentException.class)
	public void testCreateZeroSizeGameThrows() throws Exception {
		// given a zero sized game, the creation of the model will throw
		factory.createGame(0, NOUGHT, mockPlayer(NOUGHT), mockPlayer(CROSS));
	}

	@Test(expected = NullPointerException.class)
	public void testCreateGameNullStartSideThrows() throws Exception {
		// given that the starting side is null, the creation of the model will
		// throw
		factory.createGame(3, null, mockPlayer(NOUGHT), mockPlayer(CROSS));
	}

	@Test(expected = NullPointerException.class)
	public void testCreateGameNullNoughtPlayerThrows() throws Exception {
		// given that the nought player is null, the creation of the model will
		// throw
		factory.createGame(3, null, null, mockPlayer(CROSS));
	}

	@Test(expected = NullPointerException.class)
	public void testCreateGameNullCrossPlayerThrows() throws Exception {
		// given that the cross player is null, the creation of the model will
		// throw
		factory.createGame(3, null, mockPlayer(NOUGHT), null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGameBoardIsImmutable() {
		// given a successfully created model, the board obtained with the
		// getter should not be mutable is any way as the integrity of the game
		// will be compromised
		OXOGame game = factory.createGame(3, NOUGHT, mockPlayer(NOUGHT), mockPlayer(CROSS));
		game.board().put(0, 0, new Cell(NOUGHT));
	}

	@Test
	public void testGameBoardIsAllEmptyBeforeStart() {
		/// given a successfully created model, the board is empty at first
		OXOGame game = factory.createGame(2, NOUGHT, mockPlayer(NOUGHT), mockPlayer(CROSS));
		assertThat(game.board()).isEqualTo(new SquareMatrix<>(2, new Cell()));
	}

	@Test
	public void testSizeRespected() {
		// given a successful creation of a 3*3 board, the board would have 3
		// rows and 3 columns
		OXOGame game = factory.createGame(3, NOUGHT, mockPlayer(NOUGHT), mockPlayer(CROSS));
		assertThat(game.board().columnSize()).isEqualTo(3);
		assertThat(game.board().rowSize()).isEqualTo(3);
	}

	// view tests

	@Test
	public void testStartingSideRespected() {
		// given nought and cross, and nought as the starting side, nought will
		// be asked to make a move first followed by cross
		Player nought = mockPlayer(NOUGHT);
		Player cross = mockPlayer(CROSS);
		OXOGame noughtFirst = factory.createGame(3, NOUGHT, nought, cross);
		noughtFirst.start();
		// verify that makeMove is called with a not null view, some set, and a
		// not null consumer
		verify(nought).makeMove(notNull(), anySet(), notNull());

		// given that cross as the starting side, cross will be asked to make a
		// move first
		OXOGame crossFirst = factory.createGame(3, CROSS, nought, cross);
		crossFirst.start();
		verify(cross).makeMove(notNull(), anySet(), notNull());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPlayerSideSwitchesAfterMove() {
		// given that the first player picked a move, the model will ask the
		// next player to make a move to continue the game
		Player nought = mockPlayer(NOUGHT);
		Player cross = mockPlayer(CROSS);

		// we pick a random move when nought has asked
		doAnswer(trySelectAnyMove())
				.when(nought).makeMove(notNull(), anySet(), notNull());

		// start the game to validate assertions
		OXOGame game = factory.createGame(3, NOUGHT, nought, cross);
		game.start();

		// given nought has picked a move, we expect cross to be asked next
		verify(cross).makeMove(notNull(), anySet(), notNull());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testValidMoveShouldBeTheEntireBoardForFirstMove() {
		// given that the first player is asked to pick a move, the player
		// should see the entire board as valid moves
		Player nought = mockPlayer(NOUGHT);
		Player cross = mockPlayer(CROSS);
		factory.createGame(3, NOUGHT, nought, cross).start();
		// capture moves when nought is asked to play for the first time as
		// nought was set as the first player to play
		ArgumentCaptor<Set<Move>> movesCaptor = ArgumentCaptor.forClass(Set.class);
		verify(nought).makeMove(any(), movesCaptor.capture(), any());
		// given moves were captured, we assert that it contains all moves
		assertThat(movesCaptor.getValue()).containsExactlyInAnyOrder(
				new Move(0, 0),
				new Move(0, 1),
				new Move(0, 2),
				new Move(1, 0),
				new Move(1, 1),
				new Move(1, 2),
				new Move(2, 0),
				new Move(2, 1),
				new Move(2, 2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testValidMoveShouldBeTheEntireBoardMinusOneForSecondMove() {
		// given that the second player is asked to pick a move, the player
		// should see the entire board minus one valid move(since it was already
		// chosen by the first player)
		Player nought = mockPlayer(NOUGHT);
		Player cross = mockPlayer(CROSS);

		// select (0,0) when nought has to make a move
		doAnswer(trySelect(new Move(0, 0))).when(nought).makeMove(notNull(), anySet(), notNull());

		factory.createGame(3, NOUGHT, nought, cross).start();
		// capture moves when cross is asked to play for the first time
		ArgumentCaptor<Set<Move>> movesCaptor = ArgumentCaptor.forClass(Set.class);
		verify(cross).makeMove(any(), movesCaptor.capture(), any());
		// assert that (0,0) is not part of the valid moves as it was used by
		// nought
		assertThat(movesCaptor.getValue()).containsExactlyInAnyOrder(
				new Move(0, 1),
				new Move(0, 2),
				new Move(1, 0),
				new Move(1, 1),
				new Move(1, 2),
				new Move(2, 0),
				new Move(2, 1),
				new Move(2, 2));
	}

	@Test
	public void testGameOverWhenMainDiagonalFormsLine() {
		// given that someone successfully occupies the main diagonal of the
		// board, the game is over with the correct winning side
		Player nought = mockPlayer(NOUGHT);
		Player cross = mockPlayer(CROSS);
		// cross select moves to occupy the main diagonal
		doAnswer(trySelect(new Move(0, 0)))
				.doAnswer(trySelect(new Move(1, 1)))
				.doAnswer(trySelect(new Move(2, 2)))
				.when(cross).makeMove(notNull(), anySet(), notNull());
		// nought select moves that does nothing in particular
		doAnswer(trySelect(new Move(0, 1)))
				.doAnswer(trySelect(new Move(1, 2)))
				.when(nought).makeMove(notNull(), anySet(), notNull());
		OXOGame game = factory.createGame(3, CROSS, nought, cross);
		// given a spectator that is registered to the game
		Spectator mock = mock(Spectator.class);
		game.registerSpectators(mock);
		game.start();
		// the gameOver method of the spectator should be called with the
		// correct winning player
		verify(mock).gameOver(eq(new Outcome(CROSS)));
		// end game:
		// |X|O| |
		// | |X|O|
		// | | |X|
	}

	@Test
	public void testGameOverWhenAntiDiagonalFormsLine() {
		// given that someone successfully occupies the anti-diagonal of the
		// board, the game is over with the correct winning side
		Player nought = mockPlayer(NOUGHT);
		Player cross = mockPlayer(CROSS);
		// cross select moves to occupy the anti diagonal
		doAnswer(trySelect(new Move(0, 2)))
				.doAnswer(trySelect(new Move(1, 1)))
				.doAnswer(trySelect(new Move(2, 0)))
				.when(cross).makeMove(notNull(), anySet(), notNull());
		// nought select moves that does nothing in particular
		doAnswer(trySelect(new Move(0, 1)))
				.doAnswer(trySelect(new Move(2, 1)))
				.when(nought).makeMove(notNull(), anySet(), notNull());
		OXOGame game = factory.createGame(3, CROSS, nought, cross);
		// given a spectator that is registered to the game
		Spectator mock = mock(Spectator.class);
		game.registerSpectators(mock);
		game.start();
		// the gameOver method of the spectator should be called with the
		// correct winning player
		verify(mock).gameOver(eq(new Outcome(CROSS)));
		// end game:
		// | |O|X|
		// | |X| |
		// |X|O| |
	}

	@Test
	public void testGameOverWhenColumnFormsLine() {
		// given that someone successfully occupies a vertical line of the
		// board, the game is over with the correct winning side
		Player nought = mockPlayer(NOUGHT);
		Player cross = mockPlayer(CROSS);
		// nought select moves that does nothing in particular
		doAnswer(trySelect(new Move(0, 1)))
				.doAnswer(trySelect(new Move(1, 1)))
				.doAnswer(trySelect(new Move(2, 1)))
				.when(nought).makeMove(notNull(), anySet(), notNull());
		// cross select moves to occupy a vertical line
		doAnswer(trySelect(new Move(0, 2)))
				.doAnswer(trySelect(new Move(2, 0)))
				.when(cross).makeMove(notNull(), anySet(), notNull());
		OXOGame game = factory.createGame(3, NOUGHT, nought, cross);
		Spectator mock = mock(Spectator.class);
		game.registerSpectators(mock);
		game.start();
		verify(mock).gameOver(eq(new Outcome(NOUGHT)));
		// end game:
		// | |O|X|
		// | |O| |
		// |X|O| |
	}

	@Test
	public void testGameOverWhenRowFormsLine() {
		// given that someone successfully occupies a horizontal line of the
		// board, the game is over with the correct winning side
		Player nought = mockPlayer(NOUGHT);
		Player cross = mockPlayer(CROSS);
		// nought select moves that does nothing in particular
		doAnswer(trySelect(new Move(1, 0)))
				.doAnswer(trySelect(new Move(1, 1)))
				.doAnswer(trySelect(new Move(1, 2)))
				.when(nought).makeMove(notNull(), anySet(), notNull());
		// cross select moves to occupy a horizontal line
		doAnswer(trySelect(new Move(0, 2)))
				.doAnswer(trySelect(new Move(2, 0)))
				.when(cross).makeMove(notNull(), anySet(), notNull());
		OXOGame game = factory.createGame(3, NOUGHT, nought, cross);
		Spectator mock = mock(Spectator.class);
		game.registerSpectators(mock);
		game.start();
		verify(mock).gameOver(eq(new Outcome(NOUGHT)));
		// end game:
		// | | |X|
		// |O|O|O|
		// |X| | |
	}

	@Test
	public void testGameOverWhenBoardFull() {
		// given that the board is filled, the game is over and no one wins
		Player nought = mockPlayer(NOUGHT);
		Player cross = mockPlayer(CROSS);
		// nought select moves that does nothing in particular
		doAnswer(trySelect(new Move(2, 1)))
				.doAnswer(trySelect(new Move(1, 0)))
				.doAnswer(trySelect(new Move(0, 2)))
				.doAnswer(trySelect(new Move(0, 0)))
				.when(nought).makeMove(notNull(), anySet(), notNull());
		// cross select moves that does nothing in particular
		doAnswer(trySelect(new Move(1, 1)))
				.doAnswer(trySelect(new Move(1, 2)))
				.doAnswer(trySelect(new Move(0, 1)))
				.doAnswer(trySelect(new Move(2, 0)))
				.doAnswer(trySelect(new Move(2, 2)))
				.when(cross).makeMove(notNull(), anySet(), notNull());
		OXOGame game = factory.createGame(3, CROSS, nought, cross);
		Spectator mock = mock(Spectator.class);
		game.registerSpectators(mock);
		game.start();
		// no winning player
		verify(mock).gameOver(eq(new Outcome()));
		// end game:
		// |O|X|O|
		// |O|X|X|
		// |X|O|X|
	}

	// creates a fake player that returns the correct side
	private static Player mockPlayer(Side side) {
		Player nought = mock(Player.class);
		when(nought.side()).thenReturn(side);
		return nought;
	}

	/**
	 * Attempts to select an arbitrary move
	 *
	 * @return an answer; never null
	 */
	private static Answer trySelectAnyMove() {
		return trySelect(new BiConsumer<Set<Move>, Consumer<Move>>() {
			@Override
			public void accept(Set<Move> moves, Consumer<Move> callback) {
				if (moves.isEmpty()) throw new AssertionError("No moves to select from");
				callback.accept(moves.iterator().next());
			}
		});
	}

	/**
	 * Attempts to select an arbitrary move
	 * 
	 * @param move the move to select
	 * @return an answer; never null
	 */
	private static Answer trySelect(Move move) {
		return trySelect(new BiConsumer<Set<Move>, Consumer<Move>>() {
			@Override
			public void accept(Set<Move> moves, Consumer<Move> callback) {
				if (!moves.contains(move)) throw new AssertionError(
						"Expected " + move +
								" to be in the list of available moves, but was "
								+ moves);
				callback.accept(move);
			}
		});
	}

	/**
	 * Attempts to select a move from a set of provided moves
	 * 
	 * @param consumer the callback to handle moves
	 * @return an answer for mockito; never null
	 */
	private static Answer trySelect(BiConsumer<Set<Move>, Consumer<Move>> consumer) {
		return new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				// @formatter:off
				// as in  Player#makeMove(OXOView, Set<Move>, Consumer<Move>)
				//                           ▲        ▲           ▲
				//                           |        |           |
				//                           #0       #1          #2
				// @formatter:on
				Set<Move> moves = invocation.getArgument(1);
				Consumer<Move> callback = invocation.getArgument(2);
				consumer.accept(moves, callback);
				return null;
			}
		};
	}

}