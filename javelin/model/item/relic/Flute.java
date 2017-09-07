package javelin.model.item.relic;

import javelin.Javelin;
import javelin.model.Realm;
import javelin.model.transport.FlyingNimbus;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.screen.WorldScreen;

/**
 * Summons a {@link FlyingNimbus}.
 * 
 * @author alex
 */
public class Flute extends Relic {
	/** Constructor. */
	public Flute() {
		super("Flute of Wind", Realm.AIR);
		usedinbattle = false;
		usedoutofbattle = true;
	}

	@Override
	protected boolean activate(Combatant user) {
		if (Dungeon.active != null) {
			Javelin.app.switchScreen(WorldScreen.active);
			Javelin.message(
					"You play the flute but nothing happens. Try it outside next time!",
					false);
			return true;
		}
		Squad.active.transport = new FlyingNimbus(Squad.active.transport);
		Squad.active.updateavatar();
		Javelin.app.switchScreen(WorldScreen.active);
		Javelin.message("You are taken by a " + Squad.active.transport + "!",
				false);
		return true;
	}

}
