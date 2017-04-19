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


// TODO name the AI
@ManagedAI("Heathkinsv5")
public class Heathkinsv5 implements PlayerFactory {

	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
            return new MyAI();
	}

	private static class MyAI implements Player {
            //Allows random numbers to be generated
            private final Random random = new Random();
            //How many moves ahead to look
            int depth = 3; 
            //Best Score and node at depth
            int best = -9999; 
            DataNode bestNode;
            //Save doubleMoves in variable to increase efficiency
            Set<DataNode> doubleMoves = new HashSet<>();
            //Instanstiate Scorer Object
            Scorer scorer = new Scorer();
            //Types Of Ticket
            List<Ticket> ticketTypes = new ArrayList<>(Arrays.asList(Bus, Taxi, Underground, Double, Secret));

		@Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves,Consumer<Move> callback){
                    List<PlayerData> playerList = new ArrayList<>();
                    for(Colour c : view.getPlayers()){
                        Map<Ticket, Integer> tickets = new HashMap<>();
                        for(Ticket t : ticketTypes) tickets.put(t, view.getPlayerTickets(c, t));
                        PlayerData player = new PlayerData(c, view.getPlayerLocation(c),tickets);
                        playerList.add(player);
                    }
                    System.out.println("Start Location Is: "+location);
                    System.out.println("Score of Start Location: " + scorer.scorenode(view, location, 0));
                    //make sure best is reset for new move
                    best = -9999;
                    //Initialise bestmove to a random move
                    Move bestmove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                    
                    //Create Set Of Move Nodes
                    Set<DataNode> nextMovesNodes = new HashSet<>();
                    
                    //If in rubbish position and we have double move
                    if(scorer.scorenode(view, location, 0) < 0 && view.getPlayerTickets(Black, Double) > 0){
                        //use Double Move
                        while(bestmove instanceof TicketMove)
                        {
                            bestmove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                        }
                        System.out.println("Double Move It: "+bestmove);
                        //Add all double moves to our DataNode Set
                        for(Move move : moves){
                            /////////vonfig for data nodes////////////////////////////List<PlayerData> newPD = move.playerList(); newPD.get(0).location(edge.destination().value());
                            if (move instanceof DoubleMove){
                                DoubleMove Dmove = (DoubleMove) move;
                                if(scorer.scorenode(view,Dmove.finalDestination(),0)>0){
                                    
                                    DataNode node = new DataNode(playerList, move);
                                    nextMovesNodes.add(node);
                                }
                            }
                        }
                    }
                    else
                    {
                        //use Ticket Move
                        while(bestmove instanceof DoubleMove)
                        {
                            bestmove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                        }
                        //Add all Ticket moves to our DataNode Set
                        for(Move move : moves){
                            if (move instanceof TicketMove){
                                TicketMove tmove = (TicketMove) move;
                                if(scorer.scorenode(view,tmove.destination(),0)>0){
                                    DataNode node = new DataNode(playerList,move);
                                    nextMovesNodes.add(node);
                                }
                            }
                        }
                    }
                    System.out.println("Random Move is: "+bestmove);
                    
                    //Iniatilise Best Move
                    bestNode = new DataNode(playerList,bestmove);    
                    
                    
                    //Node Stuff - explore tree to depth - sets best node correctly
                    for(int i = 0; i < depth; i++){
                        nextMovesNodes = nextMovesNodes(nextMovesNodes,view,i);
                    }
                                     
                    //find first move to get to endnode
                    for(int i = 0; i<depth ;i++){
                        System.out.println("Move  "+ bestNode.move());
                        bestNode = bestNode.previous();
                    }
                    bestmove = bestNode.move();
                    
                    int thismove = 0;                    
                    if (bestmove instanceof TicketMove){
                        thismove = scorer.scorenode(view,((TicketMove) bestmove).destination(),0);
                    }
                    //tbh dont think its needed but cba to remove as wont be activated anyway
                    else if (bestmove instanceof DoubleMove){
                        thismove = scorer.scorenode(view,((DoubleMove) bestmove).finalDestination(),0);
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
                
            private Set<DataNode> nextMovesNodes(Set<DataNode> moves, ScotlandYardView view, int future) {
                Graph<Integer, Transport> graph = view.getGraph();
                Set<DataNode> nextMovesNodes = new HashSet<>();
                //For all moves in set
                for(DataNode move : moves){
                    //Find where move ends up
                    int playerLocation = 0;
                    if (move.move() instanceof TicketMove){
                        TicketMove tmove = (TicketMove) move.move();
                        playerLocation = tmove.destination();
                    }
                    if (move.move() instanceof DoubleMove){
                        DoubleMove Dmove = (DoubleMove) move.move();
                        playerLocation = Dmove.finalDestination();                   
                    }
                    if(graph.containsNode(playerLocation)){
                        //Find all paths from current location
                        Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(playerLocation));
                        //For each path check if the destination is empty then check if they have the tickets needed to follow it
                        for(Edge<Integer, Transport> edge : edges) {
                            //is next spot empty
                            boolean empty = true;
                            for(Colour player : view.getPlayers()){
                                //Added exemption of Black's location, as with double he can return to his start.
                                if(view.getPlayerLocation(player)==edge.destination().value() && player != Black)
                                empty = false;
                            } 

                            if (empty) {
                                int score = scorer.scorenode(view,edge.destination().value(),future);
                                
                                if(score > 0){
                                    //GetPlayerData
                                    List<PlayerData> newPD = move.playerList();
                                    newPD.get(0).location(edge.destination().value());
                                    if (view.getPlayerTickets(Black, Ticket.fromTransport(edge.data())) >= 1){
                                        TicketMove tmove = new TicketMove(Black,Ticket.fromTransport(edge.data()),edge.destination().value());
                                        //Set up node and make them point correctly
                                        //Adjust PlayerData to reflect game after this move
                                        newPD.get(0).adjustTicketCount(Ticket.fromTransport(edge.data()), -1);
                                        DataNode node = new DataNode(newPD,tmove);
                                        node.setprevious(move);
                                        move.setnext(node);
                                        nextMovesNodes.add(node);
                                        //Are we at final depth - if so is it better than a previous soln - if yes store it
                                        if(this.depth==future+1) {
                                            if(score > this.best){ this.best = score; this.bestNode = node; }
                                        }
                                    }
                                    if (view.getPlayerTickets(Black,Secret) >= 1){
                                        TicketMove tmove = new TicketMove(Black,Secret,edge.destination().value());
                                        newPD.get(0).adjustTicketCount(Secret, -1);
                                        //Set up node and make them point correctly
                                        DataNode node = new DataNode(newPD,tmove);
                                        node.setprevious(move);
                                        move.setnext(node);
                                        nextMovesNodes.add(node);
                                        //Are we at final depth - if so is it better than a previous soln - if yes store it
                                        if(this.depth==future+1) {
                                            if(score > this.best){ this.best = score; this.bestNode = node; }
                                        }
                                    }
                                }
                            }   
                        }  
                    }
                }
                return nextMovesNodes;
            }             
    }
}