package javelin;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import javelin.controller.TextReader;
import javelin.controller.WorldGenerator;
import javelin.controller.ai.ThreadManager;
import javelin.controller.db.Preferences;
import javelin.controller.db.StateManager;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.kit.Kit;
import javelin.controller.scenario.Campaign;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.unique.Haxor;
import javelin.model.world.location.unique.UniqueLocation;
import javelin.view.screen.SquadScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.QuestApp;

/**
 * Application and game life-cycle.
 * 
 * TODO refactor a time manager out of this which works with
 * TimeManager#tick(int time,WorldState world)?
 * 
 * @author alex
 */
public class JavelinApp extends QuestApp {
	private static final long serialVersionUID = 1L;
	static String[] ERRORQUOTES = new String[] { "A wild error appears!",
			"You were eaten by a grue.", "So again it has come to pass...",
			"Mamma mia!", };

	/** Controller. */
	static public WorldScreen context;
	/** Last defeated enemies. */
	public static ArrayList<Monster> lastenemies = new ArrayList<Monster>();

	/**
	 * Keeps track of monster status before combat so we can restore any
	 * temporary effects.
	 */
	public static ArrayList<Combatant> originalteam;
	/**
	 * TODO see {@link Fight#originalredteam} }
	 */
	public static ArrayList<Combatant> originalfoes;
	/**
	 * Controller for active battle. Should be <code>null</code> at any point a
	 * battle is not occurring.
	 */
	public Fight fight;
	/** Root window. */
	public JFrame frame;

	@Override
	public void run() {
		javelin.controller.db.Preferences.init();// pre
		initialize();
		if (!StateManager.load()) {
			if (StateManager.nofile) {
				disclaimer();
			}
			startcampaign();
		}
		javelin.controller.db.Preferences.init();// post
		preparedebug();
		if (Dungeon.active != null) {
			Dungeon.active.activate(true);
		}
		StateManager.save(true, StateManager.SAVEFILE);
		if (Javelin.DEBUG) {
			Debug.oninit();
			while (true) {
				loop();
			}
		} else {
			while (true) {
				try {
					loop();
				} catch (RuntimeException e) {
					handlefatalexception(e);
				}
			}
		}
	}

	void loop() {
		try {
			if (Dungeon.active == null) {
				JavelinApp.context = new WorldScreen(true);
			}
			while (true) {
				switchScreen(JavelinApp.context);
				JavelinApp.context.turn();
			}
		} catch (final StartBattle e) {
			fight = e.fight;
			try {
				e.battle();
			} catch (final EndBattle end) {
				EndBattle.end();
				Javelin.app.fight = null;
			}
			// }
		}
	}

	/**
	 * @param e
	 *            Show this error to the user and log it.
	 */
	static public void handlefatalexception(RuntimeException e) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(Javelin.app,
				RPG.pick(ERRORQUOTES) + "\n\nUnfortunately an error ocurred.\n"
						+ "Please send a screenshot of the next message or the log file (error.txt)\n"
						+ "to one of the channels below, so we can get this fixed on future releases.\n\n"
						+ "If for any reason your current game fails to load when you restart Javelin\n"
						+ "you can find backups on the \"backup\" folder. Just move one of them to the\n"
						+ "main folder and rename it to \"javelin.save\" to restore your progress.\n\n"
						+ "Post to our reddit forum -- http://www.reddit.com/r/javelinrl\n"
						+ "Leave a comment on our website -- http://javelinrl.wordpress.com\n"
						+ "Or write us an e-mail -- javelinrl@gmail.com\n");
		String error = printstack(e);
		Throwable t = e;
		HashSet<Throwable> errors = new HashSet<Throwable>(2);
		while (t.getCause() != null && errors.add(t)) {
			t = t.getCause();
			error += System.lineSeparator() + printstack(t);
		}
		try {
			Preferences.write(error, "error.txt");
		} catch (IOException e1) {
			// ignore
		}
		JOptionPane.showMessageDialog(Javelin.app, error);
		System.exit(1);
	}

	static String printstack(Throwable e) {
		String error = e.getMessage() + " (" + e.getClass() + ")";
		error += "\n";
		for (StackTraceElement stack : e.getStackTrace()) {
			error += stack.toString() + "\n";
		}
		return error;
	}

	void disclaimer() {
		while (TextReader
				.show(new File("README.txt"),
						"This message will only be shown once, press ENTER to continue.")
				.getKeyCode() != KeyEvent.VK_ENTER) {
			// wait for enter
		}
	}

	private void initialize() {
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(new File("README.txt")));
			String line = reader.readLine();
			while (line != null) {
				if (!line.trim().isEmpty()) {
					System.out.println(line);
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ThreadManager.determineprocessors();
	}

	void startcampaign() {
		SquadScreen.open();
		WorldGenerator.build();
		UpgradeHandler.singleton.gather();
		if (Javelin.DEBUG) {
			JavelinApp.printstatistics();
			Debug.oncampaignstart();
		}
	}

	/** stats */
	static void printstatistics() {
		if (!(World.scenario instanceof Campaign)) {
			return;
		}
		System.out.println();
		printoptions();
		System.out.println(Javelin.ALLMONSTERS.size() + " monsters");
		System.out.println((Item.ALL.size() - Item.ARTIFACT.size()) + " items, "
				+ Item.ARTIFACT.size() + " artifacts, 7 relics");
		Collection<Spell> spells = Spell.SPELLS.values();
		int nupgrades = UpgradeHandler.singleton.count() - spells.size();
		int nspells = spells.size() - countsummon(spells) + 1;
		int nskills = UpgradeHandler.singleton.countskills();
		int nkits = Kit.KITS.size();
		System.out.println(nupgrades + " upgrades, " + nspells + " spells, "
				+ nskills + " skills, " + nkits + " kits");
		HashSet<Class<? extends Actor>> locationtypes = new HashSet<Class<? extends Actor>>();
		int uniquelocations = 0;
		for (Actor a : World.getall()) {
			if (!(a instanceof Location)) {
				continue;
			}
			locationtypes.add(a.getClass());
			if (a instanceof UniqueLocation) {
				uniquelocations += 1;
			}
		}
		System.out.println((locationtypes.size() - uniquelocations)
				+ " world location types, " + uniquelocations
				+ " unique locations");
		Deck.printstats();
		int maps = Terrain.UNDERGROUND.getmaps().size();
		for (Terrain t : Terrain.ALL) {
			maps += t.getmaps().size();
		}
		System.out.println(maps + " battle maps");
	}

	static void printoptions() {
		HashMap<String, HashSet<Upgrade>> allupgrades = UpgradeHandler.singleton
				.getall();
		ArrayList<Upgrade> upgradelist = new ArrayList<Upgrade>();
		HashMap<String, ItemSelection> allitems = Item.getall();
		List<String> primary = Arrays.asList(new String[] { "earth", "wind",
				"fire", "water", "good", "evil", "magic" });
		for (String realm : primary) {
			printrealm(allupgrades, allitems, realm);
			upgradelist.addAll(allupgrades.get(realm));
		}
		ArrayList<String> extrarealms = new ArrayList<String>(
				allupgrades.keySet());
		extrarealms.sort(null);
		for (String realm : extrarealms) {
			if (!primary.contains(realm)) {
				printrealm(allupgrades, allitems, realm);
				upgradelist.addAll(allupgrades.get(realm));
			}
		}
		for (Kit k : Kit.KITS) {
			for (Upgrade u : k.upgrades) {
				if (!(u instanceof Summon) && !upgradelist.contains(u)) {
					throw new RuntimeException("Unregistered upgrade: " + u);
				}
			}
		}
	}

	static void printrealm(HashMap<String, HashSet<Upgrade>> allupgrades,
			HashMap<String, ItemSelection> allitems, String realm) {
		HashSet<Upgrade> upgrades = allupgrades.get(realm);
		int count = 1;
		System.out.println(realm);
		for (Upgrade u : upgrades) {
			System.out.println("\t" + count + " - " + u);
			count += 1;
		}
		System.out.println();
		ItemSelection inventory = allitems.get(realm);
		for (int i = 0; inventory != null && i < inventory.size(); i++) {
			Item item = inventory.get(i).clone();
			item.shop();
			System.out.println(
					"\t" + count + " - " + item + " ($" + item.price + ")");
			count += 1;
		}
		System.out.println();
	}

	static int countsummon(Collection<Spell> spells) {
		int summon = 0;
		for (Spell s : spells) {
			if (s instanceof Summon) {
				summon += 1;
			}
		}
		return summon;
	}

	@SuppressWarnings("deprecation")
	void preparedebug() {
		if (Preferences.DEBUGSGOLD != null) {
			Squad.active.gold = Preferences.DEBUGSGOLD;
		}
		if (Preferences.DEBUGSXP != null) {
			for (final Combatant m : Squad.active.members) {
				m.xp = new BigDecimal(Preferences.DEBUGSXP / 100f);
			}
		}
		if (Preferences.DEBUGRUBIES != null) {
			Haxor.singleton.rubies = Preferences.DEBUGRUBIES;
		}
	}

	@Override
	public void setupScreen() {
	}
}