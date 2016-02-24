package javelin.controller.upgrade.damage;

import java.util.List;

import javelin.model.unit.AttackSequence;
import javelin.model.unit.Monster;

/**
 * Upgrades ranged damage.
 * 
 * @author alex
 */
public class RangedDamage extends Damage {
	public RangedDamage(final String name) {
		super(name);
	}

	@Override
	protected List<AttackSequence> getattacktype(final Monster m) {
		return m.ranged;
	}

	@Override
	public void incrementupgradecount(final Monster m) {
		m.rangeddamageupgrades += 1;
	}
}