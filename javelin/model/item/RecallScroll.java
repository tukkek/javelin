package javelin.model.item;

import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.world.WorldScreen;

public class RecallScroll extends Scroll {

	public RecallScroll() {
		super("Scroll of word of recall", 1650,
				"Returns squad to last visited town.");
	}

	@Override
	public boolean use(Combatant c) {
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		Squad.active.x = Squad.active.lasttown.x;
		Squad.active.y = Squad.active.lasttown.y;
		Squad.active.displace();
		WorldScreen.worldmap.removeThing(Squad.active.visual);
		Squad.active.visual.x = Squad.active.x;
		Squad.active.visual.y = Squad.active.y;
		WorldScreen.worldmap.addThing(Squad.active.visual,
				Squad.active.visual.x, Squad.active.visual.y);
		return true;
	}

}
