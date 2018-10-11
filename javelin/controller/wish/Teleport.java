package javelin.controller.wish;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.conjuration.teleportation.GreaterTeleport;

/**
 * Teleports player to any town and shows their .
 *
 * @author alex
 */
public class Teleport extends Wish{
	/** Constructor. */
	public Teleport(Character keyp,WishScreen s){
		super("teleport",keyp,Squad.active.members.size(),false,s);
	}

	@Override
	boolean wish(Combatant target){
		GreaterTeleport spell=new GreaterTeleport();
		spell.showterrain=true;
		spell.castpeacefully(null);
		return true;
	}
}
