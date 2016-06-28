package javelin.controller.action.ai;

/**
 * Data structure representing the result of a damage roll.
 * 
 * @author alex
 */
public class DamageChance {
	final float chance;
	int damage;
	boolean critical;
	Boolean save;

	DamageChance(final float chance, final int damage, boolean criticalp,
			Boolean save) {
		super();
		this.chance = chance;
		this.damage = damage;
		critical = criticalp;
		this.save = save;
	}
}