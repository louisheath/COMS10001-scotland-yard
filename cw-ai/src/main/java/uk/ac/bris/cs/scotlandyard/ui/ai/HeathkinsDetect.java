package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collection;
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
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Taxi;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Underground;


@ManagedAI("HeathkinsDetect")
public class HeathkinsDetect implements PlayerFactory {
        
	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
            return new MyAI();
	}

	// TODO A sample player that selects a random move
	private static class MyAI implements Player {
            
            private final Random random = new Random();

		@Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves,Consumer<Move> callback) {
                    Colour colour = view.getCurrentPlayer();
                    Move bestmove = new PassMove(colour);
                    Graph<Integer, Transport> graph = view.getGraph();
                    int MrXLastKnown = view.getPlayerLocation(Black);
                    int rounds = 0;
                    //Work out how many rounds since MrX Surfaced
                    while(!view.getRounds().get(view.getCurrentRound()-rounds))
                    {
                        rounds++;
                        if(view.getCurrentRound()-rounds == 0) break;
                    }
                    
                    
                    
                    //Get to good transport links MrX hasnt surfaced yet
                    if(view.getCurrentRound() == rounds)
                    {
                        int best = -9999;
                        for(Move move : moves){
                            if (move instanceof TicketMove){
                                TicketMove tmove = (TicketMove) move;
                                int tmp = scorenode(view,tmove.destination());
                                if(tmp>best){
                                    best = tmp;
                                    bestmove = move;
                                }
                            }
                        }
			
                    }
                    else bestmove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                    
                    
                    
                    //Where could MrX be?
                    List<Node> possibleLocations = new ArrayList<>();
                    
                    
                    
                            
                    // picks best movegit 
                    callback.accept(bestmove);
                    
		}
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
                        if (view.getPlayerTickets(view.getCurrentPlayer(), fromTransport(edge.data()))>0);
                        {
                            switch(edge.data()){
                                case Taxi: edgescore+=1;
                                case Bus: edgescore+=3;
                                case Underground: edgescore+=6;
                            }
                        }
                    }
                }  
            }
           
            //calc score based on weighted combination of above
            score = edgescore;
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
                        if(!SettledNodes.contains(edge.destination())){
                            if(distance[edge.destination().value()] > distance[node.value()] + 1) distance[edge.destination().value()] = distance[node.value()] + 1;
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