package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.model.item.Item;
import javelin.model.item.relic.Relic;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.temple.Temple;

/**
 * Holds the {@link Relic} for this temple. If for any reason the {@link Relic}
 * is lost by the player it shall be available for pickup here again.
 *
 * @author alex
 */
public class Altar extends Feature {
	Temple temple;

	/** Constructor. */
	public Altar(Point p, Temple temple) {
		super(p.x, p.y, "dungeonaltar");
		this.temple = temple;
		remove = false;
	}

	@Override
	public boolean activate() {
		if (Item.getplayeritems().contains(temple.relic)) {
			Javelin.message("The " + temple.relic + " is not here anymore...",
					true);
		} else {
			String text = "This altar holds the " + temple.relic + "!";
			if (!World.scenario.expiredungeons) {
				text += "\nIf it is lost for any reason it shall be teleported back to safety here.";
			}
			Javelin.message(text, true);
			temple.relic.clone().grab();
			if (World.scenario.expiredungeons) {
				remove();
				temple.remove();
			}
		}
		return true;
	}
}
