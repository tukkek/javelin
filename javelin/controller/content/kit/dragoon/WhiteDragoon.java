package javelin.controller.content.kit.dragoon;

import javelin.controller.content.upgrade.damage.effect.DamageEffect;
import javelin.controller.content.upgrade.movement.Burrow;
import javelin.controller.content.upgrade.movement.Swimming;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.BreathWeapon.BreathArea;
import javelin.model.unit.abilities.BreathWeapon.SavingThrow;

/**
 * Cold-breathing dragoon.
 *
 * @author alex
 */
public class WhiteDragoon extends Dragoon{
	static final BreathWeapon BREATH=new BreathWeapon("cold",BreathArea.CONE,40,6,
			6,0,SavingThrow.REFLEXES,23,.5f,true);
	/** Singleton. */
	public static final Dragoon INSTANCE=new WhiteDragoon();

	/** Constructor. */
	WhiteDragoon(){
		super("White",BREATH,17,60,200,DamageEffect.PARALYSIS);
		extension.add(new Burrow("White dragon burrow",30));
		extension.add(new Swimming("White dragon swimming",60));
	}
}
