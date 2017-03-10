package uk.ac.bris.cs.scotlandyard.model;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;

import org.junit.Test;

/**
 * Tests whether the visitor methods work in moves
 */
public class MoveTest {

	@Test
	public void testVisitDoubleMove() {
		DoubleMove move = new DoubleMove( //
				Black, //
				mock(TicketMove.class), //
				mock(TicketMove.class) //
		);
		MoveVisitor visitor = mock(MoveVisitor.class);
		move.visit(visitor);
		verify(visitor).visit(eq(move));
	}

	@Test
	public void testVisitTicketMove() {
		TicketMove move = new TicketMove(Black, Taxi, 42);
		MoveVisitor visitor = mock(MoveVisitor.class);
		move.visit(visitor);
		verify(visitor).visit(eq(move));
	}

	@Test
	public void testVisitPassMove() {
		PassMove move = new PassMove(Black);
		MoveVisitor visitor = mock(MoveVisitor.class);
		move.visit(visitor);
		verify(visitor).visit(eq(move));
	}

}
