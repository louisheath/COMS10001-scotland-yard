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
@ManagedAI("Heathkinsv6")
public class Heathkinsv6 implements PlayerFactory {

	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
            return new MyAI();
	}

	private static class MyAI implements Player {
            //Allows random numbers to be generated
            private final Random random = new Random();
            //How many moves ahead to look(1 is just as what the opponents do)
            int depth = 2; 
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
                    
                    //Add all Ticket moves to our DataNode Set
                    for(Move move : moves){
                        if (move instanceof TicketMove){               
                            List<PlayerData> newPD = new ArrayList<>();
                            //Stops it altering original list objects
                            for(PlayerData p : playerList) newPD.add(p.clone());
                            TicketMove tmove = (TicketMove) move;
                            newPD.get(0).location(tmove.destination());
                            newPD.get(0).adjustTicketCount(tmove.ticket(), -1);
                            DataNode node = new DataNode(newPD, move);
                            nextMovesNodes.add(node);
                        }
                    }
                    System.out.println("Random Move is: "+bestmove);
                    
                    //Iniatilise Best Move
                    bestNode = new DataNode(playerList,bestmove);    
                    
                    //Node Stuff - explore tree to depth - sets best node correctly
                    for(int i = 0; i < depth; i++){
                        System.out.println("Depth Iteration");
                        //Do MrX Next Move
                        //probs dont need nextMovesNodes = nextMrXNodes(nextMovesNodes,graph,i);
                        //Do Detective Moves
                        System.out.println(nextMovesNodes.size());
                        nextMovesNodes = nextDetectiveNodes(nextMovesNodes,graph,i);
                        System.out.println(nextMovesNodes.size());
                    }
                                     
                    //find first move to get to endnode
                    for(int i = 0; i<depth*playerList.size()-1 ;i++){
                        if((i+1)%playerList.size()==0) System.out.println("Black?");
                        System.out.println("Move  "+ bestNode.move());
                         /*
                            NOT SURE ON THIS YET
                            if(i == depth-1){
                            //Can we use a double move
                            if(view.getPlayerTickets(Black, Double) > 0){
                                //if first move isnt great or a double move would help
                                int d = scorer.scorenode(graph,bestNode.playerList());
                                int s = scorer.scorenode(graph,bestNode.previous().playerList());
                                if(s < 50 || 1.8*s < d){
                                    System.out.println("Double");
                                   //use double move
                                   DoubleMove doubleMove = new DoubleMove(Black,(TicketMove)bestNode.previous().move(),(TicketMove)bestNode.move());
                                   DataNode node = new DataNode(bestNode.playerList(),doubleMove);
                                   node.setprevious(bestNode.previous());
                                   bestNode.setprevious(node);
                                }
                            }
                        }
*/
                        bestNode = bestNode.previous();
                    }
                    bestmove = bestNode.move();
                    
                    int thismove = 0;                    
                    if (bestmove instanceof TicketMove){
                        thismove = scorer.scorenode(graph,bestNode.playerList());
                    }
                    else if (bestmove instanceof DoubleMove){
                        thismove = scorer.scorenode(graph,bestNode.playerList());
                    }                   
                    
                    System.out.println(depth + " Move Best Score:"+best);
                    System.out.println("This Move  "+ bestmove);
                    System.out.println("This Move score: :"+thismove);
                    System.out.println("---------------------------");
                    System.out.println("---------New Move----------");
                    System.out.println("---------------------------");
		    // picks best move
		    callback.accept(bestmove);

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
                                        if(player2.location() == edge.destination().value()) empty=false;
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
                                                       //If last player at last depth - (final nodes - e.g leaves)
                                                       if(this.depth==future+1) {
                                                           score = scorer.scorenode(graph,newPD);
                                                           if(score > this.best){  this.best = score; this.bestNode = node; }
                                                       }
                                                   }
                                                }
                                            }
                                        }
                                        if (player.hasTickets(Secret, 1)){
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
