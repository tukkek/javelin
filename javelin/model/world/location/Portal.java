package javelin.model.world.location;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.town.Town;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * Portals take a {@link Squad} from a place to another. They can also be used
 * to enter {@link PlanarFight}s.
 * 
 * TODO a portal probably is more of an actor than a location...
 * 
 * @author alex
 */
public class Portal extends Location {
	private static final String DESCRIPTION = "A portal";

	private static final int NFEATURES = 6;

	final WorldActor to;
	final WorldActor from;
	final boolean safe;
	final boolean instantaneous;
	/**
	 * Expires at this hour since the start of the game. If <code>null</code>
	 * never expire.
	 * 
	 * @see Squad#hourselapsed
	 */
	public Long expiresat;
	final boolean wandering;
	/**
	 * An invasion portal is used by incoming {@link Incursion}s as an entry
	 * point to the {@link World}.
	 */
	public boolean invasion;

	/**
	 * @param fromp
	 *            Source town.
	 * @param top
	 *            Destination town.
	 * @param bidirectional
	 *            If <code>true</code> will allow travel back.
	 * @param wanderingp
	 *            If <code>true</code> the portal moves with time.
	 * @param safep
	 *            If <code>false</code> deals damage upon entering.
	 * @param instantaneousp
	 *            If <code>false</code> will take some time to get to the other
	 *            side.
	 * @param expiresatp
	 *            See {@link #expiresat}.
	 * @param invasionp
	 *            If <code>true</code> will periodically bring creatures from
	 *            the other side until closed.
	 */
	public Portal(WorldActor fromp, WorldActor top, boolean bidirectional,
			boolean wanderingp, boolean safep, boolean instantaneousp,
			Long expiresatp, boolean invasionp) {
		super(DESCRIPTION);
		link = false;
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
		setinvasion(invasionp);
	}

	/**
	 * @param invasionp
	 *            Changes all relevant field values according to this.
	 * @see #invasion
	 */
	public void setinvasion(boolean invasionp) {
		invasion = invasionp;
		impermeable = !invasion;
		allowentry = invasion;
		if (invasion) {
			realm = Realm.random();
			description = "Invasion portal";
		} else {
			realm = null;
			description = DESCRIPTION;
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
		while (WorldActor.get(p.x, p.y) != null) {
			p = new Point(determinedistance(t.x), determinedistance(t.y));
			if (p.x < 0 || p.x >= World.SIZE || p.y < 0 || p.y >= World.SIZE
					|| World.seed.map[p.x][p.y].equals(Terrain.WATER)) {
				World.retry();
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
			Game.message("You close the invasion portal!", Delay.NONE);
			IntroScreen.feedback();
			super.interact();
			return true;
		}
		// Key haskey = Key.use(Squad.active);
		// if (haskey != null) {
		// super.interact();
		// throw new StartBattle(new PlanarFight(haskey));
		// }
		travel();
		if (expiresat == null) {
			super.interact();// remove
		}
		return true;
	}

	void travel() {
		Point p = spawn(to);
		Squad.active.x = p.x;
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
			Game.message(description, Delay.BLOCK);
			Game.getInput();
		}
	}

	/**
	 * @return
	 */
	public static Portal open() {
		ArrayList<WorldActor> towns = Location.getall(Town.class);
		WorldActor from = RPG.pick(towns);
		WorldActor to = RPG.pick(towns);
		while (to == from) {
			to = RPG.pick(towns);
		}
		return new Portal(from, to);
	}

	@Override
	public void turn(long time, WorldScreen world) {
		if (expiresat != null && Squad.active != null
				&& Squad.active.hourselapsed >= expiresat) {
			super.interact();
			return;
		}
		if (wandering) {
			displace();
			place();
		}
	}

	@Override
	protected Integer getel(int attackerel) {
		assert !impermeable;
		return attackerel - 4;
	}

	/**
	 * Opens a portal that a {@link Key} can be safely used upon.
	 */
	public static void opensafe() {
		Portal p = Portal.open();
		p.setinvasion(false);
		p.expiresat = null;
		p.place();
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}
}
