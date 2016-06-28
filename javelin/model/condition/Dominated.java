package javelin.model.condition;

import java.util.ArrayList;

import javelin.model.spell.enchantment.compulsion.DominateMonster;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * @see DominateMonster
 * @author alex
 */
public class Dominated extends Condition {

	private Combatant target;

	public Dominated(float expireatp, Combatant c, Integer casterlevelp) {
		super(expireatp, c, Effect.NEUTRAL, "dominated", casterlevelp);
		this.target = c;
	}

	@Override
	public void start(Combatant c) {
		/* can't access here so use #switchteams */
	}

	@Override
	public void end(Combatant c) {
		// see #finish
	}

	public static void switchteams(Combatant target, BattleState s) {
		final ArrayList<Combatant> from =
				s.redTeam.contains(target) ? s.redTeam : s.blueTeam;
		from.remove(target);
		final ArrayList<Combatant> to =
				from == s.redTeam ? s.blueTeam : s.redTeam;
		to.add(target);
	}

	@Override
	public void finish(BattleState s) {
		target = s.clone(target);
		switchteams(target, s);
	}
}
