package javelin.controller.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.map.Map;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.town.Town;
import javelin.view.screen.BattleScreen;

/**
 * Battle when a player invades a hostile town.
 * 
 * @see Town#ishostile()
 * 
 * @author alex
 */
public class Siege implements Fight {
	private WorldPlace place;

	public Siege(WorldPlace town) {
		this.place = town;
		Javelin.settexture(IncursionFight.INCURSIONTEXTURE);
	}

	@Override
	public int getel(JavelinApp javelinApp, int teamel) {
		throw new RuntimeException("#siege getel");
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		ArrayList<Combatant> clones = new ArrayList<Combatant>(place.garrison);
		for (int i = 0; i < clones.size(); i++) {
			clones.set(0, clones.get(0).clonedeeply());
		}
		return clones;
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
				super.onEnd();
			}
		};
	}

	@Override
	public boolean rewardgold() {
		return true;
	}

	@Override
	public boolean hide() {
		return false;
	}

	@Override
	public boolean canbribe() {
		return true;
	}

	@Override
	public void bribe() {
		place.garrison.clear();
		place.realm = null;
	}
}
