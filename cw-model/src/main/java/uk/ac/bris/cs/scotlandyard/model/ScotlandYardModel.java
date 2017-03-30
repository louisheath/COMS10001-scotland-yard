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
import java.util.HashMap;
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
        
        int currentPlayer, round, lastKnownMrX, passMoveCount = 0;
        boolean mrXTrapped, doubling = false;
        
        Set<Colour> winners = new HashSet<>();
        Set<Colour> detectives = new HashSet<>();
        private Collection<Spectator> spectators = new HashSet<>();
        
        Set<Move> validMoves = new HashSet<>();
        
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
            detectives.add(firstDetective.colour);
            for (PlayerConfiguration configuration : restOfTheDetectives){
               configurations.add(requireNonNull(configuration));
               detectives.add(configuration.colour);
            }
            
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
            
            for (Spectator s : spectators){
                if (s.equals(spectator)) throw new IllegalArgumentException("Duplicate spectator");
            }
                
            spectators.add(requireNonNull(spectator));
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {

            spectator = requireNonNull(spectator);
            if (spectators.contains(spectator)) spectators.remove(spectator);
            else throw new IllegalArgumentException("Cant unregister a spectator who wasnt registered");

	}

	@Override
	public void startRotate() {
            System.out.println(round);
            ScotlandYardPlayer player = playerList.get(currentPlayer);
            isGameOver();

            
            validMoves = validMoves();
            player.player().makeMove(this, player.location(), validMoves, this);
        }
        
        @Override
        public void accept(Move movein){
            Move move = requireNonNull(movein); 
            
            if(!validMoves.contains(move) && doubling == false) throw new IllegalArgumentException("Invalid Move");
            
            ScotlandYardPlayer player = playerList.get(currentPlayer);
            System.out.println("start" + move +"round: " + round);
            
            // DoubleMove  
            if (move instanceof DoubleMove)
            {
                DoubleMove doubleMove = (DoubleMove) move;
                doubling = true;
                for(Spectator spectator : spectators)
                {
                    spectator.onMoveMade(this, doubleMove);
                }
                accept(doubleMove.firstMove());
                doubling = false;
                player.removeTicket(Double);
                move = doubleMove.secondMove();
            }
            
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
            
            // PassMove
            else if (move instanceof PassMove)
            {
                if (currentPlayer == 0)  mrXTrapped = true;
                else passMoveCount++;
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
            
            
            //check if game is over
            isGameOver();

            if(!doubling){
                currentPlayer++;
                //if next player isnt in the list reset else call make move on next player
                System.out.println("Current PLayer" + currentPlayer + "Player list size " + playerList.size());
                if(currentPlayer == playerList.size())  
                {
                    currentPlayer = 0;
                    passMoveCount = 0;
                    for(Spectator spectator : spectators)
                        {
                            spectator.onRotationComplete(this);
                        }
                }
                else
                {
                    ScotlandYardPlayer playernext = playerList.get(currentPlayer);
                    // initiate the next move
                    validMoves = validMoves();
                    playernext.player().makeMove(this, playernext.location(), validMoves, this);
                }
            }
            
        }
     
        private Set<Move> validMoves(){
            int playerLocation = playerList.get(currentPlayer).location();
            Set<Move> validMoves = new HashSet<>();
            
            if(graph.containsNode(playerLocation))
            {
                Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(playerLocation));
                for (Edge<Integer, Transport> edge : edges) {
                    
                    //is next spot empty
                    boolean empty = true;
                    for(ScotlandYardPlayer player : playerList)
                    {
                        /*
                        
                        Is this IF correct logic? 
                        -> testMrXMovesOmittedIfDestinationOccupiedByDetectives
                        
                        
                        */
                        if(!player.isMrX())
                        {
                            if(player.location()==edge.destination().value()){
                                empty = false;
                            }
                        }
                    } 

                    if (empty) {
                        if (playerList.get(currentPlayer).hasTickets(Ticket.fromTransport(edge.data()),1))
                        {
                            TicketMove move = new TicketMove(playerList.get(currentPlayer).colour(),Ticket.fromTransport(edge.data()),edge.destination().value());
                            validMoves.add(move);
                        }
                        if (playerList.get(currentPlayer).hasTickets(Secret,1))
                        {
                            TicketMove move = new TicketMove(playerList.get(currentPlayer).colour(),Secret,edge.destination().value());
                            validMoves.add(move);
                        }
                    }
                }  
            }
            //If MrX Consider Double Moves
            /*
            
            added checks for if there are sufficient rounds left to use a double
                             and for if MrX has a double ticket
            
            */
            if(currentPlayer==0 && round < rounds.size()-1 && playerList.get(0).hasTickets(Double))
            {
                ArrayList<DoubleMove> toAdd = new ArrayList<>();
                
                for (Move m : validMoves)
                {
                    TicketMove firstMove = (TicketMove) m;
                    int destination = firstMove.destination();
                    
                    Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(destination));
                    for (Edge<Integer, Transport> edge : edges) 
                    {
                        //is next spot empty
                        boolean empty = true;
                        for(ScotlandYardPlayer player : playerList)
                        {
                            /*
                            
                            Added exemption of Black's location, as with double he can return to his start.
                            
                            */                           
                            if(player.location()==edge.destination().value() && player.colour() != Black)
                                empty = false;
                        } 

                        if (empty)
                        {
                            int ticketsNeeded = 1;
                            if (firstMove.ticket() == Ticket.fromTransport(edge.data())) ticketsNeeded = 2;
                            
                            if (playerList.get(currentPlayer).hasTickets(Ticket.fromTransport(edge.data()),ticketsNeeded))
                            {
                                TicketMove secondMove = new TicketMove(playerList.get(currentPlayer).colour(),Ticket.fromTransport(edge.data()),edge.destination().value());
                                DoubleMove Dmove = new DoubleMove(playerList.get(currentPlayer).colour(),firstMove,secondMove);
                                toAdd.add(Dmove);
                            }
                            /*
                            
                            before, this was
                            if (playerList.get(currentPlayer).hasTickets(Secret,1)
                            
                            
                            */
                            if (playerList.get(currentPlayer).hasTickets(Secret,2) && firstMove.ticket() == Secret
                                    || playerList.get(currentPlayer).hasTickets(Secret,1) && firstMove.ticket() != Secret)
                            {
                                TicketMove secondMove = new TicketMove(playerList.get(currentPlayer).colour(),Secret,edge.destination().value());
                                DoubleMove Dmove = new DoubleMove(playerList.get(currentPlayer).colour(),firstMove,secondMove);
                                toAdd.add(Dmove);
                            }
                        }
                    }                                      
                }
                validMoves.addAll(toAdd);
            }
            
            if(validMoves.isEmpty()) {
                Move Pmove = new PassMove(playerList.get(currentPlayer).colour());
                validMoves.add(Pmove);
            }
            System.out.println("endofvalidmoves");
            return Collections.unmodifiableSet(validMoves);
        }
        
        
	@Override
	public Collection<Spectator> getSpectators() {
		return Collections.unmodifiableCollection(spectators);
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
            boolean isGameOver = false;
            
            //Rounds Over
            /*
            
            Changed this to -1
            
            */
            if (round > rounds.size()-1 )
                winners.add(Black);
            
            //MrX Surrounded
            if (mrXTrapped)
                winners = detectives;
            
            //No Detectives Can Move
            if (passMoveCount == detectives.size())
                winners.add(Black);
            
            int counter = 0;
            int mrXCurrent = playerList.get(0).location();
            
            Map<Ticket, Integer> emptytickets = new HashMap<>();
                emptytickets.put(Ticket.Double, 0);
		emptytickets.put(Ticket.Bus, 0);
		emptytickets.put(Ticket.Underground, 0);
		emptytickets.put(Ticket.Taxi, 0);
		emptytickets.put(Ticket.Secret, 0);
                
            for (ScotlandYardPlayer player : playerList){
                if(player.tickets() == emptytickets && !player.isMrX()) counter++;
                
                //Detective landed On MrX
                if(!player.isMrX() && player.location() == mrXCurrent)
                    winners = detectives;   
            }
            
            //Players Have No Tickets Left
            if (counter == detectives.size())
                winners.add(Black);
            
            if (!winners.isEmpty()){
                isGameOver = true;
                for(Spectator spectator : spectators){   
                    spectator.onGameOver(this, winners);
                }
            }
            return isGameOver;
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