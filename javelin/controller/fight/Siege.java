package javelin.controller.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.model.BattleMap;
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

	public Siege(Location l) {
		this.place = l;
		texture = IncursionFight.INCURSIONTEXTURE;
		hide = true;
		meld = true;
	}

	@Override
	public int getel(int teamel) {
		throw new RuntimeException("#siege getel");
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		ArrayList<Combatant> clones = new ArrayList<Combatant>(place.garrison);
		for (int i = 0; i < clones.size(); i++) {
			clones.set(i, clones.get(i).clonedeeply());
		}
		return clones;
	}

	@Override
	public void bribe() {
		place.garrison.clear();
		place.realm = null;
	}

	@Override
	public void onEnd(BattleScreen screen, ArrayList<Combatant> originalTeam,
			BattleState s) {
		garrison: for (Combatant garrison : new ArrayList<Combatant>(
				place.garrison)) {
			for (Combatant active : BattleMap.combatants) {
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
		super.onEnd(screen, originalTeam, s);
	}
}
