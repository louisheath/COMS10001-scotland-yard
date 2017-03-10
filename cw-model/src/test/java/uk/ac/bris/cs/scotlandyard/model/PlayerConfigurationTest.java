package uk.ac.bris.cs.scotlandyard.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import uk.ac.bris.cs.scotlandyard.model.PlayerConfiguration.Builder;

/**
 * Tests for {@link PlayerConfiguration}
 */
public class PlayerConfigurationTest extends ModelTestBase {

	@Test
	public void testProducesCorrectOutput() throws Exception {
		Player player = mocked();
		PlayerConfiguration configuration = new Builder(Colour.Black).using(player)
				.with(mrXTickets()).at(10).build();
		assertThat(configuration.colour).isEqualTo(Colour.Black);
		assertThat(configuration.player).isSameAs(player);
		assertThat(configuration.tickets).isEqualTo(mrXTickets());
		assertThat(configuration.location).isEqualTo(10);
	}

	@Test(expected = NullPointerException.class)
	public void testNullColourThrows() throws Exception {
		new PlayerConfiguration.Builder(null);
	}

	@Test(expected = NullPointerException.class)
	public void testMissingPlayerThrows() throws Exception {
		new PlayerConfiguration.Builder(Colour.Black).with(mrXTickets()).build();
	}

	@Test(expected = NullPointerException.class)
	public void testMissingTicketThrows() throws Exception {
		new PlayerConfiguration.Builder(Colour.Black).using(mocked()).build();
	}

	@Test(expected = NullPointerException.class)
	public void testNullPlayerThrows() throws Exception {
		new PlayerConfiguration.Builder(Colour.Black).using(null).with(mrXTickets()).build();
	}

	@Test(expected = NullPointerException.class)
	public void testNullTicketThrows() throws Exception {
		new PlayerConfiguration.Builder(Colour.Black).using(mocked()).with(null).build();
	}

}
