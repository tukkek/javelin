package javelin.model.world.location.town.labor.productive;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.fight.Siege;
import javelin.controller.terrain.Mountains;
import javelin.controller.terrain.Terrain;
import javelin.model.EquipmentMap;
import javelin.model.item.Ruby;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.unique.AdventurersGuild;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

/**
 * Found only on {@link Mountains}, mines allow a {@link Squad} to mine gold
 * over a period of time or leave {@link Combatant}s there for long-term mining
 * - in which case the resulting gold needs to be gathered there by a
 * {@link Squad} later on.
 *
 * @author alex
 */
public class Mine extends Fortification {
	static final String DESCRIPTION = "Gold mine";
	static final Option MINEDAY = new Option("", 0, 'd');
	static final Option MINEWEEK = new Option("", 0, 'w');
	static final Option PLACEMINER = new Option(
			"Assign an unit to work on this mine", 0, 'a');
	static final Option RECALLMINER = new Option("Recall a miner", 0, 'r');

	public static class BuildMine extends Build {
		public BuildMine() {
			super("Build mine", 10, null, Rank.HAMLET);
		}

		@Override
		public Location getgoal() {
			return new Mine();
		}

		@Override
		public boolean validate(District d) {
			return super.validate(d) && d.getlocationtype(Mine.class).isEmpty()
					&& getsitelocation() != null;
		}

		@Override
		protected Point getsitelocation() {
			ArrayList<Point> free = town.getdistrict().getfreespaces();
			for (Point p : free) {
				if (Terrain.get(p.x, p.y).equals(Terrain.MOUNTAINS)) {
					return p;
				}
			}
			return null;
		}
	}

	public class UpgradeMine extends BuildingUpgrade {
		public UpgradeMine(Mine mine) {
			super("Ruby mine", Math.max(0, 10 - mine.miners.size()), 5, mine,
					Rank.VILLAGE);
		}

		@Override
		public Location getgoal() {
			return previous;
		}

		@Override
		public void done(Location l) {
			super.done(l);
			Mine m = (Mine) l;
			m.rename("Ruby mine");
			m.minesrubies = true;
		}

		@Override
		public boolean validate(District d) {
			return !((Mine) previous).minesrubies && super.validate(d);
		}
	}

	class MineScreen extends SelectScreen {
		MineScreen() {
			super("You find yourself at the entrance of a "
					+ descriptionknown.toLowerCase() + ".", null);
			stayopen = false;
		}

		@Override
		public String getCurrency() {
			return "";
		}

		@Override
		public String printinfo() {
			return "";
		}

		@Override
		public boolean select(Option o) {
			if (o == MINEDAY) {
				Squad.active.gold += mine(1);
				Squad.active.hourselapsed += 24;
				return true;
			}
			if (o == MINEWEEK) {
				Squad.active.gold += mine(7);
				Squad.active.hourselapsed += 7 * 24;
				return true;
			}
			if (o == PLACEMINER) {
				ArrayList<Combatant> eligible = geteligible();
				int choice = Javelin.choose(
						"Note that only rational, non-mercenary units in decent health can be stationed.\n\n"
								+ "Which unit will stay mining?",
						eligible, true, false);
				if (choice >= 0) {
					assign(eligible.get(choice), Squad.active.members, miners,
							Squad.active.equipment, equipment);
				}
				return false;
			}
			if (o == RECALLMINER) {
				int choice = Javelin.choose("Recall which miner?", miners, true,
						false);
				if (choice >= 0) {
					assign(miners.get(choice), miners, Squad.active.members,
							equipment, Squad.active.equipment);
				}
				return false;
			}
			throw new RuntimeException(o.name + " #unknownmineoption");
		}

		public int mine(int days) {
			return Math
					.round(AdventurersGuild.pay(0, days, Squad.active.members));
		}

		void assign(Combatant recruit, List<Combatant> from, List<Combatant> to,
				EquipmentMap fromequipment, EquipmentMap toequipment) {
			toequipment.put(recruit.id, fromequipment.get(recruit.id));
			fromequipment.remove(recruit.id);
			from.remove(recruit);
			to.add(recruit);
		}

		@Override
		public List<Option> getoptions() {
			ArrayList<Option> options = new ArrayList<Option>();
			MINEDAY.name = "Mine for a day ($" + mine(1) + ")";
			MINEWEEK.name = "Mine for a week ($" + mine(7) + ")";
			options.add(MINEDAY);
			options.add(MINEWEEK);
			if (Squad.active.members.size() > 1) {
				options.add(PLACEMINER);
			}
			if (!miners.isEmpty()) {
				options.add(RECALLMINER);
			}
			return options;
		}

		@Override
		protected Comparator<Option> sort() {
			return new Comparator<Option>() {
				@Override
				public int compare(Option o1, Option o2) {
					return o1.name.compareTo(o2.name);
				}
			};
		}

		@Override
		public String printpriceinfo(Option o) {
			return "";
		}
	}

	List<Combatant> miners = new ArrayList<Combatant>();
	float gold = 0;
	EquipmentMap equipment = new EquipmentMap();
	boolean minesrubies = false;
	int rubies = 0;

	/** Constructor. */
	public Mine() {
		super(DESCRIPTION, DESCRIPTION, 6, 10);
		terrain = Terrain.UNDERGROUND;
		allowedinscenario = false;
	}

	/**
	 * Avoids {@link #generate()}. Use this for when a player is building a mine
	 * via {@link Work}.
	 */
	public Mine(int x, int y) {
		this();
		this.x = x;
		this.y = y;
	}

	ArrayList<Combatant> geteligible() {
		ArrayList<Combatant> eligible = new ArrayList<Combatant>();
		for (Combatant c : Squad.active.members) {
			if (!c.mercenary && c.getnumericstatus() >= Combatant.STATUSHURT
					&& c.source.think(-1)) {
				eligible.add(c);
			}
		}
		return eligible;
	}

	@Override
	public Integer getel(int attackerel) {
		return miners.isEmpty() ? Integer.MIN_VALUE
				: ChallengeCalculator.calculateel(miners);
	}

	@Override
	public void turn(long time, WorldScreen world) {
		if (ishostile()) {
			return;
		}
		gold += AdventurersGuild.pay(0, 1, miners);
		if (minesrubies && RPG.random() < 1 / 30f) {
			rubies += 1;
		}
	}

	@Override
	public boolean hasupgraded() {
		return gold >= 1;
	}

	@Override
	public boolean hascrafted() {
		return rubies > 0;
	}

	@Override
	public boolean isworking() {
		return !miners.isEmpty();
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		String collected = "";
		if (gold >= 1) {
			int g = Math.round(Math.round(Math.ceil(gold)));
			Squad.active.gold += g;
			gold = 0;
			collected += "You collect $" + SelectScreen.formatcost(g)
					+ " from the mine!\n";
		}
		if (rubies > 0) {
			collected += "You collect " + rubies + " rubies from the mine!\n";
			for (int i = 0; i < rubies; i++) {
				new Ruby().grab();
			}
			rubies = 0;
		}
		if (!collected.isEmpty()) {
			Javelin.message(collected, false);
		}
		new MineScreen().show();
		return true;
	}

	@Override
	protected void generate() {
		if (x != -1) {
			return;
		}
		while (x == -1 || !Terrain.get(x, y).equals(Terrain.MOUNTAINS)
				|| !validatedistance()) {
			super.generate();
		}
	}

	boolean validatedistance() {
		for (Actor mine : World.getall(Mine.class)) {
			if (mine != this
					&& distance(mine.x, mine.y) < Outpost.VISIONRANGE * 2) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected Siege fight() {
		Siege s = super.fight();
		s.terrain = Terrain.UNDERGROUND;
		return s;
	}

	@Override
	public List<Combatant> getcombatants() {
		ArrayList<Combatant> combatants = new ArrayList<Combatant>(garrison);
		combatants.addAll(miners);
		return combatants;
	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		ArrayList<Labor> upgrades = super.getupgrades(d);
		upgrades.add(new UpgradeMine(this));
		return upgrades;
	}
}
