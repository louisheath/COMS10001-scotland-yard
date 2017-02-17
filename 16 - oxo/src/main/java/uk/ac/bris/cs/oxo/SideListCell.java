package uk.ac.bris.cs.oxo;

import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

class SideListCell extends ListCell<Side> {
	private ResourceManager manager;

	public SideListCell(ResourceManager manager) {
		this.manager = manager;
	}

	@Override
	protected void updateItem(Side item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			ImageView graphic = new ImageView(manager.of(item));
			graphic.setFitWidth(100);
			graphic.setFitHeight(100);
			graphic.setPreserveRatio(true);
			setGraphic(graphic);
		}
	}
}
