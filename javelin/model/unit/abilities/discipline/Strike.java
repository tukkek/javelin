package javelin.model.unit.abilities.discipline;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.attack.AbstractAttack;
import javelin.controller.action.ai.attack.DamageChance;
import javelin.controller.action.ai.attack.MeleeAttack;
import javelin.controller.action.ai.attack.RangedAttack;
import javelin.controller.action.target.MeleeTarget;
import javelin.controller.action.target.RangedTarget;
import javelin.controller.action.target.Target;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurn;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.discipline.serpent.DizzyingVenomPrana;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.attack.Combatant;

/**
 * Lets the user select a standard attack (not a full sequence) then sets (and
 * later clears) {@link AbstractAttack#maneuver} and delegates to either
 * {@link MeleeAttack} or {@link RangedAttack}.
 * 
 * The line between Strikes and Boosts is often blurred, especially when the
 * Boost only gives you a bonus to the next attack (like with
 * {@link DizzyingVenomPrana}), in which case they are, effectively, a Strike.
 * 
 * For {@link Maneuver#ap}, uses a {@link ActionCost#SWIFT} action (for the
 * maneuver) plus a {@link ActionCost#STANDARD} action (for the attack), by
 * default.
 * 
 * {@link #validate(Combatant)} requires that the given {@link Combatant} have
 * at least one ranged or melee attack.
 * 
 * @author alex
 */
public abstract class Strike extends Maneuver {
	public Strike(String name) {
		super(name);
		ap = ActionCost.SWIFT + ActionCost.STANDARD;
	}

	@Override
	public boolean perform(Combatant c) {
		ArrayList<Attack> melee = getattacks(c, c.source.melee);
		ArrayList<Attack> all = new ArrayList<Attack>(melee);
		all.addAll(getattacks(c, c.source.ranged));
		Attack a;
		if (all.size() == 1) {
			a = all.get(0);
		} else {
			final String prompt = "Which attack will you use?";
			int choice = Javelin.choose(prompt, all, false, false);
			if (choice == -1) {
				throw new RepeatTurn();
			}
			a = all.get(choice);
		}
		try {
			AbstractAttack.maneuver = this;
			final Target action = melee.contains(a)
					? new MeleeTarget(a, ap, 'm')
					: new RangedTarget(a, ap, 'm');
			action.perform(c);
			return true;
		} finally {
			AbstractAttack.maneuver = null;
		}
	}

	/**
	 * @return First attack of each {@link AttackSequence}.
	 */
	ArrayList<Attack> getattacks(Combatant c, List<AttackSequence> attacktype) {
		ArrayList<Attack> attacks = new ArrayList<Attack>();
		for (AttackSequence sequence : attacktype) {
			attacks.add(sequence.get(0));
		}
		return attacks;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(BattleState s, Combatant c) {
		try {
			ArrayList<List<ChanceNode>> outcomes = new ArrayList<List<ChanceNode>>();
			AbstractAttack.maneuver = this;
			final Monster m = c.source;
			getoutcomes(c, s, m.melee, MeleeAttack.SINGLETON, outcomes);
			getoutcomes(c, s, m.ranged, RangedAttack.SINGLETON, outcomes);
			return outcomes;
		} finally {
			AbstractAttack.maneuver = null;
		}
	}

	void getoutcomes(Combatant c, BattleState s,
			ArrayList<AttackSequence> attacks, AbstractAttack action,
			ArrayList<List<ChanceNode>> outcomes) {
		for (Attack a : getattacks(c, attacks)) {
			outcomes.addAll(action.getoutcomes(s, c));
		}
	}

	/**
	 * Called when a strike hits. There isn't a "miss" yet because that would
	 * interfere with the {@link AbstractAttack} logic a little bit.
	 * 
	 * {@link BattleState} and {@link Combatant}s are already cloned. Make sure
	 * to clone {@link Combatant#source} internally, if necessary.
	 * 
	 * Does not support randomness, so use take-10 rules whenever possible (for
	 * saving throws, etc).
	 * 
	 * @param dc
	 *            Make sure to use this to apply extra damage so it will handle
	 *            death, {@link Monster#dr}, etc.
	 */
	abstract public void hit(Combatant active, Combatant target, BattleState s,
			DamageChance dc);

	@Override
	public boolean validate(Combatant c) {
		return !c.source.melee.isEmpty() || !c.source.ranged.isEmpty();
	}
}