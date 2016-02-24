package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.JavelinApp;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.Thing;

public class WorldMove extends WorldAction {
	public static final int TIMECOST = 4;
	public static boolean isleavingplace = false;
	private final int deltax;
	private final int deltay;

	public WorldMove(final int[] is, final int deltax, final int deltay,
			final String[] s) {
		super("Move, enter towns and dungeons, fight incursions", is, s);
		this.deltax = deltax;
		this.deltay = deltay;
	}

	@Override
	public void perform(final WorldScreen s) {
		JavelinApp.context.ellapse(TIMECOST);
		final Thing t = JavelinApp.context.updatehero();
		if (JavelinApp.context.entertown(t, s, t.x + deltax, t.y + deltay)
				|| !place(t, deltax, deltay)
				|| JavelinApp.context.react(t, this)) {
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
		for (final Squad s : Squad.squads) {
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
