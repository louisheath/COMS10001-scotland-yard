package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import uk.ac.bris.cs.scotlandyard.model.Spectator;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Double;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Secret;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Underground;
import uk.ac.bris.cs.scotlandyard.model.Transport;

/*

No spectator, no tree, the detective goes towards lastKnownMrX

*/

@ManagedAI("HeathkinsDSimple")
public class HeathkinsDSimple implements PlayerFactory {
    
    // TODO create a new player here
    @Override
    public Player createPlayer(Colour colour) {
        return new MyAI();
    }

    // TODO A sample player that selects a random move
    private static class MyAI implements Player {
        Scorer2 scorer = new Scorer2();
        private final Random random = new Random();

        @Override
        public void makeMove(ScotlandYardView view, int location, Set<Move> moves, Consumer<Move> callback) {
            Graph<Integer, Transport> graph = view.getGraph();

            // colour and number of current player
            Colour playerColour = view.getCurrentPlayer();
            int playerNumber = view.getPlayers().indexOf(playerColour);
            int mrXLocation = view.getPlayerLocation(Black);

            // Build PlayerList
            List<PlayerData> playerList = new ArrayList<>();
            for(Colour c : view.getPlayers()) {
                Map<Ticket, Integer> tickets = new HashMap<>();
                for(Ticket t : Arrays.asList(Bus, Taxi, Underground, Double, Secret)) tickets.put(t, view.getPlayerTickets(c, t));
                PlayerData player = new PlayerData(c, view.getPlayerLocation(c),tickets);
                playerList.add(player);
            }

            // If there hasn't yet been a reveal round, then we have no idea
            // where MrX is and so there's no use in building a tree
            if (mrXLocation == 0) {
                System.out.println(playerColour + " making random move");
                Move randomMove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                callback.accept(randomMove);
                return;
            }

            Move bestMove = new PassMove(playerColour);
            int bestScore = 9999;
            // of the valid moves available, find and make the best move
            Set<DataNode> treeLayer = new HashSet<>();
            for (Move m : moves) {
                if (m instanceof TicketMove) {

                    List<PlayerData> newPDList = new ArrayList<>();
                    for (PlayerData p : playerList) newPDList.add(p.clone());

                    TicketMove move = (TicketMove) m;

                    // simulate move
                    newPDList.get(playerNumber).location(move.destination());
                    newPDList.get(playerNumber).adjustTicketCount(move.ticket(), -1);
                    newPDList.get(0).adjustTicketCount(move.ticket(), 1);

                    // find average score for the move across all of mrX's
                    // potential locations
                    int score = scorer.scorenode(graph, newPDList);

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
