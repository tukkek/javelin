package javelin.controller.upgrade;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more information.
 */
public class NaturalArmor extends Upgrade{
	private final int target;

	public NaturalArmor(final String name,int target){
		super(name);
		this.target=target;
	}

	@Override
	public String inform(final Combatant c){
		return "Current armor class: "+c.source.getrawac();
	}

	@Override
	public boolean apply(final Combatant c){
		Monster m=c.source;
		int from=m.getrawac();
		int to=from+target-m.armor;
		if(target<m.armor||to>from+10||to>m.dexterity+m.constitution) return false;
		m.armor=target;
		m.setrawac(to);
		return true;
	}
}