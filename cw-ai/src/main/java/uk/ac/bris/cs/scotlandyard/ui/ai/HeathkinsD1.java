package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
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
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Transport;

/*

No spectator, no tree, the detective just goes towards lastKnownMrX

*/

@ManagedAI("HeathkinsD1")
public class HeathkinsD1 implements PlayerFactory {
    
    // TODO create a new player here
    @Override
    public Player createPlayer(Colour colour) {
        return new MyAI();
    }

    // TODO A sample player that selects a random move
    private static class MyAI implements Player {
        Dijkstras dijkstras = new Dijkstras();
        private final Random random = new Random();

        @Override
        public void makeMove(ScotlandYardView view, int location, Set<Move> moves, Consumer<Move> callback) {
            
            Graph<Integer, Transport> graph = view.getGraph();
            
            // colour and number of current player
            Colour playerColour = view.getCurrentPlayer();
            int playerLocation = view.getPlayerLocation(playerColour);
            
            int mrXLocation = view.getPlayerLocation(Black);
            
            // If there hasn't yet been a reveal round then go to a node with
            // good transport links
            if (mrXLocation == 0 || mrXLocation == playerLocation) {
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
            Move bestMove = new PassMove(playerColour);
            int bestScore = 100;
            // of the valid moves available, find and make the best move
            int[] distances = dijkstras.calculate(mrXLocation, graph, true);
            for (Move m : moves) {
                if (m instanceof TicketMove) {

                    TicketMove move = (TicketMove) m;

                    // find the move that will take you closest to mrX
                    int score = distances[move.destination()];

                    // check if it's the best move
                    if (score < bestScore) {
                        bestScore = score;
                        bestMove = m;
                    }
                }
            }
            
            callback.accept(bestMove);
        }
    }
}
