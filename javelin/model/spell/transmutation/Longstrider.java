package javelin.model.spell.transmutation;

import java.util.List;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.condition.Strider;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * http://www.d20srd.org/srd/spells/longstrider.htm
 * 
 * Only castable in battle but will live up to 1 hour if inside a
 * {@link Dungeon}. Can't cast outside because it will seem useless for now.
 * TODO allow leveling up spells
 * 
 * @author alex
 */
public class Longstrider extends Spell {
	/** Constructor. */
	public Longstrider() {
		super("Longstrider", 1, SpellsFactor.ratespelllikeability(1),
				Realm.EARTH);
		ispotion = true;
		castinbattle = true;
		castonallies = false;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		caster.addcondition(new Strider(caster, casterlevel));
		return "Walking speed for " + caster + " is now " + caster.source.walk
				+ "ft!";
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		targetself(combatant, targets);
	}
}
