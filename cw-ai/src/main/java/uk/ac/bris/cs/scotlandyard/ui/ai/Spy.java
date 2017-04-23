package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Spectator;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 *  This is a super sneaky spectator that goes in the detective AI ?
 *  I have a feeling this is the wrong way to do it
 * 
 */
public class Spy implements Spectator {
    ScotlandYardView currentView;
    
    @Override
    public void onMoveMade(ScotlandYardView view, Move move) {
        currentView = view;
    }
    
    public ScotlandYardView getCurrentView() {
        return currentView;
    }
}
