package javelin.model.item;

import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.town.RestingScreen;

public class SecureShelterScroll extends Item {

	public SecureShelterScroll() {
		super("Scroll of secure shelter", 800, "Use it to rest on the map");
	}

	@Override
	public boolean use(Combatant c) {
		return false;
	}

	@Override
	public boolean isusedinbattle() {
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		Squad.active.hourselapsed += 8;
		RestingScreen.recover();
		return true;
	}

}
