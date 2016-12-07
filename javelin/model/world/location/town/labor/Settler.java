package javelin.model.world.location.town.labor;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;

public class Settler extends Labor {
	public Settler(Town t) {
		super("Produce settler", 0, t);
		automatic = false;
	}

	@Override
	public void done() {
		ArrayList<Squad> nearby = town.getdistrict().getsquads();
		produce(town, nearby.contains(Squad.active) ? Squad.active : nearby.get(0));
	}

	@Override
	public boolean validate(District d) {
		return town.population > 1 && !d.getsquads().isEmpty();
	}

	/**
	 * Adds a Worker units to the active {@link Squad}.
	 * 
	 * @param t
	 *            If not <code>null</code> will reduce town {@link #population} by 1.
	 *            If already at 1 (minimum) returns without any effect.
	 */
	static public void produce(Town t, Squad s) {
		if (t != null) {
			if (t.population == 1) {
				return;
			}
			t.population -= 1;
		}
		s.members.add(new Combatant(Javelin.getmonster("Settler"), false));
	}

}
