package javelin.controller.db;

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
import javelin.model.BattleMap;
import javelin.model.state.Member;
import javelin.model.unit.Monster;
import javelin.model.world.Dungeon;
import javelin.model.world.Incursion;
import javelin.model.world.Squad;
import javelin.model.world.Town;
import javelin.model.world.WorldMap;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.tyrant.Game;

public class StateManager {

	private static final File SAVEFILE = new File(
			System.getProperty("user.dir"), "javelin.save");
	public static boolean abandoned = false;

	public static void save() {
		try {
			// for (final Squad s : Squad.squads) {
			// /**
			// * prevents list subclasses
			// */
			// s.members = new ArrayList<Combatant>(s.members);
			// }
			final ObjectOutputStream stream = new ObjectOutputStream(
					new FileOutputStream(SAVEFILE));
			stream.writeBoolean(abandoned);
			for (final Squad s : Squad.squads) {
				s.x = s.visual.x;
				s.y = s.visual.y;
			}
			stream.writeObject(Squad.squads);
			stream.writeObject(WorldMap.seed);
			stream.writeObject(Dungeon.dungeons);
			stream.writeObject(Town.towns);
			stream.writeObject(Incursion.squads);
			stream.writeObject(Incursion.currentel);
			stream.writeObject(Weather.now);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static ArrayList<Member> monsterstomembers(
			final List<Monster> blueTeam) {
		final ArrayList<Member> members = new ArrayList<Member>();
		for (final Monster member : blueTeam) {
			members.add(new Member(member.challengeRating, member.name,
					member.customName));
		}
		return members;
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
				return false;
			}
			Squad.squads = (List<Squad>) stream.readObject();
			// Squad.active = Squad.squads.get(0);
			Javelin.act();
			BattleMap.blueTeam = Squad.active.members;
			WorldMap.seed = (WorldMap) stream.readObject();
			JavelinApp.overviewmap = WorldScreen.makemap(WorldMap.seed);
			Dungeon.dungeons = (List<Dungeon>) stream.readObject();
			for (final Dungeon d : Dungeon.dungeons) {
				d.place();
			}
			for (final Squad s : Squad.squads) {
				s.place();
			}
			Town.towns = (List<Town>) stream.readObject();
			for (final Town t : Town.towns) {
				t.place();
			}
			Incursion.squads = (List<Incursion>) stream.readObject();
			for (final Incursion t : Incursion.squads) {
				t.place();
			}
			Incursion.currentel = (Integer) stream.readObject();
			Weather.read((Integer) stream.readObject());
			return true;
		} catch (final Throwable e1) {
			e1.printStackTrace(System.out);
			System.out
					.println("Your save game could not be loaded\n"
							+ "It has been deleted so you can restart the game with a new save\n"
							+ "If this is happening constantly please inform us of the message error above");
			StateManager.clear();
			if (!Javelin.DEBUG) {
				System.out.println("\nPress any key to exit...");
				Game.getInput();
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
