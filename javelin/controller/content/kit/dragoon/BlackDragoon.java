package javelin.controller.content.kit.dragoon;

import javelin.controller.content.upgrade.damage.effect.DamageEffect;
import javelin.controller.content.upgrade.movement.Swimming;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.BreathWeapon.BreathArea;
import javelin.model.unit.abilities.BreathWeapon.SavingThrow;
import javelin.model.unit.abilities.TouchAttack;

/**
 * Acid-breathing dragoon.
 *
 * @author alex
 */
public class BlackDragoon extends Dragoon{
	static final BreathWeapon BREATH=new BreathWeapon("acid",BreathArea.LINE,80,
			12,4,0,SavingThrow.REFLEXES,23,.5f,true);

	/** Singleton. */
	public static final Dragoon INSTANCE=new BlackDragoon();

	/** Constructor. */
	BlackDragoon(){
		super("Black",BREATH,18,60,150,DamageEffect.POISON);
		extension.add(new Swimming("Black dragon swimming",60));
	}

	@Override
	protected void define(){
		super.define();
		extension.add(new TouchAttack("Acid spray",8,8,17));
	}
}
