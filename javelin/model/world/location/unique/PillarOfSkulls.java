package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Heroic;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Dwelling;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.haxor.HaxorScreen;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

/**
 * Sacrifice allies, experience and items for benefits.
 * 
 * @author alex
 */
public class PillarOfSkulls extends UniqueLocation {
	static String[] QUOTES = new String[] { "hisses at you!",
			"is blabbering nonsense...", "is talking to its neighbor.",
			"submerges deep into the pillar!",
			"emerges from the pillar and looks at you with an intense stare...",
			"asks \"Have you seen Morte?\"", "asks \"Who are you?\"",
			"sings \"... it must be a dream from below!\"", "bobs playfully." };

	static Option SHOWLOCATION = new Option("Show location (50XP)", 0, 'l');
	static Option OFFERITEM = new Option("Offer item (1 item)", 0, 'o');
	static Option SACRIFICE =
			new Option("Sacrifice (1 non-mercenary unit)", 0, 's');
	static Option UNHOLYBLESSING = new Option(
			"Unholy blessing (99% of a non-mercenary's hit points)", 0, 'u');

	class Screen extends SelectScreen {
		public Screen() {
			super("You approach The Pillar of Skulls, a towering abomination made of living skulls!",
					null);
			stayopen = false;
		}

		@Override
		public String getCurrency() {
			return "";
		}

		@Override
		public String printinfo() {
			return "One of the skulls "
					+ HaxorScreen.getdailyquote(Arrays.asList(QUOTES)) + " ";
		}

		@Override
		public String printpriceinfo(Option o) {
			return "";
		}

		@Override
		public List<Option> getoptions() {
			ArrayList<Option> list = new ArrayList<Option>();
			list.add(SHOWLOCATION);
			list.add(OFFERITEM);
			list.add(SACRIFICE);
			list.add(UNHOLYBLESSING);
			return list;
		}

		@Override
		public boolean select(Option o) {
			if (o == SACRIFICE) {
				return sacrifice();
			}
			if (o == SHOWLOCATION) {
				return showlocation();
			}
			if (o == UNHOLYBLESSING) {
				return bless();
			}
			if (o == OFFERITEM) {
				return offeritem();
			}
			return false;
		}

		boolean offeritem() {
			ArrayList<Item> items = new ArrayList<Item>();
			ArrayList<Combatant> owners = new ArrayList<Combatant>();
			ArrayList<String> choices = new ArrayList<String>();
			gatheritems(items, owners, choices);
			if (items.isEmpty()) {
				print(text + "\nCome back with an item to offer!");
				return false;
			}
			int choice = Javelin.choose("Sacrifice which item?", choices, true,
					false);
			if (choice == -1) {
				return false;
			}
			Item i = items.get(choice);
			Combatant owner = owners.get(choice);
			if (i instanceof Artifact) {
				Artifact a = (Artifact) i;
				if (owner.equipped.contains(a)) {
					a.remove(owner);
				}
			}
			Squad.active.equipment.get(owner.id).remove(i);
			ArrayList<Combatant> mock = new ArrayList<Combatant>();
			float targetcr = CrCalculator.goldtocr(i.price);
			while (mock.isEmpty()) {
				List<Monster> tier = Javelin.MONSTERSBYCR.get(targetcr);
				if (tier == null) {
					targetcr -= 1;
					if (targetcr < Javelin.MONSTERSBYCR.firstKey()) {
						return false;
					}
					continue;
				}
				mock.add(new Combatant(RPG.pick(tier), false));
			}
			RewardCalculator.rewardxp(Squad.active.members,
					Squad.active.members, mock, 1);
			return true;
		}

		void gatheritems(ArrayList<Item> items, ArrayList<Combatant> owners,
				ArrayList<String> choices) {
			for (Integer id : Squad.active.equipment.keySet()) {
				Combatant owner = null;
				for (Combatant c : Squad.active.members) {
					if (c.id == id) {
						owner = c;
						break;
					}
				}
				if (owner == null) {
					continue;
				}
				for (Item i : Squad.active.equipment.get(id)) {
					items.add(i);
					owners.add(owner);
					choices.add(i + " (with " + owner + ")");
				}
			}
		}

		boolean showlocation() {
			if (!Dwelling.canrecruit(50)) {
				print(text + "\nReturn when you have more experience!");
				return false;
			}
			Dwelling.spend(.5f);
			Actor closest = find(UniqueLocation.class);
			if (closest == null) {
				closest = find(Town.class);
			}
			if (closest == null) {
				closest = find(Location.class);
			}
			if (closest == null) {
				print(text + "\nWe have nothing more to show you!");
			} else {
				WorldScreen.setVisible(closest.x, closest.y);
			}
			return true;
		}

		boolean sacrifice() {
			ArrayList<Combatant> sacrifices =
					getsacrifices(Combatant.STATUSDYING);
			if (Squad.active.members.size() == 1 || sacrifices.isEmpty()) {
				print(text + "\nBring me a good sacrifice first!");
				return false;
			}
			int choice =
					Javelin.choose("Sacrifice who?", sacrifices, true, false);
			if (choice < 0) {
				return false;
			}
			Combatant sacrifice = sacrifices.get(choice);
			Squad.active.remove(sacrifice);
			ArrayList<Combatant> mock = new ArrayList<Combatant>();
			mock.add(sacrifice);
			RewardCalculator.rewardxp(Squad.active.members,
					Squad.active.members, mock, 2);
			RewardCalculator.distributexp(Squad.active.members,
					sacrifice.xp.floatValue());
			return true;
		}

		boolean bless() {
			ArrayList<Combatant> sacrifices =
					getsacrifices(Combatant.STATUSSCRATCHED);
			if (sacrifices.isEmpty()) {
				print(text + "\nCome back in better health!");
				return false;
			}
			int choice = Javelin.choose("Who shall we drain?", sacrifices, true,
					false);
			if (choice == -1) {
				return false;
			}
			sacrifices.get(choice).hp = 1;
			for (Combatant c : Squad.active.members) {
				c.addcondition(new Heroic(c, 20, 24));
			}
			return true;
		}
	}

	private static final String DESCRIPTION = "The Pillar of Skulls";

	/** Constructor. */
	public PillarOfSkulls() {
		super(DESCRIPTION, DESCRIPTION, 1, 1);
	}

	Actor find(Class<? extends Location> class1) {
		Actor closest = null;
		for (Actor a : World.getactors()) {
			if (class1.isInstance(a) && !WorldScreen.see(new Point(a.x, a.y))) {
				if (closest == null
						|| a.distance(x, y) < closest.distance(x, y)) {
					closest = (Actor) a;
				}
			}
		}
		return closest;
	}

	ArrayList<Combatant> getsacrifices(int status) {
		ArrayList<Combatant> sacrifices = new ArrayList<Combatant>();
		for (Combatant c : Squad.active.members) {
			if (!c.mercenary && c.getnumericstatus() >= status) {
				sacrifices.add(c);
			}
		}
		return sacrifices;
	}

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		garrison.clear();
	}

	@Override
	protected void generate() {
		while (x == -1 || !Terrain.get(x, y).equals(Terrain.MARSH)) {
			super.generate();
		}
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		new Screen().show();
		return true;
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}
}