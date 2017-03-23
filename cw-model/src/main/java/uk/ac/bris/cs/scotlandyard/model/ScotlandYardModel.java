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

        List<Boolean> rounds;
        Graph<Integer, Transport> graph;
        List<ScotlandYardPlayer> playerList = new ArrayList<ScotlandYardPlayer>();
        int currentPlayer = 0;
        int round = 0;
        int lastKnownBlack = 0;
        Set<Colour> winners = Collections.emptySet();
        
	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {
            
            this.rounds = requireNonNull(rounds);
            this.graph = requireNonNull(graph);
            
            if (rounds.isEmpty()) {
            throw new IllegalArgumentException("Empty rounds");
            }
            
            if (graph.isEmpty()) {
            throw new IllegalArgumentException("Empty graph");
            }
            
            if (mrX.colour != Black) { // or mr.colour.isDetective()
            throw new IllegalArgumentException("MrX should be Black");
            } 
	
            ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
            for (PlayerConfiguration configuration : restOfTheDetectives)
               configurations.add(requireNonNull(configuration));
            configurations.add(0, firstDetective);
            configurations.add(0, mrX);
                
            Set<Integer> set = new HashSet<>();
            Set<Colour> colours = new HashSet<>();
            Set<Ticket> ticketset = new HashSet<>();
            int typecount = 0;
            
            for (PlayerConfiguration configuration : configurations) {
                
                for ( Map.Entry<Ticket,Integer> ticket : configuration.tickets.entrySet()) {
                    if (ticket.getKey() == Double || ticket.getKey() == Secret )
                        if(ticket.getValue() > 0 && configuration.colour != Black)
                            throw new IllegalArgumentException("Players Aren't Allowed Double or SecretTickets");
                        else typecount++;
                    else if(ticket.getKey() != Bus && ticket.getKey() != Taxi && ticket.getKey() != Underground)
                        throw new IllegalArgumentException("Illegal Ticket Type");
                    else typecount++;                        
                }
                
                if(typecount != 5)
                    throw new IllegalArgumentException("Tickets Are Missing");
                typecount = 0;

                
                if (set.contains(configuration.location))
                        throw new IllegalArgumentException("Duplicate location");
                set.add(configuration.location);
                
                if (colours.contains(configuration.colour))
                        throw new IllegalArgumentException("Duplicate colour");
                colours.add(configuration.colour);
                
                ScotlandYardPlayer player = new ScotlandYardPlayer(configuration.player,configuration.colour,configuration.location,configuration.tickets);
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
            ScotlandYardPlayer player = playerList.get(0);
            player.player().makeMove(this, player.location(), validMoves(), this);
        }
        
        @Override
        public void accept(Move move){
            boolean accepted = false;
            for (Move valid : validMoves()) {
                if ( move == valid ) accepted = true;
            }
            if ( !accepted ) throw new IllegalArgumentException("Invalid Move");
            else{
                // end of rotation, go back to mrX for startRotate()
                if(currentPlayer == playerList.size()-1)
                {
                    currentPlayer = 0;
                }
                else
                {
                    if (move instanceof TicketMove)
                    {
                        TicketMove ticketMove = (TicketMove) move;
                        /*for(Spectator spectator : spectators){
                        //    spectator.onMoveMade(this, move);
                        }*/
                        //make the move
                        playerList.get(currentPlayer).location(ticketMove.destination());
                        
                        //remove tickets
                        playerList.get(currentPlayer).tickets().put(ticketMove.ticket(),playerList.get(currentPlayer).tickets().get(ticketMove.ticket())-1);
                        
                        //if detective give mrx ticket
                        if(currentPlayer!=0)
                        {
                            playerList.get(0).tickets().put(ticketMove.ticket(),playerList.get(0).tickets().get(ticketMove.ticket())+1);
                        }
                    }
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
            List<Colour> playercolours = new ArrayList<Colour> ();
            for(ScotlandYardPlayer player : playerList)
            {
                playercolours.add(player.colour());
            }    
            return Collections.unmodifiableList(playercolours);
	}

	@Override
	public Set<Colour> getWinningPlayers() {
            return Collections.unmodifiableSet(winners);
	}

	@Override
	public int getPlayerLocation(Colour colour) {
		if(colour != Black)
                {
                    for (ScotlandYardPlayer player : playerList) {
                        if (player.colour() == colour) return player.location();
                    }
                }
		return lastKnownBlack;
	}

	@Override
	public int getPlayerTickets(Colour colour, Ticket ticket) {
            int ticketcount = 0;
		 for (ScotlandYardPlayer player : playerList) {
                        if (player.colour() == colour) ticketcount = player.tickets().get(ticket);
                    }
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
