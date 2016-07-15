package javelin.controller.action.world;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.improvement.BuildHighway;
import javelin.controller.action.world.improvement.BuildInn;
import javelin.controller.action.world.improvement.BuildMine;
import javelin.controller.action.world.improvement.BuildRoad;
import javelin.controller.action.world.improvement.BuildTown;
import javelin.controller.action.world.improvement.Deforestate;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Improvement;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;

/**
 * Special actions only a Worker can execute (see XML). These are things like
 * building roads, inns, towns, mines...
 * 
 * Having more workers with you reduces the time needed to complete tasks.
 * 
 * TODO build mine, see TODO
 * 
 * TODO how to handle random encounters during build time?
 * 
 * @see Squad#work
 * @author alex
 */
public class Work extends WorldAction {
	static final Improvement ROAD = new BuildRoad("Build road", 7, 'r', false);
	static final Improvement HIGHWAY =
			new BuildHighway("Upgrade road", 7, 'r', false);
	static final Improvement INN = new BuildInn("Build inn", 7, 'i', false);
	static final Improvement TOWN = new BuildTown("Build town", 30, 't', false);
	static final Improvement DEFORESTATE =
			new Deforestate("Deforestate", 15, 'd', true);
	static final Improvement MINE = new BuildMine("Build mine", 30, 'm', true);

	class WorkScreen extends SelectScreen {
		public WorkScreen() {
			super("Build what?", null);
			stayopen = false;
		}

		@Override
		public String getCurrency() {
			return "days";
		}

		@Override
		public String printInfo() {
			return "You have " + countworkers() + " worker(s).";
		}

		@Override
		public List<Option> getoptions() {
			ArrayList<Option> options = new ArrayList<Option>();
			options.add(INN);
			options.add(TOWN);
			Terrain t = Terrain.current();
			if (t.getspeed() != t.speedhighway) {
				if (!World.roads[Squad.active.x][Squad.active.y]) {
					options.add(ROAD);
				} else if (!World.highways[Squad.active.x][Squad.active.y]) {
					options.add(HIGHWAY);
				}
			}
			if (t.equals(Terrain.MOUNTAINS)) {
				options.add(MINE);
			} else if (t.equals(Terrain.FOREST)) {
				options.add(DEFORESTATE);
			}
			return options;
		}

		@Override
		public boolean select(Option o) {
			Squad.active.hourselapsed += 24 * build((Improvement) o);
			Squad.active.work = (Improvement) o;
			return true;
		}

		@Override
		public String printpriceinfo(Option o) {
			return " (" + Math.round(Math.round(build((Improvement) o)))
					+ " days)";
		}

	}

	double build(Improvement o) {
		double days = o.price / countworkers();
		return o.absolute ? days : days / Terrain.current().getspeed();
	}

	/** Constructor. */
	public Work() {
		super("Work", new int[] { KeyEvent.VK_W }, new String[] { "w" });
	}

	@Override
	public void perform(WorldScreen screen) {
		if (Dungeon.active != null) {
			Javelin.message("Can't build improvements inside dungeons...",
					false);
			return;
		}
		int workers = countworkers();
		if (workers == 0 && !Javelin.DEBUG) {
			Javelin.message("Take some workers from out of a town first...",
					false);
			return;
		}
		if (Terrain.current().equals(Terrain.WATER)) {
			Javelin.message("Can't build improvements on water...", false);
			return;
		}
		new WorkScreen().show();
	}

	int countworkers() {
		int workers = 0;
		for (Combatant c : Squad.active.members) {
			if (c.source.name.equals("Worker")) {
				workers += 1;
			}
		}
		if (Javelin.DEBUG && workers == 0) {
			return 1;
		}
		return workers;
	}
}
