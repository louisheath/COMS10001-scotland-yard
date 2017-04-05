package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Boat;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Taxi;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Underground;


// TODO name the AI
@ManagedAI("Heathkinsv4")
public class Heathkinsv4 implements PlayerFactory {

	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
            return new MyAI();
	}

	// TODO A sample player that selects a random move
	private static class MyAI implements Player {
            private final Random random = new Random();
            //How many moves ahead to look
            int depth = 1; 
            //Best Score and node at depth
            int best = -9999; 
            MoveNode bestNode;
            //Save doubleMoves to increase efficiency
            Set<MoveNode> doubleMoves = new HashSet<>();
            static Dijkstras dijkstras = new Dijkstras();
		@Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves,Consumer<Move> callback){
                    Move bestmove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                    Graph<Integer, Transport> graph = view.getGraph();
                    
                    //Create Set Of Move Nodes
                    Set<MoveNode> nextMovesNodes = new HashSet<>();
                    for(Move move : moves){
                        if (move instanceof TicketMove){
                            TicketMove tmove = (TicketMove) move;
                            if(scorenode(view,tmove.destination(),0)>30){
                                MoveNode node = new MoveNode(move);
                                nextMovesNodes.add(node);
                            }
                        }
                        else if (move instanceof DoubleMove){
                            DoubleMove Dmove = (DoubleMove) move;
                            if(scorenode(view,Dmove.finalDestination(),0)>30){
                                MoveNode node = new MoveNode(move);
                                nextMovesNodes.add(node);
                            }
                        }
                    }
                    
                    bestNode = new MoveNode(bestmove);                  
                    //Node Stuff - explore tree to depth - sets best node correctly
                    for(int i = 1; i <= depth; i++){
                        nextMovesNodes = nextMovesNodes(nextMovesNodes,view,depth);
                    }
                    System.out.println("Here");
                                      
                    //find best end node doublemove - use if substantially better
                    for(MoveNode node : doubleMoves){
                        Move move = node.move();
                        if (move instanceof DoubleMove){
                            DoubleMove Dmove = (DoubleMove) move;
                            int tmp = scorenode(view,Dmove.finalDestination(),depth);
                            //not sure on these numbers basically want to conserve double moves for when they make big difference
                            if(tmp > best+50){
                                best = tmp;
                                bestNode = node;
                            }
                        }
                    }
                    //find first move to get to endnode
                    for(int i = 0; i<depth ;i++){
                        System.out.println("Move  "+ bestNode.move());
                        bestNode = bestNode.previous();
                    }
                    bestmove = bestNode.move();
                    
                    int thismove = 0;                    
                    if (bestmove instanceof TicketMove){
                        thismove = scorenode(view,((TicketMove) bestmove).destination(),0);
                    }
                    if (bestmove instanceof DoubleMove){
                        thismove = scorenode(view,((DoubleMove) bestmove).finalDestination(),0);
                    }
                    
                    System.out.println(depth + " Move Best Score:"+best);
                    System.out.println("This Move  "+ bestmove);
                    System.out.println("This Move score: :"+thismove);
                    
		    // picks best move
		    callback.accept(bestmove);

		}              
                
            private Set<MoveNode> nextMovesNodes(Set<MoveNode> moves, ScotlandYardView view, int depth) {
                Graph<Integer, Transport> graph = view.getGraph();
                Set<MoveNode> nextMovesNodes = new HashSet<>();
                for(MoveNode move : moves){
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
                        for (Edge<Integer, Transport> edge : edges) {
                            //is next spot empty
                            boolean empty = true;
                            for(Colour player : view.getPlayers()){
                                //Added exemption of Black's location, as with double he can return to his start.
                                if(view.getPlayerLocation(player)==edge.destination().value() && player != Black)
                                empty = false;
                            } 

                            if (empty) {
                                int score = scorenode(view,edge.destination().value(),depth);
                                if(score > 60){
                                    if (view.getPlayerTickets(Black, Ticket.fromTransport(edge.data())) >= 1){
                                        TicketMove tmove = new TicketMove(Black,Ticket.fromTransport(edge.data()),edge.destination().value());
                                        MoveNode node = new MoveNode(tmove);
                                        node.setprevious(move);
                                        move.setnext(node);
                                        nextMovesNodes.add(node);
                                        //Are we at final depth
                                        if(this.depth==depth) {
                                            if(score > this.best){ this.best = score; this.bestNode = node; }
                                        }
                                    }
                                    if (view.getPlayerTickets(Black,Secret) >= 1){
                                        TicketMove tmove = new TicketMove(Black,Secret,edge.destination().value());
                                        MoveNode node = new MoveNode(tmove);
                                        node.setprevious(move);
                                        move.setnext(node);
                                        nextMovesNodes.add(node);
                                        //Are we at final depth
                                        if(this.depth==depth) {
                                            if(score > this.best){ this.best = score; this.bestNode = node; }
                                        }
                                    }
                                }
                            }   
                        }  
                    }
                }
                if(view.getPlayerTickets(Black, Double) >= 1){
                    ArrayList<MoveNode> toAdd = new ArrayList<>();
                    for (MoveNode move : nextMovesNodes){
                        TicketMove firstMove = (TicketMove) move.move();
                        int destination = firstMove.destination();

                        Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(destination));
                        for (Edge<Integer, Transport> edge : edges) {
                            //is next spot empty
                            boolean empty = true;
                            for(Colour player : view.getPlayers()){
                                    //Added exemption of Black's location, as with double he can return to his start.
                                    if(view.getPlayerLocation(player)==edge.destination().value() && player !=Black)
                                    empty = false;
                                } 

                            if (empty)
                            {
                                int score = scorenode(view,edge.destination().value(),depth);
                                if(score > 80){
                                    //checks MrX has both tickets needed
                                    int ticketsNeeded = 1;
                                    if (firstMove.ticket() == Ticket.fromTransport(edge.data())) ticketsNeeded = 2;

                                    if (view.getPlayerTickets(Black, Ticket.fromTransport(edge.data())) >= ticketsNeeded)
                                    {
                                        TicketMove secondMove = new TicketMove(Black,Ticket.fromTransport(edge.data()),edge.destination().value());
                                        DoubleMove Dmove = new DoubleMove(Black,firstMove,secondMove);
                                        MoveNode node = new MoveNode(Dmove);
                                        node.setprevious(move.previous());
                                        move.previous().setnext(node);
                                        toAdd.add(node);
                                        
                                    }

                                    if (view.getPlayerTickets(Black, Secret) >= 2 && firstMove.ticket() == Secret
                                            || view.getPlayerTickets(Black, Secret) >= 1 && firstMove.ticket() != Secret)
                                    {
                                        TicketMove secondMove = new TicketMove(Black,Secret,edge.destination().value());
                                        DoubleMove Dmove = new DoubleMove(Black,firstMove,secondMove);
                                        MoveNode node = new MoveNode(Dmove);
                                        node.setprevious(move.previous());
                                        move.previous().setnext(node);
                                        toAdd.add(node);
                                    }
                                }
                            }
                        } 
                    }
                    if(this.depth == depth)this.doubleMoves.addAll(toAdd);
                    nextMovesNodes.addAll(toAdd);
                }
                return nextMovesNodes;
            }   
        
        private static int scorenode(ScotlandYardView view, int location, int depth){
            int score = 0;
            int edgescore = 0;
            Graph<Integer, Transport> graph = view.getGraph();
            //work out how many usuable paths leaving node
            if(graph.containsNode(location))
            {
                //Find all paths from current location
                Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(location));
                //For each path check if the destination is empty then check if they have the tickets needed to follow it
                for (Edge<Integer, Transport> edge : edges) {
                    if (view.getPlayerTickets(Black, fromTransport(edge.data()))>0);
                    {
                        switch(edge.data()){
                            case Taxi: edgescore = edgescore + 1; break;
                            case Bus: edgescore = edgescore + 6; break;
                            case Underground: edgescore = edgescore + 10; break;
                            case Boat: edgescore = edgescore + 15; break;
                        }
                    }
                }
            }  
            int totaldistance = 0;
            //gives you shortest distance to each node from starting location
            int[] distance = dijkstras.calculate(location, graph);
            //Adds up the distance of all the detectives from node.
            for(Colour player : view.getPlayers()){
                if(player.isDetective())
                {
                    //discourage going 1 move away from detectives
                    if(distance[view.getPlayerLocation(player)]-depth < 2) totaldistance -= 50;
                    else totaldistance += distance[view.getPlayerLocation(player)]-depth;
                }
            }
            //If game over in future situation and MrX isnt one move away from a detective set score large so will be chosen
            if(view.getRounds().size()<= view.getCurrentRound()+depth && totaldistance > view.getPlayers().size()) score = 9999;
            //calc score based on weighted combination of above
            else score = edgescore + (totaldistance/(view.getPlayers().size()-1)*20);
            //check if gameover?? if we can and won score massive if loss score 0
            return score;
        }          
    }
}