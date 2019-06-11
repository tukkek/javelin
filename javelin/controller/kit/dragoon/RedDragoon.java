package javelin.controller.kit.dragoon;

import javelin.controller.upgrade.damage.effect.DamageEffect;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.BreathWeapon.BreathArea;
import javelin.model.unit.abilities.BreathWeapon.SavingThrow;

/**
 * Fire-breathing dragoon.
 *
 * @author alex
 */
public class RedDragoon extends Dragoon{
	static final BreathWeapon BREATH=new BreathWeapon("fire",BreathArea.CONE,50,
			12,10,0,SavingThrow.REFLEXES,25,.5f,true);
	/** Singleton. */
	public static final Dragoon INSTANCE=new RedDragoon();

	RedDragoon(){
		super("Red",BREATH,21,40,150,DamageEffect.FEAR);
	}
}
