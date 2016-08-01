package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.List;

import javelin.model.unit.Combatant;
import javelin.view.frame.arena.ArenaWindow;

public class Arena extends UniqueLocation {

	static final String DESCRIPTION = "The arena";
	public ArrayList<Combatant> gladiators = new ArrayList<Combatant>();
	public boolean welcome = true;
	public int coins = 100;

	public Arena() {
		super(DESCRIPTION, DESCRIPTION, 0, 0);
	}

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		// don't
	}

	@Override
	public List<Combatant> getcombatants() {
		return gladiators;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		ArenaWindow w = new ArenaWindow(this);
		w.show();
		w.defer();
		if (w.action != null) {
			w.action.run();
		}
		return true;

	}
}