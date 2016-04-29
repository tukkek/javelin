package javelin.view.screen.haxor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Changes the image used to represent a {@link Combatant}.
 * 
 * @see Monster#avatarfile
 * @author alex
 */
public final class ChangeAvatar extends Hax {
	public ChangeAvatar(String name, double price, boolean requirestargetp) {
		super(name, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		HashSet<String> avatars = new HashSet<String>();
		try {
			File avatarfolder = new File("avatars");
			if (avatarfolder.isDirectory()) {
				for (String avatar : avatarfolder.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".png");
					}
				})) {
					avatars.add(avatar.substring(0, avatar.length() - 4));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Monster m : Javelin.ALLMONSTERS) {
			avatars.add(m.avatarfile);
		}
		ArrayList<String> alphabetical = new ArrayList<String>(avatars);
		Collections.sort(alphabetical);
		int delta = 0;
		while (delta < alphabetical.size()) {
			s.text = "";
			for (int i = delta; i < delta + 9 && i < alphabetical.size(); i++) {
				s.text += "[" + (i - delta + 1) + "] " + alphabetical.get(i)
						+ "\n";
			}
			s.text +=
					"\nPress ENTER to see more options or a number to select a new avatar.";
			Character feedback = s.print();
			if (feedback == '\n') {
				delta += 9;
				if (delta >= alphabetical.size()) {
					delta = 0;
				}
				continue;
			}
			try {
				final int index =
						Integer.parseInt(Character.toString(feedback));
				target.source.avatarfile = alphabetical.get(delta + index - 1);
				break;
			} catch (NumberFormatException e) {
				continue;
			} catch (IndexOutOfBoundsException e) {
				continue;
			}
		}
		return true;
	}
}