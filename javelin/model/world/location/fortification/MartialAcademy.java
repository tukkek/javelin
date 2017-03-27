package javelin.model.world.location.fortification;

import java.util.HashSet;

import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.world.location.town.Academy;

/**
 * Allows a player to learn one upgrade set.
 * 
 * @author alex
 */
public class MartialAcademy extends Academy {
	/**
	 * See {@link Academy#Academy(String, String, int, int, HashSet)}.
	 * 
	 * @param raise
	 */
	public MartialAcademy(HashSet<Upgrade> upgrades, String descriptionknownp,
			RaiseAbility raise) {
		super(descriptionknownp, "An academy", 6, 10, upgrades, raise,
				Warrior.SINGLETON);
	}
}
