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
public class ScotlandYardModel implements ScotlandYardGame {

        List<Boolean> rounds;
        Graph<Integer, Transport> graph;
        List<ScotlandYardPlayer> playerlist = new ArrayList<ScotlandYardPlayer>();
        
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
                playerlist.add(player);
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
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Collection<Spectator> getSpectators() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Colour> getPlayers() {
            List<Colour> playercolours = new ArrayList<Colour> ();
            for(ScotlandYardPlayer player : playerlist)
            {
                playercolours.add(player.colour());
            }    
            return Collections.unmodifiableList(playercolours);
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public int getPlayerLocation(Colour colour) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public int getPlayerTickets(Colour colour, Ticket ticket) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public boolean isGameOver() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Colour getCurrentPlayer() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public int getCurrentRound() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public boolean isRevealRound() {
		// TODO
		throw new RuntimeException("Implement me");
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
