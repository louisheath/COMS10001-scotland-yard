
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Boat;

/**
 *
 * @author Will
 */
public class Dijkstras {
    public int[] calculate(int location, Graph<Integer, Transport> graph){
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
                distance[node.value()] = 9;
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
                //Efficiency - doesnt look at nodes further than 6 away - just leaves them as distance 9
                if(min == 6) break;
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
