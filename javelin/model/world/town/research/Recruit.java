package javelin.model.world.town.research;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Incursion;
import javelin.model.world.town.Town;
import javelin.view.screen.town.option.ResearchScreenOption.ResearchScreen;

/**
 * This is a special research card that allows hostile towns to generate units.
 * 
 * Is also responsible for generating new {@link Incursion}s from the town's
 * garrison.
 * 
 * @author alex
 */
public class Recruit extends Research {

	Monster m;

	public Recruit(Monster m) {
		super("Recruit: " + m.toString().toLowerCase(), m.challengeRating);
		this.m = m;
	}

	@Override
	public void apply(Town t, ResearchScreen s) {
		t.garrison.add(new Combatant(null, m.clone(), true));
		if (t.garrison.size() > t.size) {
			return;
		}
		Collections.shuffle(t.garrison);
		Collections.sort(t.garrison, new Comparator<Combatant>() {
			@Override
			public int compare(Combatant o1, Combatant o2) {
				return Float.compare(o1.source.challengeRating,
						o2.source.challengeRating);
			}
		});
		long nmembers = Math.round(Math.floor(t.garrison.size() / 2f));
		ArrayList<Combatant> incursion = new ArrayList<Combatant>();
		for (int i = 0; i < nmembers; i++) {
			incursion.add(t.garrison.get(0));
		}
		if (incursion.size() < 3) {
			return;
		}
		for (int i = 0; i < nmembers; i++) {
			t.garrison.remove(0);
		}
		Incursion.place(t.realm, t.x, t.y, incursion);
	}

	@Override
	protected boolean isrepeated(Town t) {
		return false;
	}

}
