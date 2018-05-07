package javelin.view;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;

/**
 * Cache for {@link Monster#avatar}
 *
 * @author alex
 */
public class Images {
	static final TreeMap<String, Image> CACHE = new TreeMap<String, Image>();
	static final TreeMap<String, Image> BURIEDCACHE = new TreeMap<String, Image>();

	/** @see {@link Combatant#ispenalized(javelin.model.state.BattleState)} */
	public static final Image PENALIZED = Images.maketransparent(3 / 4f,
			Images.getImage("overlaypenalized"));
	/** @see Location#hascrafted() */
	public static final Image CRAFTING = Images.getImage("overlaycrafting");
	/** @see Location#hasupgraded() */
	public static final Image UPGRADING = Images.getImage("overlayupgrading");
	/** @see Town#isworking() */
	public static final Image LABOR = Images.getImage("overlaylabor");
	/** Show while a {@link Meld} is being generated. */
	public static final Image DEAD = Images.getImage("overlaydead");
	/** Show when a {@link Meld} is generated. */
	public static final Image MELD = Images.getImage("overlaymeld");
	/** @see Location#ishostile() */
	public static final Image HOSTILE = Images.getImage("overlayhostile");
	/** @see Town#ishosting() */
	public static final Image TOURNAMENT = Images
			.getImage("locationtournament");
	/** Distinguishes {@link Combatant#mercenary} units. */
	public static final Image MERCENARY = Images.getImage("overlaymercenary");
	public static final Image SUMMONED = Images.getImage("overlaysummoned");
	public static final Image ELITE = Images.getImage("overlayelite");

	/**
	 * @param combatant
	 *            Unit to be shown.
	 * @return image resource.
	 */
	public static Image getImage(Combatant combatant) {
		if (!combatant.burrowed) {
			return getImage(combatant.source.avatarfile);
		}
		final String avatar = combatant.source.avatarfile;
		Image buried = BURIEDCACHE.get(avatar);
		if (buried == null) {
			buried = maketransparent(1 / 3f, getImage(avatar));
			BURIEDCACHE.put(avatar, buried);
		}
		return buried;
	}

	/**
	 * This should be the preferred method of loading images because this way
	 * it's friendlier for modding purposes.
	 *
	 * @param file
	 *            PNG extension is added.
	 * @return An image from the avatar folder.
	 */
	synchronized public static Image getImage(final String file) {
		Image i = CACHE.get(file);
		if (i != null) {
			return i;
		}
		try {
			i = ImageIO
					.read(new File("avatars" + File.separator + file + ".png"));
			CACHE.put(file, i);
			return i;
		} catch (IOException e) {
			throw new RuntimeException(file, e);
		}
	}

	/**
	 * @param alpha
	 *            Alpha level. 1 is 100% opaque, 0 is 100% transparent.
	 */
	public static Image maketransparent(float alpha, Image image) {
		BufferedImage transparent = new BufferedImage(32, 32,
				Transparency.TRANSLUCENT);
		Graphics2D g = transparent.createGraphics();
		g.setComposite(
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return transparent;
	}

	public static void clearcache() {
		CACHE.clear();
		BURIEDCACHE.clear();
	}
}
