package javelin.controller.fight;

import java.util.ArrayList;

import javelin.controller.map.location.TownMap;
import javelin.controller.terrain.Terrain;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.view.screen.BattleScreen;

/**
 * Battle when a player invades a hostile town.
 *
 * @see Town#ishostile()
 *
 * @author alex
 */
public class Siege extends Fight{
	public Location location;
	/**
	 * If <code>false</code> will skip
	 * {@link #onEnd(BattleScreen, ArrayList, BattleState)} but still call
	 * {@link Fight#onEnd(BattleScreen, ArrayList, BattleState)}. This allows
	 * subclasses to take control of after-fight consequences.
	 */
	protected boolean cleargarrison=true;

	/**
	 * @param l Where this fight is occurring at.
	 */
	public Siege(Location l){
		location=l;
		hide=false;
		meld=true;
		terrain=Terrain.get(l.x,l.y);
		rewardreputation=true;
		var d=l.getdistrict();
		if(d!=null) map=new TownMap(d.town);
	}

	@Override
	public ArrayList<Combatant> getfoes(Integer teamel){
		ArrayList<Combatant> clones=new ArrayList<>(location.garrison);
		for(int i=0;i<clones.size();i++)
			clones.set(i,clones.get(i).clone().clonesource());
		return clones;
	}

	@Override
	public void bribe(){
		afterwin();
		location.realm=null;
	}

	@Override
	public boolean onend(){
		if(cleargarrison){
			if(Fight.victory)
				afterwin();
			else
				afterlose();
			/* TODO this should probably be inside afterwin() */
			if(location.garrison.isEmpty()) location.capture();
		}
		return super.onend();
	}

	/**
	 * TODO ideally would have a d10 check to give a chance for negative
	 * {@link Combatant}s in {@link BattleState#dead} to recover: for example, a
	 * character that ends with -5hp should have a 50% of immediately recovering
	 * to full HP instead of being removed from {@link Location#garrison}. 0hp
	 * would mean 100% recovery chance while -10 or less would be guaranteed
	 * removal.
	 */
	protected void afterlose(){
		ArrayList<Combatant> alive=state.getcombatants();
		for(Combatant c:new ArrayList<>(location.garrison))
			if(!alive.contains(c)) location.garrison.remove(c);
	}

	protected void afterwin(){
		location.garrison.clear();
	}
}
