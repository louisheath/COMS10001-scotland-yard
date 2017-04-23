package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.Spectator;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Taxi;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Underground;

/*
The PlayerFactory interface also comes with three further (empty) default methods,
which you can implement with your own custom behaviour (also see the JavaDocs of 
PlayerFactory). To add your own spectators to the game, return your implemented 
spectators as a List in the createSpectators method. You may perform setup and 
clean up tasks in the ready() and finish() methods, respectively.


*/

@ManagedAI("HeathkinsDetect")
public class HeathkinsDetect implements PlayerFactory {
        
	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
            return new MyAI();
	}
        
        // create a spectator which keeps track of MrX's potential locations
        @Override 
	public List<Spectator> createSpectators(ScotlandYardView view) {
            List<Spectator> spectators = Collections.emptyList();
            
            Spectator s = new MrXFinder();
            spectators.add(s);
            
            return spectators;
        }

	// TODO A sample player that selects a random move
	private static class MyAI implements Player {
            
            private final Random random = new Random();
            Scorer2 scorer = new Scorer2();
            
            @Override
            public void makeMove(ScotlandYardView view, int location, Set<Move> moves, Consumer<Move> callback) {
                
                Graph<Integer, Transport> graph = view.getGraph();
                int lastKnownMrX = view.getPlayerLocation(Black);
                
                
                Colour colour = view.getCurrentPlayer();
                Move bestmove = new PassMove(colour);
                
                
                int rounds = 0;
                //Work out how many rounds since MrX Surfaced
                while(!view.getRounds().get(view.getCurrentRound()-rounds))
                {
                    rounds++;
                    if(view.getCurrentRound()-rounds == 0) break;
                }


                // picks best move
                callback.accept(bestmove);

            }
	}
            
            
}
