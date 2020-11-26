package javelin.model.world.location.haunt;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.comparator.MonstersByName;
import javelin.controller.db.EncounterIndex;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
import javelin.controller.fight.LocationFight;
import javelin.controller.fight.mutator.Boss;
import javelin.controller.fight.mutator.Waves;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.map.location.LocationMap;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Tier;
import javelin.model.item.consumable.Ruby;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.old.RPG;
import javelin.test.TestHaunt;
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
	/**
	 * Would be great to scale infinitely but for now setting parameters is
	 * paramount.
	 *
	 * @see TestHaunt
	 */
	public static final int MAXEL=Tier.EPIC.maxlevel+Difficulty.DEADLY;

	static Set<Monster> defeated=new HashSet<>(0);
	static Hashtable<Class<? extends Haunt>,EncounterIndex> INDEXCACHE=new Hashtable<>();

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

	class HauntWaves extends Waves{
		HauntWaves(){
			super(targetel,null);
		}

		@Override
		public Combatants generate(Fight f){
			try{
				return generatemonsters(waveel);
			}catch(GaveUp e){
				if(Javelin.DEBUG) throw new RuntimeException(e);
				return new Combatants();
			}
		}
	}

	class HauntBoss extends Boss{
		public HauntBoss(){
			super(targetel,null);
		}

		@Override
		protected List<Monster> listbosses(List<Terrain> terrains){
			return pool;
		}

		@Override
		protected List<Encounter> listminions(List<Terrain> terrains){
			var minions=new ArrayList<Encounter>(pool.size()*4);
			for(var m:pool){
				var group=new Combatants(2);
				for(var i=1;i<=8;i++)
					group.add(new Combatant(m,true));
				minions.add(new Encounter(group));
			}
			return minions;
		}
	}

	class HauntFight extends LocationFight{
		HauntFight(Location l,LocationMap m){
			super(l,m);
			mutators.add(RPG.pick(List.of(new HauntWaves(),new HauntBoss())));
		}

		@Override
		public boolean win(){
			if(!super.win()) return false;
			defeated=Fight.originalredteam.stream().map(c->c.source)
					.collect(Collectors.toSet());
			garrison.clear();
			return true;
		}
	}

	/** Every unit that can be generated here. */
	protected List<Monster> pool;

	List<Monster> hires=new ArrayList<>();
	Class<? extends LocationMap> map;
	Combatant recruit;

	/** Constructor. */
	protected Haunt(String description,Class<? extends LocationMap> map,
			List<Monster> pool,List<Terrain> terrains){
		super(description,description,0,0);
		if(Javelin.DEBUG&&pool.isEmpty())
			throw new RuntimeException("empty pool: "+getClass());
		terrain=terrains;
		discard=false;
		allowentry=false;
		this.map=map;
		this.pool=pool;
		var tieri=0;
		while(RPG.chancein(2)&&tieri<Tier.TIERS.size()-1)
			tieri+=1;
		var t=Tier.TIERS.get(tieri);
		targetel=t.getrandomel(false);
	}

	@Override
	public void generategarrison(){
		while(garrison.isEmpty())
			try{
				testboss();
				garrison.addAll(testwaves());
			}catch(GaveUp e){
				WorldGenerator.retry();
				garrison.clear();
				targetel+=1;
				if(targetel>MAXEL){
					if(Javelin.DEBUG) throw new InvalidParameterException();
					return;
				}
			}
	}

	@Override
	public List<Combatant> getcombatants(){
		return garrison;
	}

	@Override
	public Integer getel(Integer attackerel){
		return ishostile()?super.getel(attackerel):targetel;
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
		return new HauntFight(this,getmap());
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
			var pool=this.pool.stream().filter(m->m.cr<=targetel)
					.collect(Collectors.toList());
			var nhires=Math.min(RPG.rolldice(2,4),pool.size());
			add(new HashSet<>(RPG.shuffle(pool).subList(0,nhires)));
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
		targetel=Math.min(targetel+RPG.r(1,4),MAXEL);
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
	protected boolean validateplacement(boolean water,World w,List<Actor> actors){
		return terrain.contains(Terrain.get(x,y))
				&&super.validateplacement(water,w,actors);
	}

	/** @return <code>true</code> if monster has any of the subtypes. */
	protected static boolean include(Monster m,List<String> subtypes){
		if(subtypes.contains(m.group.toLowerCase())) return true;
		for(String subtype:m.subtypes)
			if(subtypes.contains(subtype)) return true;
		return false;
	}

	/** @return A haunt encounter or wave. */
	synchronized public Combatants generatemonsters(int waveel) throws GaveUp{
		var index=INDEXCACHE.get(getClass());
		if(index==null){
			index=new EncounterIndex(pool);
			INDEXCACHE.put(getClass(),index);
		}
		return Waves.generate(waveel,index);
	}

	@Override
	public void spawn(){
		try{
			Incursion.spawn(new Incursion(x,y,generatemonsters(targetel),realm));
		}catch(GaveUp e){
			return;
		}
	}

	/** @return All {@link World} haunts. */
	static public List<Haunt> gethaunts(){
		return World.getactors().stream().filter(a->a instanceof Haunt)
				.map(a->(Haunt)a).collect(Collectors.toList());
	}

	/** @see TestHaunt */
	public Combatants testboss() throws GaveUp{
		return new HauntBoss().generate(null);
	}

	/** @see TestHaunt */
	public Combatants testwaves() throws GaveUp{
		generatemonsters(targetel+Waves.ELMODIFIER.get(4));
		return generatemonsters(targetel+Waves.ELMODIFIER.get(1));
	}
}
