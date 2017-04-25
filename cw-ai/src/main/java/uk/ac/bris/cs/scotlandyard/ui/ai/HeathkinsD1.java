package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
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
            int mrXLocation = view.getPlayerLocation(Black);
            int playerLocation = view.getPlayerLocation(playerColour);

            // If there hasn't yet been a reveal round, or if we are at lastKnownMrX
            // then just take a random move
            if (mrXLocation == 0 || mrXLocation == playerLocation) {
                System.out.println(playerColour + " making random move");
                Move randomMove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                callback.accept(randomMove);
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
            System.out.println(playerColour + " making move towards lastKnownMrX");
            callback.accept(bestMove);
        }
    }
}
