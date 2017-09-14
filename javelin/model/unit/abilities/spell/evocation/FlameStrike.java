package javelin.model.unit.abilities.spell.evocation;

import java.util.List;

import javelin.controller.challenge.CrCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;

/**
 * Produces a vertical column of divine fire.
 * 
 * @author alex
 */
public class FlameStrike extends Spell {
	/** Constructor. */
	public FlameStrike() {
		super("Flame strike", 5, CrCalculator.ratespelllikeability(5),
				Realm.FIRE);
		castinbattle = true;
		isscroll = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		for (Combatant c : getradius(target, 2, this, s)) {
			s.clone(c).damage(casterlevel * 6 / 2, s,
					c.source.energyresistance);
		}
		return "A roaring column of fire descends around " + target + "!";
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		// all targets is fine
	}
}
