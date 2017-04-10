package javelin.model.feat;

import javelin.model.unit.Combatant;
import javelin.model.unit.Skills;

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
