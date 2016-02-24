package javelin.view;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Cache for {@link Monster#avatar}
 * 
 * @author alex
 */
public class ImageHandler {
	static TreeMap<String, Image> cache = new TreeMap<String, Image>();

	public static Image getImage(Combatant combatant) {
		final String file = combatant.source.avatarfile;
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
			throw new RuntimeException(e);
		}
	}

}
