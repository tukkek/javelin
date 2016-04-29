package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.JavelinApp;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.dungeon.Dungeon;
import javelin.view.screen.DungeonScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.Thing;

/**
 * Makes a movement on the overworld or {@link Dungeon}.
 * 
 * TODO {@link WorldScreen} hierarchy should be refactored into proper Battle /
 * Dungeon / World screens.
 * 
 * @author alex
 */
public class WorldMove extends WorldAction {
	public static final int TIMECOST = 4;
	public static boolean isleavingplace = false;// TODO remove
	private final int deltax;
	private final int deltay;

	public WorldMove(final int[] is, final int deltax, final int deltay,
			final String[] s) {
		super("Move, enter places of interest, fight incursions", is, s);
		this.deltax = deltax;
		this.deltay = deltay;
	}

	@Override
	public void perform(final WorldScreen s) {
		JavelinApp.context.ellapse(TIMECOST);
		final Thing t = JavelinApp.context.updatehero();
		int tox = t.x + deltax;
		int toy = t.y + deltay;
		WorldActor actor =
				Dungeon.active == null ? WorldScreen.getactor(tox, toy) : null;
		WorldPlace place =
				actor instanceof WorldPlace ? (WorldPlace) actor : null;
		try {
			if (JavelinApp.context.react(actor, tox, toy)) {
				if (Dungeon.active != null) {
					if (DungeonScreen.dontmove) {
						DungeonScreen.dontmove = false;
						return;
					}
					place(t, deltax, deltay);
				} else if (place != null) {
					if (place.allowentry && place.garrison.isEmpty()) {
						place(t, deltax, deltay);
					}
					if (place instanceof Dungeon) {
						((Dungeon) place).activate();
					}
				}
				return;
			}
			if (place != null && !place.allowentry) {
				return;
			}
		} catch (StartBattle e) {
			if (place != null && place.allowentry) {
				place(t, deltax, deltay);
			}
			throw e;
		}
		if (s instanceof DungeonScreen && Dungeon.active == null) {
			return;// TODO hack
		}
		if (!place(t, deltax, deltay)) {
			return;
		}
		if (WorldMove.walk(t)) {
			JavelinApp.context.encounter();
		}
		heal();
	}

	public static boolean place(final Thing t, final int deltax,
			final int deltay) {
		JavelinApp.context.map.removeThing(t);
		t.x += deltax;
		t.y += deltay;
		if (!JavelinApp.context.allowmove(t.x, t.y)) {
			t.x -= deltax;
			t.y -= deltay;
			JavelinApp.context.map.addThing(t, t.x, t.y);
			return false;
		}
		if (t.x < 0 || t.x >= JavelinApp.context.map.width || t.y < 0
				|| t.y >= JavelinApp.context.map.height) {
			t.x -= deltax;
			t.y -= deltay;
		}
		JavelinApp.context.updatelocation(t.x, t.y);
		JavelinApp.context.map.addThing(t, t.x, t.y);
		return true;
	}

	public static void heal() {
		for (final Combatant m : Squad.active.members) {
			if (m.source.fasthealing != 0) {
				m.hp = m.maxhp;
			}
		}
	}

	public static boolean walk(final Thing t) {
		final List<Squad> here = new ArrayList<Squad>();
		for (final WorldActor p : Squad.getall(Squad.class)) {
			Squad s = (Squad) p;
			if (s.x == t.x && s.y == t.y) {
				here.add(s);
			}
		}
		if (here.size() <= 1) {
			return true;
		}
		here.get(0).join(here.get(1));
		return false;
	}
}
