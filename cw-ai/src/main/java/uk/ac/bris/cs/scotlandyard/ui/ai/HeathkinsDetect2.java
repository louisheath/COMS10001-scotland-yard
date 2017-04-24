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
The PlayerFactory interface also comes with three further (empty) default methods,
which you can implement with your own custom behaviour (also see the JavaDocs of 
PlayerFactory). To add your own spectators to the game, return your implemented 
spectators as a List in the createSpectators method. You may perform setup and 
clean up tasks in the ready() and finish() methods, respectively.


*/

@ManagedAI("HeathkinsDetect")
public class HeathkinsDetect2 implements PlayerFactory {
        // spectator has to be static in order to be accessible by MyAI ?
        protected static MrXFinder mrXFinder = new MrXFinder();
    
	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
            return new MyAI();
	}
        
        boolean added = false;
        // create a spectator which keeps track of MrX's potential locations
        @Override 
	public List<Spectator> createSpectators(ScotlandYardView view) {
            List<Spectator> spectators = Collections.emptyList();
            
            if (!added) { 
                spectators.add(mrXFinder);
                added = true;
            }
            
            return spectators;
        }

	// TODO A sample player that selects a random move
	private static class MyAI implements Player {
            Scorer2 scorer = new Scorer2();
            private final Random random = new Random();
            // when looking ahead several rounds, MrX's possible locations will
            // change. This list holds those.
            List<Integer> futureMrXLocations;
            
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
                
                // If there hasn't yet been a reveal round, then we have no idea
                // where MrX is and so there's no use in building a tree
                if (mrXLocations.isEmpty()) {
                    Move randomMove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                    callback.accept(randomMove);
                    return;
                }
                
                // Create node to be start of the tree
                PassMove pMove = new PassMove(playerColour);
                DataNode startNode = new DataNode(playerList, pMove);
                startNode.score(9999999);
                
                // of the valid moves available, find their scores and create first layer of tree
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
                        int score = 0;
                        for (int l : mrXLocations) {
                            newPDList.get(0).location(l);
                            score += scorer.scorenode(graph, newPDList);
                        }
                        score /= mrXLocations.size();
                        
                        // configure and add to the tree
                        DataNode newNode = new DataNode(newPDList, m);
                        newNode.score(score);
                        newNode.setprevious(startNode);
                        startNode.setnext(newNode);
                        treeLayer.add(newNode);
                    }
                }
                
                // build the tree for certain number of rounds
                int depth = 2;
                futureMrXLocations = mrXLocations;
                for (int i = 0; i < depth; i++) {
                    // buildTree adds a layer to the tree for each player
                    // that takes a turn, then returns the last layer. The nodes
                    // are still connected
                    treeLayer = buildTree(treeLayer, view, depth, playerNumber);
                }
     
                /*
                int bestScore = 9999999;
                Move bestMove = new PassMove(playerColour);
                callback.accept(bestMove);*/
            }

            public Set<DataNode> buildTree(Set<DataNode> lastLayer, ScotlandYardView view, int depth, int startPlayer) {
                Graph<Integer, Transport> graph = view.getGraph();
                int playerListSize = view.getPlayers().size();
                
                // loop through the players to create a tree layer for each of them
                for (int i = 1; i < playerListSize; i++) {
                    Set<DataNode> newLastLayer = new HashSet<>();
                    // modulo allows us to count beyond list size and come back to start
                    int playerNum = (startPlayer + i) % playerListSize;
                    
                    // create new nodes for every node in the most recent layer
                    for (DataNode node : lastLayer) {

                        PlayerData player = node.playerList().get(playerNum);
                        int currentLocation = player.location();
                        
                        Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(currentLocation));
                        for (Edge<Integer, Transport> e : edges) {
                            // if the destination is empty
                            boolean empty = true;
                            for (PlayerData p : node.playerList()) {
                                if (e.destination().value() == p.location() && p.colour() != Black) empty = false;
                            }
                            // and if the player has tickets
                            if (empty && player.hasTickets(fromTransport(e.data()))) {
                                
                                List<PlayerData> newPDList = new ArrayList<>();
                                for(PlayerData p : node.playerList()) newPDList.add(p.clone());

                                TicketMove move = new TicketMove(player.colour(), fromTransport(e.data()), e.destination().value());

                                // simulate move
                                newPDList.get(playerNum).location(move.destination());
                                newPDList.get(playerNum).adjustTicketCount(move.ticket(), -1);
                                if (playerNum != 0) newPDList.get(0).adjustTicketCount(move.ticket(), 1);
                                
                                // score the move
                                int score;
                                if (playerNum == 0) {
                                    score = scorer.scorenode(graph, newPDList);
                                }
                                else {
                                    
                                }
                                
                                // create node for the move
                            }
                        }
                    }
                    lastLayer = newLastLayer;
                }
                
                
                
                
                
                if (depth != 0) {
                    //build tree layer for startPlayer
                    
                }
                
                
                
                
                
                
                
                
                
                return lastLayer;
            }
	}
            
            
}
