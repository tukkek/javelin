package javelin.model.world.location.town.labor.religious;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.controller.kit.Kit;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.view.Images;
import javelin.view.screen.SquadScreen;
import tyrant.mikera.engine.RPG;

public class Sanctuary extends Guild {
	static final int UPGRADECOST = Kit.PALADIN.upgrades.size();
	static final ArrayList<Monster> PRIESTS = new ArrayList<Monster>();
	static final ArrayList<Monster> PALADINS = new ArrayList<Monster>();

	static {
		for (Monster m : SquadScreen.CANDIDATES) {
			if (Boolean.FALSE.equals(m.lawful) || !m.think(-2)) {
				continue;
			}
			if (!Boolean.FALSE.equals(m.good)) {
				PRIESTS.add(m);
				if (Boolean.TRUE.equals(m.good)) {
					PALADINS.add(m);
				}
			}
		}
	}

	public static class BuildSanctuary extends BuildAcademy {
		public BuildSanctuary() {
			super(Rank.VILLAGE);
		}

		@Override
		protected Academy getacademy() {
			return new Sanctuary();
		}
	}

	public class UpǵradeSanctuary extends BuildingUpgrade {
		public UpǵradeSanctuary(Location previous) {
			super("Cathedral", UPGRADECOST, UPGRADECOST, previous, Rank.TOWN);
		}

		@Override
		public Location getgoal() {
			return previous;
		}

		@Override
		protected void done(Location goal) {
			super.done(goal);
			Sanctuary s = (Sanctuary) goal;
			s.upgrades.addAll(Kit.PALADIN.upgrades);
			s.upgraded = true;
			s.descriptionknown = s.descriptionunknown = "Cathedral";
			boolean forcepriest = false;
			boolean forcepaladin = false;
			while (!forcepriest && !forcepaladin) {
				forcepriest = RPG.chancein(2);
				forcepaladin = RPG.chancein(2);
			}
			s.generatehires(true, forcepriest, forcepaladin);
		}
	}

	boolean upgraded = false;

	public Sanctuary() {
		super("Sanctuary", Kit.CLERIC, true);
	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		ArrayList<Labor> upgrades = super.getupgrades(d);
		if (!upgraded) {
			upgrades.add(new UpǵradeSanctuary(this));
		}
		return upgrades;
	}

	@Override
	public Image getimage() {
		return upgraded ? Images.getImage("locationcathedral")
				: super.getimage();
	}

	void generatehires(boolean forceacolyte, boolean forcepriest,
			boolean forcepaladin) {
	}

	@Override
	protected javelin.model.unit.Combatant[] generatehires() {
		return new Combatant[] { generatehire(7, "Acolyte", 1, 5),
				generatehire(30, "Priest", 6, 10),
				upgraded && RPG.chancein(30) ? generatehire("Paladin", 6, 15,
						Kit.PALADIN, RPG.pick(PALADINS)) : null };
	}

	@Override
	protected List<Monster> getcandidates() {
		return PRIESTS;
	}
}
