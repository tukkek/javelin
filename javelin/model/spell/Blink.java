package javelin.model.spell;

import java.util.List;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.controller.upgrade.Spell;
import javelin.model.condition.Blinking;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class Blink extends Spell {

	public Blink() {
		super("Blink", SpellsFactor.ratespelllikeability(3), false, 3, true);
	}

	@Override
	public int calculatetouchdc(Combatant combatant, Combatant targetCombatant,
			BattleState s) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		caster.conditions.add(new Blinking(caster.ap + 5, caster));
		return caster + " is blinking!";
	}

	@Override
	public int calculatehitdc(Combatant active, Combatant target,
			BattleState state) {
		return 1;
	}

	@Override
	public int calculatesavetarget(Combatant caster, Combatant target) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant combatant)
			throws NotPeaceful {
		throw new NotPeaceful();
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		targetself(combatant, targets);
	}
}
