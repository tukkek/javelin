package javelin.model.spell;

import java.util.List;

import javelin.Javelin;
import javelin.controller.exception.NotPeaceful;
import javelin.controller.upgrade.Spell;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class DeeperDarkness extends Spell {

	public DeeperDarkness(String name) {
		super(name + "deeper darkness", .15f, false, 5, false);
	}

	@Override
	public int calculatetouchdc(Combatant combatant, Combatant targetCombatant,
			BattleState s) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		s.period = Javelin.PERIOD_NIGHT;
		return "Light dims!";
	}

	@Override
	public int calculatehitdc(Combatant active, Combatant target,
			BattleState state) {
		return -Integer.MAX_VALUE;
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
	public void filtertargets(final Combatant combatant,
			final List<Combatant> targets, final BattleState s) {
		self(combatant, targets);
	}
}
