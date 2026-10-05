package javelin.model.world.location.dungeon.feature.trap;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;

/**
 * Single-target mechanical trap. Deals subdual (non-lethal) damage to a single
 * character. It's no fun to step onto a hard trap you didn't see coming and
 * possibly lose the game.
 *
 * TODO evasion upgrade (half damage/no damage instead of full/half)
 *
 * @author alex
 */
public class MechanicalTrap extends Trap{
	/**
	 * TODO this is entirely circunstancial. Hopefully, at the most basic, adding
	 * new trap types should allow for lesser CRs.
	 */
	public static final int MINIMUMCR=-2;

	/** d6 */
	int damagedie=0;

	/**
	 * @param p Creates a trap at this point.
	 */
	public MechanicalTrap(int cr,DungeonFloor f){
		super(cr,"trap",f);
		int currentcr=-1;// doesn't kill ("subdual damage", kinda)
		while(currentcr!=cr||damagedie<1){
			savedc=RPG.r(10,35);
			disarmdc=RPG.r(10,35);
			searchdc=RPG.r(10,35);
			damagedie=0;
			currentcr=calculatecr();
			damagedie=Math.max(1,(cr-currentcr)*2);
			currentcr=calculatecr();
		}
	}

	int calculatecr(){
		return ratefactor(savedc)+ratefactor(searchdc)+ratefactor(disarmdc)
				+damagedie/2;
	}

	static int ratefactor(float f){
		if(f<=15) return -1;
		if(f<=24) return 0;
		if(f<=29) return 1;
		return 2;
	}

	@Override
	protected void spring(){
		String status="You step onto a mechanical trap!\n";
		ArrayList<Combatant> targets=new ArrayList<>();
		for(Combatant c:Squad.active.members)
			if(c.hp>1) targets.add(c);
		if(targets.isEmpty()) targets.add(Squad.active.members.get(0));
		Combatant target=RPG.pick(targets);
		if(RPG.r(1,20)+target.source.ref>=savedc)
			status+=target+" dodges!";
		else{
			int damage=-target.source.dr;
			for(int i=0;i<damagedie;i++)
				damage+=RPG.r(1,6);
			if(damage<0) damage=0;
			target.hp=Math.max(1,target.hp-damage);
			status+=target+" is "+target.getstatus()+".";
		}
		Javelin.message(status,true);
	}
}
