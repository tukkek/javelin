package javelin.controller.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.JavelinApp;
import javelin.controller.map.Map;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.world.town.Town;
import javelin.view.screen.BattleScreen;

/**
 * Battle when a player invades a hostile town.
 * 
 * @see Town#ishostile()
 * 
 * @author alex
 */
public class Siege implements Fight {

	private Town town;

	public Siege(Town town) {
		this.town = town;
	}

	@Override
	public int getel(JavelinApp javelinApp, int teamel) {
		throw new RuntimeException("#siege getel");
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		return town.garrison;
	}

	@Override
	public boolean meld() {
		return true;
	}

	@Override
	public Map getmap() {
		/*
		 * TODO would be super cool to have a procedural map generator that
		 * would produce similar maps for similar Towns
		 */
		return null;
	}

	@Override
	public boolean friendly() {
		return false;
	}

	@Override
	public BattleScreen getscreen(JavelinApp javelinApp, BattleMap battlemap) {
		return new BattleScreen(javelinApp, battlemap, true) {
			@Override
			public void onEnd() {
				garrison: for (Combatant garrison : new ArrayList<Combatant>(
						town.garrison)) {
					for (Combatant active : BattleMap.combatants) {
						if (garrison.equals(active)) {
							continue garrison;
						}
					}
					town.garrison.remove(garrison);
				}
				if (town.garrison.isEmpty()) {
					town.capture();
				}
				super.onEnd();
			}
		};
	}
}
