
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

    public int[] calculate(int pivot, Graph<Integer, Transport> graph){
        
        // initiate structures
        List <Node<Integer>> unsettledNodes = new ArrayList<>();
        unsettledNodes.addAll(graph.getNodes());
        List <Node<Integer>> settledNodes = new ArrayList<>();
        int[] distance = new int[unsettledNodes.size()+1];
        
        // prepare distances
        for (Node<Integer> node : unsettledNodes)
            distance[node.value()] = 7;
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
                if (distance[dest] > distance[pivot/*graph.getNode(pivot).value()*/] + 1 && e.data() != Boat) 
                    distance[dest] = distance[pivot/*graph.getNode(pivot).value()*/] + 1;
            }

            // the pivot node can now be settled
            settledNodes.add(pivotNode);
            unsettledNodes.remove(pivotNode);

            // find new pivot which is the closest unsettled node
            int newDist = 9;
            int newPiv = -1;
            for (Edge<Integer, Transport> e : edges) {
                int dest = e.destination().value();
                if (distance[dest] < newDist && !settledNodes.contains(e.destination())) {
                    newDist = distance[dest];
                    newPiv = dest;
                }
            }

            // repeat with new pivot until the closest node is 4 moves or further
            if (newDist <= 3) pivot = newPiv;
            else return distance;
        }
        
        return distance;
    }  
    public int[] calculateto(int pivot, Graph<Integer, Transport> graph, int endpoint){
        
        // initiate structures
        List <Node<Integer>> unsettledNodes = new ArrayList<>();
        unsettledNodes.addAll(graph.getNodes());
        List <Node<Integer>> settledNodes = new ArrayList<>();
        int[] distance = new int[unsettledNodes.size()+1];
        
        // prepare distances
        for (Node<Integer> node : unsettledNodes)
            distance[node.value()] = 7;
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
                if (distance[dest] > distance[pivot/*graph.getNode(pivot).value()*/] + 1 && e.data() != Boat) 
                    distance[dest] = distance[pivot/*graph.getNode(pivot).value()*/] + 1;
            }

            // the pivot node can now be settled
            settledNodes.add(pivotNode);
            unsettledNodes.remove(pivotNode);

            //We found the distance to the end point
            if(pivot == endpoint) return distance;
            
            // find new pivot which is the closest unsettled node
            int newDist = 9;
            int newPiv = -1;
            for (Edge<Integer, Transport> e : edges) {
                int dest = e.destination().value();
                if (distance[dest] < newDist && !settledNodes.contains(e.destination())) {
                    newDist = distance[dest];
                    newPiv = dest;
                }
            }

            // repeat with new pivot until the closest node is 4 moves or further
            if (newDist <= 3) pivot = newPiv;
            else return distance;
        }
        
        return distance;
    }  
}
