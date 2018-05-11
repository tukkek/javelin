package javelin.model.world.location.dungeon.feature;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.temple.EarthTemple;

/**
 * @see EarthTemple
 * @author alex
 */
public class FruitTree extends Feature {
	/** Constructor. */
	public FruitTree(int xp, int yp) {
		super(xp, yp, "dungeontreefruit");
	}

	@Override
	public boolean activate() {
		ArrayList<Combatant> squad = Squad.active.members;
		ArrayList<String> names = new ArrayList<String>(squad.size());
		for (Combatant c : squad) {
			names.add(c + " (" + c.getstatus() + ")");
		}
		int choice = Javelin.choose("Who will eat the fruit?", names, true,
				false);
		if (choice < 0) {
			return false;
		}
		Combatant target = squad.get(choice);
		target.heal(target.maxhp, true);
		return true;
	}

}
