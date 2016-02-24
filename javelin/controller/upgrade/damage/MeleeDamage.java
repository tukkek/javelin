package javelin.controller.upgrade.damage;

import java.util.List;

import javelin.model.unit.AttackSequence;
import javelin.model.unit.Monster;

/**
 * Upgrades mêlée damage.
 * 
 * @author alex
 */
public class MeleeDamage extends Damage {
	public MeleeDamage(final String name) {
		super(name);
	}

	@Override
	protected List<AttackSequence> getattacktype(final Monster m) {
		return m.melee;
	}

	@Override
	public void incrementupgradecount(final Monster m) {
		m.meleedamageupgrades += 1;
	}
}