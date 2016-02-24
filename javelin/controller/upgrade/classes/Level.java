package javelin.controller.upgrade.classes;

/**
 * Represent a line on the level table for a class.
 * 
 * @author alex
 */
public class Level {
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