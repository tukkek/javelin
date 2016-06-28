package javelin.model.item;

import javelin.Javelin;
import javelin.controller.upgrade.Spell;
import javelin.model.unit.Combatant;

/**
 * Represent a consumable potion to be used in-battle. Any monster can use a
 * potion.
 * 
 * @author alex
 */
public class Potion extends Item {
	javelin.controller.upgrade.Spell spell;

	/**
	 * @param s
	 *            One-use spell effect of drinking this potion.
	 */
	public Potion(Spell s) {
		super("Potion of " + s.name.toLowerCase(), s.level * s.casterlevel * 50,
				s.realm.getitems());
		usedinbattle = s.castinbattle;
		usedoutofbattle = s.castoutofbattle;
		spell = s;
	}

	@Override
	public boolean use(Combatant user) {
		Javelin.message(spell.cast(user, user, null, false), false);
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant user) {
		spell.castpeacefully(user, user);
		return true;
	}
}
