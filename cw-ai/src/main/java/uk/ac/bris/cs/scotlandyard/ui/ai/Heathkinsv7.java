package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import uk.ac.bris.cs.scotlandyard.model.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Double;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Secret;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Underground;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;


// TODO name the AI
@ManagedAI("Heathkinsv7")
public class Heathkinsv7 implements PlayerFactory {

	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
            return new MyAI();
	}

	private static class MyAI implements Player {
            //Allows random numbers to be generated
            private final Random random = new Random();
            //How many moves ahead to look(1 is just as what the opponents do)
            int depth = 3; 
            //Best Score and node at depth
            int best = -9999; 
            DataNode bestNode;
            //Save doubleMoves in variable to increase efficiency
            Set<DataNode> doubleMoves = new HashSet<>();
            //Instanstiate Scorer Object
            Scorer2 scorer = new Scorer2();
            //Types Of Ticket
            List<Ticket> ticketTypes = new ArrayList<>(Arrays.asList(Bus, Taxi, Underground, Double, Secret));

		@Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves,Consumer<Move> callback){
                    Graph<Integer, Transport> graph = view.getGraph();
                    //Build PlayerList
                    List<PlayerData> playerList = new ArrayList<>();
                    for(Colour c : view.getPlayers()){
                        Map<Ticket, Integer> tickets = new HashMap<>();
                        for(Ticket t : ticketTypes) tickets.put(t, view.getPlayerTickets(c, t));
                        PlayerData player = new PlayerData(c, view.getPlayerLocation(c),tickets);
                        playerList.add(player);
                    }
                    //Makes Black Location Correct
                    playerList.get(0).location(location);
                    System.out.println("Start Location Is: "+location);
                    System.out.println("Score of Start Location: " + scorer.scorenode(graph, playerList));
                    //make sure best is reset for new move
                    best = -9999;
                    //Initialise bestmove to a random move
                    Move bestmove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                    
                    //Create Set Of Move Nodes
                    Set<DataNode> nextMovesNodes = new HashSet<>();
                    
                    //use Ticket Move
                    while(bestmove instanceof DoubleMove) bestmove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                    
                    //Set up starting DataNode
                    PassMove pMove = new PassMove(Black);
                    DataNode startNode = new DataNode(playerList, pMove);
                    startNode.score(-9999);
                    
                    //Add all Ticket moves to our DataNode Set
                    for(Move move : moves){
                        if (move instanceof TicketMove){
                            //ISSUE HERE MEANS WE CANT TAKE BOAT BUT FOR NOW TRYING TO KEEP PATHS OUT
                            if (((TicketMove) move).ticket() == Secret) continue;
                            List<PlayerData> newPD = new ArrayList<>();
                            //Stops it altering original list objects
                            for(PlayerData p : playerList) newPD.add(p.clone());
                            TicketMove tmove = (TicketMove) move;
                            newPD.get(0).location(tmove.destination());
                            newPD.get(0).adjustTicketCount(tmove.ticket(), -1);
                            int score = scorer.scorenode(graph,newPD);
                            //Dont allow black moves which put it in danger
                            if(score > 0){ 
                                DataNode node = new DataNode(newPD, move);
                                node.setprevious(startNode);
                                startNode.setnext(node);
                                nextMovesNodes.add(node);
                            }
                        }
                    }
                    System.out.println("Random Move is: "+bestmove);
                    
                    //Iniatilise Best Move
                    bestNode = new DataNode(playerList,bestmove);    
                    
                    //Build Game tree to given depth
                    for(int i = 0; i < depth; i++){
                        System.out.println("new depth:" + i);
                        System.out.println("IN: "+nextMovesNodes.size());
                        //Do Detective Moves
                        nextMovesNodes = nextDetectiveNodes(nextMovesNodes,graph,i);
                        System.out.println("OUT: "+nextMovesNodes.size());
                    }
                    
                    //Alpha Beta - used wikipedia psuedo code
                    //https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
                    int alphabest = alphabeta(startNode,-999999,999999, graph);
                    System.out.println("Alpha Best:  "+ alphabest );
                    
                    for(DataNode node : startNode.next()){
                        if (alphabest == node.score()){
                            bestNode = node;
                        }
                    }
                                     
                    //need to work out when to do a double move
                    bestmove = bestNode.move();
                    
                    int thismove = 0;                    
                    thismove = scorer.scorenode(graph,bestNode.playerList());
                    
                    System.out.println("This Move  "+ bestmove );
                    System.out.println("This Move score: :"+thismove);
                    System.out.println("---------------------------");
                    System.out.println("---------New Move----------");
                    System.out.println("---------------------------");
		    // picks best move
		    callback.accept(bestmove);

		}  
                
        private int alphabeta(DataNode node, int alpha,int beta, Graph<Integer, Transport> graph){
            //If 'Leaf' Node - i.e last one
            if (node.next().isEmpty()) {
                node.score(scorer.scorenode(graph,node.playerList())); 
                return node.score();
            }
            //Get Max - MrX
            if (node.next().get(0).move().colour() == Black){
                int v = -999999;
                for (DataNode child : node.next()){
                    v = max(v, alphabeta(child, alpha, beta, graph));
                    alpha = max(alpha, v);
                    if (beta <= alpha) break;
                }
                return v;
            }
            //Get Min - Detective
            else{
                int v = 999999;
                for (DataNode child : node.next()){
                    v = min(v, alphabeta(child, alpha, beta, graph));
                    beta = min(beta, v);
                    if (beta <= alpha) break;   
                }
                return v;
            }
        }

        private int min(int a, int b){
            if(a<b){
                return a;
            }
            else{
                return b;
            }   
        }
        private int max(int a, int b){
            if(a<b){
                return b;
            }
            else{
                return a;
            }   
        }                
                
        private Set<DataNode> nextDetectiveNodes(Set<DataNode> moves, Graph<Integer, Transport> graph, int future) {
                //Create Set Of Move Nodes
                Set<DataNode> nextMovesNodes = new HashSet<>();
                for(DataNode move : moves){
                    Set<DataNode> nextPlayerNodes = new HashSet<>();
                    nextPlayerNodes.add(move);
                    int i = 0;
                    for(PlayerData player : move.playerList()){
                            //Tmp storage set
                            Set<DataNode> tmp = new HashSet<>();
                            //ignores black on first depth as black moves passed in so detectives go next
                            if(player.colour()==Black && future == 0){  i++; continue; }
                            //Find where out where they are
                            int playerLocation = player.location();
                            if(graph.containsNode(playerLocation)){
                                //Find all paths from current location
                                Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(playerLocation));
                                //For each path check if the destination is empty then check if they have the tickets needed to follow it
                                for(Edge<Integer, Transport> edge : edges) {
                                    //is next spot empty
                                    boolean empty = true;
                                    for(PlayerData player2 : move.playerList()){
                                        //Detectives Can land on black to win
                                        if(player2.location() == edge.destination().value() && player2.colour()!=Black) empty=false;
                                    } 
                                    if (empty) {     
                                        if (player.hasTickets(fromTransport(edge.data()), 1)){
                                            //GetPlayerData
                                            List<PlayerData> newPD = new ArrayList<>();
                                            //Stops it altering original list objects
                                            for(PlayerData p : move.playerList()) newPD.add(p.clone());
                                            TicketMove tmove = new TicketMove(player.colour(),Ticket.fromTransport(edge.data()),edge.destination().value());
                                            //Adjust PlayerData to reflect game after this move
                                            newPD.get(i).location(edge.destination().value());
                                            newPD.get(i).adjustTicketCount(Ticket.fromTransport(edge.data()), -1);
                                            //MrX get given detective tickets
                                            if(player.colour()!=Black) newPD.get(0).adjustTicketCount(Ticket.fromTransport(edge.data()), +1);
                                            int score = 1;
                                            if(player.colour() == Black) score = scorer.scorenode(graph,newPD);
                                            //Dont allow black moves which put it in danger
                                            if(score > 0){ 
                                                for(DataNode previousnode : nextPlayerNodes){
                                                    //Set up node and make them point correctly
                                                    DataNode node = new DataNode(newPD,tmove);
                                                    node.setprevious(previousnode);
                                                    previousnode.setnext(node);
                                                    tmp.add(node);
                                                
                                                   //If last player
                                                   if(i == move.playerList().size()-1){
                                                       nextMovesNodes.add(node);
                                                   }
                                                }
                                            }
                                        }
                                        //Secret only allow if only option to limit tree size
                                        else if (player.hasTickets(Secret, 1)){
                                            //GetPlayerData
                                            List<PlayerData> newPD = new ArrayList<>();
                                            //Stops it altering original list objects
                                            for(PlayerData p : move.playerList()) newPD.add(p.clone());
                                            TicketMove tmove = new TicketMove(player.colour(),Secret,edge.destination().value());
                                            //Adjust PlayerData to reflect game after this move
                                            newPD.get(i).location(edge.destination().value());
                                            newPD.get(i).adjustTicketCount(Secret, -1);                      
                                            int score = 1;
                                            if(player.colour() == Black) score = scorer.scorenode(graph,newPD);
                                            //Dont allow black moves which put it in danger
                                            if(score > 0){
                                                for(DataNode previousnode : nextPlayerNodes){
                                                    //Set up node and make them point correctly
                                                    DataNode node = new DataNode(newPD,tmove);
                                                    node.setprevious(previousnode);
                                                    previousnode.setnext(node);
                                                    tmp.add(node);     
                                                }
                                            }                                        
                                        }
                                        
                                    }
                                }    
                        }
                        nextPlayerNodes.clear();
                        nextPlayerNodes.addAll(tmp);
                        i++;
                    }    
                }
            return nextMovesNodes;
        } 
        
        
    }
}
