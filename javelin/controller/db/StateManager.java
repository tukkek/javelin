package javelin.controller.db;

import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Weather;
import javelin.controller.exception.battle.EndBattle;
import javelin.model.unit.Combatant;
import javelin.model.world.Dungeon;
import javelin.model.world.Haxor;
import javelin.model.world.Incursion;
import javelin.model.world.Lair;
import javelin.model.world.Portal;
import javelin.model.world.Squad;
import javelin.model.world.WorldMap;
import javelin.model.world.WorldPlace;
import javelin.model.world.town.Town;
import javelin.view.screen.world.WorldScreen;

/**
 * Saves and loads game progress to a file.
 * 
 * @author alex
 */
public class StateManager {

	private static final File SAVEFILE =
			new File(System.getProperty("user.dir"), "javelin.save");
	public static boolean abandoned = false;

	public static void save() {
		try {
			final ObjectOutputStream stream =
					new ObjectOutputStream(new FileOutputStream(SAVEFILE));
			stream.writeBoolean(abandoned);
			for (final Squad s : Squad.squads) {
				s.x = s.visual.x;
				s.y = s.visual.y;
			}
			stream.writeObject(Squad.squads);
			stream.writeObject(WorldMap.seed);
			stream.writeObject(Lair.lairs);
			stream.writeObject(Dungeon.dungeons);
			stream.writeObject(Dungeon.active);
			stream.writeObject(Town.towns);
			stream.writeObject(Portal.portals);
			stream.writeObject(Incursion.squads);
			stream.writeObject(Incursion.currentel);
			stream.writeObject(Weather.now);
			stream.writeObject(Haxor.singleton);
			stream.writeObject(EndBattle.lastkilled);
			stream.flush();
			stream.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	static public boolean nofile = false;

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
