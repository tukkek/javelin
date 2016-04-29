package javelin.model.spell;

import javelin.controller.exception.NotPeaceful;
import javelin.controller.upgrade.Spell;
import javelin.model.item.scroll.Scroll;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.WorldActor;

/**
 * Convenience method for enabling {@link Scroll}s to be used as spells. This
 * approach can cause problems like {@link #equals(Object)}, so ideally this
 * should be handled in a more conceptually correct manner.
 * 
 * @author alex
 */
public class ScrollSpell extends Spell {

	public Scroll s;

	public ScrollSpell(Scroll s, WorldActor here) {
		super(s.name.toLowerCase().replace("scroll of ", ""), s.incrementcost,
				true, s.level, true);
		this.s = s;
	}

	@Override
	public int calculatetouchdc(Combatant combatant, Combatant targetCombatant,
			BattleState s) {
		throw new RuntimeException("Cannot be used in combat");
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		throw new RuntimeException("Cannot be used in combat");
	}

	@Override
	public int calculatehitdc(Combatant active, Combatant target,
			BattleState state) {
		throw new RuntimeException("Cannot be used in combat");
	}

	@Override
	public int calculatesavetarget(Combatant caster, Combatant target) {
		throw new RuntimeException("Cannot be used in combat");
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant combatant)
			throws NotPeaceful {
		s.usepeacefully(combatant);
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ScrollSpell
				&& ((ScrollSpell) obj).s.getClass().equals(s.getClass());
	}
}
