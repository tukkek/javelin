package javelin.model.unit.abilities.spell.evocation;

import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class DayLight extends Spell {

	public DayLight() {
		super("Daylight", 3, .15f, Realm.GOOD);
		castinbattle = true;
		isscroll = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, boolean saved,
			BattleState s, ChanceNode cn) {
		s.period = Javelin.PERIODNOON;
		return "The area brightens!";
	}

	@Override
	public void filtertargets(final Combatant combatant,
			final List<Combatant> targets, final BattleState s) {
		targetself(combatant, targets);
	}
}
