package javelin.controller.action.ai.attack;

/**
 * Data structure representing the result of a damage roll.
 *
 * @author alex
 */
public class DamageChance{
	public final float chance;
	public int damage;
	public boolean critical;
	public Boolean save;

	DamageChance(final float chance,final int damage,boolean criticalp,
			Boolean save){
		super();
		this.chance=chance;
		this.damage=damage;
		critical=criticalp;
		this.save=save;
	}
}