package javelin.controller.upgrade.classes;

import java.io.Serializable;

/**
 * Represent a line on the level table for a class.
 * 
 * @author alex
 */
public class Level implements Serializable {
	public int bab;
	int fort;
	int ref;
	int will;

	public Level(int bab, int fort, int ref, int will) {
		super();
		this.bab = bab;
		this.fort = fort;
		this.ref = ref;
		this.will = will;
	}
}