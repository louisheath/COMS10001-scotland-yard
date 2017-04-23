package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Collections;
import java.util.Collection;
import java.util.List;

import uk.ac.bris.cs.scotlandyard.model.Spectator;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Move;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.gamekit.graph.Edge;

/**
 *  This is a spectator that goes in the detective AI and works out where MrX
 *  could be
 */
public class MrXFinder implements Spectator {
    List<Integer> possibleMrXLocations = Collections.emptyList();
    int lastKnownMrX = 0;
    
    @Override
    public void onMoveMade(ScotlandYardView view, Move move) {
        int newLastKnownMrX = view.getPlayerLocation(Black);
        // if a reveal round has just passed
        if (lastKnownMrX != newLastKnownMrX) {
            lastKnownMrX = newLastKnownMrX;
            possibleMrXLocations.clear();
            possibleMrXLocations.add(lastKnownMrX);
        }
        // if black has just made a ticket move and we have had a reveal round
        else if (view.getCurrentPlayer() == Black && move instanceof TicketMove && lastKnownMrX != 0) {
            Graph<Integer, Transport> graph = view.getGraph();
            TicketMove tMove = (TicketMove) move;
            Ticket ticketUsed = tMove.ticket();
            List<Integer> newMrXLocations = Collections.emptyList();
            
            // see which paths MrX could have taken with the ticket he used
            for (int l : possibleMrXLocations) {
                Node startNode = graph.getNode(l);
                Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(startNode);
                for (Edge<Integer, Transport> e : edges) {
                    if (Ticket.fromTransport(e.data()) == ticketUsed) {
                        int destination = e.destination().value();
                        if (!newMrXLocations.contains(destination))
                            newMrXLocations.add(destination);
                    }
                }
            }
            possibleMrXLocations = newMrXLocations;
        }
        // we don't need to worry about double move calls as they're followed by two tickets.
    }
    
    public List<Integer> getMrXLocations() {
        return possibleMrXLocations;
    }
}
