package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Double;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Secret;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Underground;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move> {
    
        // Boolean for each round indicating whether it's a reveal round
        List<Boolean> rounds;
        // The map
        Graph<Integer, Transport> graph;
        
        List<ScotlandYardPlayer> playerList = new ArrayList<>();
        int currentPlayer = 0;
        int round = 0;
        int lastKnownMrX = 0;
        Set<Colour> winners = Collections.emptySet();
        
	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {
            
            this.rounds = requireNonNull(rounds);
            if (rounds.isEmpty()) throw new IllegalArgumentException("Empty rounds");
            
            this.graph = requireNonNull(graph);
            if (graph.isEmpty()) throw new IllegalArgumentException("Empty graph");
            
            // Check if MrX is Black
            if (mrX.colour.isDetective()) throw new IllegalArgumentException("MrX should be Black");
	
            // Make a list of configurations (players)
            ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
            configurations.add(mrX);
            configurations.add(firstDetective);
            for (PlayerConfiguration configuration : restOfTheDetectives)
               configurations.add(requireNonNull(configuration));
            
            Set<Integer> locations = new HashSet<>();
            Set<Colour> colours = new HashSet<>();
            int numTicketTypes;
            
            // for each player, and for each of their tickets
            for (PlayerConfiguration c : configurations) {
                
                numTicketTypes = 0;
                
                for (Map.Entry<Ticket,Integer> ticket : c.tickets.entrySet()) {
                    
                    Ticket key = ticket.getKey();
                    Integer count = ticket.getValue();
                    
                    if (key == Double || key == Secret) {
                        
                        if (count > 0 && c.colour != Black)
                            throw new IllegalArgumentException("Players Aren't Allowed Double or SecretTickets");
                        
                    }
                    else if (key != Bus && key != Taxi && key != Underground)
                        throw new IllegalArgumentException("Illegal Ticket Type");
                    
                    numTicketTypes++;                        
                }
                
                if(numTicketTypes != 5)
                    throw new IllegalArgumentException("Tickets Are Missing");
                
                if (locations.contains(c.location))
                        throw new IllegalArgumentException("Duplicate location");
                locations.add(c.location);
                
                if (colours.contains(c.colour))
                        throw new IllegalArgumentException("Duplicate colour");
                colours.add(c.colour);
                
                ScotlandYardPlayer player = new ScotlandYardPlayer(c.player, c.colour, c.location, c.tickets);
                playerList.add(player);
            }
	}

	@Override
	public void registerSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void startRotate() {
            round++;
            ScotlandYardPlayer player = playerList.get(currentPlayer);
            player.player().makeMove(this, player.location(), validMoves(), this);
        }
        
        @Override
        public void accept(Move move){
            
            // check if the move is accepted
            boolean accepted = false;
            for (Move valid : validMoves()) {
                if ( move == valid ) {
                    accepted = true;
                    break;
                }
            }
            if ( !accepted ) throw new IllegalArgumentException("Invalid Move");
            
            else{
                // end of rotation, go back to mrX for startRotate()
                /*
                
                isn't " playerList.size()-1 " the last detective?
                if so this statement will skip the last player's move?
                
                */
                if(currentPlayer == playerList.size()-1)
                    currentPlayer = 0;
                
                else
                {
                    if (move instanceof TicketMove)
                    {
                        TicketMove ticketMove = (TicketMove) move;
                        ScotlandYardPlayer player = playerList.get(currentPlayer);
                        
                        /*for(Spectator spectator : spectators){
                        //    spectator.onMoveMade(this, move);
                        }*/
                        
                        
                        //make the move
                        player.location(ticketMove.destination());
                        
                        //remove tickets
                        player.removeTicket(ticketMove.ticket());
                        // lol //player.tickets().put(ticketMove.ticket(), player.tickets().get(ticketMove.ticket())-1);
                        
                        //if detective give mrx ticket
                        if (currentPlayer != 0)
                            playerList.get(0).addTicket(ticketMove.ticket());// lol again //playerList.get(0).tickets().put(ticketMove.ticket(),playerList.get(0).tickets().get(ticketMove.ticket())+1);
                    }
                    // DoubleMove
                    
                    // PassMove
                    
                    
                    
                    // initiate the next move
                    currentPlayer++;
                    ScotlandYardPlayer player = playerList.get(currentPlayer);
                    player.player().makeMove(this, player.location(), validMoves(), this);
                }

            }
        }
     
        

        private Set<Move> validMoves(){
            int playerLocation = getPlayerLocation(playerList.get(currentPlayer).colour());
            Set<Move> validMoves = Collections.emptySet();
            
            if(graph.containsNode(playerLocation)){
                Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(playerLocation));
                for (Edge<Integer, Transport> edge : edges) {
                    if (playerList.get(currentPlayer).hasTickets(Ticket.fromTransport(edge.data()))) {
                        //is next spot empty
                        boolean empty = true;
                        for(ScotlandYardPlayer player : playerList)
                        {
                            if(player.location()==edge.destination().value()){
                                empty = false;
                            }
                        } 
                        if(empty){
                            Move move = new TicketMove(playerList.get(currentPlayer).colour(),Ticket.fromTransport(edge.data()),edge.destination().value());
                            validMoves.add(move);
                            
                            if(currentPlayer==0)
                            {
                                //double moves
                            }
                        }
                    }
                }
            }
            if(validMoves.isEmpty() /*Collections.emptySet()*/){
                validMoves.add(new PassMove(playerList.get(currentPlayer).colour()));
            }

            
            return validMoves;
        }
        
        
	@Override
	public Collection<Spectator> getSpectators() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Colour> getPlayers() {
            List<Colour> playerColours = new ArrayList<>();
            
            for(ScotlandYardPlayer player : playerList)
                playerColours.add(player.colour());

            return Collections.unmodifiableList(playerColours);
	}

	@Override
	public Set<Colour> getWinningPlayers() {
            return Collections.unmodifiableSet(winners);
	}

	@Override
	public int getPlayerLocation(Colour colour) {
            if (colour != Black)
            {
                for (ScotlandYardPlayer player : playerList) {
                    if (player.colour() == colour) return player.location();
                }
            }
            return lastKnownMrX;
	}

	@Override
	public int getPlayerTickets(Colour colour, Ticket ticket) {
            int ticketcount = 0;
            for (ScotlandYardPlayer player : playerList)
                   if (player.colour() == colour) ticketcount = player.tickets().get(ticket);

            return ticketcount;
	}

	@Override
	public boolean isGameOver() {
            boolean gameOver = false;
            //if it's the last round game over
            //for ( player : playerlist)

            return gameOver;
	}

	@Override
	public Colour getCurrentPlayer() {
		return playerList.get(currentPlayer).colour();
	}

	@Override
	public int getCurrentRound() {
		return round;
	}

	@Override
	public boolean isRevealRound() {
		return rounds.get(getCurrentRound());
	}

	@Override
	public List<Boolean> getRounds() {
		return Collections.unmodifiableList(rounds);
	}

	@Override
	public Graph<Integer, Transport> getGraph() {
                return new ImmutableGraph<Integer, Transport>(graph);
                
	}

}
