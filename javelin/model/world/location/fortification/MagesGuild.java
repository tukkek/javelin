package javelin.model.world.location.fortification;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.model.unit.Combatant;
import javelin.model.world.location.town.Academy;

/**
 * The only place a {@link Combatant} can learn new spells.
 * 
 * @author alex
 */
public class MagesGuild extends Academy {
	/**
	 * See {@link Academy#Academy(String, String, int, int, HashSet)}.
	 * 
	 * @param raiseWisdom
	 */
	public MagesGuild(String knownnamep, HashSet<Upgrade> spells,
			RaiseAbility raise) {
		super(knownnamep, "Mages guild", 0, 0, spells);
		ArrayList<Spell> ascending = new ArrayList<Spell>(spells.size());
		for (Upgrade u : spells) {
			ascending.add((Spell) u);
		}
		ascending.sort(new Comparator<Spell>() {
			@Override
			public int compare(Spell o1, Spell o2) {
				return o1.casterlevel - o2.casterlevel;
			}
		});
		minlevel = ascending.get(0).casterlevel;
		maxlevel = ascending.get(ascending.size() - 1).casterlevel;
		if (maxlevel > 10) {
			maxlevel = 10;
		}
		upgrades.add(raise);
	}

	@Override
	protected void sort(ArrayList<Upgrade> upgrades) {
		upgrades.sort(new Comparator<Upgrade>() {
			@Override
			public int compare(Upgrade o1, Upgrade o2) {
				if (o1 instanceof RaiseAbility) {
					return 1;
				}
				if (o2 instanceof RaiseAbility) {
					return -1;
				}
				Spell s1 = (Spell) o1;
				Spell s2 = (Spell) o2;
				return s1.casterlevel - s2.casterlevel;
			}
		});
	}
}
