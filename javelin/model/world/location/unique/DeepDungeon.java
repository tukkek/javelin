package javelin.model.world.location.unique;

import java.util.List;

import javelin.Javelin;
import javelin.controller.fight.Siege;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Ruby;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Location;
import javelin.old.RPG;

/**
 * Offers increasingly difficult fights, starting at Level 1 and being removed
 * at {@link #MAXLEVEL}.
 *
 * @author alex
 */
public class DeepDungeon extends UniqueLocation{
	class DeepDungeonFight extends Siege{
		public DeepDungeonFight(Location l){
			super(l);
			terrain=Terrain.UNDERGROUND;
		}

		@Override
		protected void afterlose(){
			garrison.clear();
			generategarrison(minlevel,maxlevel);
		}

		@Override
		protected void afterwin(){
			capture();
		}
	}

	private static final int MAXLEVEL=20;
	static final String DESCRIPTION="Deep dungeon";

	public DeepDungeon(){
		super(DESCRIPTION,DESCRIPTION,1,1);
		terrain=Terrain.UNDERGROUND;
		showgarrison=false;
	}

	@Override
	public List<Combatant> getcombatants(){
		return garrison;
	}

	public void clear(){
		garrison.clear();
		minlevel+=1;
		maxlevel+=1;
		if(minlevel>MAXLEVEL){
			remove();
			Javelin.message("You have vanquished the deep dungeon!",true);
		}else
			generategarrison(minlevel,maxlevel);
	}

	@Override
	protected Siege fight(){
		return new DeepDungeonFight(this);
	}

	@Override
	public void capture(){
		clear();
		if(RPG.chancein(5)){
			String text="You find a ruby after vanquishing this layer of the Deep Dungeon!";
			Javelin.message(text,true);
			new Ruby().grab();
		}
	}
}
