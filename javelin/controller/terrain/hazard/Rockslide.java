package javelin.controller.terrain.hazard;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.Season;
import tyrant.mikera.engine.RPG;

/**
 * Takes a single creature (or the whole vehicle) downhill. Can also be an
 * avalanche if on a snowy mountain.
 * 
 * @author alex
 */
public class Rockslide extends Hazard {
	boolean isavalanche = Season.current == Season.WINTER
			&& Terrain.current().equals(Terrain.MOUNTAINS);
	String description = isavalanche ? "an avalanche" : "a rockslide";
	int damage = isavalanche ? 8 : 3;

	@Override
	public void hazard(int hoursellapsed) {
		String message;
		if (Squad.active.transport == null) {
			Combatant c = RPG.pick(Squad.active.members);
			damage(c);
			message = c + " is caught on " + description + "!";
		} else {
			for (Combatant c : Squad.active.members) {
				damage(c);
			}
			message = "Your " + Squad.active.transport.toString().toLowerCase()
					+ " is caught on " + description + "!";
		}
		Javelin.message(message, false);
	}

	void damage(Combatant c) {
		int damage = 0;
		for (int i = 0; i < this.damage; i++) {
			damage += RPG.r(1, 6);
		}
		if (c.source.save(c.source.ref, 16)) {
			damage = damage / 2;
		}
		c.damage(damage);
	}

	@Override
	public boolean validate() {
		return !Squad.active.fly();
	}

}
