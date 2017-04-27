package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import uk.ac.bris.cs.scotlandyard.model.Spectator;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Move;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Secret;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.gamekit.graph.Edge;

/**
 *  This is a spectator that goes in the detective AI and works out where MrX
 *  could be
 */
public class MrXFinder implements Spectator {
    List<Integer> possibleMrXLocations = new ArrayList<>();
    int lastKnownMrX = 0;
    
    public List<Integer> calcNewLocations(ScotlandYardView view, List<Integer> possibleMrXLocations, Ticket ticketUsed) {
        List<Integer> newMrXLocations = new ArrayList<>();
        Graph<Integer, Transport> graph = view.getGraph();
        // see which paths MrX could have taken with the ticket he used
        for (int l : possibleMrXLocations) {
            Node<Integer> startNode = graph.getNode(l);
            Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(startNode);
            for (Edge<Integer, Transport> e : edges) {
                if (Ticket.fromTransport(e.data()) == ticketUsed || ticketUsed == Secret) {
                    int destination = e.destination().value();
                    
                    // make sure that no detectives are occupying the destination
                    // this is key when MrX is almost cornered, as his options are
                    // far less
                    List<Colour> players = new ArrayList<>();
                    players.addAll(view.getPlayers());
                    players.remove(Black);
                    List<Integer> playerLocations = new ArrayList<>();
                    for (Colour p : players) {
                        playerLocations.add(view.getPlayerLocation(p));
                    }
                    boolean emptyNode = true;
                    if (playerLocations.contains(destination)) emptyNode = false;
                    
                    
                    if (!newMrXLocations.contains(destination) && emptyNode)
                        newMrXLocations.add(destination);
                }
            }
        }
        
        return newMrXLocations;
    }
    
    @Override
    public void onMoveMade(ScotlandYardView view, Move move) {
        Colour currentPlayer = view.getCurrentPlayer();
        if (currentPlayer == Black) {
            
            // if you've reset the game, then attributes need resetting
            if (view.getCurrentRound() == 1) {
                lastKnownMrX = 0;
                possibleMrXLocations.clear();
            }
            int newLastKnownMrX = view.getPlayerLocation(Black);
            // if a reveal round has just passed
            if (lastKnownMrX != newLastKnownMrX) {
                lastKnownMrX = newLastKnownMrX;
                possibleMrXLocations.clear();
                possibleMrXLocations.add(lastKnownMrX);
            }
            // if black has just made a ticket move and we have had a reveal round
            else if (move instanceof TicketMove && lastKnownMrX != 0) {
                Graph<Integer, Transport> graph = view.getGraph();
                TicketMove tMove = (TicketMove) move;
                Ticket ticketUsed = tMove.ticket();

                possibleMrXLocations = calcNewLocations(view, possibleMrXLocations, ticketUsed);
            }
        }
        // if a detective can confirm that MrX isn't on a node, then remove it
        else if (possibleMrXLocations.contains(view.getPlayerLocation(currentPlayer))) {
            possibleMrXLocations.remove((Integer) view.getPlayerLocation(currentPlayer));
        }
    }
    
    public List<Integer> getMrXLocations() {
        return possibleMrXLocations;
    }
}
