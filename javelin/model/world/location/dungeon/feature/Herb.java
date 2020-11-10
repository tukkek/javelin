package javelin.model.world.location.dungeon.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.ItemsByPrice;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.model.item.Item;
import javelin.model.item.potion.Potion;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.unit.skill.Survival;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.old.RPG;

/**
 * Similar to a {@link Chest} but requires someone with enough {@link Survival}
 * and results in a higher-than average {@link Potion} treasure.
 *
 * @see RewardCalculator
 * @author alex
 */
public class Herb extends Feature{
	/**
	 * Unfortunately {@link Potion}s are on the lower end of magic {@link Item}s,
	 * so it's hard to scale this {@link DungeonFloor} feature indefinitely.
	 * Instead, set this max allowed level as per {@link DungeonFloor#level}.
	 *
	 * Note that this feature will generate one or multiple instances of the same
	 * potion type.
	 */
	public static final int MAXLEVEL;

	static final List<Potion> POTIONS=Item.ITEMS.stream()
			.filter(i->i.getClass().equals(Potion.class)).map(i->(Potion)i)
			.collect(Collectors.toList());
	static final int MAXCOPIES=2;

	static{
		if(Javelin.DEBUG&&POTIONS.isEmpty())
			throw new RuntimeException("Could not find potions!");
		int ceiling=POTIONS.get(POTIONS.size()-1).price;
		ceiling*=MAXCOPIES;
		int level=1;
		while(RewardCalculator.getgold(level+1)<=ceiling)
			level+=1;
		MAXLEVEL=level;
	}

	int dc;
	List<Potion> loot;

	/** Constructor. */
	public Herb(DungeonFloor f){
		super("herb");
		remove=false;
		dc=10+f.level+f.gettable(FeatureModifierTable.class).roll();
		loot=generate(f);
	}

	@Override
	public boolean activate(){
		if(Javelin.prompt("Do you want to check these herbs?\n"
				+"Press ENTER to continue, any other key to cancel...")!='\n')
			return false;
		String description=describe(loot);
		Squad s=Squad.active;
		Combatant survivalist=s.getbest(Skill.SURVIVAL);
		if(survivalist.taketen(Skill.SURVIVAL)<dc){
			String text=survivalist+" is not familiar with this plant...";
			if(s.getbest(Skill.KNOWLEDGE).taketen(Skill.KNOWLEDGE)>=dc)
				text="A more skilled group could turn these herbs into "+description
						+"...";
			Javelin.message(text,false);
			return true;
		}
		if(RPG.chancein(2)&&!Debug.disablecombat){
			String interupted="You are interrupted while extracting the "+description
					+" herbs!";
			Javelin.message(interupted,false);
			throw new StartBattle(new RandomDungeonEncounter(Dungeon.active));
		}
		s.delay(1);
		String success="You extract "+description+" from the herbs!";
		Javelin.message(success,false);
		for(Potion p:loot)
			p.grab();
		remove();
		return false;
	}

	/**
	 * @param level Approximate reward level.
	 * @return Generates a set of potions.
	 * @see RewardCalculator
	 * @see #MAXLEVEL
	 */
	List<Potion> generate(DungeonFloor f){
		if(!validate(f)) return Collections.EMPTY_LIST;
		RPG.shuffle(POTIONS).sort(ItemsByPrice.SINGLETON);
		var potions=new ArrayList<Potion>(MAXCOPIES);
		int reward=RewardCalculator.getgold(f.level);
		for(var p:POTIONS){
			potions.clear();
			int pool=reward;
			for(var i=0;i<MAXCOPIES&&pool>0;i++){
				var c=(Potion)p.clone();
				c.identified=true;
				potions.add(c);
				pool-=p.price;
			}
			if(-reward*10<=pool&&pool<=0) return potions;
		}
		return potions;
	}

	/**
	 * @param potions A number of the same type of item.
	 * @return "a potion of cure light wounds", "4x potion of barkskin"...
	 */
	static String describe(List<? extends Item> potions){
		int amount=potions.size();
		String description=potions.get(0).toString().toLowerCase();
		return (amount==1?"a":amount+"x")+" "+description;
	}

	@Override
	public boolean validate(DungeonFloor f){
		return super.validate(f)&&f.level<=MAXLEVEL;
	}

	@Override
	public String toString(){
		return "Herb ("+loot.get(0).name.toLowerCase()+")";
	}
}
