package javelin.controller.action.ai.attack;

/**
 * Data structure representing the result of a damage roll.
 *
 * @author alex
 */
public class DamageChance{
	public float chance;
	public int damage;
	public boolean critical;
	/**
	 * <code>true</code> or <code>false</code> as the result of a saving throw.
	 * <code>null</code> if no effects should be even considered (such as in a
	 * miss) - the distinction being necessary because some effects still apply
	 * (but are reduced) on a failed save.
	 */
	public Boolean save;
	public String message="hits";

	DamageChance(final float chance,final int damage,boolean criticalp,
			Boolean save){
		super();
		this.chance=chance;
		this.damage=damage;
		critical=criticalp;
		this.save=save;
	}
}