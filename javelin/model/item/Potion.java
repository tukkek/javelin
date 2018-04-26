package javelin.model.item;

import javelin.Javelin;
import javelin.controller.old.Game;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.view.screen.BattleScreen;

/**
 * Represent a consumable potion to be used in-battle. Any monster can use a
 * potion.
 * 
 * @author alex
 */
public class Potion extends Item {
	javelin.model.unit.abilities.spell.Spell spell;

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
		String text = spell.cast(user, user, false, null, null);
		Game.redraw();
		/* TODO should be less awkward once Context are implemented (2.0) */
		if (BattleScreen.active.getClass().equals(BattleScreen.class)) {
			BattleScreen.active.center(user.location[0], user.location[1]);
		}
		Javelin.message(text, false);
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant user) {
		spell.castpeacefully(user, user);
		return true;
	}
}
