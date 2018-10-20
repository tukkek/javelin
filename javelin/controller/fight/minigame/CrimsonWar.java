package javelin.controller.fight.minigame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.challenge.TensionDirector;
import javelin.controller.challenge.TensionDirector.TensionAction;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.building.ArenaFountain;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.generator.encounter.MisalignmentDetector;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.old.RPG;

/**
 * An endless, large scale {@link Fight} where you control a single character
 * with streamlined resting, loot and leveling.
 *
 * @author alex
 */
public class CrimsonWar extends Minigame{
	/** Encounter level in which to end. */
	static final int WIN=25;
	static final List<Encounter> GOOD=new ArrayList<>();
	static final List<Encounter> EVIL=new ArrayList<>();
	static final List<Upgrade> UPGRADES=new ArrayList<>();

	static{
		for(Encounter e:Organization.ENCOUNTERS){
			for(Combatant c:e.group)
				if(c.source.elite) continue;
			var alignment=new MisalignmentDetector(e.group);
			if(!alignment.check()) continue;
			if(alignment.good)
				GOOD.add(e);
			else if(alignment.evil) EVIL.add(e);
		}
		UpgradeHandler.singleton.gather();
		UPGRADES.addAll(UpgradeHandler.singleton.getalluncategorized());
	}

	class CrimsonDirector extends TensionDirector{
		List<Encounter> pool;

		public CrimsonDirector(List<Encounter> pool){
			this.pool=pool;
		}

		@Override
		protected List<Combatant> generate(int el) throws GaveUp{
			Collections.shuffle(pool);
			for(var encounter:pool)
				if(encounter.el==el) return encounter.generate();
			throw new GaveUp();
		}
	}

	CrimsonDirector allies=new CrimsonDirector(GOOD);
	CrimsonDirector enemies=new CrimsonDirector(EVIL);
	Combatant hero=null;
	ArrayList<Item> bag=new ArrayList<>(0);

	@Override
	public ArrayList<Combatant> getblueteam(){
		var blue=new ArrayList<Combatant>(1);
		blue.add(hero);
		return blue;
	}

	@Override
	public ArrayList<Combatant> getfoes(Integer teamel){
		enemies.force();
		enemies.check(state.redTeam,state.blueTeam,state.blueTeam.get(0).ap);
		assert !enemies.monsters.isEmpty();
		enemies.force();
		return new ArrayList<>(enemies.monsters);
	}

	@Override
	public void run(){
		List<String> sides=List.of("Random","Good","Evil");
		var choice=Javelin.choose("Which side will you fight on?",sides,true,false);
		if(choice==-1) return;
		if(choice==2||choice==0&&RPG.chancein(2)){
			var swap=allies;
			allies=enemies;
			enemies=swap;
		}
		hero=selecthero();
		if(hero==null) return;
		super.run();
	}

	Combatant selecthero(){
		var set=new HashSet<String>();
		for(Encounter e:allies.pool)
			for(var combatant:e.group)
				if(combatant.source.cr<=5) set.add(combatant.source.name);
		var list=new ArrayList<>(set);
		list.sort(null);
		list.add(0,"Random");
		var choice=Javelin.choose("Choose your hero:",list,true,false);
		if(choice==-1) return null;
		if(choice==0) choice=RPG.r(1,list.size()-1);
		String name=list.get(choice);
		return new Combatant(Javelin.getmonster(name),true);
	}

	@Override
	public void checkend(){
		var s=Fight.state;
		if(s.dead.contains(hero)){
			Javelin.message("Your hero died! Game over :S",true);
			throw new EndBattle();
		}
		s.dead.clear();
		if(s.redTeam.isEmpty()&&ChallengeCalculator.calculateel(s.blueTeam)>=WIN){

		}
	}

	@Override
	public void startturn(Combatant acting){
		super.startturn(acting);
		var s=Fight.state;
		hero=s.clone(hero);
		if(allies.check(s.blueTeam,s.redTeam,acting.ap)==TensionAction.LOWER){
			enter(allies.monsters,s.blueTeam,RPG.pick(s.blueTeam).getlocation());
			if(!allies.monsters.isEmpty()){
				for(var combatant:allies.monsters)
					combatant.setmercenary(true);
				Javelin.message("New allies join the battlefield!",true);
			}
		}
		if(s.redTeam.isEmpty()) enemies.force();
		if(enemies.check(s.redTeam,s.blueTeam,acting.ap)==TensionAction.LOWER){
			enter(enemies.monsters,s.redTeam,RPG.pick(s.redTeam).getlocation());
			if(!enemies.monsters.isEmpty()){
				Javelin.message("New allies join the battlefield!",true);
				boost();
			}
		}
	}

	//TODO offer a choice at least when it comes to upgrades
	void boost(){
		var boost=hero+" powers up!";
		Collections.shuffle(UPGRADES);
		for(Upgrade u:UPGRADES)
			if(u.upgrade(hero)){
				boost+=" Gained "+u.name.toLowerCase()+"!";
				break;
			}
		var loot=RewardCalculator
				.getgold(ChallengeCalculator.calculatecr(hero.source));
		var items=Item.randomize(Item.ALL);
		Collections.shuffle(items);
		for(var i:items)
			if(loot/2<=i.price&&i.price<=loot*2&&i.canuse(hero)==null){
				bag.add(i.clone());
				boost+="Received "+i.name.toLowerCase()+"!";
				break;
			}
		if(heal()) boost+="\n"+hero+" is fully restored!";
		Javelin.message(boost,true);
	}

	boolean heal(){
		float chance=1-hero.hp/(float)hero.maxhp;
		for(Spell s:hero.spells)
			chance+=.1*s.used;
		if(RPG.random()>=chance) return false;
		ArenaFountain.heal(hero);
		return true;
	}

	@Override
	public List<Item> getbag(Combatant combatant){
		return combatant.equals(hero)?bag:Collections.emptyList();
	}
}
