/*
 * Scorer contains method to Score Nodes
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Collection;
import java.util.List;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Secret;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;
import uk.ac.bris.cs.scotlandyard.model.Transport;

/**
 *
 * @author Will
 */
public class Scorer2 {
    //Create a dikstras so we can call it later
    static Dijkstras dijkstras = new Dijkstras();
    public int scorenode(Graph<Integer, Transport> graph, List<PlayerData> playerList){
            int location = playerList.get(0).location();
            //Create Variables to store scores in
            int score;
            int edgescore = 0;
            int totaldistance = 0;
            
            //work out how many usuable paths leaving node
            if(graph.containsNode(location))
            {
                //Find all paths from current location
                Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(location));
                //Set boolean to see if MrX has tickets to escape from node
                Boolean hasEscape = false;
                //For each path check if the destination is empty then check if they have the tickets needed to follow it
                for (Edge<Integer, Transport> edge : edges) {
                    if (playerList.get(0).hasTickets(fromTransport(edge.data()), 1) || playerList.get(0).hasTickets(Secret, 1));
                    {
                        hasEscape = true;
                        //Score the path based on desirabilty - Boat is most as it allows large movements and Detectives can't follow
                        switch(edge.data()){
                            case Taxi: edgescore += 1; break;
                            case Bus: edgescore += 6; break;
                            case Underground: edgescore += 10; break;
                            case Boat: edgescore += 40; break;
                        }
                    }
                }
                //Stops MrX going to a node from which he cannot escape
                if(!hasEscape) { return -9998; }
            }  
            
            //Gives you shortest distance to each node from starting location
            int[] distance = dijkstras.calculate(location, graph);
            
            //Adds up the distance of all the detectives from node.
            for(PlayerData player : playerList){
                if(player.colour()!=Black)
                {
                    //discourage going 1 move away from detectives as this is dangerous
                    if(distance[player.location()] == 1) totaldistance -= 150;
                    //score extremely negatively as this means youd lose
                    else if(distance[player.location()] < 1) { return -9998; }
                    else totaldistance += distance[player.location()];
                }
            }
            //Calculate score based on weighted combination of edges leaving node and how close it is to detectives
            score = edgescore + (totaldistance/(playerList.size()-1)*30);
            return score;
        }
    
}
