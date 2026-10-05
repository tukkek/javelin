package javelin.controller.content.kit.dragoon;

import javelin.controller.content.upgrade.damage.effect.DamageEffect;
import javelin.controller.content.upgrade.movement.Swimming;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.BreathWeapon.BreathArea;
import javelin.model.unit.abilities.BreathWeapon.SavingThrow;

/**
 * Gas-breathing dragoon.
 *
 * @author alex
 */
public class GreenDragoon extends Dragoon{
	static final BreathWeapon BREATH=new BreathWeapon("corrosive gas",
			BreathArea.CONE,50,12,6,0,SavingThrow.REFLEXES,25,.5f,true);
	/** Singleton. */
	public static final Dragoon INSTANCE=new GreenDragoon();

	/** Constructor. */
	GreenDragoon(){
		super("Green",BREATH,19,40,150,DamageEffect.FEAR);
		extension.add(new Swimming("Green dragon swimming",90));
	}
}
