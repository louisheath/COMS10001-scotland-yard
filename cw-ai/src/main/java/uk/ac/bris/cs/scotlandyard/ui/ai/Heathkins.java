package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.ui.ai.HeathkinsMrX.MrXAI;

@ManagedAI("Heathkins")
public class Heathkins implements PlayerFactory {

	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
            if(colour == Black) return new MrXAI();
            else return new MrXAI(); //should be detective
	}
    }        
