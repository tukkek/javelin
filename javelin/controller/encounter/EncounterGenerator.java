package javelin.controller.encounter;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.db.Properties;
import javelin.controller.db.reader.factor.Organization;
import javelin.controller.exception.GaveUpException;
import javelin.model.unit.Combatant;
import tyrant.mikera.engine.RPG;

/**
 * Generates an {@link Encounter}.
 * 
 * @author alex
 */
public class EncounterGenerator {
	public static final int MAXGROUPSIZE =
			Integer.parseInt(Properties.getString("el.maxfoes"));
	static final int MAXTRIES = 1000;

	public static ArrayList<Combatant> generate(int el) throws GaveUpException {
		ArrayList<Combatant> encounter = null;
		for (int i = 0; i < MAXTRIES; i++) {
			encounter = select(el);
			if (encounter != null) {
				return encounter;
			}
		}
		throw new GaveUpException();
	}

	public static ArrayList<Combatant> select(int elp) {
		ArrayList<Integer> popper = new ArrayList<Integer>();
		popper.add(elp);
		while (RPG.r(0, 1) == 1) {
			Integer pop = popper.get(RPG.r(0, popper.size() - 1));
			popper.remove(popper.indexOf(pop));
			pop -= 2;
			popper.add(pop);
			popper.add(pop);
		}
		final ArrayList<Combatant> foes = new ArrayList<Combatant>();
		for (final int el : popper) {
			List<Combatant> group = Organization.makeencounter(el);
			if (group == null) {
				return null;
			}
			for (Combatant invitee : group) {
				if (foes.indexOf(invitee) >= 0) {
					/* prevent joining groups of same creatures */
					return null;
				}
			}
			for (Combatant invitee : group) {
				foes.add(invitee);
			}
		}
		if (Javelin.DEBUGMINIMUMFOES != null
				&& foes.size() != Javelin.DEBUGMINIMUMFOES) {
			return null;
		}
		return foes.size() > MAXGROUPSIZE ? null : foes;
	}
}
