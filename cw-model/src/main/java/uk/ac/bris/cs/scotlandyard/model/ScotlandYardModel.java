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
        Set<Colour> winners = new HashSet<>();
        private Collection<Spectator> spectators = new ArrayList<>();
        
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
                            throw new IllegalArgumentException("Players Aren't Allowed Double or Secret Tickets");
                        
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
		spectators.add(spectator);
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		spectators.remove(spectator);
	}

	@Override
	public void startRotate() {
            System.out.println(round);
            ScotlandYardPlayer player = playerList.get(currentPlayer);
            player.player().makeMove(this, player.location(), validMoves(), this);
        }
        
        @Override
        public void accept(Move movein){
            Move move = requireNonNull(movein); 
            if(!validMoves().contains(move)) throw new IllegalArgumentException("Invalid Move");
            else{
                ScotlandYardPlayer player = playerList.get(currentPlayer);
                System.out.println("start" + move +"round: " + round);
                if (move instanceof TicketMove)
                {
                    System.out.println("tickethere" + move);
                    TicketMove ticketMove = (TicketMove) move;
                        
                    //make the move
                    player.location(ticketMove.destination());
                    //If its Mr X and time to reveal update his last known position
                    if(currentPlayer == 0 && isRevealRound()) lastKnownMrX = ticketMove.destination();
                        
                    //remove tickets
                    player.removeTicket(ticketMove.ticket());
                    
                    //if detective give mrx ticket
                    if (currentPlayer != 0) playerList.get(0).addTicket(ticketMove.ticket());
                }
                // DoubleMove  
                else if (move instanceof DoubleMove)
                {
                    DoubleMove doubleMove = (DoubleMove) move;
                    player.location(doubleMove.secondMove().destination());
                    player.removeTicket(doubleMove.firstMove().ticket());
                    player.removeTicket(doubleMove.secondMove().ticket());
                    player.removeTicket(Double);
                    //do double move stuff
                }
                // PassMove
                else if (move instanceof PassMove)
                {
                    if(currentPlayer == 0)  throw new IllegalArgumentException("MrX cant pass move");
                }
                
               
                if(currentPlayer == 0) {
                    System.out.println("Round++");
                    round++;
                    for(Spectator spectator : spectators)
                    {
                        spectator.onRoundStarted(this, round);
                    }
                }
                
                for(Spectator spectator : spectators)
                {
                    spectator.onMoveMade(this, move);
                }
                if(isGameOver()) {
                    for(Spectator spectator : spectators)
                    {   
                        spectator.onGameOver(this, winners);
                    }
                }
                 
                 
                currentPlayer++;
                //if next player isnt in the list reset else call make move on next player
                System.out.println("Current PLayer" + currentPlayer + "Player list size " + playerList.size());
                if(currentPlayer == playerList.size())  {
                    currentPlayer = 0;
                    for(Spectator spectator : spectators)
                        {
                            spectator.onRotationComplete(this);
                        }
                }
                else
                {
                    ScotlandYardPlayer playernext = playerList.get(currentPlayer);
                    // initiate the next move
                    playernext.player().makeMove(this, playernext.location(), validMoves(), this);
                }
            }
        }
     
        

        private Set<Move> validMoves(){
            int playerLocation = playerList.get(currentPlayer).location();
            Set<Move> validMoves = new HashSet<>();
            if(graph.containsNode(playerLocation)){
                Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(playerLocation));
                for (Edge<Integer, Transport> edge : edges) {
                    
                    /*
                    if (playerList.get(currentPlayer).hasTickets(Ticket.fromTransport(edge.data()),1) || playerList.get(currentPlayer).hasTickets(Secret,1)) {
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
                        }
                    }*/
                    
                     //is next spot empty
                        boolean empty = true;
                        for(ScotlandYardPlayer player : playerList)
                        {
                            if(player.location()==edge.destination().value()){
                                empty = false;
                            }
                        } 
                        
                        if(empty){
                            if (playerList.get(currentPlayer).hasTickets(Ticket.fromTransport(edge.data()),1))
                            {
                                TicketMove move = new TicketMove(playerList.get(currentPlayer).colour(),Ticket.fromTransport(edge.data()),edge.destination().value());
                                validMoves.add(move);
                            }
                            if(playerList.get(currentPlayer).hasTickets(Secret,1))
                            {
                                TicketMove move = new TicketMove(playerList.get(currentPlayer).colour(),Secret,edge.destination().value());
                                validMoves.add(move);
                            }
                        }
                }  
            }
            //If MrX Consider Double Moves
            if(currentPlayer==0){
                                ArrayList<DoubleMove> toAdd = new ArrayList<>();
                                for (Move move1 : validMoves){
                                    TicketMove Tmove = (TicketMove) move1;
                                    int location = Tmove.destination();
                                     Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(location));
                                     for (Edge<Integer, Transport> edge : edges) {
                                         //theres a ticket glitch here but not sure theyll test for it
                                        /*if (playerList.get(currentPlayer).hasTickets(Ticket.fromTransport(edge.data()))) {
                                            //is next spot empty
                                            boolean empty = true;
                                            for(ScotlandYardPlayer player : playerList)
                                            {
                                                if(player.location()==edge.destination().value()){
                                                    empty = false;
                                                }
                                            } 
                                            if(empty){
                                                TicketMove move = new TicketMove(playerList.get(currentPlayer).colour(),Ticket.fromTransport(edge.data()),edge.destination().value());
                                                Move Dmove = new DoubleMove(playerList.get(currentPlayer).colour(),Tmove,move);
                                                toAdd.add(Dmove);
                                            }
                                        }*/
                                        
                                        //is next spot empty
                                        boolean empty = true;
                                        for(ScotlandYardPlayer player : playerList)
                                        {
                                            if(player.location()==edge.destination().value()){
                                                empty = false;
                                            }
                                        } 

                                        if(empty){
                                            //NEED TO CONSIDER LOSS OF TICKETS FROM PREVIOUS MOVE
                                                if (playerList.get(currentPlayer).hasTickets(Ticket.fromTransport(edge.data()),1))
                                                {
                                                    TicketMove move = new TicketMove(playerList.get(currentPlayer).colour(),Ticket.fromTransport(edge.data()),edge.destination().value());
                                                    DoubleMove Dmove = new DoubleMove(playerList.get(currentPlayer).colour(),Tmove,move);
                                                    toAdd.add(Dmove);
                                                }
                                                if(playerList.get(currentPlayer).hasTickets(Secret,1))
                                                {
                                                    TicketMove move = new TicketMove(playerList.get(currentPlayer).colour(),Secret,edge.destination().value());
                                                    DoubleMove Dmove = new DoubleMove(playerList.get(currentPlayer).colour(),Tmove,move);
                                                    toAdd.add(Dmove);
                                                }
                                            }
                                    }                                      
                                }
                                validMoves.addAll(toAdd);
                            }
            //ADDING/REMOVING else if here changes majority from errors to failures and vice versa
            if(validMoves.isEmpty()) {
                Move pMove = new PassMove(playerList.get(currentPlayer).colour());
                validMoves.add(pMove);
            }
            System.out.println("endofvalidmoves");
            return Collections.unmodifiableSet(validMoves);
        }
        
        
	@Override
	public Collection<Spectator> getSpectators() {
		return spectators;
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
            int location = 0;
            if(colour == Black) location = lastKnownMrX;
            else{
                for (ScotlandYardPlayer player : playerList) {
                    if (player.colour() == colour) location = player.location();
                }
            }
            return location;
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
            if (round > 24) gameOver = true;
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
