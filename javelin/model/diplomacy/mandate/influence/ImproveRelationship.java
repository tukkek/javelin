package javelin.model.diplomacy.mandate.influence;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.diplomacy.mandate.Mandate;
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
	protected int compatiblebonus=+1;
	/**
	 * Possible relationship bonus/penalty to apply to factions incompatible with
	 * the target.
	 */
	protected int incompatiblebonus=-1;

	/** Reflection constructor. */
	public ImproveRelationship(Relationship r){
		super(r);
	}

	@Override
	public boolean validate(Diplomacy d){
		return target.getstatus()<Relationship.ALLY;
	}

	@Override
	public String getname(){
		return "Improve relationship with "+target;
	}

	@Override
	public void act(Diplomacy d){
		target.changestatus(targetbonus);
		var a=target.town.alignment;
		var relationships=d.getdiscovered();
		var compatible=new ArrayList<String>(0);
		var incompatible=new ArrayList<String>(0);
		for(var t:relationships.keySet()){
			if(t==target.town) continue;
			if(t.alignment.iscompatible(a)){
				if(RPG.chancein(INDIRECTCHANCEIN)){
					relationships.get(t).changestatus(compatiblebonus);
					compatible.add(t.toString());
				}
			}else if(t.alignment.isincompatible(a)&&RPG.chancein(INDIRECTCHANCEIN)){
				relationships.get(t).changestatus(incompatiblebonus);
				incompatible.add(t.toString());
			}
		}
		var result="New relationship status with "+target+": "
				+target.describestatus().toLowerCase()+"!";
		result+=react(compatible,compatiblebonus);
		result+=react(incompatible,incompatiblebonus);
		Javelin.message(result,true);
	}

	static String react(List<String> factions,int bonus){
		if(factions.isEmpty()) return "";
		var reaction=bonus>0?"positively":"negatively";
		return "\nThe following factions reacted "+reaction+" to this action: "
				+String.join(", ",factions)+".";
	}
}
