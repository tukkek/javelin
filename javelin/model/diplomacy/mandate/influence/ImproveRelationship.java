package javelin.model.diplomacy.mandate.influence;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.model.diplomacy.mandate.Mandate;
import javelin.model.town.diplomacy.Diplomacy;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * Improves a {@link Relationship}. Has a chance to affect similarly aligned
 * {@link Relationship}s as well.
 *
 * @author alex
 */
public class ImproveRelationship extends Mandate{
	static final int INDIRECTCHANCEIN=2;

	/** Relationship bonus/penalty to applyu to target. */
	protected int targetbonus=+1;
	/**
	 * Possible relationship bonus/penalty to apply to factions compatible with
	 * the target.
	 */
	protected float compatiblebonus=1.5f;
	/**
	 * Possible relationship bonus/penalty to apply to factions incompatible with
	 * the target.
	 */
	protected float incompatiblebonus=.5f;

	/** Reflection constructor. */
	public ImproveRelationship(Town t){
		super(t);
	}

	@Override
	public boolean validate(Diplomacy d){
		return d.town!=target&&target.diplomacy.getstatus()<1;
	}

	@Override
	public String getname(){
		return "Improve relationship with "+target;
	}

	@Override
	public void act(Diplomacy d){
		target.diplomacy.reputation+=d.town.population*targetbonus;
		var a=target.alignment;
		var relationships=Town.getdiscovered();
		var compatible=new ArrayList<String>(0);
		var incompatible=new ArrayList<String>(0);
		for(var t:relationships){
			if(t==target) continue;
			if(t.alignment.iscompatible(a)){
				if(RPG.chancein(INDIRECTCHANCEIN)){
					t.diplomacy.reputation+=d.town.population*compatiblebonus;
					compatible.add(t.toString());
				}
			}else if(t.alignment.isincompatible(a)&&RPG.chancein(INDIRECTCHANCEIN)){
				t.diplomacy.reputation+=d.town.population*incompatiblebonus;
				incompatible.add(t.toString());
			}
		}
		var result="New relationship status with "+target+": "
				+target.diplomacy.describestatus().toLowerCase()+"!";
		result+=react(compatible,compatiblebonus);
		result+=react(incompatible,incompatiblebonus);
		Javelin.message(result,true);
	}

	static String react(List<String> factions,float bonus){
		if(factions.isEmpty()) return "";
		var reaction=bonus>0?"positively":"negatively";
		return "\nThe following factions reacted "+reaction+" to this action: "
				+String.join(", ",factions)+".";
	}
}
