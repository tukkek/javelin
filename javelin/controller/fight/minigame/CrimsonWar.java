package javelin.controller.fight.minigame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.challenge.TensionDirector;
import javelin.controller.challenge.TensionDirector.TensionAction;
import javelin.controller.comparator.ItemsByName;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.building.ArenaFountain;
import javelin.controller.fight.setup.BattleSetup;
import javelin.controller.generator.encounter.AlignmentDetector;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.upgrade.Upgrade;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;

/**
 * An endless, large scale {@link Fight} where you control a single character
 * with streamlined resting, loot and leveling.
 *
 * @author alex
 */
public class CrimsonWar extends Minigame{
	static final int CHOICESUPGRADES=3;
	static final int CHOICESITEMS=CHOICESUPGRADES;
	static final String JOINRED="Enemy reinforcements arrive:";
	static final String JOINBLUE="New allies join the battlefield:";
	/** Encounter level in which to end. */
	static final int WIN=25;
	static final List<Encounter> GOOD=new ArrayList<>();
	static final List<Encounter> EVIL=new ArrayList<>();
	static final List<Upgrade> UPGRADES=new ArrayList<>();
	static final List<Item> ITEMS=new ArrayList<>();
	static final int MAPSIZE=25;

	/*Goods is taking neutrals as well to balance and optimize the number of options. Ideally wouldn't have to.*/
	static{
		for(Encounter e:Organization.ENCOUNTERS){
			if(e.group.size()<2) continue;
			for(Combatant c:e.group)
				if(c.source.elite) continue;
			var alignment=new AlignmentDetector(e.group);
			if(!alignment.check()) continue;
			if(alignment.evil)
				EVIL.add(e);
			else
				GOOD.add(e);
		}
		for(var upgrade:Upgrade.getall())
			if(upgrade.isusedincombat()) UPGRADES.add(upgrade);
		for(var item:Item.ITEMS)
			if(!(item instanceof Artifact)&&item.usedinbattle) ITEMS.add(item);
	}

	class CrimsonDirector extends TensionDirector{
		List<Encounter> pool;

		public CrimsonDirector(List<Encounter> pool){
			this.pool=pool;
			escalating=true;
		}

		@Override
		protected List<Combatant> generate(int el) throws GaveUp{
			var encounter=CrimsonWar.reinforce(el,pool);
			if(encounter==null) throw new GaveUp();
			return encounter;
		}
	}

	CrimsonDirector allies=new CrimsonDirector(GOOD);
	CrimsonDirector enemies=new CrimsonDirector(EVIL);
	Combatant hero=null;
	ArrayList<Item> bag=new ArrayList<>(0);

	public CrimsonWar(){
		setup=new BattleSetup(){
			@Override
			public void generatemap(Fight f){
				super.generatemap(f);
				Square[][] map=new Square[MAPSIZE][];
				for(int i=0;i<MAPSIZE;i++)
					map[i]=Arrays.copyOfRange(f.map.map[i],0,MAPSIZE);
				state.map=map;
				f.map.map=map;
			}
		};
		onready.add(()->{
			allies.delay(hero.ap);
			enemies.delay(hero.ap);
		});
	}

	@Override
	public ArrayList<Combatant> getblueteam(){
		var blue=new ArrayList<Combatant>(1);
		blue.add(hero);
		int cr=Math.max(2,Math.round(hero.source.cr));
		for(var squads=RPG.r(1,4);squads>0;){
			var squadcr=RPG.r(1,cr)+RPG.randomize(3);
			var allies=reinforce(squadcr,this.allies.pool);
			if(allies==null) continue;
			blue.addAll(allies);
			for(var a:allies)
				a.setmercenary(true);
			squads-=1;
		}
		return blue;
	}

	@Override
	public ArrayList<Combatant> getfoes(Integer teamel){
		teamel=ChallengeCalculator.calculateel(state.blueTeam);
		var foes=new ArrayList<Combatant>(4);
		while(Difficulty.calculate(state.blueTeam,foes)<Difficulty.MODERATE){
			int el=teamel-2-2+RPG.randomize(4); //about 4 squads
			if(el<1) el=1;
			var reinforcements=reinforce(el,enemies.pool);
			if(reinforcements!=null) foes.addAll(reinforcements);
		}
		return foes;
	}

	@Override
	public boolean start(){
		List<String> sides=List.of("Random","Good","Evil");
		var choice=Javelin.choose("Which side will you fight on?",sides,true,false);
		if(choice==-1) return false;
		if(choice==2||choice==0&&RPG.chancein(2)){
			var swap=allies;
			allies=enemies;
			enemies=swap;
		}
		hero=selecthero();
		return hero!=null&&super.start();
	}

	Combatant selecthero(){
		var set=new HashSet<String>();
		for(Encounter e:allies.pool)
			for(var combatant:e.group){
				Monster m=combatant.source;
				if(2<=m.cr&&m.cr<=10&&m.think(-1)) set.add(m.name);
			}
		var list=new ArrayList<>(set);
		list.sort(null);
		list.add(0,"Random");
		var choice=Javelin.choose("Choose your hero:",list,true,false);
		if(choice==-1) return null;
		if(choice==0) choice=RPG.r(1,list.size()-1);
		String name=list.get(choice);
		Combatant hero=new Combatant(Monster.get(name),true);
		hero.source.elite=true;
		hero.maxhp=hero.source.hd.maximize();
		hero.hp=hero.maxhp;
		return hero;
	}

	@Override
	public void checkend(){
		if(state.dead.contains(hero)){
			Javelin.message("Your leader is dead! Game over :S",true);
			throw new EndBattle();
		}
		state.dead.clear();
		if(state.redTeam.isEmpty()
				&&ChallengeCalculator.calculateel(state.blueTeam)>=WIN){
			Javelin.message("You have conquered this territory!",true);
			throw new EndBattle();
		}
	}

	@Override
	public void startturn(Combatant acting){
		super.startturn(acting);
		ArrayList<Combatant> blue=state.blueTeam;
		ArrayList<Combatant> red=state.redTeam;
		if(!acting.equals(hero)&&!red.isEmpty()) return;
		hero=state.clone(hero);
		if(blue.size()==1) allies.force();
		if(enter(allies,blue,red,acting.ap,JOINBLUE))
			for(var combatant:allies.monsters)
			combatant.setmercenary(true);
		if(red.isEmpty()) enemies.force();
		if(enter(enemies,red,blue,acting.ap,JOINRED)){
			heal();
			upgrade();
			loot();
			BattleScreen.active.messagepanel.clear();
		}
	}

	boolean enter(CrimsonDirector director,ArrayList<Combatant> allies,
			List<Combatant> enemies,float ap,String message){
		if(director.check(allies,enemies,ap)!=TensionAction.LOWER) return false;
		var entry=allies.isEmpty()
				?new Point(RPG.r(0,state.map.length-1),RPG.r(0,state.map[0].length-1))
				:RPG.pick(allies).getlocation();
		if(director.monsters.isEmpty()) return false;
		enter(director.monsters,allies,entry);
		Point p=director.monsters.get(0).getlocation();
		Javelin.redraw();
		BattleScreen.active.center(p.x,p.y);
		Javelin.message(message+" "+Javelin.group(director.monsters)+".",true);
		return true;
	}

	void boost(){
	}

	void loot(){
		var gold=RewardCalculator
				.getgold(ChallengeCalculator.calculatecr(hero.source));
		var all=Item.randomize(ITEMS);
		Collections.shuffle(all);
		var items=new ArrayList<Item>(CHOICESITEMS);
		for(int i=0;i<all.size()&&items.size()<CHOICESITEMS;i++){
			var item=all.get(i);
			if(gold/2<=item.price&&item.price<=gold*2&&item.canuse(hero)==null)
				items.add(item.clone());
		}
		items.sort(ItemsByName.SINGLETON);
		String prompt="Select an item:";
		var choice=Javelin.choose(prompt,items,CHOICESITEMS>3,false);
		if(choice>=0) bag.add(items.get(choice));
	}

	void upgrade(){
		Collections.shuffle(UPGRADES);
		var upgrades=new ArrayList<Upgrade>(CHOICESUPGRADES);
		for(int i=0;i<UPGRADES.size()&&upgrades.size()<CHOICESUPGRADES;i++){
			var upgrade=UPGRADES.get(i);
			if(upgrade.validate(hero,true)) upgrades.add(upgrade);
		}
		upgrades.sort((a,b)->a.name.compareTo(b.name));
		var names=upgrades.stream().map(u->u.name).collect(Collectors.toList());
		String prompt="Select an upgrade:";
		var choice=Javelin.choose(prompt,names,CHOICESUPGRADES>3,false);
		if(choice>=0) upgrades.get(choice).upgrade(hero);
	}

	void heal(){
		float chance=1-hero.hp/(float)hero.maxhp;
		for(Spell s:hero.spells)
			chance+=.1*s.used;
		if(RPG.random()>=chance) return;
		ArenaFountain.heal(hero);
		Javelin.app.switchScreen(BattleScreen.active);
		Javelin.message(hero+" is fully restored!",true);
	}

	@Override
	public List<Item> getbag(Combatant combatant){
		return combatant.equals(hero)?bag:Collections.emptyList();
	}

	static List<Combatant> reinforce(int el,List<Encounter> pool){
		Collections.shuffle(pool);
		for(var encounter:pool)
			if(encounter.el==el) return encounter.generate();
		return null;
	}

}
