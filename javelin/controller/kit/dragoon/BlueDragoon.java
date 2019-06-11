package javelin.controller.kit.dragoon;

import javelin.controller.upgrade.movement.Burrow;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.BreathWeapon.BreathArea;
import javelin.model.unit.abilities.BreathWeapon.SavingThrow;
import javelin.model.unit.abilities.TouchAttack;

/**
 * Lightning-breathing dragoon.
 *
 * @author alex
 */
public class BlueDragoon extends Dragoon{
	static final BreathWeapon BREATH=new BreathWeapon("lightning",BreathArea.LINE,
			100,12,8,0,SavingThrow.REFLEXES,25,.5f,true);
	/** Singleton. */
	public static final Dragoon INSTANCE=new BlueDragoon();

	/** Constructor. */
	BlueDragoon(){
		super("Blue",BREATH,20,40,150,null);
		extension.add(new Burrow("Black dragon burrow",20));
	}

	@Override
	protected void define(){
		super.define();
		extension.add(new TouchAttack("Stunning shock",2,8,12));
	}
}
