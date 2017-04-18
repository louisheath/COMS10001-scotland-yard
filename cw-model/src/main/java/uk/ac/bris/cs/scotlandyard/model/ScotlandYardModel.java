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

public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move> {
    
        // Boolean for each round indicating whether it's a reveal round
        List<Boolean> rounds;
        // The map
        Graph<Integer, Transport> graph;
        // List of all players - MrX at index 0
        List<ScotlandYardPlayer> playerList = new ArrayList<>();
        //Keeps track of Game state - Could combine somehow??
        int currentPlayer, round, lastKnownMrX = 0;
        // Boolean indicating whether the current move is the first in a double
        boolean doubling = false;

        Set<Colour> winners = new HashSet<>();
        Set<Colour> detectives = new HashSet<>();
        private Collection<Spectator> spectators = new HashSet<>();
        
        // validMoves set to store validMoves in to reduce number of calls
        Set<Move> validMoves = new HashSet<>();
        
	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {
            
            this.rounds = requireNonNull(rounds);
            if (rounds.isEmpty()) throw new IllegalArgumentException("Empty rounds");
            
            this.graph = requireNonNull(graph);
            if (graph.isEmpty()) throw new IllegalArgumentException("Empty graph");
            
            // Confirm that MrX is Black
            if (mrX.colour.isDetective()) throw new IllegalArgumentException("MrX should be Black");
	
            // Make a list of player configurations..
            ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
            // ..and populate it
            configurations.add(mrX);
            configurations.add(firstDetective);
            detectives.add(firstDetective.colour);
            for (PlayerConfiguration d : restOfTheDetectives){
               configurations.add(requireNonNull(d));
               detectives.add(d.colour);
            }
            
            Set<Integer> locations = new HashSet<>();
            Set<Colour> colours = new HashSet<>();
            int numTicketTypes;
            
            /*
               Check that the game is in a playable initial state
            */
            // for each player config, and for each of their tickets
            for (PlayerConfiguration c : configurations) {
                
                numTicketTypes = 0;
                
                for (Map.Entry<Ticket,Integer> ticket : c.tickets.entrySet()) {
                    
                    Ticket key = ticket.getKey();
                    Integer count = ticket.getValue();
                    
                    if (key == Double || key == Secret) {
                        
                        if (count > 0 && c.colour != Black)
                            throw new IllegalArgumentException("Players aren't allowed double or secret tickets");
                        
                    }
                    else if (key != Bus && key != Taxi && key != Underground)
                        throw new IllegalArgumentException("Illegal ticket type");
                    
                    numTicketTypes++;                        
                }
                
                if(numTicketTypes != 5)
                    throw new IllegalArgumentException("Tickets are missing");
                
                if (locations.contains(c.location))
                        throw new IllegalArgumentException("Duplicate starting location");
                locations.add(c.location);
                
                if (colours.contains(c.colour))
                        throw new IllegalArgumentException("Duplicate player colour");
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
            else throw new IllegalArgumentException("Cannot unregister spectator as it is not registered");

	}

	@Override
	public void startRotate() {
            ScotlandYardPlayer player = playerList.get(currentPlayer);
            if(isGameOver()) throw new IllegalStateException("Game is already over, cannot start rotation");
            validMoves = validMoves();
            player.player().makeMove(this, player.location(), validMoves, this);
        }
        
        @Override
        public void accept(Move movein){
            Move move = requireNonNull(movein); 
            //INFO - "doubling" allows us to use accept recursively on double moves - it equals true when this occurs.
            
            // took out "&& !doubling" and still passes tests. If you think this is dangerous put it back in, else delete
            if(!validMoves.contains(move) /*&& !doubling*/) throw new IllegalArgumentException("Invalid move");
            
            ScotlandYardPlayer player = playerList.get(currentPlayer);
            
            // DoubleMove  
            if (move instanceof DoubleMove)
            {
                DoubleMove doubleMove = (DoubleMove) move;
                TicketMove firstMove = doubleMove.firstMove();
                TicketMove secondMove = doubleMove.secondMove();
                
                // Reveal location when necessary        
                if(!isRevealRound())
                    firstMove = new TicketMove(Black,firstMove.ticket(),lastKnownMrX);
                else lastKnownMrX = firstMove.destination();
                    
                int tmp = round;
                round++;
                if(!isRevealRound())
                    secondMove = new TicketMove(Black,secondMove.ticket(),lastKnownMrX);
                round = tmp;
                
                // Create a new doubleMove with hidden locations depending on reveal rounds
                DoubleMove doubleMove2 = new DoubleMove(Black,firstMove,secondMove);
                // Notify spectators with the masked double move
                for(Spectator spectator : spectators)
                    spectator.onMoveMade(this, doubleMove2);
                // Call accept on the firstMove, using "doubling" to avoid calling things twice
                doubling = true;
                accept(doubleMove.firstMove());
                doubling = false;
                player.removeTicket(Double);
                // Continue "accept()" with the second move, now that the first has been processed
                move = doubleMove.secondMove();
            }
            if (move instanceof TicketMove)
            {
                TicketMove ticketMove = (TicketMove) move;
                
                // make the move
                player.location(ticketMove.destination());
                
                // If it's MrX and time to reveal update his last known position
                if (currentPlayer == 0) {
                    if (isRevealRound()) lastKnownMrX = ticketMove.destination();
                    else move = new TicketMove(Black,ticketMove.ticket(),lastKnownMrX);
                }

                // remove used ticket
                player.removeTicket(ticketMove.ticket());

                // if player is a detective then give MrX the used ticket
                if (currentPlayer != 0) playerList.get(0).addTicket(ticketMove.ticket());
            }

            // If MrX is the current player then the round is new
            if(currentPlayer == 0) {
                //Increment round and notify spectators.
                round++;
                for(Spectator s : spectators)
                    s.onRoundStarted(this, round);
            }
            // Notify spectators that move has been made
            for(Spectator s : spectators)
                s.onMoveMade(this, move);
            
            // Initiate next move if necessary
            if (!isGameOver() && !doubling) {
                currentPlayer++;
                // If end of round reset and notify spectators
                if(currentPlayer == playerList.size())  
                {
                    currentPlayer = 0;
                    for(Spectator s : spectators)
                            s.onRotationComplete(this);
                }
                // Otherwise tell the next player to move
                else
                {
                    ScotlandYardPlayer playernext = playerList.get(currentPlayer);
                    // initiate the next move
                    validMoves = validMoves();
                    playernext.player().makeMove(this, playernext.location(), validMoves, this);
                }
            }
            
        }
     
        private boolean nextNodeEmpty(Edge<Integer, Transport> edge) {         
            for(ScotlandYardPlayer player : playerList)
            {
                if(player.location()==edge.destination().value() && !player.isMrX())
                    return false;
            } 
            return true;
        }
        
        private Set<Move> validMoves(){
            int playerLocation = playerList.get(currentPlayer).location();
            Set<Move> validMoves = new HashSet<>();
            
            if(graph.containsNode(playerLocation))
            {
                // Find all paths from current location
                Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(playerLocation));
                // For each path check if the destination is empty and reachable
                for (Edge<Integer, Transport> edge : edges) {

                    if (nextNodeEmpty(edge)) {
                        // If the player has appropriate tickets to move to the empty node, the move(s) are valid
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
            // Consider double moves if there are sufficient rounds left if MrX has a double ticket
            if (round < rounds.size()-1 && playerList.get(currentPlayer).hasTickets(Double))
            {
                ArrayList<DoubleMove> toAdd = new ArrayList<>();
                
                for (Move m : validMoves)
                {
                    TicketMove firstMove = (TicketMove) m;
                    int destination = firstMove.destination();
                    
                    // For each move, and for each edge branching from that move's destination, find valid second moves.
                    Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(destination));
                    for (Edge<Integer, Transport> edge : edges) 
                    {
                        if (nextNodeEmpty(edge))
                        {
                            // check MrX has both required tickets
                            int ticketsNeeded = 1;
                            if (firstMove.ticket() == Ticket.fromTransport(edge.data())) ticketsNeeded = 2;
                            
                            if (playerList.get(currentPlayer).hasTickets(Ticket.fromTransport(edge.data()),ticketsNeeded))
                            {
                                TicketMove secondMove = new TicketMove(playerList.get(currentPlayer).colour(),Ticket.fromTransport(edge.data()),edge.destination().value());
                                DoubleMove Dmove = new DoubleMove(playerList.get(currentPlayer).colour(),firstMove,secondMove);
                                toAdd.add(Dmove);
                            }

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
            
            // If there are no valid moves then add a PassMove
            if(validMoves.isEmpty()) {
                Move Pmove = new PassMove(playerList.get(currentPlayer).colour());
                validMoves.add(Pmove);
            }
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
            if (colour == Black) return lastKnownMrX;
            else 
            {
                for (ScotlandYardPlayer player : playerList)
                    if (player.colour() == colour) return player.location();
            }
            return 0;
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
            
            // If last player in round has made their move
            if (currentPlayer == playerList.size()-1) {
                
                // MrX wins if all rounds complete
                if (round > rounds.size()-1)
                    winners.add(Black);

                int tmp = currentPlayer;
                
                int stuckDetectives = 0;
                for (currentPlayer = 0; currentPlayer < playerList.size(); currentPlayer++) {
                    Move Pmove = new PassMove(playerList.get(currentPlayer).colour());

                    if (validMoves().contains(Pmove)) {
                        if (currentPlayer == 0) {
                            winners = detectives;
                            break;
                        }
                        else stuckDetectives++;
                    }
                    else if (currentPlayer != 0) break;
                }
                
                //No detectives can Move
                if (stuckDetectives == detectives.size()) winners.add(Black);
                currentPlayer = tmp;
            }
             
            int ticketlessDetectives = 0;
            int mrXCurrent = playerList.get(0).location();
            // Check if detectives have tickets
            for (ScotlandYardPlayer player : playerList) {
                if (player.isDetective()) {
                    
                    boolean notickets = true;
                    for (Ticket t : player.tickets().keySet())
                        if (player.hasTickets(t)) {
                            notickets = false;
                            break;
                        }
                    if (notickets) ticketlessDetectives++;

                    // Detective landed on MrX
                    if (player.location() == mrXCurrent)
                        winners = detectives;   
                }
            }
            
            // All detectives have no tickets
            if (ticketlessDetectives == detectives.size())
                winners.add(Black);
            
            // If game over, notify spectators and correct return variable
            if (!winners.isEmpty()){
                isGameOver = true;
                for(Spectator spectator : spectators)
                    spectator.onGameOver(this, winners);
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