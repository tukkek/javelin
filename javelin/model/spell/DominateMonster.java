package javelin.model.spell;

import java.util.ArrayList;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Based on the spell Dominate Monster but trades the duration (1 day/level) to
 * a single battle and to maintain spell-level balance cuts out all the costs of
 * redirecting and commanding the enchanted target.
 * 
 * It's not really a ray but we're abusing the existing logic here because it's
 * a lot easier.
 */
/**
 * @author alex
 * 
 */
public class DominateMonster extends Ray {
	public DominateMonster(String name) {
		super(name + "dominate monster", SpellsFactor
				.calculatechallengeforspelllikeability(18, 9), false, false, 18);
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		if (saved) {
			return target + " resists!";
		}
		final ArrayList<Combatant> targetteam = s.redTeam.contains(target) ? s.redTeam
				: s.blueTeam;
		final ArrayList<Combatant> myteam = targetteam == s.redTeam ? s.blueTeam
				: s.redTeam;
		targetteam.remove(target);
		myteam.add(target);
		return "Dominated " + target + "!";
	}

	@Override
	public double apcost() {
		return 1;
	}

	@Override
	public int calculatetouchdc(Combatant combatant, Combatant target,
			BattleState s) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public int calculatehitdc(Combatant active, Combatant target,
			BattleState state) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public int calculatesavetarget(final Combatant caster,
			final Combatant target) {
		final int will = target.source.will();
		return will == Integer.MAX_VALUE ? will : save(9, will);
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant combatant)
			throws NotPeaceful {
		throw new NotPeaceful();
	}
}
