package javelin.controller.upgrade;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.BreathWeapon;

/**
 * See {@link Monster#breaths}
 * 
 * @author alex
 */
public class BreathUpgrade extends Upgrade {
	final private BreathWeapon breath;

	/**
	 * @param breathp
	 *            Breath to apply.
	 */
	public BreathUpgrade(BreathWeapon breathp) {
		super("Breath weapon: " + breathp.description);
		breath = breathp;
	}

	@Override
	public String inform(Combatant m) {
		return breath.toString();
	}

	@Override
	public boolean apply(Combatant m) {
		if (breath.damage[0] > m.source.hd.count()) {
			return false;
		}
		for (BreathWeapon b : m.source.breaths) {
			if (b.description.equals(breath.description)) {
				return false;
			}
		}
		BreathWeapon b = breath.clone();
		b.savedc = Math.round(10 + m.source.originalhd / 2f);
		if (m.source.constitution > 0) {
			b.savedc += Monster.getbonus(m.source.constitution);
		}
		m.source.breaths.add(b);
		return true;
	}

}
