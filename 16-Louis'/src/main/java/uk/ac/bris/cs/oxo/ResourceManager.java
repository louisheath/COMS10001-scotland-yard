package uk.ac.bris.cs.oxo;

import java.util.Objects;

import javafx.scene.image.Image;

public class ResourceManager {

	private final Image nought;
	private final Image cross;

	public ResourceManager() {
		nought = new Image("/nought.png", -1, -1, true, true, true);
		cross = new Image("/cross.png", -1, -1, true, true, true);
	}

	Image of(Side side) {
		return Objects.requireNonNull(side) == Side.CROSS ? cross : nought;
	}

}
