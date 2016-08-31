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
	public int getel(int teamel) {
		throw new RuntimeException("#siege getel");
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
		place.garrison.clear();
		place.realm = null;
	}

	@Override
	public boolean onEnd(BattleScreen screen, ArrayList<Combatant> originalTeam,
			BattleState s) {
		if (cleargarrison) {
			garrison: for (Combatant garrison : new ArrayList<Combatant>(
					place.garrison)) {
				for (Combatant active : Fight.state.getcombatants()) {
					if (garrison.equals(active)) {
						continue garrison; // is alive
					}
				}
				place.garrison.remove(garrison);
			}
			if (place.garrison.isEmpty()) {
				place.realm = null;
				Town t = place instanceof Town ? (Town) place : null;
				if (t != null) {
					t.capture(true);
				}
			}
		}
		return super.onEnd(screen, originalTeam, s);
	}
}
