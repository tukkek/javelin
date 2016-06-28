package javelin.model.spell.conjuration.healing;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.Realm;
import javelin.model.condition.Condition;
import javelin.model.condition.Exhausted;
import javelin.model.condition.Fatigued;
import javelin.model.condition.Poisoned;
import javelin.model.spell.Touch;
import javelin.model.spell.abjuration.DispelMagic;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * http://www.d20srd.org/srd/spells/restorationLesser.htm
 * 
 * @author alex
 */
public class Restoration extends Touch {
	static final ArrayList<Class<? extends Condition>> CONDITIONS =
			new ArrayList<Class<? extends Condition>>();

	static {
		CONDITIONS.add(Poisoned.class);
	}

	/** Constructor. */
	public Restoration() {
		super("Lesser restoration", 2, SpellsFactor.ratespelllikeability(2),
				Realm.WATER);
		ispotion = true;
		castinbattle = true;
		castoutofbattle = true;
		isritual = true;
		castonallies = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		return castpeacefully(caster, target);
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		ArrayList<Condition> dispelled = new ArrayList<Condition>();
		remove(Fatigued.class, target, dispelled);
		Condition exhausted = remove(Exhausted.class, target, dispelled);
		if (exhausted != null) {
			target.addcondition(new Fatigued(target, null, exhausted.longterm));
		}
		if (target.source.poison > 0) {
			target.detox(1);
			return target + " heals 2 constitution damage!";
		}
		for (Class<? extends Condition> c : CONDITIONS) {
			remove(c, target, dispelled);
		}
		return DispelMagic.printconditions(dispelled);
	}

	Condition remove(Class<? extends Condition> c, Combatant target,
			ArrayList<Condition> dispelled) {
		Condition has = target.hascondition(c);
		if (has == null) {
			return null;
		}
		target.removecondition(has);
		dispelled.add(has);
		return has;
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		super.filtertargets(combatant, targets, s);
		for (Combatant c : new ArrayList<Combatant>(targets)) {
			if (!combatant.isAlly(c, s)) {
				targets.remove(c);
			}
		}
	}
}
