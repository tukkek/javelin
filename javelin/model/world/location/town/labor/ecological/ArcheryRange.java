package javelin.model.world.location.town.labor.ecological;

import java.util.ArrayList;

import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.model.world.location.town.labor.military.MartialAcademy;
import javelin.model.world.location.town.labor.religious.Sanctuary;
import javelin.model.world.location.unique.TrainingHall;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.hiringacademy.HiringAcademy;
import javelin.view.screen.hiringacademy.RecruitingAcademyScreen;
import javelin.view.screen.upgrading.AcademyScreen;
import tyrant.mikera.engine.RPG;

public class ArcheryRange extends MartialAcademy implements HiringAcademy {
	static final ArrayList<Monster> CANDIDATES = new ArrayList<Monster>();

	static {
		for (Monster m : TrainingHall.CANDIDATES) {
			if (!m.ranged.isEmpty()) {
				CANDIDATES.add(m);
			}
		}
	}

	public static class BuildArcheryRange extends BuildAcademy {
		public BuildArcheryRange() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy getacademy() {
			return new ArcheryRange();
		}
	}

	Combatant tracker = generatetracker();
	Combatant ranger = null;

	public ArcheryRange() {
		super(Kit.RANGER.upgrades, "Academy (shooting range)",
				RaiseDexterity.SINGLETON);
		descriptionknown = descriptionunknown = "Archery range";
	}

	@Override
	protected void generate() {
		while (x == -1 || !Terrain.get(x, y).equals(Terrain.FOREST)) {
			super.generate();
		}
	}

	@Override
	public void turn(long time, WorldScreen world) {
		super.turn(time, world);
		if (tracker == null && RPG.chancein(7)) {
			tracker = generatetracker();
		}
		if (ranger == null && RPG.chancein(30)) {
			ranger = Sanctuary.generatehire("Ranger", 6, 10, Kit.RANGER,
					RPG.pick(CANDIDATES));
		}
	}

	Combatant generatetracker() {
		return Sanctuary.generatehire("Tracker", 1, 5, Kit.RANGER,
				RPG.pick(CANDIDATES));
	}

	@Override
	protected AcademyScreen getscreen() {
		return new RecruitingAcademyScreen(this, null);
	}

	@Override
	public Combatant[] gethires() {
		return new Combatant[] { tracker, ranger };
	}

	@Override
	public void clearhire(Combatant hire) {
		if (hire == tracker) {
			tracker = null;
		} else {
			ranger = null;
		}
	}

	@Override
	public boolean isworking() {
		return tracker == null && ranger == null;
	}
}
