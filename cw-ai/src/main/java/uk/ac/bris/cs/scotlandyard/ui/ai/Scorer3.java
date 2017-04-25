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
 Class used to value a node depending on the number of escape routes it has 
 * and how far away it is from the detectives
 */
public class Scorer3 {
    // Create a dikstras so we can call it later
    static Dijkstras dijkstras = new Dijkstras();
    
    public int scorenode(Graph<Integer, Transport> graph, List<PlayerData> playerList){

        // Save a variable for MrX data because it is called frequently
        PlayerData MrX = playerList.get(0);
        int location = MrX.location();
        
        int score = 0;

        // Look at escape routes from the node and their value
        Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(location));
        // Set boolean to see if MrX has tickets to escape from node
        Boolean hasEscape = false;
        // Check if MrX has the tickets needed to follow it
        for (Edge<Integer, Transport> edge : edges) {
            if (MrX.hasTickets(fromTransport(edge.data())) || MrX.hasTickets(Secret));
            {
                hasEscape = true;
                //Score the path based on desirabilty - Boat is most as it allows large movements and Detectives can't follow
                switch(edge.data()){
                    case Taxi: score += 5; break;
                    case Bus: score += 7; break;
                    case Underground: score += 10; break;
                    case Boat: score += 40; break;
                }
            }
        }
        // Stops MrX going to a node from which he cannot escape
        if(!hasEscape) { return -9998; } 

        // Gives you shortest distance to each node from starting location
        int[] distance = dijkstras.calculate(location, graph);
        int totaldistance = 0;
        
        // Adds up the distance of all the detectives from node.
        for(PlayerData player : playerList){
            if (player.colour() != Black) {
                //discourage going 1 move away from detectives as this is dangerous
                if(distance[player.location()] == 1) totaldistance -= 150;
                //score extremely negatively as this means youd lose
                else if(distance[player.location()] < 1) return -999;
                else totaldistance += distance[player.location()];
            }
        }
        // Calculate score based on possible escape routes and average distance from detectives
        score += (totaldistance/(playerList.size()-1)*30);
        return score;
    }
    
}
