package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Spectator;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Transport;

/*

Uses a spectator, no tree
The detective goes towards the nearest of MrX's possible locations

*/

@ManagedAI("HeathkinsD3")
public class HeathkinsD3 implements PlayerFactory {
    // spectator has to be static in order to be accessible by MyAI
    protected static MrXFinder mrXFinder = new MrXFinder();

    // TODO create a new player here
    @Override
    public Player createPlayer(Colour colour) {
        return new MyAI();
    }

    // create a spectator which keeps track of MrX's potential locations
    @Override 
    public List<Spectator> createSpectators(ScotlandYardView view) {
        return Collections.singletonList(mrXFinder);
    }

    // TODO A sample player that selects a random move
    private static class MyAI implements Player {
        Dijkstras dijkstras = new Dijkstras();
        private final Random random = new Random();

        @Override
        public void makeMove(ScotlandYardView view, int location, Set<Move> moves, Consumer<Move> callback) {
            Graph<Integer, Transport> graph = view.getGraph();
            
            // key info about current player
            Colour playerColour = view.getCurrentPlayer();
            
            // info about MrX
            List<Integer> mrXLocations = mrXFinder.getMrXLocations();
            int lastKnownMrX = view.getPlayerLocation(Black);
            
            // If there hasn't yet been a reveal round then go to a node with
            // good transport links
            if (lastKnownMrX == 0) {
                // score each move depending on its destination's transport links
                int[] scores = new int[200];
                for (Move m : moves) {
                    if (m instanceof TicketMove) {
                        TicketMove move = (TicketMove) m;
                        int dest = move.destination();
                        Node<Integer> destNode = graph.getNode(dest);
                        
                        Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(destNode);
                        for (Edge<Integer, Transport> e : edges) {
                            switch(e.data()) {
                            case Taxi: scores[dest] += 5; break;
                            case Bus: scores[dest] += 7; break;
                            case Underground: scores[dest] += 25; break;
                            }
                        }
                    }
                }
                // choose best move
                int bestScore = 0;
                Move bestMove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                for (Move m : moves) {
                    if (m instanceof TicketMove) {
                        TicketMove move = (TicketMove) m;
                        int dest = move.destination();
                        
                        if (scores[dest] > bestScore) {
                            bestScore = scores[dest];
                            bestMove = m;
                        }
                    }
                }
                
                callback.accept(bestMove);
                return;
            }
            // Scores each valid move
            int[] scores = new int[200];
            for (int l : mrXLocations) {
                int[] distances = dijkstras.calculate(l, graph, true);
                for (Move m : moves) {
                    if (m instanceof TicketMove) {

                        TicketMove move = (TicketMove) m;
                        int dest = move.destination();
                        
                        // the score for each destination node will be its distance
                        // from the nearest possible MrX location
                        if (distances[dest] < scores[dest])
                            scores[dest] = distances[dest];
                        
                        // If you can land on a possible MX, go for it
                        if (distances[dest] == 0) {
                            callback.accept(m);
                            return;
                        }
                    }
                }
            }
            // find the best move
            Move bestMove = new PassMove(playerColour);
            int bestScore = 1000;
            for (Move m : moves) {
                if (m instanceof TicketMove) {

                    TicketMove move = (TicketMove) m;
                    int dest = move.destination();
                    
                    if (scores[dest] < bestScore) {
                        bestScore = scores[dest];
                        bestMove = m;
                    }
                }
            }
            callback.accept(bestMove);
        }
    }
}
