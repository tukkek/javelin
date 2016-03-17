package javelin.controller.db;

import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Weather;
import javelin.controller.exception.battle.EndBattle;
import javelin.model.unit.Combatant;
import javelin.model.world.Incursion;
import javelin.model.world.Squad;
import javelin.model.world.WorldMap;
import javelin.model.world.place.Dungeon;
import javelin.model.world.place.Haxor;
import javelin.model.world.place.Lair;
import javelin.model.world.place.Portal;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.town.Town;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.Point;

/**
 * Saves and loads game progress to a file.
 * 
 * @author alex
 */
public class StateManager {

	private static final File SAVEFILE =
			new File(System.getProperty("user.dir"), "javelin.save");
	public static boolean abandoned = false;
	static public boolean nofile = false;

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				while (!SAVEFILE.canWrite()) {
					System.out.println("Waiting for save to finish...");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// should not happen
					}
				}
				save();
			}
		}));

	}

	public static void save() {
		try {
			ObjectOutputStream writer =
					new ObjectOutputStream(new FileOutputStream(SAVEFILE));
			writer.writeBoolean(abandoned);
			for (final Squad s : Squad.squads) {
				s.x = s.visual.x;
				s.y = s.visual.y;
			}
			writer.writeObject(Squad.squads);
			writer.writeObject(WorldMap.seed);
			writer.writeObject(Lair.lairs);
			writer.writeObject(Dungeon.dungeons);
			writer.writeObject(Dungeon.active);
			writer.writeObject(Town.towns);
			writer.writeObject(Portal.portals);
			writer.writeObject(Incursion.squads);
			writer.writeObject(Incursion.currentel);
			writer.writeObject(Weather.now);
			writer.writeObject(Haxor.singleton);
			writer.writeObject(EndBattle.lastkilled);
			writer.writeObject(WorldScreen.discovered);
			writer.flush();
			writer.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean load() {
		if (!SAVEFILE.exists()) {
			nofile = true;
			return false;
		}
		try {
			final FileInputStream filestream = new FileInputStream(SAVEFILE);
			final ObjectInputStream stream = new ObjectInputStream(filestream);
			abandoned = stream.readBoolean();
			if (abandoned) {
				abandoned = false;
				stream.close();
				return false;
			}
			Squad.squads = (ArrayList<Squad>) stream.readObject();
			Javelin.act();
			WorldMap.seed = (WorldMap) stream.readObject();
			JavelinApp.overviewmap = WorldScreen.makemap(WorldMap.seed);
			Lair.lairs = (List<WorldPlace>) stream.readObject();
			for (final WorldPlace l : Lair.lairs) {
				l.place();
			}
			Dungeon.dungeons = (List<WorldPlace>) stream.readObject();
			Dungeon.active = (Dungeon) stream.readObject();
			for (final WorldPlace d : Dungeon.dungeons) {
				d.place();
			}
			for (final Squad s : Squad.squads) {
				s.place();
			}
			Town.towns = (ArrayList<Town>) stream.readObject();
			for (final Town t : Town.towns) {
				t.place();
			}
			Portal.portals = (ArrayList<WorldPlace>) stream.readObject();
			for (final WorldPlace p : Portal.portals) {
				p.place();
			}
			Incursion.squads = (List<Incursion>) stream.readObject();
			for (final Incursion t : Incursion.squads) {
				t.place();
			}
			Incursion.currentel = (Integer) stream.readObject();
			Weather.read((Integer) stream.readObject());
			Haxor.singleton = (Haxor) stream.readObject();
			Haxor.singleton.place();
			EndBattle.lastkilled = (Combatant) stream.readObject();
			WorldScreen.discovered = (HashSet<Point>) stream.readObject();
			stream.close();
			return true;
		} catch (final Throwable e1) {
			e1.printStackTrace(System.out);
			System.out.println("Your save game could not be loaded\n"
					+ "It has been deleted so you can restart the game with a new save\n"
					+ "If this is happening constantly please inform us of the error message above");
			StateManager.clear();
			if (!Javelin.DEBUG) {
				Window.getWindows()[0].dispose();
				System.out.println("\nPress any key to exit...");
				// Game.getInput();
				try {
					System.in.read();
				} catch (IOException e) {
					// die gracefully
				}
			}
			System.exit(20140406);
			return false;
		}
	}

	/**
	 * For some reason delete() doesn't work on all systems. The field 'abandon'
	 * should take care of any uncleared files.
	 */
	public static void clear() {
		abandoned = true;
		save();
	}

}
