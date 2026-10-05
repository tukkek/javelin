package javelin.model.unit.feat;

import javelin.model.unit.Combatant;
import javelin.model.unit.skill.Concentration;

/**
 * +4 to {@link Concentration}.
 *
 * @author alex
 */
public class CombatCasting extends Feat{
	public static final Feat SINGLETON=new CombatCasting();

	private CombatCasting(){
		super("Combat casting");
	}

	@Override
	public boolean upgrade(Combatant c){
		return super.upgrade(c)&&!c.spells.isEmpty();
	}
}
