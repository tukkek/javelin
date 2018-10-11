package javelin.controller.wish;

import javelin.model.item.key.door.MasterKey;
import javelin.model.unit.Combatant;

public class ConjureMasterKey extends Wish{
	public ConjureMasterKey(Character keyp,WishScreen s){
		super("conjure master key",keyp,2,false,s);
	}

	@Override
	boolean wish(Combatant target){
		new MasterKey().grab();
		return true;
	}
}
