package javelin.controller.upgrade.damage;

import java.util.HashSet;

import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;

/**
 * Adds an effect to all melee attacks.
 * 
 * @author alex
 */
public class EffectUpgrade extends Upgrade {

	private Spell effect;

	/**
	 * @param name
	 *            Upgrade name.
	 * @param effect
	 *            Effect to bestow when a melee attack hits.
	 */
	public EffectUpgrade(String name, Spell effect) {
		super("Damage effect: " + name.toLowerCase());
		this.effect = effect;
	}

	@Override
	public String info(Combatant c) {
		HashSet<String> effects = new HashSet<String>();
		for (AttackSequence as : c.source.melee) {
			for (Attack a : as) {
				if (a.effect != null) {
					effects.add(a.effect.name);
				}
			}
		}
		String output = "";
		for (String effect : effects) {
			output += effect.toLowerCase() + ", ";
		}
		return "Current damage effects: " + (output.isEmpty() ? "none"
				: output.substring(0, output.length() - 2));
	}

	@Override
	protected boolean apply(Combatant c) {
		for (AttackSequence as : c.source.melee) {
			for (Attack a : as) {
				a.effect = effect;
			}
		}
		return true;
	}

}
