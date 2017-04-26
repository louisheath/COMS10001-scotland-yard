package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Graph;

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

Upgraded to use a spectator, still no tree
The detective goes towards the average of all possible MrX locations

*/

@ManagedAI("HeathkinsD2")
public class HeathkinsD2 implements PlayerFactory {
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
            // colour and number of current player
            Colour playerColour = view.getCurrentPlayer();
            System.out.println(playerColour+" makeMove called. lastKnownMrX:"+view.getPlayerLocation(Black));
            
            int lastKnownMrX = view.getPlayerLocation(Black);
            int playerLocation = view.getPlayerLocation(playerColour);
            
            Graph<Integer, Transport> graph = view.getGraph();
            List<Integer> mrXLocations = mrXFinder.getMrXLocations();

            System.out.println(playerColour+" getMrXLocations() return value: "+Arrays.toString(mrXLocations.toArray()));
            System.out.println(playerColour+" lastKnownMrX:"+view.getPlayerLocation(Black));
            
            // If there hasn't yet been a reveal round, or if we are at lastKnownMrX
            // then just take a random move
            if (mrXLocations.isEmpty() || lastKnownMrX == playerLocation) {
                System.out.println(playerColour + " making random move");
                Move randomMove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                callback.accept(randomMove);
                return;
            }
            System.out.println(playerColour+" scoring moves, lastKnownMrX:"+view.getPlayerLocation(Black));
            // find the scores for each move
            int[] scores = new int[200];
            for (int l : mrXLocations) {
                int[] distances = dijkstras.calculate(l, graph, true);
                for (Move m : moves) {
                    if (m instanceof TicketMove) {

                        TicketMove move = (TicketMove) m;
                        int dest = move.destination();
                        // find the move that will take you closest to mrX
                        
                        scores[dest] += distances[dest];
                    }
                }
            }
            System.out.println(playerColour+" choosing best move, lastKnownMrX:"+view.getPlayerLocation(Black));
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
            System.out.println(playerColour + " accepting best move, MrX:"+view.getPlayerLocation(Black));
            callback.accept(bestMove);
        }
    }
}
