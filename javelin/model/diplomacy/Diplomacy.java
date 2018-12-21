package javelin.model.diplomacy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javelin.controller.db.StateManager;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * Offers long-term card-game-like actions to pursue.
 *
 * Each {@link Town} is treated as an independent entity from the player,
 * regardless of it being hostile or not. A town must be discovered, however,
 * before diplomatic actions are allowed on it.
 *
 * @author alex
 * @see Town#ishostile()
 */
public class Diplomacy implements Serializable{
	/** Generated at campaign start of set by {@link StateManager#load()}. */
	public static Diplomacy instance=null;

	class Relationship implements Serializable{
		boolean showethics=false;
		boolean showmorals=false;
		int status=0;

		String printstatus(){
			if(status==2) return "Ally";
			if(status==1) return "Friendly";
			if(status==0) return "Neutral";
			if(status==-1) return "Reserved";
			if(status==-2) return "Hostile";
			throw new RuntimeException("Out-of-bounds status #diplomacy: "+status);
		}

		void changestatus(int delta){
			status+=delta;
			if(status>2)
				status=2;
			else if(status<-2) status=-2;
		}
	}

	/** Map of town by relationship data. */
	public Map<Town,Relationship> relationships=new HashMap<>();
	/**
	 * On reaching 100, enables a diplomatic action. Surplus is lost and should
	 * never be reduced, only increased.
	 */
	public int reputation=0;

	/** Generates a fresh set of relationships, when a campaign starts. */
	public Diplomacy(List<Town> towns){
		for(var t:towns)
			relationships.put(t,new Relationship());
	}

	/** To be called once a day to generate {@link #reputation}. */
	public void turn(){
		for(Town t:Town.gettowns())
			if(!t.ishostile()){
				var reputation=t.generatereputation();
				this.reputation+=reputation+RPG.randomize(reputation);
			}
	}
}
