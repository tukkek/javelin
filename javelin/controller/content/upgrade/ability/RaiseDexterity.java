package javelin.controller.content.upgrade.ability;

import javelin.controller.content.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseDexterity extends RaiseAbility{
	/** Unique instance for this {@link Upgrade}. */
	public static final RaiseDexterity SINGLETON=new RaiseDexterity();

	private RaiseDexterity(){
		super("dexterity");
	}

	@Override
	int getabilityvalue(Monster m){
		return m.dexterity;
	}

	@Override
	boolean setattribute(Combatant m,int l){
		m.source.changedexteritymodifier(+1);
		return true;
	}

	@Override
	public int getattribute(Monster source){
		return source.dexterity;
	}
}
