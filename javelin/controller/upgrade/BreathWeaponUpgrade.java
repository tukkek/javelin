package javelin.controller.upgrade;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.BreathWeapon.BreathArea;
import javelin.model.unit.abilities.BreathWeapon.SavingThrow;

/**
 * See {@link Monster#breaths}
 * 
 * @author alex
 */
public class BreathWeaponUpgrade extends Upgrade {
	final private BreathWeapon breath;

	public BreathWeaponUpgrade(String name, String description, int i,
			BreathArea cone, int j, int k) {
		super("Breath weapon: " + name.toLowerCase());
		breath = new BreathWeapon(description.toLowerCase(), cone, i, j, k, 0,
				SavingThrow.REFLEXES, 0, .5f, true);
	}

	@Override
	public String info(Combatant m) {
		return breath.toString();
	}

	@Override
	public boolean apply(Combatant m) {
		for (BreathWeapon b : m.source.breaths) {
			if (b.description.equals(breath.description)) {
				return false;
			}
		}
		BreathWeapon b = breath.clone();
		b.savedc = Math.round(10 + m.source.originalhd / 2f
				+ Monster.getbonus(m.source.constitution));
		m.source.breaths.add(b);
		return true;
	}

	@Override
	public boolean isstackable() {
		return false;
	}

}
