package javelin.model.world.location.haunt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.comparator.MonstersByName;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
import javelin.controller.fight.LocationFight;
import javelin.controller.fight.setup.LocationFightSetup;
import javelin.controller.map.location.LocationMap;
import javelin.model.item.Tier;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Incursion;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.old.RPG;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;

public abstract class NewHaunt extends Fortification{
	class RecruitOption extends Option{
		Monster hire;

		RecruitOption(Monster m){
			super(m.toString(),MercenariesGuild.getfee(m),null);
			hire=m;
		}

	}

	class HauntScreen extends SelectScreen{
		HauntScreen(){
			super("Select your mercenaries:",null);
		}

		@Override
		public List<Option> getoptions(){
			return hires.stream().sequential().map(h->new RecruitOption(h))
					.collect(Collectors.toList());
		}

		@Override
		public String getCurrency(){
			return "$";
		}

		@Override
		public String printpriceinfo(Option o){
			return " ($"+Javelin.format(o.price)+"/day)";
		}

		@Override
		public String printinfo(){
			return "Your currently have $"+Javelin.format(Squad.active.gold)+".\n\n"
					+"Your squad: "+Javelin.group(Squad.active.members);
		}

		@Override
		public boolean select(Option o){
			RecruitOption ro=(RecruitOption)o;
			if(MercenariesGuild.recruit(new Combatant(ro.hire,false),false))
				return true;
			print(text+"\nNot enough gold to hire this unit!");
			return false;
		}
	}

	static final int ATTEMPTS=10_000;
	/** EL modifier by number of waves. */
	static final Map<Integer,Integer> WAVECR=new TreeMap<>();

	static{
		WAVECR.put(1,0);
		WAVECR.put(2,-2);
		WAVECR.put(3,-3);
		WAVECR.put(4,-4);
	}

	class HauntFight extends LocationFight{
		HauntFight() throws ReflectiveOperationException{
			super(NewHaunt.this,NewHaunt.this.map.getConstructor().newInstance());
		}

		@Override
		public void checkend(){
			try{
				var s=Fight.state;
				if(s.redTeam.isEmpty()&&generatewave()!=null){
					for(var c:garrison)
						c.rollinitiative(s.next.ap);
					s.redTeam.clear();
					s.redTeam.addAll(garrison);
					Fight.originalredteam.addAll(garrison);
					((LocationFightSetup)setup).placeredteam();
					Javelin.redraw();
					Javelin.message("A new wave of enemies appear!",true);
				}
			}catch(GaveUp e){
				if(Javelin.DEBUG) throw new RuntimeException(e);
			}
			super.checkend();
		}
	}

	List<Monster> hires=new ArrayList<>();
	Class<? extends LocationMap> map;
	List<Monster> pool;
	int waves;
	int waveel;

	/** Constructor. */
	protected NewHaunt(String description,Class<? extends LocationMap> map,
			List<Monster> pool){
		super(description,description,0,0);
		discard=false;
		allowentry=false;
		this.map=map;
		this.pool=pool;
		var tieri=0;
		while(RPG.chancein(2)&&tieri<Tier.TIERS.size()-1)
			tieri+=1;
		var t=Tier.TIERS.get(tieri);
		targetel=RPG.r(t.minlevel,t.maxlevel);
	}

	@Override
	public void generategarrison(){
		waves=RPG.r(1,4);
		if(waves>targetel) waves=targetel;
		waveel=targetel+WAVECR.get(waves);
		try{
			generatewave();
		}catch(GaveUp e){
			targetel+=1;
			generategarrison();
		}
	}

	Combatants generatewave() throws GaveUp{
		garrison.clear();
		waves-=1;
		if(waves<0) return null;
		var pool=getpool();
		if(pool.isEmpty()) throw new GaveUp();
		var wave=new Combatants();
		for(var attempt=1;attempt<=ATTEMPTS;attempt++){
			wave.clear();
			Integer el=null;
			while(el==null||el<waveel){
				wave.add(new Combatant(RPG.pick(pool),true));
				el=ChallengeCalculator.calculateel(wave);
			}
			if(el>waveel) continue;
			garrison=wave;
			return garrison;
		}
		throw new GaveUp();
	}

	/**
	 * @return
	 */
	List<Monster> getpool(){
		return pool.stream().filter(m->m.cr<=waveel).collect(Collectors.toList());
	}

	@Override
	public List<Combatant> getcombatants(){
		return garrison;
	}

	@Override
	public Integer getel(Integer attackerel){
		return waves==1?ChallengeCalculator.calculateel(garrison):targetel;
	}

	@Override
	public String describe(){
		var squad=Squad.active.getel();
		return descriptionknown+" ("+Difficulty.describe(targetel-squad)+").";
	}

	@Override
	public void turn(long time,WorldScreen world){
		super.turn(time,world);
		if(ishostile()||!RPG.chancein(30)) return;
		targetel+=RPG.r(1,4);
		generategarrison();
	}

	@Override
	protected Fight fight(){
		try{
			return new HauntFight();
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public void capture(){
		super.capture();
		hires.clear();
		var nhires=RPG.rolldice(2,4);
		var pool=RPG.shuffle(getpool());
		for(var i=0;i<pool.size()&&hires.size()<nhires;i++)
			hires.add(pool.get(i));
		hires.sort(MonstersByName.INSTANCE);
	}

	@Override
	protected void captureforai(Incursion attacker){
		super.captureforai(attacker);
		waves=0;
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		new HauntScreen().show();
		return true;
	}
}
