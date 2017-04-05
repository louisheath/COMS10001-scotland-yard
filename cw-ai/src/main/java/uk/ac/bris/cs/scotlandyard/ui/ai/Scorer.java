/*
 * Scorer contains method to Score Nodes
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Collection;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;
import uk.ac.bris.cs.scotlandyard.model.Transport;

/**
 *
 * @author Will
 */
public class Scorer {
    //Create a dikstras so we can call it later
    static Dijkstras dijkstras = new Dijkstras();
    public int scorenode(ScotlandYardView view, int location, int depth){
            //Create Variables to store scores in
            int score;
            int edgescore = 0;
            int totaldistance = 0;
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
                        //Score the path based on desirabilty - Boat is most as it allows large movements and Detectives can't follow
                        switch(edge.data()){
                            case Taxi: edgescore = edgescore + 1; break;
                            case Bus: edgescore = edgescore + 6; break;
                            case Underground: edgescore = edgescore + 10; break;
                            case Boat: edgescore = edgescore + 40; break;
                        }
                    }
                }
            }  
            
            //Gives you shortest distance to each node from starting location
            int[] distance = dijkstras.calculate(location, graph);
            
            //Adds up the distance of all the detectives from node.
            for(Colour player : view.getPlayers()){
                if(player.isDetective())
                {
                    //discourage going 1 move away from detectives as this is dangerous
                    if(distance[view.getPlayerLocation(player)] < 2) totaldistance -= 100;
                    else totaldistance += distance[view.getPlayerLocation(player)]-depth;
                }
            }
            //If game over in future situation and MrX isnt one move away from a detective set score large so will be chosen
            if(view.getRounds().size()<= view.getCurrentRound()+depth && totaldistance > view.getPlayers().size()) score = 9999;
            //Calculate score based on weighted combination of edges leaving node and how close it is to detectives
            else score = edgescore + (totaldistance/(view.getPlayers().size()-1)*30);
            return score;
        }
    
}
