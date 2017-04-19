
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Boat;


/**
 * Dijkstra's algorithm
 */
public class Dijkstras2 {  

    public int[] calculate(int pivot, Graph<Integer, Transport> graph){
        
        // initiate structures
        List <Node<Integer>> unsettledNodes = new ArrayList<>();
        unsettledNodes.addAll(graph.getNodes());
        List <Node<Integer>> settledNodes = new ArrayList<>();
        int[] distance = new int[unsettledNodes.size()+1];
        
        // prepare distances
        for (Node<Integer> node : unsettledNodes)
            distance[node.value()] = 99;
        distance[pivot] = 0;
        
        // find new distances of nodes next to pivot
        // find new pivot which is the closest unsettled node
        // repeat with new pivot until the closest node is 6 moves or further
        while (!unsettledNodes.isEmpty()) {
            Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(pivot));
            
            // exclude any settled nodes
            Collection<Edge<Integer, Transport>> toRemove = new HashSet<>();
            for (Edge<Integer, Transport> e : edges)
                if (settledNodes.contains(e.destination())) toRemove.add(e);
            edges.removeAll(toRemove);
            
            // find new distances of nodes next to pivot
            for (Edge<Integer, Transport> e : edges)
                if (distance[e.destination().value()] > distance[graph.getNode(pivot).value()] + 1 && e.data() != Boat) 
                    distance[e.destination().value()] = distance[graph.getNode(pivot).value()] + 1;

            // the pivot node can now be settled
            settledNodes.add(graph.getNode(pivot));
            unsettledNodes.remove(graph.getNode(pivot));

            // find new pivot which is the closest unsettled node
            int newDist = 99;
            int newPiv = -1;
            for (Edge<Integer, Transport> e : edges)
                if (distance[e.destination().value()] < newDist) {
                    newDist = distance[e.destination().value()];
                    newPiv = e.destination().value();
                }

            // repeat with new pivot until the closest node is 6 moves or further
            if (newDist <= 5) pivot = newPiv;
            else return distance;
        }
        
        return distance;
    }  
    
}
