package javelin.model.world;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.PlanarFight;
import javelin.model.item.Key;
import javelin.model.unit.Combatant;
import javelin.model.world.town.Town;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * Portals usually take a {@link Squad} from a place to another.
 * 
 * TODO Lots of good ideas here
 * http://www.campaignmastery.com/blog/portals-to-celest-morph-1/
 * 
 * TODO would be cool to have a portal feature that drops you on a wider area
 * 
 * @author alex
 */
public class Portal extends WorldPlace {
	private static final int NFEATURES = 6;

	public static ArrayList<WorldPlace> portals = new ArrayList<WorldPlace>();

	final WorldActor to;
	final WorldActor from;
	final boolean safe;
	final boolean instantaneous;
	final Long expiresat;
	final boolean wandering;
	/**
	 * An invasion portal is used by incoming {@link Incursion}s as an entry
	 * point to the {@link WorldMap}.
	 */
	public final boolean invasion;

	public Portal(WorldActor fromp, WorldActor top, boolean bidirectional,
			boolean wanderingp, boolean safep, boolean instantaneousp,
			Long expiresatp, boolean invasion) {
		super("traveler portal", "a portal");
		from = fromp;
		to = top;
		Point p = spawn(from);
		x = p.x;
		y = p.y;
		if (bidirectional && !invasion) {
			new Portal(to, from, false, wanderingp, safep, instantaneousp,
					expiresatp, false).place();
		}
		wandering = wanderingp;
		instantaneous = instantaneousp;
		expiresat = expiresatp;
		safe = safep;
		this.invasion = false; // TODO
	}

	public Portal(WorldActor from, WorldActor to) {
		this(from, to, Portal.activatefeature(), Portal.activatefeature(),
				!Portal.activatefeature(), !Portal.activatefeature(),
				Portal.activatefeature()
						? Squad.active.hourselapsed + RPG.r(7, 30) * 24 : null,
				Portal.activatefeature());
	}

	static boolean activatefeature() {
		return RPG.r(1, NFEATURES + 1) == 1;
	}

	@Override
	protected void generate() {
		// do nothing
	}

	private Point spawn(WorldActor t) {
		Point p = new Point(t.getx(), t.gety());
		while (WorldScreen.getactor(p.x, p.y) != null || p.x < 0
				|| p.x >= WorldMap.MAPDIMENSION - 1 || p.y < 0
				|| p.y >= WorldMap.MAPDIMENSION - 1) {
			p = new Point(determinedistance(t.getx()),
					determinedistance(t.gety()));
		}
		return p;
	}

	private int determinedistance(int coordinate) {
		int deltamin;
		int deltamax;
		if (RPG.r(1, 2) == 2) {
			deltamin = +4;
			deltamax = +5;
		} else {
			deltamin = -5;
			deltamax = -4;
		}
		return RPG.r(coordinate + deltamin, coordinate + deltamax);
	}

	@Override
	public void enter() {
		if (invasion) {
			Game.messagepanel.clear();
			Game.message("You close the invasion portal!", null, Delay.NONE);
			IntroScreen.feedback();
			super.enter();
			return;
		}
		Key haskey = Key.use(Squad.active);
		if (haskey != null) {
			super.enter();
			throw new StartBattle(new PlanarFight(haskey));
		}
		Point p = spawn(to);
		Squad.active.x = p.x;
		Squad.active.y = p.y;
		Squad.active.displace();
		Squad.active.visual.remove();
		Squad.active.place();
		String description = "";
		if (!instantaneous) {
			description += "It didn't seem that long on the way in...\n";
			Squad.active.hourselapsed += RPG.r(1, 7) * 24;
		}
		if (!safe) {
			description += "Ouch!";
			for (Combatant c : Squad.active.members) {
				c.hp -= Math.round(c.hp * RPG.r(1, 3) / 10f);
			}
		}
		if (!description.isEmpty()) {
			Game.messagepanel.clear();
			Game.message(description, null, Delay.BLOCK);
			Game.getInput();
		}
		if (expiresat == null) {
			super.enter();
		}
	}

	@Override
			List<WorldPlace> getall() {
		return Portal.portals;
	}

	public static void open(float f) {
		for (WorldPlace p : (List<WorldPlace>) Portal.portals.clone()) {
			((Portal) p).update();
		}
		if (RPG.random() < f) {
			WorldActor from = RPG.pick(Town.towns);
			WorldActor to = RPG.pick(Town.towns);
			while (to == from) {
				to = RPG.pick(Town.towns);
			}
			new Portal(from, to).place();
		}
	}

	void update() {
		if (expiresat != null && Squad.active != null
				&& Squad.active.hourselapsed >= expiresat) {
			super.enter();
			return;
		}
		if (wandering) {
			visual.remove();
			WorldScreen.displace(this);
			place();
		}
	}
}
