package javelin.view;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import tyrant.mikera.tyrant.QuestApp;

/**
 * Cache for {@link Monster#avatar}
 * 
 * @author alex
 */
public class Images {

	static final TreeMap<String, Image> cache = new TreeMap<String, Image>();

	public static final Image penalized = QuestApp.maketransparent(2 / 3f,
			Images.getImage("overlaypenalized"));
	public static final Image crafting = Images.getImage("overlaycrafting");
	public static final Image upgrading = Images.getImage("overlayupgrading");
	public static Image labor = Images.getImage("overlaylabor");
	public static final Image dead = Images.getImage("overlaydead");
	public static final Image crystal = Images.getImage("overlaycrystal");
	public static final Image hostile = Images.getImage("overlayhostile");
	public static final Image tournament =
			Images.getImage("locationtournament");

	/**
	 * @param combatant
	 *            Unit to be shown.
	 * @return image resource.
	 */
	public static Image getImage(Combatant combatant) {
		return getImage(combatant.source.avatarfile);
	}

	/**
	 * This should be the preferred method of loading images because this way
	 * it's friendlier for modding purposes.
	 * 
	 * @param file
	 *            PNG extension is added.
	 * @return An image from the avatar folder.
	 */
	public static Image getImage(final String file) {
		Image i = cache.get(file);
		if (i != null) {
			return i;
		}
		try {
			i = ImageIO
					.read(new File("avatars" + File.separator + file + ".png"));
			cache.put(file, i);
			return i;
		} catch (IOException e) {
			throw new RuntimeException(file, e);
		}
	}

}
