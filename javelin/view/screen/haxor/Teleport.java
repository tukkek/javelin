package javelin.view.screen.haxor;

import javelin.model.unit.abilities.spell.conjuration.teleportation.GreaterTeleport;
import javelin.model.unit.attack.Combatant;

/**
 * Teleports player to any town and shows their .
 * 
 * @author alex
 */
public class Teleport extends Hax {
	/** See {@link Hax#Hax(String, double, boolean)}. */
	public Teleport(String name, Character keyp, int price,
			boolean requirestargetp) {
		super(name, keyp, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		GreaterTeleport spell = new GreaterTeleport();
		spell.showterrain = true;
		spell.castpeacefully(null);
		return true;
	}
}
