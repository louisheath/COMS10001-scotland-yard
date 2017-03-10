package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Blue;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Green;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Red;
import static uk.ac.bris.cs.scotlandyard.model.Colour.White;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Yellow;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Double;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Secret;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Underground;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.assertj.core.api.SoftAssertions;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.stubbing.Answer;

import uk.ac.bris.cs.gamekit.graph.Graph;

/**
 * Base class for all tests. Contains various helper methods for convenience
 */
@RunWith(Parameterized.class)
public abstract class ModelTestBase implements ScotlandYardGameFactory {

	/**
	 * A list of game factories to use
	 */
	public static final List<Class<? extends ScotlandYardGameFactory>> FACTORIES = ModelFactories
			.factories();

	/**
	 * Default reveal rounds
	 */
	private static final Set<Integer> DEFAULT_REVEAL = unmodifiableSet(
			new HashSet<>(asList(3, 8, 13, 18, 23)));

	/**
	 * Default detective locations
	 */
	private static final List<Integer> DETECTIVE_LOCATIONS = unmodifiableList(
			asList(26, 29, 50, 53, 91, 94, 103, 112, 117, 123, 138, 141, 155, 174));

	/**
	 * Default Mr.X locations
	 */
	private static final List<Integer> MRX_LOCATIONS = unmodifiableList(
			asList(35, 45, 51, 71, 78, 104, 106, 127, 132, 166, 170, 172));

	@SuppressWarnings("WeakerAccess") @Parameter public ScotlandYardGameFactory factory;

	private static Graph<Integer, Transport> defaultGraph;

	@Parameters(name = "{0}")
	public static ScotlandYardGameFactory[] data() {
		return FACTORIES.stream().map(f -> {
			try {
				return f.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}).toArray(ScotlandYardGameFactory[]::new);
	}

	@BeforeClass
	public static void setUp() {
		try {
			defaultGraph = ScotlandYardGraphReader.fromLines(Files.readAllLines(
					Paths.get(ModelTestBase.class.getResource("/game_graph.txt").toURI())));

		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the default graph used in the actual game
	 * 
	 * @return the graph; never null
	 */
	static Graph<Integer, Transport> defaultGraph() {
		return defaultGraph;
	}

	/**
	 * Create a new configuration with colour, location and a mocked player
	 *
	 * The player will also have correct amount of tickets depending oh which
	 * colour they are. See {@link #detectiveTickets()} and
	 * {@link #mrXTickets()}
	 * 
	 * @param colour the colour of the player
	 * @param location the location the player is at
	 * @return a player configuration; never null
	 */
	static PlayerConfiguration of(Colour colour, int location) {
		Player mock = mocked();
		return new PlayerConfiguration.Builder(colour).using(mock)
				.with(colour == Colour.Black ? mrXTickets() : detectiveTickets()).at(location)
				.build();
	}

	/**
	 * The default amount of tickets for a detective, which is:
	 *
	 * Taxi = 11 <br>
	 * Bus = 8 <br>
	 * Underground = 4 <br>
	 * Double = 0 <br>
	 * Secret = 0 <br>
	 *
	 * @return a map with created using
	 *         {@link #makeTickets(int, int, int, int, int)}; never null
	 */
	static Map<Ticket, Integer> detectiveTickets() {
		return makeTickets(11, 8, 4, 0, 0);
	}

	/**
	 * The default amount of tickets for Mr.X, which is:
	 *
	 * Taxi = 4 <br>
	 * Bus = 3 <br>
	 * Underground = 3 <br>
	 * Double = 2 <br>
	 * Secret = 5 <br>
	 *
	 * @return a map with created using
	 *         {@link #makeTickets(int, int, int, int, int)}; never null
	 */
	static Map<Ticket, Integer> mrXTickets() {
		return makeTickets(4, 3, 3, 2, 5);
	}

	/**
	 * No tickets (zero for all kinds ticket)
	 * 
	 * @return a map with created using
	 *         {@link #makeTickets(int, int, int, int, int)}; never null
	 */
	static Map<Ticket, Integer> noTickets() {
		return makeTickets(0, 0, 0, 0, 0);
	}

	/**
	 * Create a map of tickets
	 * 
	 * @param taxi amount of tickets for {@link Ticket#Taxi}
	 * @param bus amount of tickets for {@link Ticket#Bus}
	 * @param underground amount of tickets for {@link Ticket#Underground}
	 * @param x2 amount of tickets for {@link Ticket#Double}
	 * @param secret amount of tickets for {@link Ticket#Secret}
	 * @return a {@link Map} with ticket counts; never null
	 */
	static Map<Ticket, Integer> makeTickets(int taxi, int bus, int underground, int x2,
			int secret) {
		Map<Ticket, Integer> map = new HashMap<>();
		map.put(Ticket.Taxi, taxi);
		map.put(Ticket.Bus, bus);
		map.put(Ticket.Underground, underground);
		map.put(Ticket.Double, x2);
		map.put(Ticket.Secret, secret);
		return map;
	}

	/**
	 * Creates a mocked player
	 * 
	 * @return mocked player; never null
	 */
	static Player mocked() {
		return mock(Player.class);
	}

	/**
	 * A common hand picked test location with small valid move(~60)
	 * 
	 * @return a valid {@link PlayerConfiguration}
	 */
	static PlayerConfiguration mrXAt45() {
		return of(Black, 45);
	}

	/**
	 * A common hand picked test location not far from MrX
	 * 
	 * @return a valid {@link PlayerConfiguration}
	 */
	static PlayerConfiguration blueAt94() {
		return of(Blue, 94);
	}

	/**
	 * A common hand picked test location not far from MrX
	 * 
	 * @return a valid {@link PlayerConfiguration}
	 */
	static PlayerConfiguration redAt111() {
		return of(Red, 111);
	}

	/**
	 * Asserts that all given tickets are valid or -1 to skip assertion
	 * 
	 * @param game the game to use
	 * @param colour the colour to assert
	 * @param taxi taxi ticket count or -1 to skip
	 * @param bus bus ticket count or -1 to skip
	 * @param underground underground ticket count or -1 to skip
	 * @param x2 x2 ticket count or -1 to skip
	 * @param secret secret ticket count or -1 to skip
	 */
	static void assertTickets(ScotlandYardGame game, Colour colour, int taxi, int bus,
			int underground, int x2, int secret) {
		if (taxi != -1) assertThat(game.getPlayerTickets(colour, Taxi)).isEqualTo(taxi);
		if (bus != -1) assertThat(game.getPlayerTickets(colour, Bus)).isEqualTo(bus);
		if (underground != -1)
			assertThat(game.getPlayerTickets(colour, Underground)).isEqualTo(underground);
		if (x2 != -1) assertThat(game.getPlayerTickets(colour, Double)).isEqualTo(x2);
		if (secret != -1) assertThat(game.getPlayerTickets(colour, Secret)).isEqualTo(secret);
	}

	ScotlandYardGame createValidGame() {
		return createGame(ofRounds(24, DEFAULT_REVEAL), defaultGraph, validMrX(), validRed(),
				validGreen(), validBlue(), validWhite(), validYellow());
	}

	@Override
	public ScotlandYardGame createGame(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {
		return factory.createGame(rounds, graph, mrX, firstDetective, restOfTheDetectives);
	}

	/**
	 * Creates a new game with 24 rounds and the default graph
	 * 
	 * @param mrX Mr.X
	 * @param firstDetective first detective
	 * @param restOfTheDetectives the rest of the detectives
	 * @return the created game; never null
	 */
	ScotlandYardGame createGame(PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {
		return createGame(ofRounds(24, DEFAULT_REVEAL), defaultGraph, mrX, firstDetective,
				restOfTheDetectives);
	}

	/**
	 * Creates a new game with given rounds and the default graph
	 * 
	 * @param rounds rounds
	 * @param mrX Mr.X
	 * @param firstDetective first detective
	 * @param restOfTheDetectives the rest of the detectives
	 * @return the created game; never null
	 */
	ScotlandYardGame createGame(List<Boolean> rounds, PlayerConfiguration mrX,
			PlayerConfiguration firstDetective, PlayerConfiguration... restOfTheDetectives) {
		return createGame(rounds, defaultGraph, mrX, firstDetective, restOfTheDetectives);
	}

	/**
	 * Returns a list of rounds
	 * 
	 * @param rounds the rounds
	 * @return a list; never null
	 */
	static List<Boolean> rounds(Boolean... rounds) {
		return asList(requireNonNull(rounds));
	}

	private static List<Boolean> ofRounds(int rounds, Collection<Integer> reveal) {
		return IntStream.range(1, rounds).mapToObj(reveal::contains).collect(toList());
	}

	/**
	 * Any given valid Mr.X configuration
	 *
	 * @return a valid {@link PlayerConfiguration}
	 */
	static PlayerConfiguration validMrX() {
		return of(Black, MRX_LOCATIONS.get(0));
	}

	/**
	 * Any given valid red configuration
	 *
	 * @return a valid {@link PlayerConfiguration}
	 */
	static PlayerConfiguration validRed() {
		return of(Red, DETECTIVE_LOCATIONS.get(0));
	}

	/**
	 * Any given valid green configuration
	 *
	 * @return a valid {@link PlayerConfiguration}
	 */
	static PlayerConfiguration validGreen() {
		return of(Green, DETECTIVE_LOCATIONS.get(1));
	}

	/**
	 * Any given valid blue configuration
	 *
	 * @return a valid {@link PlayerConfiguration}
	 */
	static PlayerConfiguration validBlue() {
		return of(Blue, DETECTIVE_LOCATIONS.get(2));
	}

	/**
	 * Any given valid yellow configuration
	 *
	 * @return a valid {@link PlayerConfiguration}
	 */
	static PlayerConfiguration validYellow() {
		return of(Yellow, DETECTIVE_LOCATIONS.get(3));
	}

	/**
	 * Any given valid white configuration
	 * 
	 * @return a valid {@link PlayerConfiguration}
	 */
	static PlayerConfiguration validWhite() {
		return of(White, DETECTIVE_LOCATIONS.get(4));
	}

	/**
	 * Keep calling {@link ScotlandYardGame#startRotate()} for the specified
	 * amount of time
	 * 
	 * @param game the game to use
	 * @param round the amount to time to start a round
	 */
	static void startRoundUntil(ScotlandYardGame game, int round) {
		for (int i = 0; i < round; i++)
			game.startRotate();
	}

	/**
	 * Creates a new {@link TicketMove}
	 * 
	 * @param colour the colour of the move
	 * @param ticket the ticket of the move
	 * @param destination the destination of move
	 * @return a new ticket move
	 */
	static TicketMove ticket(Colour colour, Ticket ticket, int destination) {
		return new TicketMove(requireNonNull(colour), requireNonNull(ticket), destination);
	}

	/**
	 * Creates a new {@link DoubleMove}
	 * 
	 * @param colour colour for the move
	 * @param first the first ticket
	 * @param firstDestination the first destination
	 * @param second the second ticket
	 * @param secondDestination the second destination
	 * @return a new double move
	 */
	static DoubleMove x2(Colour colour, Ticket first, int firstDestination, Ticket second,
			int secondDestination) {
		return new DoubleMove(requireNonNull(colour), requireNonNull(first), firstDestination,
				requireNonNull(second), secondDestination);
	}

	/**
	 * Creates a new {@link PassMove}
	 * 
	 * @param colour colour for the move
	 * @return a new pass move
	 */
	static PassMove pass(Colour colour) {
		return new PassMove(colour);
	}

	/**
	 * Force choosing a move for the
	 * {@link Player#makeMove(ScotlandYardView, int, Set, Consumer)} callback
	 * <br>
	 * 
	 * @param consumer represents the 2nd and 3rd argument of
	 *        {@link Player#makeMove(ScotlandYardView, int, Set, Consumer)}
	 * @return an answer to be used with
	 *         {@link org.mockito.Mockito#doAnswer(Answer)}
	 */
	static Answer<Void> choose(BiConsumer<Set<Move>, Consumer<Move>> consumer) {
		nonNull(consumer);
		return invocation -> {
			Set<Move> moves = invocation.getArgument(2);
			Consumer<Move> callback = invocation.getArgument(3);
			consumer.accept(moves, callback);
			return null;
		};
	}

	/**
	 * Try selecting a move for the
	 * {@link Player#makeMove(ScotlandYardView, int, Set, Consumer)} callback
	 * <br>
	 * An assertion error will be thrown if the supplied move is not in the
	 * callback
	 * 
	 * @param move the move to choose
	 * @return an answer to be used with
	 *         {@link org.mockito.Mockito#doAnswer(Answer)}
	 */
	static Answer<Void> tryChoose(Move move) {
		nonNull(move);
		return invocation -> {
			int location = invocation.getArgument(1);
			Set<Move> moves = invocation.getArgument(2);
			Consumer<Move> callback = invocation.getArgument(3);
			assertThat(moves).as("[Location %s] trying to select %s from given valid moves of %s",
					location, moves, move).contains(move);
			callback.accept(move);
			return null;
		};
	}

	/**
	 * Try selecting a move for the
	 * {@link Player#makeMove(ScotlandYardView, int, Set, Consumer)} callback
	 * <br>
	 * An assertion error will be thrown if no move is in the callback
	 *
	 * @return an answer to be used with
	 *         {@link org.mockito.Mockito#doAnswer(Answer)}
	 */
	static Answer<Void> chooseFirst() {
		return invocation -> {
			Set<Move> moves = invocation.getArgument(2);
			Consumer<Move> callback = invocation.getArgument(3);
			SoftAssertions softly = new SoftAssertions();
			softly.assertThat(moves).isNotEmpty();
			softly.assertThat(moves).doesNotContainNull();
			softly.assertAll();
			callback.accept(moves.iterator().next());
			return null;
		};
	}

	/**
	 * A spectator that delegates call to the consumer for the
	 * {@link Spectator#onMoveMade(ScotlandYardView, Move)} callback
	 *
	 * @param viewConsumer the consumer where calls will be delegated to
	 * @return an answer to be used with
	 *         {@link org.mockito.Mockito#doAnswer(Answer)}
	 */
	static Answer<Void> forSpectator(Consumer<ScotlandYardView> viewConsumer) {
		return invocation -> {
			viewConsumer.accept(invocation.getArgument(0));
			return null;
		};
	}

}
