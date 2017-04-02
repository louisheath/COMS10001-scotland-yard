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
        //List Of all Players - MrX at index 0
        List<ScotlandYardPlayer> playerList = new ArrayList<>();
        //Keeps track of Game state - Could combine somehow??
        int currentPlayer, round, lastKnownMrX; //not needed anymore passMoveCount = 0;
        //More Game State
        boolean mrXTrapped, doubling = false;

        Set<Colour> winners = new HashSet<>();
        Set<Colour> detectives = new HashSet<>();
        private Collection<Spectator> spectators = new HashSet<>();
        
        //validMoves set to store validMoves in to reduce number of calls
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
            ScotlandYardPlayer player = playerList.get(currentPlayer);
            if(isGameOver()) throw new IllegalStateException("Game is Already over");
            validMoves = validMoves();
            player.player().makeMove(this, player.location(), validMoves, this);
        }
        
        @Override
        public void accept(Move movein){
            Move move = requireNonNull(movein); 
            //INFO - doubling allows us to use accept recursively on double moves - it equals true when this occurs.
            
            if(!validMoves.contains(move) && doubling == false) throw new IllegalArgumentException("Invalid Move");
            
            ScotlandYardPlayer player = playerList.get(currentPlayer);
            
            // DoubleMove  
            if (move instanceof DoubleMove)
            {
                DoubleMove doubleMove = (DoubleMove) move;
                TicketMove firstMove = doubleMove.firstMove();
                TicketMove secondMove = doubleMove.secondMove();
                
                //Reveal Location When Necessary        
                if(!isRevealRound()){
                firstMove = new TicketMove(Black,firstMove.ticket(),lastKnownMrX);
                }
                else lastKnownMrX = firstMove.destination();
                    
                int tmp = round;
                round++;
                if(!isRevealRound()){
                    secondMove = new TicketMove(Black,secondMove.ticket(),lastKnownMrX);
                }
                round = tmp;
                
                DoubleMove doubleMove2 = new DoubleMove(Black,firstMove,secondMove);
                //Notify Spectators about Double Move
                for(Spectator spectator : spectators)
                {
                    spectator.onMoveMade(this, doubleMove2);
                }
                //Call accept on the firstMove
                doubling = true;
                accept(doubleMove.firstMove());
                doubling = false;
                player.removeTicket(Double);
                //Continue in accept with secondMove
                move = doubleMove.secondMove();
            }
            if (move instanceof TicketMove)
            {
                TicketMove ticketMove = (TicketMove) move;
                
                //make the move
                player.location(ticketMove.destination());
                
                //If its Mr X and time to reveal update his last known position
                if(currentPlayer == 0 && isRevealRound()) lastKnownMrX = ticketMove.destination();
                else if(currentPlayer == 0 && !isRevealRound()) move = new TicketMove(Black,ticketMove.ticket(),lastKnownMrX);

                //remove tickets
                player.removeTicket(ticketMove.ticket());

                //if detective give MrX ticket
                if (currentPlayer != 0) playerList.get(0).addTicket(ticketMove.ticket());
            }
            
            // PassMove
            else if (move instanceof PassMove)
            {
                //if (currentPlayer != 0) passMoveCount++; NOT NEEDED?
            }

            //If MrX
            if(currentPlayer == 0) {
                //Increment round and Notify Spectators.
                round++;
                for(Spectator spectator : spectators)
                {
                    spectator.onRoundStarted(this, round);
                }
            }
            //Notify Spectators about Move Made
            for(Spectator spectator : spectators)
            {
                spectator.onMoveMade(this, move);
            }
            
            //Check if game is over
            if (!isGameOver()) {
                if(!doubling){
                    currentPlayer++;
                    //If end of round reset and notify spectators
                    if(currentPlayer == playerList.size())  
                    {
                        currentPlayer = 0;
                        //not needed passMoveCount = 0;
                        for(Spectator spectator : spectators)
                            {
                                spectator.onRotationComplete(this);
                            }
                    }
                    //Otherwise tell the next player to move
                    else
                    {
                        ScotlandYardPlayer playernext = playerList.get(currentPlayer);
                        // initiate the next move
                        validMoves = validMoves();
                        playernext.player().makeMove(this, playernext.location(), validMoves, this);
                    }
                }
            }
            
        }
     
        private Set<Move> validMoves(){
            int playerLocation = playerList.get(currentPlayer).location();
            Set<Move> validMoves = new HashSet<>();
            
            if(graph.containsNode(playerLocation))
            {
                //Find all paths from current location
                Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(playerLocation));
                //For each path check if the destination is empty then check if they have the tickets needed to follow it
                for (Edge<Integer, Transport> edge : edges) {
                    
                    //is next spot empty
                    boolean empty = true;
                    for(ScotlandYardPlayer player : playerList)
                    {
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
            //If MrX Consider Double Moves and check for if there are sufficient rounds left to use a double and for if MrX has a double ticket
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

                            //Added exemption of Black's location, as with double he can return to his start.
                            if(player.location()==edge.destination().value() && player.colour() != Black)
                                empty = false;
                        } 

                        if (empty)
                        {
                            //checks MrX has both tickets needed
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
            
            //If last player in round has gone
            if(currentPlayer == playerList.size()-1){
                
                //All Rounds are Over
                if (round > rounds.size()-1 )
                    winners.add(Black);

                int tmp = currentPlayer;
                int counter = 0;
                for(int i = 0; i<playerList.size(); i++){
                    currentPlayer = i;
                    Move Pmove = new PassMove(playerList.get(i).colour());
                    if(validMoves().contains(Pmove)){
                        //MrX Has No Moves
                        if(i==0) winners = detectives;
                        //One of the detectives cant move
                        counter++;
                    }
                    else{
                        //If a detective can move break loop
                        if(i!=0) break;
                    }
                }
                
                //No detectives can Move
                if (counter == detectives.size()) winners.add(Black);
                currentPlayer = tmp;

            }
            
            //No Detectives Can Move - not needed
            //if (passMoveCount == detectives.size())
             //   winners.add(Black);
            
            int counter = 0;
            int mrXCurrent = playerList.get(0).location();
            
            for (ScotlandYardPlayer player : playerList){
                
                boolean notickets = true;
                for(Ticket t : player.tickets().keySet())
                {
                    if(player.tickets().get(t) != 0) notickets =false;
                }
                if(notickets && !player.isMrX()) counter++;
                
                //Detective landed On MrX
                if(!player.isMrX() && player.location() == mrXCurrent)
                    winners = detectives;   
            }
            
            //Players Have No Tickets Left
            if (counter == detectives.size())
                winners.add(Black);
            
            //If someone has won notify spectators and change variable to return.
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