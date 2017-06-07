package javelin.model.world.location;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.WorldGenerator;
import javelin.model.unit.Combatant;
import javelin.model.world.World;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.view.screen.WorldScreen;

/**
 * An outpost allows for vision of a wide area around it.
 *
 * TODO fog of war: daily obscure all unseen squares. non-hostile locations
 * become source of vision. outposts become hostile (easy) and
 * non-sacrificeable. Probably better to make it progressive (like 1/7 chance a
 * day of a unseen spot disappearing - as to not negate the benefire of Gather
 * Information and such).
 *
 * @see WorldGenerator#makemap()
 * @author alex
 */
public class Outpost extends Fortification {
	/** How many squares away to help vision with. */
	public static final int VISIONRANGE = 3;
	private static final String DESCRIPTION = "Outpost";

	public static class BuildOutpost extends Build {
		public BuildOutpost() {
			super("Build outpost", 5, null, Rank.HAMLET);
		}

		@Override
		public Location getgoal() {
			return new Outpost();
		}

		@Override
		public boolean validate(District d) {
			if (!super.validate(d)) {
				return false;
			}
			if (site != null) {
				return true;
			}
			if (site == null && d.getlocationtype(Outpost.class)
					.size() >= d.town.getrank().rank) {
				return false;
			}
			return super.validate(d) && getsitelocation() != null;
		}

		@Override
		protected Point getsitelocation() {
			District d = town.getdistrict();
			ArrayList<Point> free = d.getfreespaces();
			for (Point p : free) {
				if (town.distance(p.x, p.y) == d.getradius()) {
					return p;
				}
			}
			return null;
		}

		@Override
		protected void done(Location goal) {
			super.done(goal);
			if (!town.ishostile()) {
				Outpost.discover(goal.x, goal.y, Outpost.VISIONRANGE);
			}
		}
	}

	/** Constructor. */
	public Outpost() {
		super(DESCRIPTION, DESCRIPTION, 1, 5);
		gossip = true;
		vision = VISIONRANGE;
		allowedinscenario = false;
	}

	/** Puts a new instance in the {@link World} map. */
	public static void build() {
		new Outpost().place();
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		// Squad.active.view(Squad.active.perceive(false, true) + 10);
		if (Javelin
				.prompt("This outpost grants you vision of the surrounding area.\n"
						+ "Do you want to pillage it for $" + getspoils()
						+ "\n\n"
						+ "Press p to pillage it and any other key to laave...") == 'p') {
			pillage();
			return true;
		}
		return false;
	}

	/**
	 * Given a coordinate shows a big amount of land around that.
	 *
	 * @param range
	 *            How far squares away will become visible.
	 * @see WorldScreen#discovered
	 */
	static public void discover(int xp, int yp, int range) {
		for (int x = xp - range; x <= xp + range; x++) {
			for (int y = yp - range; y <= yp + range; y++) {
				WorldScreen.setVisible(x, y);
			}
		}
	}

	@Override
	protected void generate() {
		x = -1;
		while (x == -1 || findnearest(Outpost.class) != null
				&& findnearest(Outpost.class).distance(x, y) <= VISIONRANGE
						* 2) {
			generateawayfromtown();
		}
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}
}
