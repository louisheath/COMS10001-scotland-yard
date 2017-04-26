
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        return calculate(pivot, graph, false);
    }
    
    // wholeTree adds an option to calculate distances for the entire tree
    public int[] calculate(int pivot, Graph<Integer, Transport> graph, boolean wholeTree){
    System.out.println("Dijkstras, startPivot: "+pivot);

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
            if (newDist > 4 && !wholeTree) return distance;
            else pivot = newPiv;
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
            distance[node.value()] = 21;
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
            settledNodes.add(pivotNode);
            unsettledNodes.remove(pivotNode);
            
            //Got to endpoint so return
            if(pivot == endpoint) return distance;

            // find new pivot which is the closest unsettled node
            int newDist = 9;
            int newPiv = -1;
            for (Node<Integer> n : unsettledNodes) {
                int nodeNum = n.value();
                if (distance[nodeNum] < newDist) {
                    newDist = distance[nodeNum];
                    newPiv = nodeNum;
                }
            }

            // repeat with new pivot until the closest node is 21 moves or further
            if (newDist <= 20) pivot = newPiv;
            else return distance;
        }
      return distance;
    }  
}
