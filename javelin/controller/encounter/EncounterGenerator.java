package javelin.controller.encounter;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.db.Preferences;
import javelin.controller.db.reader.Organization;
import javelin.controller.exception.GaveUpException;
import javelin.model.unit.Monster;
import tyrant.mikera.engine.RPG;

public class EncounterGenerator {
	public static final int MAXGROUPSIZE = Integer.parseInt(Preferences
			.getString("el.maxfoes"));
	static final int MAXTRIES = 1000;

	public static ArrayList<Monster> generate(int el, boolean aquatic)
			throws GaveUpException {
		ArrayList<Monster> encounter = null;
		for (int i = 0; i < MAXTRIES; i++) {
			encounter = select(el, aquatic);
			if (encounter != null) {
				return encounter;
			}
		}
		throw new GaveUpException();
	}

	public static ArrayList<Monster> select(int elp, boolean aquatic) {
		ArrayList<Integer> popper = new ArrayList<Integer>();
		popper.add(elp);
		while (RPG.r(0, 1) == 1) {
			Integer pop = popper.get(RPG.r(0, popper.size() - 1));
			popper.remove(popper.indexOf(pop));
			pop -= 2;
			popper.add(pop);
			popper.add(pop);
		}
		final ArrayList<Monster> foes = new ArrayList<Monster>();
		for (final int el : popper) {
			List<Monster> group = Organization.makeencounter(el, aquatic);
			if (group == null) {
				return null;
			}
			for (Monster invitee : group) {
				if (foes.indexOf(invitee) >= 0) {
					/* prevent joining groups of same creatures */
					return null;
				}
			}
			for (Monster invitee : group) {
				foes.add(invitee);
			}
		}
		if (Javelin.DEBUGMINIMUMFOES != null
				&& foes.size() < Javelin.DEBUGMINIMUMFOES) {
			return null;
		}
		return foes.size() > MAXGROUPSIZE ? null : foes;
	}
}
