package javelin.controller.map;

import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Portal;

/**
 * Adapter that uses a map type from the original tyrant code-base to create a
 * {@link Map}.
 * 
 * @author alex
 */
public class TyrantMap extends Map {
	private final String maptype;

	public TyrantMap(String maptype) {
		super(null);
		this.maptype = maptype;
	}

	public TyrantMap(String maptype, int maxrain) {
		this(maptype);
		maxflooding = maxrain;
	}

	@Override
	public void generate() {
		map = Portal.getTargetMap(Game.instance().createWorld().find(maptype));
		for (final Thing t : map.getThings()) {
			inheritance: for (BaseObject i = t.getInherited(); i != null; i =
					i.getInherited()) {
				for (final String s : new String[] { "trap", "ladder", "temple",
						"portal", "secret", "being", "monster", "door" }) {
					if (i.toString().contains(s)) {
						map.removeThing(t);
						break inheritance;
					}
				}
			}
		}
	}
}
