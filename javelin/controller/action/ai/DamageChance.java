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

	public DamageChance(final float chance, final int damage,
			boolean criticalp) {
		super();
		this.chance = chance;
		this.damage = damage;
		critical = criticalp;
	}
}