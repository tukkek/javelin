package javelin.model.world.location.town.quest;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.kit.Barbarian;
import javelin.controller.kit.Fighter;
import javelin.controller.kit.Kit;
import javelin.controller.kit.Rogue;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.world.Incursion;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Capture an escapee.
 *
 * @author alex
 */
public class Pursue extends Quest{
	static final List<Kit> KITS=List.of(Barbarian.INSTANCE,Rogue.INSTANCE,
			Fighter.INSTANCE);

	class Fugitives extends Incursion{
		Town from=null;

		public Fugitives(int x,int y,List<Combatant> squadp,Town from){
			super(x,y,squadp,from.originalrealm);
			this.from=from;
			description="Fugitives from "+from;
			displace();
			choosetarget();
		}

		@Override
		public void turn(long time,WorldScreen world){
			super.turn(time,world);
			var d=getdistrict();
			if(d!=null&&d.town!=from||!exists()){
				remove();
				escaped=true;
			}
		}

		@Override
		protected void choosetarget(){
			var towns=Town.gettowns();
			towns.remove(from);
			var here=getlocation();
			target=towns.stream().filter(t->!Incursion.crosseswater(this,t.x,t.y))
					.sorted((a,b)->here.distanceinsteps(a.getlocation())
							-here.distanceinsteps(b.getlocation()))
					.findFirst().orElse(null);
		}

		@Override
		public boolean interact(){
			try{
				return super.interact();
			}catch(StartBattle e){
				e.fight.bribe=false;
				e.fight.hide=false;
				throw e;
			}
		}
	}

	Fugitives fugitives=null;
	boolean escaped=false;

	/** Reflection constructor. */
	public Pursue(Town t){
		super(t);
		var candidates=Terrain.get(t.x,t.y).getmonsters().stream()
				.filter(m->m.cr<=el&&m.think(-1)&&!m.alignment.isgood())
				.collect(Collectors.toList());
		if(candidates.isEmpty()) return;
		var band=new Combatants();
		for(var bandsize=RPG.r(1,8);bandsize>0;bandsize--){
			band.add(new Combatant(RPG.pick(candidates),true));
			if(ChallengeCalculator.calculateel(band)>=el) break;
		}
		while(ChallengeCalculator.calculateel(band)<el)
			RPG.pick(KITS).upgrade(band.getweakest());
		fugitives=new Fugitives(t.x,t.y,band,t);
	}

	@Override
	public boolean validate(){
		return fugitives!=null&&fugitives.target!=null;
	}

	@Override
	protected void define(){
		super.define();
		fugitives.place();
	}

	@Override
	protected String getname(){
		return "Pursue fugitives";
	}

	@Override
	public boolean cancel(){
		return super.cancel()||escaped;
	}

	@Override
	public boolean complete(){
		return !escaped&&!fugitives.exists();
	}
}
