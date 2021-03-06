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
 * Dijkstra's algorithm
 */
public class Dijkstras {  
    
    public int[] calculate(int pivot, Graph<Integer, Transport> graph) {
        return calculate(pivot, graph, 0);
    }
    
    public int[] calculateto(int pivot, Graph<Integer, Transport> graph, int endpoint) {
        return calculate(pivot, graph, endpoint);
    }
    
    /**
    *@param endpoint
    * 
    * any integer between 0 and 200 is a valid endpoint
    * 0 calculates distances up to 5 nodes away
    * -1 calculates distances for the entire graph
    *   
    */

    // wholeTree adds an option to calculate distances for the entire tree
    public int[] calculate(int pivot, Graph<Integer, Transport> graph, int endpoint){

        // initiate structures
        List<Node<Integer>> unsettledNodes = new ArrayList<>();
        unsettledNodes.addAll(graph.getNodes());
        int[] distance = new int[200];
        
        // prepare distances
        for (Node<Integer> node : unsettledNodes)
            distance[node.value()] = 100;
        distance[pivot] = 0;
        
        // find new distances of nodes next to pivot
        // find new pivot which is the closest unsettled node
        // repeat with new pivot until the closest node is 6 moves or further
        while (!unsettledNodes.isEmpty()) {
            
            Node<Integer> pivotNode = graph.getNode(pivot);
            Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(pivotNode);  
            
            // find new distances of nodes next to pivot
            for (Edge<Integer, Transport> e : edges) {
                int dest = e.destination().value();
                if (distance[dest] > distance[pivot] + 1 && e.data() != Boat) 
                    distance[dest] = distance[pivot] + 1;
            }

            // the pivot node can now be settled
            unsettledNodes.remove(pivotNode);
            if (unsettledNodes.isEmpty()) return distance;
            
            //Got to endpoint so return
            if (pivot == endpoint) return distance;

            // find new pivot which is the closest unsettled node
            int newDist = 100;
            int newPiv = -1;
            for (Node<Integer> n : unsettledNodes) {
                int nodeNum = n.value();
                if (distance[nodeNum] < newDist) {
                    newDist = distance[nodeNum];
                    newPiv = nodeNum;
                }
            }

            // repeat with new pivot until the closest node is 5 moves or further
            if (newDist > 4 && endpoint == 0) return distance;
            else pivot = newPiv;
        }
        
        return distance;
    }  
}