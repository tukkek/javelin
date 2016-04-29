package javelin.model.world.place;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.PlanarFight;
import javelin.model.Realm;
import javelin.model.item.Key;
import javelin.model.unit.Combatant;
import javelin.model.world.Incursion;
import javelin.model.world.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.World;
import javelin.model.world.place.town.Town;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.WorldScreen;
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

	final WorldActor to;
	final WorldActor from;
	final boolean safe;
	final boolean instantaneous;
	final Long expiresat;
	final boolean wandering;
	/**
	 * An invasion portal is used by incoming {@link Incursion}s as an entry
	 * point to the {@link World}.
	 */
	public final boolean invasion;

	public Portal(WorldActor fromp, WorldActor top, boolean bidirectional,
			boolean wanderingp, boolean safep, boolean instantaneousp,
			Long expiresatp, boolean invasionp) {
		super("A portal");
		from = fromp;
		to = top;
		Point p = null;
		while (p == null) {
			try {
				p = spawn(from);
			} catch (StackOverflowError e) {
				continue;
			}
		}
		x = p.x;
		y = p.y;
		if (bidirectional && !invasionp) {
			new Portal(to, from, false, wanderingp, safep, instantaneousp,
					expiresatp, false).place();
		}
		wandering = wanderingp;
		instantaneous = instantaneousp;
		expiresat = expiresatp;
		safe = safep;
		invasion = invasionp;
		impermeable = !invasionp;
		allowentry = invasionp;
		if (invasion) {
			realm = Realm.random();
			description = "Invasion portal";
		}
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
		Point p = new Point(t.x, t.y);
		while (WorldScreen.getactor(p.x, p.y) != null) {
			p = new Point(determinedistance(t.x), determinedistance(t.y));
			if (p.x < 0 || p.x >= World.MAPDIMENSION || p.y < 0
					|| p.y >= World.MAPDIMENSION) {
				p = new Point(t.x, t.y);
			}
		}
		return p;
	}

	private int determinedistance(int coordinate) {
		int deltamin;
		int deltamax;
		if (RPG.r(1, 2) == 2) {
			deltamin = +3;
			deltamax = +5;
		} else {
			deltamin = -5;
			deltamax = -3;
		}
		return RPG.r(coordinate + deltamin, coordinate + deltamax);
	}

	@Override
	public boolean interact() {
		if (invasion) {
			Game.messagepanel.clear();
			Game.message("You close the invasion portal!", null, Delay.NONE);
			IntroScreen.feedback();
			super.interact();
			return true;
		}
		Key haskey = Key.use(Squad.active);
		if (haskey != null) {
			super.interact();
			throw new StartBattle(new PlanarFight(haskey));
		}
		travel();
		if (expiresat == null) {
			super.interact();// remove
		}
		return true;
	}

	void travel() {
		Point p = spawn(to);
		Squad.active.visual.remove();
		Squad.active.y = p.y;
		Squad.active.displace();
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
	}

	public static void open(float f) {
		if (RPG.random() < f) {
			ArrayList<WorldActor> towns = WorldPlace.getall(Town.class);
			WorldActor from = RPG.pick(towns);
			WorldActor to = RPG.pick(towns);
			while (to == from) {
				to = RPG.pick(towns);
			}
			new Portal(from, to).place();
		}
	}

	@Override
	public void turn(long time, WorldScreen world) {
		if (expiresat != null && Squad.active != null
				&& Squad.active.hourselapsed >= expiresat) {
			super.interact();
			return;
		}
		if (wandering) {
			visual.remove();
			displace();
			place();
		}
	}

	@Override
	protected Integer getel(int attackerel) {
		assert !impermeable;
		return attackerel - 4;
	}
}
