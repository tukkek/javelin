package javelin.controller.action;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.model.unit.Combatant;
import javelin.old.Game;
import javelin.old.Game.Delay;

public class AutoAttack extends Action {
	private static final Comparator<Combatant> SORTBYSTATUS = new Comparator<Combatant>() {
		@Override
		public int compare(Combatant o1, Combatant o2) {
			return o1.getnumericstatus() - o2.getnumericstatus();
		}
	};

	public AutoAttack() {
		super("Auto-attack nearest visible enemy", new String[] { "\t" });
	}

	@Override
	public boolean perform(Combatant active) {
		ArrayList<Combatant> melee = Fight.state.getsurroundings(active);
		melee.removeAll(active.getteam(Fight.state));
		if (!melee.isEmpty() && !active.source.melee.isEmpty()) {
			melee.sort(SORTBYSTATUS);
			active.meleeattacks(melee.get(0), Fight.state);
			return true;
		}
		List<Combatant> ranged = Fight.state.gettargets(active);
		ranged.removeAll(melee);
		if (!ranged.isEmpty() && !active.source.ranged.isEmpty()) {
			ranged.sort(new Comparator<Combatant>() {
				@Override
				public int compare(Combatant o1, Combatant o2) {
					Point p = active.getlocation();
					return p.distanceinsteps(o1.getlocation())
							- p.distanceinsteps(o2.getlocation());
				}
			});
			active.meleeattacks(ranged.get(0), Fight.state);
			return true;
		}
		Game.message("No targets in range...", Delay.WAIT);
		throw new RepeatTurn();
	}

	@Override
	public String[] getDescriptiveKeys() {
		return new String[] { "TAB" };
	}
}
