package javelin.model.unit.abilities.spell.enchantment.compulsion;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.Action;
import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Ray;
import javelin.model.unit.condition.Condition;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * Based on the spell Dominate Monster but trades the duration (1 day/level) to
 * a single battle and to maintain spell-level balance cuts out all the costs of
 * redirecting and commanding the enchanted target. The second save (to act
 * against your nature) at a +2 bonus is still granted though.
 *
 * It's not really a ray but we're abusing the existing logic here because it's
 * a lot easier.
 */
public class DominateMonster extends Ray {
	static final float CR = ChallengeCalculator.ratespelllikeability(9);

	public class Dominated extends Condition {
		Combatant target;

		public Dominated(float expireatp, Combatant c, Integer casterlevelp) {
			super(expireatp, c, Effect.NEUTRAL, "dominated", casterlevelp);
			target = c;
		}

		@Override
		public void start(Combatant c) {
			/* can't access here so use #switchteams */
		}

		@Override
		public void end(Combatant c) {
			// see #finish
		}

		@Override
		public void finish(BattleState s) {
			target = s.clone(target);
			switchteams(target, s);
		}
	}

	/** Constructor. */
	public DominateMonster() {
		super("Dominate monster", 9, CR, Realm.EVIL);
		automatichit = true;
		apcost = 1;
		castinbattle = true;
		isscroll = true;
		apcost = 1;
	}

	@Override
	public String cast(Combatant caster, Combatant target, boolean saved,
			BattleState s, ChanceNode cn) {
		cn.overlay = new AiOverlay(target.getlocation());
		if (saved) {
			return target + " resists!";
		}
		switchteams(target, s);
		Dominated d = new Dominated(Float.MAX_VALUE, target, casterlevel);
		target.addcondition(d);
		return "Dominated " + target + "!";
	}

	@Override
	public int save(final Combatant caster, final Combatant target) {
		return getsavetarget(target.source.getwill(), caster);
	}

	@Override
	public float getsavechance(Combatant caster, Combatant target) {
		float first = super.getsavechance(caster, target);
		if (first == 0 || first == 1) {
			return first;
		}
		float second = Action.bind(first + .1f);
		/*
		 * chance of either passing the first or not passing the first but
		 * passing the second:
		 */
		return first + second - first * second;
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		super.filtertargets(combatant, targets, s);
		for (Combatant c : new ArrayList<>(targets)) {
			if (c.source.immunitytomind) {
				targets.remove(c);
			}
		}
	}

	static void switchteams(Combatant target, BattleState s) {
		ArrayList<Combatant> from = s.getteam(target);
		from.remove(target);
		(from == s.redTeam ? s.blueTeam : s.redTeam).add(target);
	}
}
