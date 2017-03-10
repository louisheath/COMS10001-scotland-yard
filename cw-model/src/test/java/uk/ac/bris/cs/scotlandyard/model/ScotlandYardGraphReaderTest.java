package uk.ac.bris.cs.scotlandyard.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;

/**
 * Tests for {@link ScotlandYardGraphReader}
 */
public class ScotlandYardGraphReaderTest {

	@Test
	public void testValidFile() {
		ImmutableGraph<Integer, Transport> graph = ScotlandYardGraphReader
				.fromLines(Arrays.asList("3 1", "1", "2", "3", "1 2 Boat"));

		assertTrue(graph.getNodes().size() == 3);
		assertTrue((graph.getEdges().size() == 2));
		assertNotNull(graph.getNode(1));
		assertTrue(graph.getEdges().iterator().next().data() == Transport.Boat);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyInputShouldThrow() {
		ScotlandYardGraphReader.fromLines(Collections.emptyList());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadFirstLine() {
		ScotlandYardGraphReader.fromLines(Collections.singletonList("Foo Bar Baz"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadNodeCount() {
		ScotlandYardGraphReader.fromLines(Arrays.asList("4 1", "1", "2", "3", "1 2 Boat"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadEdgeCount() {
		ScotlandYardGraphReader.fromLines(Arrays.asList("3 5", "1", "2", "3", "1 2 Boat"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadNode() {
		ScotlandYardGraphReader.fromLines(Arrays.asList("1 0", "Foo"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadEdge() {
		ScotlandYardGraphReader.fromLines(Arrays.asList("2 1", "1", "2", "Foo Bar Baz"));
	}

}