package javelin.controller.upgrade.damage;

import java.util.List;

import javelin.model.unit.Monster;
import javelin.model.unit.attack.AttackSequence;

/**
 * Upgrades mêlée damage.
 *
 * @author alex
 */
public class MeleeDamage extends Damage{
	/** Singleon. */
	public static final MeleeDamage INSTANCE=new MeleeDamage();

	MeleeDamage(){
		super("More mêlée damage");
	}

	@Override
	protected List<AttackSequence> getattacktype(final Monster m){
		return m.melee;
	}

	@Override
	public void incrementupgradecount(final Monster m){
		m.meleedamageupgrades+=1;
	}
}