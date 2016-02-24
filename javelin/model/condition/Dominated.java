package javelin.model.condition;

import java.util.ArrayList;

import javelin.model.spell.DominateMonster;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * @see DominateMonster
 * @author alex
 */
public class Dominated extends Condition {

	private Combatant target;

	public Dominated(float expireatp, Combatant c) {
		super(expireatp, c, Effect.NEUTRAL, "dominated");
		this.target = c;
	}

	@Override
			void start(Combatant c) {
		/* can't access here so use #switchteams */
	}

	@Override
			void end(Combatant c) {
		// TODO Auto-generated method stub

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
