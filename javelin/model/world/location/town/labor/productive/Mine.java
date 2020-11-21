package javelin.model.world.location.town.labor.productive;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.Underground;
import javelin.model.item.Tier;
import javelin.model.item.consumable.Ruby;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Incursion;
import javelin.model.world.Period;
import javelin.model.world.World;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Gold mines provides 1 {@link Location#work}, with a limit of at most one mine
 * per Town {@link Rank}.
 *
 * Mines will work themselves, level-up, amass gold, possibly a {@link Ruby}
 * with time. It will dig into an {@link Underground} {@link Monster} lair at
 * random intervals, at which point it will require a {@link Squad} to
 * intervene, which will result in the amssed rewards being given to the player.
 *
 * Mines have a maximum depth determined at random upon creation. Once the limit
 * is reached, it runs dry and it will {@link #remove()} itself from the game
 * {@link World}.
 *
 * TODO this redesign is already better than the previous iteration but even
 * more interactity is possible, like some control of whether to dig deeper,
 * possibly through {@link Labor}, etc. Whether more complexity is desired or
 * not is another question entirely.
 *
 * @author alex
 */
public class Mine extends Fortification{
	class MineFight extends Fight{
		public MineFight(){
			terrain=Terrain.UNDERGROUND;
			hide=false;
			bribe=false;
		}

		@Override
		public ArrayList<Combatant> getfoes(Integer teamel){
			return monsters;
		}

		@Override
		public boolean onend(){
			if(!victory) return false;
			var s=Squad.active;
			gold=Javelin.round(gold+RPG.randomize(gold));
			s.gold+=gold;
			var award="The miners award you with $%s!";
			award=String.format(award,Javelin.format(gold));
			Javelin.message(award,true);
			if(rubies>0){
				for(var i=0;i<rubies;i++)
					s.equipment.add(new Ruby());
				award="The miners also award you with %s wish rubies!";
				award=String.format(award,rubies);
				Javelin.message(award,true);
			}
			clear();
			return true;
		}
	}

	static final String DESCRIPTION="Gold mine";
	static final String DRY="A gold mine has run dry...";
	static final String MONSTERS="A gold mine needs help with monsters!";

	/**
	 * {@link Town} {@link Labor}.
	 *
	 * @author alex
	 */
	public static class BuildMine extends Build{
		/** Constructor. */
		public BuildMine(){
			super("Build mine",1,Rank.HAMLET,null);
		}

		@Override
		public Location getgoal(){
			return new Mine();
		}

		@Override
		public boolean validate(District d){
			return super.validate(d)
					&&d.getlocationtype(Mine.class).size()<d.town.getrank().rank
					&&getsitelocation()!=null;
		}

		@Override
		protected Point getsitelocation(){
			var free=town.getdistrict().getfreespaces();
			for(Point p:free)
				if(Terrain.get(p.x,p.y).equals(Terrain.MOUNTAINS)) return p;
			return null;
		}
	}

	int gold=0;
	int rubies=0;
	int level=1;
	/**
	 * Different than garrison so a mine can continue working under hostile
	 * control.
	 */
	Combatants monsters=new Combatants(0);
	int progress=0;
	int maxdepth=RPG.r(1,20);

	/** Constructor. */
	public Mine(){
		super(DESCRIPTION,DESCRIPTION,6,10);
		terrain.add(Terrain.UNDERGROUND);
		allowedinscenario=false;
		gossip=true;
		work=1;
	}

	/**
	 * Avoids {@link #generate()}. Use this for when a player is building a mine
	 * via {@link Work}.
	 */
	public Mine(int x,int y){
		this();
		this.x=x;
		this.y=y;
	}

	@Override
	public void turn(long time,WorldScreen world){
		if(ishostile()||!monsters.isEmpty()) return;
		var d=getdistrict();
		if(d==null) return;
		var population=d.town.population;
		progress+=population+RPG.randomize(population);
		var upgrade=level*Period.Time.YEAR/20;
		if(progress>upgrade){
			progress-=upgrade;
			upgrade();
		}
	}

	void upgrade(){
		level+=1;
		if(level>maxdepth){
			remove();
			getdistrict().town.events.add(DRY);
			return;
		}
		if(ishostile()) return;
		gold+=RewardCalculator.getgold(level);
		if(RPG.chancein(20)) rubies+=1;
		if(RPG.chancein(20/Tier.TIERS.size())){
			monsters=EncounterGenerator.generate(level,Terrain.UNDERGROUND);
			getdistrict().town.events.add(MONSTERS);
		}
	}

	@Override
	public boolean isworking(){
		return !monsters.isEmpty();
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		if(monsters.isEmpty()){
			Javelin.message("This gold mine is operating normally.",false);
			return false;
		}
		var description="This mine shaft has run into a lair of monsters!";
		description=describe(monsters,description,true,this);
		if(!headsup(description)) return true;
		throw new StartBattle(new MineFight());
	}

	@Override
	public List<Combatant> getcombatants(){
		return monsters;
	}

	void clear(){
		gold=0;
		rubies=0;
		monsters.clear();
	}

	@Override
	protected void captureforai(Incursion attacker){
		super.captureforai(attacker);
		if(attacker.getel()>level) clear();
	}
}
