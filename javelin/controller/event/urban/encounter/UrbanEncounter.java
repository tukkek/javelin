package javelin.controller.event.urban.encounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.event.urban.UrbanEvent;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.map.location.TownMap;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * Abstract an encoutner with a group of {@link Combatants}. Offers a few
 * standard options: {@link #bribe}, {@link #fight}, {@link #surrender} and
 * {@link #rollskill(String, Squad, int)}.
 *
 * {@link #validate(Monster)}, {@link #generateguards(int)} and
 * {@link #fight(Squad, List)} help set up an eventual {@link Fight}.
 *
 * @author alex
 */
public abstract class UrbanEncounter extends UrbanEvent{
	/** Bribe your way out of trouble. <code>null</code> to disable option. */
	protected String bribe="Bribe";
	/** Fight your way out of trouble. <code>null</code> to disable option. */
	protected String fight="Fight";
	/** Give up into trouble. <code>null</code> to disable option. */
	protected String surrender="Surrender";

	int price=RewardCalculator.getgold(town.population,1);
	List<Monster> candidates=Terrain.get(town.x,town.y).getmonsters().stream()
			.filter(m->validate(m)).collect(Collectors.toList());
	Set<Upgrade> upgrades;
	String intro;

	/** Constructor. */
	public UrbanEncounter(Town t,List<String> traits,Rank minimum,
			Set<Upgrade> upgrades,String intro){
		super(t,traits,minimum);
		this.upgrades=upgrades;
		this.intro=intro;
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return s!=null&&!candidates.isEmpty()&&super.validate(s,squadel);
	}

	/** @return <code>true</code> if valid foe. */
	protected boolean validate(Monster foe){
		return foe.think(-1)&&foe.cr<=el;
	}

	/**
	 * @return A group of enemies for {@link #fight(Squad, Combatants)}.
	 * @see #el
	 */
	protected Combatants generatefoes(){
		var rank=town.getrank().rank;
		var nfoes=RPG.rolldice(rank,4);
		var foes=new Combatants(nfoes);
		while(foes.size()<nfoes&&ChallengeCalculator.calculateel(foes)<el)
			foes.add(new Combatant(RPG.pick(candidates),true));
		while(ChallengeCalculator.calculateel(foes)<el)
			Combatant.upgradeweakest(foes,upgrades);
		return foes;
	}

	@Override
	public void happen(Squad s){
		var message=intro;
		var foes=generatefoes();
		if(rollskill(s,10+el,message)) return;
		if(s.gold<price) bribe=null;
		if(bribe!=null) bribe=bribe+" ($"+Javelin.format(price)+", you have $"
				+Javelin.format(s.gold)+")";
		if(fight!=null) fight+=" ("+Difficulty.describe(s.members,foes)+" fight)";
		var options=new ArrayList<String>();
		for(var option:new String[]{bribe,fight,surrender})
			if(option!=null) options.add(option);
		if(options.isEmpty()){
			if(Javelin.DEBUG)
				throw new RuntimeException("No options for "+getClass());
			return;
		}
		options.sort(null);
		var choice=options.get(Javelin.choose(message,options,false,true));
		if(choice==bribe)
			Squad.active.gold-=price;
		else if(choice==fight)
			throw new StartBattle(fight(s,foes));
		else if(choice==surrender)
			surrender();
		else if(Javelin.DEBUG) throw new RuntimeException("Unknown option "+choice);
	}

	/** @return A fight to be thrown with {@link StartBattle}. */
	protected EventFight fight(Squad s,Combatants foes){
		EventFight f=new EventFight(foes,s);
		f.map=new TownMap(town);
		f.rewardgold=false;
		return f;
	}

	/** Called when {@link #surrender} is selected. */
	protected abstract void surrender();

	/**
	 * @param message See {@link #intro}.
	 * @param dc TODO
	 * @param foes2
	 * @return <code>true</code> to de-escalate the encounter before it even
	 *         begins. <code>false</code> if the skill roll failed or just to
	 *         ignore this optional step.
	 */
	protected abstract boolean rollskill(Squad s,int dc,String message);
}