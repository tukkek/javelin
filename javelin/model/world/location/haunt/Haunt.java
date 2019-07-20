package javelin.model.world.location.haunt;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javelin.controller.generator.NpcGenerator;
import javelin.controller.map.location.LocationMap;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Tier;
import javelin.model.item.consumable.Ruby;
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

/**
 * Shorter-lived {@link LocationFight}s, granting level-appropriate mercenaries
 * and an Easy/Very Easy recruit. Once cleared, they become stronger and
 * respawn.
 *
 * @author alex
 */
public abstract class Haunt extends Fortification{
	static final int ATTEMPTS=10_000;
	/** EL modifier by number of waves. */
	static final Map<Integer,Integer> WAVECR=new TreeMap<>();
	static final int LEADER=20;

	static Set<Monster> defeated=new HashSet<>(0);

	static{
		WAVECR.put(1,0);
		WAVECR.put(2,-2);
		WAVECR.put(3,-3);
		WAVECR.put(4,-4);
	}

	class RecruitOption extends Option{
		Monster hire;

		RecruitOption(Monster m){
			super(m.toString().toLowerCase(),MercenariesGuild.getfee(m),null);
			hire=m;
			var available=hires.stream().filter(h->h.equals(hire)).count();
			name=available+" "+name;
		}
	}

	class HauntScreen extends SelectScreen{
		Option recruitment=null;

		HauntScreen(){
			super("Select your mercenaries:",null);
			var r=recruit;
			if(r!=null){
				var rubies=Tier.get(r.source.cr).getordinal()+1;
				recruitment=new Option("Recruit: "+r,rubies);
			}
		}

		@Override
		public List<Option> getoptions(){
			var hirable=new HashSet<>(hires).stream().sequential()
					.map(h->new RecruitOption(h)).collect(Collectors.toList());
			var options=new ArrayList<Option>(hirable);
			if(recruitment!=null) options.add(recruitment);
			return options;
		}

		@Override
		public String getCurrency(){
			return "$";
		}

		@Override
		public String printpriceinfo(Option o){
			String price;
			if(o==recruitment)
				price=Math.round(o.price)+" "+(o.price==1?"ruby":"rubies");
			else
				price="$"+Javelin.format(o.price)+"/day";
			return " ("+price+")";
		}

		@Override
		public String printinfo(){
			var gold=Javelin.format(Squad.active.gold);
			var squad=Javelin.group(Squad.active.members);
			return "Your currently have $"+gold+".\n\nYour squad:\n  "+squad+".";
		}

		@Override
		public boolean select(Option o){
			if(o==recruitment){
				if(!Squad.active.equipment.pay(Ruby.class,Math.round(o.price))){
					print(text+"\nYou don't have enough rubies...");
					return false;
				}
				Squad.active.recruit(recruit);
				recruitment=null;
				recruit=null;
				return true;
			}
			var h=((RecruitOption)o).hire;
			if(!MercenariesGuild.recruit(new Combatant(h,true),false)){
				print(text+"\nNot enough gold to hire this unit...");
				return false;
			}
			hires.remove(h);
			return true;
		}
	}

	class HauntFight extends LocationFight{
		HauntFight(){
			super(Haunt.this,getmap());
		}

		@Override
		public void checkend(){
			try{
				var s=Fight.state;
				if(s.redTeam.isEmpty()&&generatewave()!=null){
					for(var c:garrison)
						c.rollinitiative(s.next.ap);
					//					s.redTeam.clear();
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

		@Override
		public boolean win(){
			if(!super.win()) return false;
			defeated=Fight.originalredteam.stream().map(c->c.source)
					.collect(Collectors.toSet());
			return true;
		}
	}

	protected List<Monster> pool;

	List<Monster> hires=new ArrayList<>();
	Class<? extends LocationMap> map;
	transient List<Terrain> terrains;
	Combatant recruit;
	int waves;
	int waveel;

	/** Constructor. */
	protected Haunt(String description,Class<? extends LocationMap> map,
			List<Monster> pool,List<Terrain> terrains){
		super(description,description,0,0);
		if(Javelin.DEBUG&&pool.isEmpty())
			throw new RuntimeException("empty pool: "+getClass());
		this.terrains=terrains;
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
		garrison=generatemonsters();
		return garrison;
	}

	Combatants generatemonsters() throws GaveUp{
		var pool=getpool();
		if(pool.isEmpty()) throw new GaveUp();
		var wave=new Combatants();
		for(var attempt=1;attempt<=ATTEMPTS;attempt++){
			wave.clear();
			var el=-Integer.MAX_VALUE;
			while(el<waveel){
				var m=RPG.pick(pool);
				Combatant c;
				if(RPG.chancein(LEADER)||m.cr<waveel-20){
					if(m.cr>=waveel) continue;
					c=NpcGenerator.generate(m,RPG.r(Math.round(m.cr),waveel));
					if(c==null) continue;
				}else
					c=new Combatant(m,true);
				wave.add(c);
				el=ChallengeCalculator.calculateel(wave);
			}
			if(el>waveel) continue;
			return garrison;
		}
		throw new GaveUp();
	}

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
		raiselevel();
		generategarrison();
	}

	@Override
	protected Fight fight(){
		return new HauntFight();
	}

	void add(Set<Monster> defeated){
		hires.clear();
		for(var h:defeated){
			if(!pool.contains(h)) continue;
			var quantity=getquantity(h);
			for(var i=0;i<quantity;i++)
				hires.add(h);
		}
		hires.sort(MonstersByName.INSTANCE);
		if(hires.isEmpty()){
			var pool=RPG.shuffle(getpool());
			var nhires=Math.min(RPG.rolldice(2,4),pool.size());
			add(new HashSet<>(pool.subList(0,nhires)));
		}
	}

	int getquantity(Monster hire){
		var t=Tier.get(hire.cr);
		if(t==Tier.LOW) return RPG.r(1,8);
		if(t==Tier.MID) return RPG.r(1,6);
		if(t==Tier.HIGH) return RPG.r(1,4);
		if(t==Tier.EPIC) return 1;
		if(Javelin.DEBUG) throw new InvalidParameterException();
		return 1;
	}

	void raiselevel(){
		targetel+=RPG.r(1,4);
		var easy=targetel+Difficulty.EASY;
		var recruits=pool.stream().filter(m->m.cr<=easy)
				.collect(Collectors.toList());
		if(!recruits.isEmpty())
			recruit=NpcGenerator.generate(RPG.pick(recruits),easy);
	}

	@Override
	protected void captureforai(Incursion attacker){
		super.captureforai(attacker);
		raiselevel();
		waves=0;
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		if(hires.isEmpty()){
			var empty="The "+descriptionknown.toLowerCase()+" is curently empty...";
			Javelin.message(empty,false);
			return true;
		}
		new HauntScreen().show();
		return true;
	}

	@Override
	public void capture(){
		super.capture();
		add(defeated);
		defeated.clear();
	}

	/** @return A new {@link #map} instance. */
	public LocationMap getmap(){
		try{
			return map.getConstructor().newInstance();
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void generate(){
		while(x==-1||!terrains.contains(Terrain.get(x,y)))
			super.generate();
	}

	/** @return <code>true</code> if monster has any of the subtypes. */
	protected static boolean include(Monster m,List<String> subtypes){
		if(subtypes.contains(m.group.toLowerCase())) return true;
		for(String subtype:m.subtypes)
			if(subtypes.contains(subtype)) return true;
		return false;
	}

	@Override
	public void spawn(){
		try{
			Incursion.spawn(new Incursion(x,y,generatemonsters(),realm));
		}catch(GaveUp e){
			return;
		}
	}
}
