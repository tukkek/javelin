package javelin.model.world.location.fortification;

import java.util.HashSet;

import javelin.controller.upgrade.Upgrade;

/**
 * Allows a player to learn one upgrade set.
 * 
 * @author alex
 */
public class MartialAcademy extends Academy {
	/** See {@link Academy#Academy(String, String, int, int, HashSet)}. */
	public MartialAcademy(HashSet<Upgrade> upgrades, String descriptionknownp) {
		super(descriptionknownp, "An academy", 6, 10, upgrades);
	}
}
