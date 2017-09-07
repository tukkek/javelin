package javelin.model.unit.feat;

import javelin.model.unit.Skills;
import javelin.model.unit.attack.Combatant;

/**
 * +4 to {@link Skills#concentration}.
 * 
 * @author alex
 */
public class CombatCasting extends Feat {
	public static final CombatCasting SINGLETON = new CombatCasting();

	private CombatCasting() {
		super("Combat casting");
	}

	@Override
	public boolean apply(Combatant c) {
		return super.apply(c) && !c.spells.isEmpty();
	}
}
