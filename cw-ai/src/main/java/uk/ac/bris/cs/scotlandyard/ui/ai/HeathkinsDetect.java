package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
The PlayerFactory interface also comes with three further (empty) default methods,
which you can implement with your own custom behaviour (also see the JavaDocs of 
PlayerFactory). To add your own spectators to the game, return your implemented 
spectators as a List in the createSpectators method. You may perform setup and 
clean up tasks in the ready() and finish() methods, respectively.


*/

@ManagedAI("HeathkinsDetect")
public class HeathkinsDetect implements PlayerFactory {
        // spectator has to be static in order to be accessible by MyAI ?
        protected static MrXFinder mrXFinder = new MrXFinder();
    
	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
            return new MyAI();
	}
        
        // create a spectator which keeps track of MrX's potential locations
        @Override 
	public List<Spectator> createSpectators(ScotlandYardView view) {
            List<Spectator> spectators = new ArrayList<>();
            
            spectators.add(mrXFinder);
            
            return spectators;
        }

	// TODO A sample player that selects a random move
	private static class MyAI implements Player {
            
            Scorer2 scorer = new Scorer2();
            
            @Override
            public void makeMove(ScotlandYardView view, int location, Set<Move> moves, Consumer<Move> callback) {
                
                Graph<Integer, Transport> graph = view.getGraph();
                List<Integer> mrXLocations = mrXFinder.getMrXLocations();
                
                // colour and number of current player
                Colour playerColour = view.getCurrentPlayer();
                int playerNumber = view.getPlayers().indexOf(playerColour);
                
                // Build PlayerList
                List<PlayerData> playerList = new ArrayList<>();
                for(Colour c : view.getPlayers()) {
                    Map<Ticket, Integer> tickets = new HashMap<>();
                    for(Ticket t : Arrays.asList(Bus, Taxi, Underground, Double, Secret)) tickets.put(t, view.getPlayerTickets(c, t));
                    PlayerData player = new PlayerData(c, view.getPlayerLocation(c),tickets);
                    playerList.add(player);
                }
                
                // of the valid moves available, find the best
                int bestScore = 9999999;
                Move bestMove = new PassMove(playerColour);
                for (Move m : moves) {
                    if (m instanceof TicketMove) {
                        
                        List<PlayerData> newPDList = new ArrayList<>();
                        for(PlayerData p : playerList) newPDList.add(p.clone());
                        
                        TicketMove move = (TicketMove) m;
                        
                        // simulate move
                        newPDList.get(playerNumber).location(move.destination());
                        newPDList.get(playerNumber).adjustTicketCount(move.ticket(), -1);
                        newPDList.get(0).adjustTicketCount(move.ticket(), 1);
                        
                        // find cumulative score for the move, checking all of mrX's
                        // potential locations
                        int score = 0;
                        for (int l : mrXLocations) {
                            newPDList.get(0).location(l);
                            score += scorer.scorenode(graph, newPDList);
                        }
                        
                        /*
                        if(score > 0){ 
                            DataNode node = new DataNode(newPD, move);
                            node.setprevious(startNode);
                            startNode.setnext(node);
                            nextMovesNodes.add(node);
                        }*/
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
