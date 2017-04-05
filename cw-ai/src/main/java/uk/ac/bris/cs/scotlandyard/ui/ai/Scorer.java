/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    static Dijkstras dijkstras = new Dijkstras();
    public int scorenode(ScotlandYardView view, int location, int depth){
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
                            case Boat: edgescore = edgescore + 40; break;
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
                    if(distance[view.getPlayerLocation(player)] < 2) totaldistance -= 50;
                    else totaldistance += distance[view.getPlayerLocation(player)]-depth;
                }
            }
            //If game over in future situation and MrX isnt one move away from a detective set score large so will be chosen
            if(view.getRounds().size()<= view.getCurrentRound()+depth && totaldistance > view.getPlayers().size()) score = 9999;
            //calc score based on weighted combination of above
            else score = edgescore + (totaldistance/(view.getPlayers().size()-1)*30);
            //check if gameover?? if we can and won score massive if loss score 0
            return score;
        }
    
}
