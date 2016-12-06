package javelin.model.world.location.fortification;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.fight.Siege;
import javelin.controller.terrain.Mountains;
import javelin.controller.terrain.Terrain;
import javelin.model.EquipmentMap;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.unique.AdventurersGuild;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;

/**
 * Found only on {@link Mountains}, mines allow a {@link Squad} to mine gold
 * over a period of time or leave {@link Combatant}s there for long-term mining
 * - in which case the resulting gold needs to be gathered there by a
 * {@link Squad} later on.
 * 
 * @author alex
 */
public class Mine extends Fortification {
	static final String DESCRIPTION = "A gold mine";
	static final Option MINEDAY = new Option("Mine for a day", 0, 'd');
	static final Option MINEWEEK = new Option("Mine for a week", 0, 'w');
	static final Option PLACEMINER = new Option("Assign a unit to this mine", 0, 'a');
	static final Option RECALLMINER = new Option("Recall a miner", 0, 'r');

	class MineScreen extends SelectScreen {
		MineScreen() {
			super("You find yourself at the entrance of a mine.", null);
			stayopen = false;
		}

		@Override
		public String getCurrency() {
			return "";
		}

		@Override
		public String printInfo() {
			return "";
		}

		@Override
		public boolean select(Option o) {
			if (o == MINEDAY) {
				Squad.active.gold += AdventurersGuild.pay(0, 1, Squad.active.members);
				Squad.active.hourselapsed += 24;
				return true;
			}
			if (o == MINEWEEK) {
				Squad.active.gold += AdventurersGuild.pay(0, 7, Squad.active.members);
				Squad.active.hourselapsed += 7 * 24;
				return true;
			}
			if (o == PLACEMINER) {
				ArrayList<Combatant> eligible = geteligible();
				int choice = Javelin
						.choose("Note that only rational, non-mercenary units in decent health can be stationed.\n\n"
								+ "Which unit will stay mining?", eligible, true, false);
				if (choice >= 0) {
					assign(eligible.get(choice), Squad.active.members, miners, Squad.active.equipment, equipment);
				}
				return false;
			}
			if (o == RECALLMINER) {
				int choice = Javelin.choose("Recall which miner?", miners, true, false);
				if (choice >= 0) {
					assign(miners.get(choice), miners, Squad.active.members, equipment, Squad.active.equipment);
				}
				return false;
			}
			throw new RuntimeException(o.name + " #unknownmineoption");
		}

		void assign(Combatant recruit, List<Combatant> from, List<Combatant> to, EquipmentMap fromequipment,
				EquipmentMap toequipment) {
			toequipment.put(recruit.id, fromequipment.get(recruit.id));
			fromequipment.remove(recruit.id);
			from.remove(recruit);
			to.add(recruit);
		}

		@Override
		public List<Option> getoptions() {
			ArrayList<Option> options = new ArrayList<Option>();
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
					return o1.key.compareTo(o2.key);
				}
			};
		}

		@Override
		public String printpriceinfo(Option o) {
			return "";
		}
	}

	List<Combatant> miners = new ArrayList<Combatant>();
	float stash = 0;
	EquipmentMap equipment = new EquipmentMap();

	/** Constructor. */
	public Mine() {
		super(DESCRIPTION, DESCRIPTION, 6, 10);
		terrain = Terrain.UNDERGROUND;
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
			if (!c.mercenary && c.getnumericstatus() >= Combatant.STATUSHURT && c.source.think(-1)) {
				eligible.add(c);
			}
		}
		return eligible;
	}

	@Override
	protected Integer getel(int attackerel) {
		return miners.isEmpty() ? Integer.MIN_VALUE : ChallengeRatingCalculator.calculateel(miners);
	}

	@Override
	public void turn(long time, WorldScreen world) {
		stash += AdventurersGuild.pay(0, 1, miners);
	}

	@Override
	public boolean hasupgraded() {
		return stash >= 1;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		if (stash >= 1) {
			int gold = Math.round(Math.round(Math.ceil(stash)));
			Javelin.message("You collect $" + gold + " from the mine!", false);
			stash = 0;
			Squad.active.gold += gold;
		}
		new MineScreen().show();
		return true;
	}

	@Override
	protected void generate() {
		if (x != -1) {
			return;
		}
		while (x == -1 || !Terrain.get(x, y).equals(Terrain.MOUNTAINS) || !validatedistance()) {
			super.generate();
		}
	}

	boolean validatedistance() {
		for (WorldActor mine : WorldActor.getall(Mine.class)) {
			if (mine != this && distance(mine.x, mine.y) < Outpost.VISIONRANGE * 2) {
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
}
