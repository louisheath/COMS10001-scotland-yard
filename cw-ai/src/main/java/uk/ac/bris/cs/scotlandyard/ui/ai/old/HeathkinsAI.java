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


// TODO name the AI
@ManagedAI("HeathkinsAI")
public class HeathkinsAI implements PlayerFactory {

	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
            return new MyAI();
	}

	private static class MyAI implements Player {
            //Allows random numbers to be generated
            private final Random random = new Random();
            //How many moves ahead to look(1 is just as what the opponents do u)
            int depth;
            //Stops bug of depth just getting bigger and bigger
            int depthreset = 10; 
            //Instanstiate Scorer Object
            Scorer2 scorer = new Scorer2();
            //Types Of Ticket
            List<Ticket> ticketTypes = new ArrayList<>(Arrays.asList(Bus, Taxi, Underground, Double, Secret));
            // Create a dikstras so we can call it later
            static DijkstrasClean dijkstras = new DijkstrasClean();
            // Create a math object so we can call it later
            static Maths math = new Maths();
            //Create Set Of Move Nodes
            Set<DataNode> nextMovesNodes  = new HashSet<>();
            //Store timeout data
            long endtime;
            //Store boolean to let us know if timeout occurs
            boolean codecompleted;

		@Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves,Consumer<Move> callback){
                    codecompleted = true;
                    long time = System.currentTimeMillis();
                    endtime = time+14000;
                    //Best Score and node at depth
                    DataNode bestNode;
                    //Sets up depth properly for function - to end of 
                    depth = (depthreset) * view.getPlayers().size();
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
                    System.out.println("Start Location Is: "+location);
                    System.out.println("Score of Start Location: " + scorer.scorenode(graph, playerList));

                    //Initialise bestmove to a random move
                    Move bestmove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                                       
                    //Set up starting DataNode
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
                            newPD.get(0).location(tmove.destination());
                            newPD.get(0).adjustTicketCount(tmove.ticket(), -1);
                            DataNode node = new DataNode(newPD, move);
                            node.setprevious(startNode);
                            startNode.setnext(node);
                            nextMovesNodes.add(node);
                        }
                    }
                    System.out.println("Moves from starting position: "+nextMovesNodes.size());
                    
                    //Iniatilise Best Move
                    bestNode = new DataNode(playerList,bestmove);    
                    
                    //Alpha Beta - used wikipedia psuedo code
                    //https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
                    int alphabest = alphabeta(startNode,-999999,999999, graph, 0);
                    System.out.println("Alpha Best:  "+ alphabest );
                    
                    if(codecompleted){
                    
                        for(DataNode node : startNode.next()){
                            System.out.println("Move: "+ node.move()+ "Score :"+ node.score());
                            if (alphabest == node.score()){     
                                bestNode = node;
                                if(((TicketMove)node.move()).ticket() != Secret) break;
                            }
                        }                    

                        bestmove = bestNode.move();


                        //Choose Secret
                        if(view.getCurrentRound()!=0){
                            if(startNode.playerList().get(0).hasTickets(Secret, 1) && view.getRounds().get(view.getCurrentRound()-1)){
                            bestmove = new TicketMove(Black,Secret,((TicketMove)bestmove).destination());
                            }
                        }

                        int thismove = 0;                    
                        thismove = scorer.scorenode(graph,bestNode.playerList());

                        //need to work out when to do a double move
                        if(thismove<100){
                            System.out.println("Double Move");
                            TicketMove firstMove = (TicketMove)bestmove;
                            TicketMove secondMove = (TicketMove)bestmove;
                            for(DataNode node : bestNode.next()){
                                System.out.println("Move: "+ node.move()+ "Score :"+ node.score());
                                if (alphabest == node.score()){  
                                    bestNode = node;
                                    secondMove = (TicketMove)node.move();
                                    break;
                                }
                            }
                            thismove = scorer.scorenode(graph,bestNode.playerList());
                            DoubleMove dMove = new DoubleMove(Black,firstMove,secondMove);
                            bestmove = dMove;
                        }

                        System.out.println("This Move  "+ bestmove );
                        System.out.println("This Move score: :"+thismove);
                        System.out.println("---------------------------");
                        System.out.println("---------New Move----------");
                        System.out.println("---------------------------");

                        //Stops a glitch happening - if it does use a random move 
                        if(moves.contains(bestmove)) callback.accept(bestmove);
                        else{
                            System.out.println("Error Caught");
                            bestmove = new ArrayList<>(moves).get(random.nextInt(moves.size()));
                            System.out.println("Actual Move" + bestmove);
                            callback.accept(bestmove);
                        }
                    }
                    //Choose a move quickly before true timeout
                    else{
                        System.out.println("Time Out occured");
                        int bscore = -9999;
                        bestmove = new PassMove(Black);
                        for(DataNode node : startNode.next()){
                            int score = scorer.scorenode(graph, node.playerList());
                            if(score > bscore){ bscore = score; bestmove = node.move(); }
                        }
                        System.out.println("Actual Move" + bestmove);
                        callback.accept(bestmove);
                    }
		}  
                
        private int alphabeta(DataNode node, int alpha,int beta, Graph<Integer, Transport> graph,int depth){  
            //If over time return so code finishes
            if(System.currentTimeMillis() >= endtime) { //codecompleted = false;
            node.score(scorer.scorenode(graph,node.playerList())); 
            return node.score();  
            }
                    
            //If MrX captured - return super low score
            if(node.next().contains(node)) return -9996; 
            
            //If 'Leaf' Node - i.e at final depth
            if(depth == this.depth){
                System.out.println("final depth");
                node.score(scorer.scorenode(graph,node.playerList())); 
                return node.score();                
            } 
            
            //Generate more nodes if needed
            if (node.next().isEmpty())
            {
                nextNode(node, graph);

                //If still empty no more nodes can be generated - i.e. score and return
                if(node.next().isEmpty()){
                    System.out.println("yep as i thought, depth: "+depth);
                    node.score(scorer.scorenode(graph,node.playerList())); 
                    return node.score();  
                }
            }
            
            //Get Max - MrX
            if (node.next().get(0).move().colour() == Black){
                int v = -999999;
                for (DataNode child : node.next()){
                    v = math.max(v, alphabeta(child, alpha, beta, graph, depth + 1));
                    alpha = math.max(alpha, v);
                    if (beta <= alpha) break;
                }
                node.score(v);
                return v;
            }
            //Get Min - Detective
            else{
                int v = 999999;
                for (DataNode child : node.next()){
                    v = math.min(v, alphabeta(child, alpha, beta, graph, depth + 1));
                    beta = math.min(beta, v);
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
                    int[] result = dijkstras.calculateto(node.playerList().get(0).location(), graph, -1);

                    //Find all paths from current location
                    Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(playerLocation));
                    //For each path check if the destination is empty then check if they have the tickets needed to follow it
                    for(Edge<Integer, Transport> edge : edges) {
                        //Dont add move unless the detective goes towards MrX
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