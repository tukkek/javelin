package javelin.controller.map;

import java.util.ArrayList;

import tyrant.mikera.engine.RPG;

/**
 * @see #pick()
 * 
 * @author alex
 */
class Maps extends ArrayList<Map> {
	/**
	 * @return a random map from this list.
	 */
	Map pick() {
		return RPG.pick(this);
	}
}