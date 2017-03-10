package uk.ac.bris.cs.scotlandyard.ui.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class BoardProperty {

	private final BooleanProperty scrollPan = new SimpleBooleanProperty(true);
	private final BooleanProperty focusPlayer = new SimpleBooleanProperty(false);
	private final BooleanProperty history = new SimpleBooleanProperty(false);

	public boolean isScrollPan() {
		return scrollPan.get();
	}

	public BooleanProperty scrollPanProperty() {
		return scrollPan;
	}

	public boolean isFocusPlayer() {
		return focusPlayer.get();
	}

	public BooleanProperty focusPlayerProperty() {
		return focusPlayer;
	}

	public boolean isHistory() {
		return history.get();
	}

	public BooleanProperty historyProperty() {
		return history;
	}

}
