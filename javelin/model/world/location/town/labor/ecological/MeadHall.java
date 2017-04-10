package javelin.model.world.location.town.labor.ecological;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.model.world.location.town.labor.military.MartialAcademy;
import javelin.model.world.location.town.labor.religious.Sanctuary;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.hiringacademy.HiringAcademy;
import javelin.view.screen.hiringacademy.HiringAcademyScreen;
import javelin.view.screen.upgrading.AcademyScreen;
import tyrant.mikera.engine.RPG;

public class MeadHall extends MartialAcademy implements HiringAcademy {
	static final ArrayList<Monster> CANDIDATES = new ArrayList<Monster>();

	static {
		ArrayList<Monster> candidates = new ArrayList<Monster>();
		for (float cr : new float[] { 1f, 1.25f, 1.5f, 1.75f, 2f }) {
			candidates.addAll(Javelin.MONSTERSBYCR.get(cr));
		}
		searching: for (Monster m : candidates) {
			if (!m.think(-1) || Boolean.TRUE.equals(m.lawful)) {
				continue searching;
			}
			int power = m.strength;
			for (int ability : new int[] { m.dexterity, m.intelligence,
					m.wisdom, m.charisma }) {
				if (ability > power) {
					continue searching;
				}
			}
			CANDIDATES.add(m);
		}
	}

	public static class BuildMeadHall extends BuildAcademy {
		public BuildMeadHall() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy getacademy() {
			return new MeadHall();
		}
	}

	Combatant whelp = generatewhelp();
	Combatant barbarian = RPG.chancein(2) ? null : generatebarbarian();
	Combatant chieftain = null;

	public MeadHall() {
		super(Kit.BARBARIAN.upgrades, "Mead hall", null);
		descriptionknown = descriptionunknown = "Mead hall";
	}

	@Override
	protected void generate() {
		while (x == -1 || !(Terrain.get(x, y).equals(Terrain.PLAIN)
				|| Terrain.get(x, y).equals(Terrain.HILL))) {
			super.generate();
		}
	}

	@Override
	public Combatant[] gethires() {
		return new Combatant[] { whelp, barbarian, chieftain };
	}

	@Override
	public void clearhire(Combatant hire) {
		if (hire == whelp) {
			whelp = null;
		} else if (hire == barbarian) {
			barbarian = null;
		} else {
			chieftain = null;
		}
	}

	@Override
	public void turn(long time, WorldScreen world) {
		super.turn(time, world);
		if (whelp == null && RPG.chancein(7)) {
			whelp = generatewhelp();
		}
		if (barbarian == null && RPG.chancein(30)) {
			barbarian = generatebarbarian();
		}
		if (chieftain == null && RPG.chancein(100)) {
			chieftain = generatehire("Chieftain", 11, 15);
		}
	}

	Combatant generatebarbarian() {
		return generatehire("Barbarian", 6, 10);
	}

	Combatant generatewhelp() {
		return generatehire("Whelp", 1, 5);
	}

	Combatant generatehire(String title, int min, int max) {
		return Sanctuary.generatehire(title, min, max, Kit.BARBARIAN,
				RPG.pick(CANDIDATES));
	}

	@Override
	protected AcademyScreen getscreen() {
		return new HiringAcademyScreen(this);
	}

	@Override
	public boolean isworking() {
		return !ishostile() && whelp == null && barbarian == null
				&& chieftain == null;
	}
}
