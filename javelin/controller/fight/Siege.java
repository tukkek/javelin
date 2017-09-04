package javelin.controller.fight;

import java.util.ArrayList;

import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.view.screen.BattleScreen;

/**
 * Battle when a player invades a hostile town.
 * 
 * @see Town#ishostile()
 * 
 * @author alex
 */
public class Siege extends Fight {
	private Location place;
	/**
	 * If <code>false</code> will skip
	 * {@link #onEnd(BattleScreen, ArrayList, BattleState)} but still call
	 * {@link Fight#onEnd(BattleScreen, ArrayList, BattleState)}. This allows
	 * subclasses to take control of after-fight consequences.
	 */
	protected boolean cleargarrison = true;

	/**
	 * @param l
	 *            Where this fight is occurring at.
	 */
	public Siege(Location l) {
		this.place = l;
		texture = IncursionFight.INCURSIONTEXTURE;
		hide = false;
		meld = true;
	}

	@Override
	public ArrayList<Combatant> getmonsters(int teamel) {
		ArrayList<Combatant> clones = new ArrayList<Combatant>(place.garrison);
		for (int i = 0; i < clones.size(); i++) {
			clones.set(i, clones.get(i).clone().clonesource());
		}
		return clones;
	}

	@Override
	public void bribe() {
		afterwin();
		place.realm = null;
	}

	@Override
	public boolean onend() {
		if (cleargarrison) {
			if (Fight.victory) {
				afterwin();
			} else {
				afterlose();
			}
			/* TODO this should probably be inside afterwin() */
			if (place.garrison.isEmpty()) {
				place.capture();
			}
		}
		return super.onend();
	}

	protected void afterlose() {
		ArrayList<Combatant> alive = state.getcombatants();
		for (Combatant c : new ArrayList<Combatant>(place.garrison)) {
			if (!alive.contains(c)) {
				place.garrison.remove(c);
			}
		}
	}

	protected void afterwin() {
		place.garrison.clear();
	}
}
