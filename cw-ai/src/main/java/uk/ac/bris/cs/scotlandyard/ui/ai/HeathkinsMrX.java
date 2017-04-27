package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import uk.ac.bris.cs.scotlandyard.model.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Double;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Secret;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Underground;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;


@ManagedAI("HeathkinsMrX")
public class HeathkinsMrX implements PlayerFactory {

    @Override
    public Player createPlayer(Colour colour) {
        return new MrXAI();
    }

    public static class MrXAI implements Player {
        //Allows random numbers to be generated
        private final Random random = new Random();
        //How many moves ahead to look(1 is just as what the opponents do u)
        int depth;
        //Stops bug of depth just getting bigger and bigger
        int depthreset = 24; 
        //Instanstiate Scorer Object
        Scorer scorer = new Scorer();
        //Types Of Ticket
        List<Ticket> ticketTypes = new ArrayList<>(Arrays.asList(Bus, Taxi, Underground, Double, Secret));
        // Create a Dijkstras so we can call it later
        static Dijkstras dijkstras = new Dijkstras();
        // Create a Maths object so we can call it later(Max and Min)
        static Maths maths = new Maths();
        //Create Set Of Move Nodes
        Set<DataNode> nextMovesNodes  = new HashSet<>();
        //Store timeout data
        long endtime;

	@Override
	public void makeMove(ScotlandYardView view, int location, Set<Move> moves,Consumer<Move> callback){
            System.out.println("MrX Making Move ........");
            //Set the time endpoint to 14 seconds in the future
            long time = System.currentTimeMillis();
            endtime = time+14000;
            //Sets up depth properly for function - to end of game
            depth = (depthreset-view.getCurrentRound()) * view.getPlayers().size();
            //Makes sure no moves are present from the last MrX move
            nextMovesNodes.clear();
            Graph<Integer, Transport> graph = view.getGraph();
            
            //Build PlayerList
            List<PlayerData> playerList = new ArrayList<>();
            for(Colour c : view.getPlayers()){
                Map<Ticket, Integer> tickets = new HashMap<>();
                for(Ticket t : ticketTypes) tickets.put(t, view.getPlayerTickets(c, t));
                PlayerData player = new PlayerData(c, view.getPlayerLocation(c),tickets);
                playerList.add(player);
            }
            //Makes Black Location Correct
            playerList.get(0).location(location);
            
            //Initialise bestmove to a random move
            Move bestmove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
            //Iniatilise Best Node
            DataNode bestNode = new DataNode(playerList,bestmove); 
                                       
            //Set up startNode
            PassMove pMove = new PassMove(Black);
            DataNode startNode = new DataNode(playerList, pMove);
            startNode.score(-9999);
                    
            //Add all Ticket moves to our DataNode Set
            for(Move move : moves){
                if (move instanceof TicketMove){
                    List<PlayerData> newPD = new ArrayList<>();
                    //Stops it altering original list objects
                    for(PlayerData p : playerList) newPD.add(p.clone());
                    TicketMove tmove = (TicketMove) move;
                    //Adjust tickets and location accordingly
                    newPD.get(0).location(tmove.destination());
                    newPD.get(0).adjustTicketCount(tmove.ticket(), -1);
                    //Build node and link it up
                    DataNode node = new DataNode(newPD, move);
                    node.setprevious(startNode);
                    startNode.setnext(node);
                    //Add to set of nodes
                    nextMovesNodes.add(node);
                }
            }        
            //Run alphabeta on the startNode to find the best option, alphabest is the score of the best option
            int alphabest = alphabeta(startNode,-999999,999999, graph, 0);
            
            //Look which move follows the best path found by alphabeta
            for(DataNode node : startNode.next()){
                if (alphabest == node.score()){     
                    bestNode = node;
                    //Means we dont use secret tickets unnecessarily
                    if(((TicketMove)node.move()).ticket() != Secret) break;
                }
            }                    

            //Assign the move stored in bestNode to bestmove
            bestmove = bestNode.move();


            //Choose Secret if last round location was revealed and we still have secret remaining
            if(view.getCurrentRound()!=0){
                if(startNode.playerList().get(0).hasTickets(Secret, 1) && view.getRounds().get(view.getCurrentRound()-1)){
                    bestmove = new TicketMove(Black,Secret,((TicketMove)bestmove).destination());
                }
            }

            //Score this move
            int thismove = 0;                    
            thismove = scorer.scorenode(graph,bestNode.playerList());
            
            TicketMove firstMove = (TicketMove)bestmove;
            //need to work out when to do a double move
            if(thismove<100){
                Move dummyMove = new PassMove(view.getPlayers().get(view.getPlayers().size()-1));
                DataNode dummyNode = new DataNode(bestNode.playerList(),dummyMove);
                nextNode(dummyNode, graph);
                int bestsofar = -9999;
                for(DataNode node : dummyNode.next()){
                    List<PlayerData> newPD = new ArrayList<>();
                    //Stops it altering original list objects by creating a new one for the moving player
                    for(PlayerData p : node.playerList()){ 
                        if(p.colour() == Black )newPD.add(p.clone());
                        else newPD.add(p);
                    }
                    //Adjust PlayerData to reflect game after this move
                    newPD.get(0).location(((TicketMove)node.move()).destination());
                    newPD.get(0).adjustTicketCount(((TicketMove)node.move()).ticket(), -1);
                    int tmp = scorer.scorenode(graph,newPD);
                    if(tmp > bestsofar){
                        bestsofar = tmp;
                        bestNode = node;
                    }
                }
                TicketMove secondMove = (TicketMove)bestNode.move();                            
                DoubleMove dMove = new DoubleMove(Black,firstMove,secondMove);
                bestmove = dMove;
            }

            //Stops a glitch happening - if it does use a random move 
            if(moves.contains(bestmove)) callback.accept(bestmove);
            else{
                //check if it was just an issue with a double move first - use just the first part
                if(moves.contains(firstMove)) callback.accept(firstMove);
                else{
                    //Error been caught - choose random move
                    bestmove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                    callback.accept(bestmove);
                }    
            }            
	}  
                
        private int alphabeta(DataNode node, int alpha,int beta, Graph<Integer, Transport> graph,int depth){  
            //If over time return with score of this node
            if(System.currentTimeMillis() >= endtime) { 
                node.score(scorer.scorenode(graph,node.playerList())); 
                return node.score();  
            }
                    
            //If MrX captured - return super low score
            if(node.next().contains(node)) return -9996; 
            
            //If 'Leaf' Node - i.e at final depth
            if(depth == this.depth){
                node.score(scorer.scorenode(graph,node.playerList())); 
                return node.score();                
            } 
            
            //Generate more nodes if needed
            if (node.next().isEmpty())
            {
                nextNode(node, graph);

                //If still empty no more nodes can be generated - i.e. score and return
                if(node.next().isEmpty()){
                    node.score(scorer.scorenode(graph,node.playerList())); 
                    return node.score();  
                }
            }
            
            //Get Max - MrX
            if (node.next().get(0).move().colour() == Black){
                int v = -999999;
                for (DataNode child : node.next()){
                    v = maths.max(v, alphabeta(child, alpha, beta, graph, depth + 1));
                    alpha = maths.max(alpha, v);
                    if (beta <= alpha) break;
                }
                node.score(v);
                return v;
            }
            //Get Min - Detective
            else{
                int v = 999999;
                for (DataNode child : node.next()){
                    v = maths.min(v, alphabeta(child, alpha, beta, graph, depth + 1));
                    beta = maths.min(beta, v);
                    if (beta <= alpha) break;   
                }
                node.score(v);
                return v;
            }
        }    
        
        //Returns next moves node given
        private void nextNode(DataNode node, Graph<Integer, Transport> graph) {
            int i = 0;
            //Find who went to make that node
            while(node.playerList().get(i).colour() != node.move().colour()) i++;
            //Go to next player
            i++;
            //Loop around back to start if needed.
            i=i%(node.playerList().size());

            PlayerData player = node.playerList().get(i);
            //Find where out where they are
            int playerLocation = player.location();
            if(graph.containsNode(playerLocation)){
                int[] result = dijkstras.calculate(node.playerList().get(0).location(), graph, -1);

                //Find all paths from current location
                Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(playerLocation));
                //For each path check if the destination is empty then check if they have the tickets needed to follow it
                for(Edge<Integer, Transport> edge : edges) {
                    //Dont add move unless the detective goes towards MrX or stays same distance
                    if(i!=0){ 
                        if(result[playerLocation] < result[edge.destination().value()]) continue;
                    }
                    //is next spot empty
                    boolean empty = true;
                    for(PlayerData player2 : node.playerList()){
                        //Detectives Can land on black to win
                        if(player2.location() == edge.destination().value() && player2.colour()!=Black) empty=false;
                    } 
                    if (empty) {    
                        if (player.hasTickets(fromTransport(edge.data()), 1)){
                            //GetPlayerData
                            List<PlayerData> newPD = new ArrayList<>();
                            //Stops it altering original list objects by creating a new one for the moving player
                            for(PlayerData p : node.playerList()){ 
                            if(player.colour() == p.colour())newPD.add(p.clone());
                            else newPD.add(p);
                            }
                            //Build new ticket
                            TicketMove tmove = new TicketMove(player.colour(),Ticket.fromTransport(edge.data()),edge.destination().value());
                            //Adjust PlayerData to reflect game after this move
                            newPD.get(i).location(edge.destination().value());
                            newPD.get(i).adjustTicketCount(Ticket.fromTransport(edge.data()), -1);
                            //MrX get given detective tickets
                            if(player.colour()!=Black) newPD.get(0).adjustTicketCount(Ticket.fromTransport(edge.data()), +1);

                            //Set up node and make them point correctly
                            DataNode newnode = new DataNode(newPD,tmove);
                            newnode.setprevious(node);
                            node.setnext(newnode);

                            //If MrX is captured loop it to itself so we can detect it
                            if(newPD.get(0).location() == newPD.get(i).location() && i != 0)newnode.setnext(newnode);
                        }
                    }
                }             
            }      
        } 

        
    }
}
