package javelin.controller.terrain.map.tyrant;

import java.awt.Image;

import javelin.controller.terrain.map.TyrantMap;
import javelin.view.Images;
import tyrant.mikera.engine.RPG;

public class Graveyard extends TyrantMap {

	private static final Image tombstone = Images.getImage("terraintombstone");

	public Graveyard() {
		super("graveyard");
	}

	@Override
	public Image getobstacle() {
		return RPG.r(1, 3) == 1 ? obstacle : tombstone;
	}
}
