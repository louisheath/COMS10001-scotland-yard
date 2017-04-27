package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;

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
import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Boat;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Taxi;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Underground;

// TODO name the AI
@ManagedAI("Heathkinsv2")
public class Heathkinsv2 implements PlayerFactory {

	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
            return new MyAI();
	}

	// TODO A sample player that selects a random move
	private static class MyAI implements Player {
            private final Random random = new Random();

		@Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves,Consumer<Move> callback){
                    int best = -9999;
                    Move bestmove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                    Graph<Integer, Transport> graph = view.getGraph();
                    
                    //Create Set Of Move Nodes
                    Set<MoveNode> nextMovesNodes = new HashSet<>();
                    for(Move move : moves){
                        if (move instanceof TicketMove){
                            TicketMove tmove = (TicketMove) move;
                            if(scorenode(view,tmove.destination())>30){
                                MoveNode node = new MoveNode(move);
                                nextMovesNodes.add(node);
                            }
                        }
                    }
                    int depth = 3;                   
                    //Node Stuff
                    for(int i = 0; i<depth ;i++){
                        nextMovesNodes = nextMovesNodes(nextMovesNodes,view);
                    }
                    MoveNode bestNode = new MoveNode(bestmove);
                    for(MoveNode node : nextMovesNodes){
                        Move move = node.move();
                        if (move instanceof TicketMove){
                            TicketMove tmove = (TicketMove) move;
                            int tmp = scorenode(view,tmove.destination());
                            if(tmp>best){
                                best = tmp;
                                bestNode = node;
                            } 
                        } 
                    }
                    for(MoveNode node : nextMovesNodes){
                        Move move = node.move();
                        if (move instanceof DoubleMove){
                            DoubleMove Dmove = (DoubleMove) move;
                            int tmp = scorenode(view,Dmove.finalDestination());
                            //not sure on these numbers basically want to conserve double moves for when they make big difference
                            if(tmp > best+40 || tmp > best*2.5){
                                best = tmp;
                                bestNode = node;
                            }
                        }
                    }
                    
                    for(int i = 0; i<depth ;i++){
                        bestNode = bestNode.previous();
                    }
                    bestmove = bestNode.move();
                    
                    
                    System.out.println("3 Move Best:"+best+ "Move  "+ bestmove);
                                        
                    System.out.println("Best:"+best);
		    // picks best move
		    callback.accept(bestmove);

		}              
                
            private Set<MoveNode> nextMovesNodes(Set<MoveNode> moves, ScotlandYardView view) {
                int bestsofar = 0;
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
                                int score = scorenode(view,edge.destination().value());
                                if(score >= bestsofar){
                                    bestsofar = score;
                                    if (view.getPlayerTickets(Black, Ticket.fromTransport(edge.data())) >= 1){
                                        TicketMove tmove = new TicketMove(Black,Ticket.fromTransport(edge.data()),edge.destination().value());
                                        MoveNode node = new MoveNode(tmove);
                                        node.setprevious(move);
                                        move.setnext(node);
                                        nextMovesNodes.add(node);
                                    }
                                    if (view.getPlayerTickets(Black,Secret) >= 1){
                                        TicketMove tmove = new TicketMove(Black,Secret,edge.destination().value());
                                        MoveNode node = new MoveNode(tmove);
                                        node.setprevious(move);
                                        move.setnext(node);
                                        nextMovesNodes.add(node);
                                    }
                                }
                            }   
                        }  
                    }
                }
                if(view.getPlayerTickets(Black, Double)>0){
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
                                int score = scorenode(view,edge.destination().value());
                                if(score >= bestsofar){
                                    bestsofar = score;
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
                    nextMovesNodes.addAll(toAdd);
                }
                return nextMovesNodes;
            }   
        
        private static int scorenode(ScotlandYardView view, int location){
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
                    //is next spot empty
                    boolean empty = true;
                    for(Colour player : view.getPlayers())
                    {
                        if(player!=Black)
                        {
                            if(view.getPlayerLocation(player)==edge.destination().value()){
                                empty = false;
                            }
                        }
                    } 

                    if (empty) {
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
            }
            int[] distance = new int[graph.size()+1];
            int totaldistance = 0;
            //gives you shortest distance to each node from starting location
            distance = Dijkstras(location, graph);
            
            //Adds up the distance of all the detectives from node.
            for(Colour player : view.getPlayers()){
                if(player.isDetective())
                {
                    //discourage going 1 move away from detectives or 2
                    if(distance[view.getPlayerLocation(player)] < 2) totaldistance -= 10;
                    else if(distance[view.getPlayerLocation(player)] < 3) totaldistance -= 5;
                    totaldistance += distance[view.getPlayerLocation(player)];
                }
            }
            
            //calc score based on weighted combination of above
            score = edgescore + (totaldistance/(view.getPlayers().size()-1)*20);
            //check if gameover?? if we can and won score massive if loss score 0
            return score;
        }
            
        private static int[] Dijkstras(int location, Graph<Integer, Transport> graph){
            //Dijkstra's algorithm
            
            List <Node<Integer>> UnSettledNodes = new ArrayList<>();
            UnSettledNodes.addAll(graph.getNodes());
            List <Node<Integer>> SettledNodes = new ArrayList<>();
            
            int[] distance = new int[UnSettledNodes.size()+1];
            //Add Starting Point
            SettledNodes.add(graph.getNode(location));
            UnSettledNodes.remove(graph.getNode(location));
            distance[location]=0;
            
            for(Node<Integer> node : UnSettledNodes)
            {
                distance[node.value()] = 9999;
            }

            while(!UnSettledNodes.isEmpty()){
                
                for(Node<Integer> node :SettledNodes){
                    Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(node);
                    for (Edge<Integer, Transport> edge : edges) {
                        //Dont count boat edges as detectives can use them
                        if(edge.data()!=Boat){
                            if(!SettledNodes.contains(edge.destination())){
                                if(distance[edge.destination().value()] > distance[node.value()] + 1) distance[edge.destination().value()] = distance[node.value()] + 1;
                            }
                        }
                    }
                }
                List <Node<Integer>> toAdd = new ArrayList<>();
                int min = 9999;
                for(Node<Integer> node :UnSettledNodes){
                    if(distance[node.value()] < min){
                        min = distance[node.value()];
                    }
                }
                for(Node<Integer> node :UnSettledNodes){
                    if(distance[node.value()] == min){
                        toAdd.add(node);
                    }
                }
                SettledNodes.addAll(toAdd);
                UnSettledNodes.removeAll(toAdd);
            }
            return distance;
        }            
    }
}