package javelin.model.world.location.town.quest;

import java.awt.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.content.ContentSummary;
import javelin.controller.content.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.Diplomacy;
import javelin.model.world.location.town.labor.Trait;
import javelin.model.world.location.town.quest.basic.Discovery;
import javelin.model.world.location.town.quest.basic.Fetch;
import javelin.model.world.location.town.quest.basic.Kill;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;

/**
 * A task that can be completed for money and reputation. Non-hostile towns will
 * have an active number of them equal to their Rank tier. Quests are timed and
 * most of them require you to come back in that time frame to collect your
 * reward.
 *
 * @see Diplomacy
 * @author alex
 */
public abstract class Quest implements Serializable{
	/** All available quest templates. */
	public final static Map<String,List<Class<? extends Quest>>> QUESTS=new HashMap<>();
	static final Class<? extends Quest> DEBUG=null;
	static final String BASIC="basic";

	static{
		QUESTS.put(BASIC,List.of(Kill.class,Fetch.class,Discovery.class));
		QUESTS.put(Trait.CRIMINAL,List.of(Pursue.class));
		QUESTS.put(Trait.MAGICAL,List.of());
		QUESTS.put(Trait.EXPANSIVE,List.of(DiscoverTown.class));
		QUESTS.put(Trait.MERCANTILE,List.of());
		QUESTS.put(Trait.MILITARY,List.of(War.class));
		QUESTS.put(Trait.NATURAL,List.of(DiscoverTerrain.class));
		QUESTS.put(Trait.RELIGIOUS,List.of(Pilgrimage.class));
	}

	/**
	 * Simple generic {@link World} actor to use with quests. Ideally subclasses
	 * need only override {@link #interact()}.
	 *
	 * Sharing a same image, reduces cognitive overload and dependency on art
	 * assets while providing a simple, standard "this is related to a
	 * {@link Town} quest and nothing more" visual feedback.
	 *
	 * Obviously, not all quests need to employ markers. Many, for example, target
	 * existing {@link Location}s in the world map.
	 *
	 * @author alex
	 */
	protected abstract class QuestMarker extends Actor{
		String markername;

		/**
		 * @param name Represented as "Quest marker (given name)."
		 * @see #describe()
		 */
		protected QuestMarker(String name){
			markername=name;
		}

		@Override
		public Boolean destroy(Incursion attacker){
			return null;
		}

		@Override
		public List<Combatant> getcombatants(){
			return null;
		}

		@Override
		public Image getimage(){
			return Images.get(List.of("world","questmarker"));
		}

		@Override
		public String describe(){
			return "Quest marker ("+markername+").";
		}

		@Override
		public Integer getel(Integer attackerel){
			return 0;
		}

		@Override
		public void place(){
			if(x==-1){
				var actors=World.getactors();
				HashSet<Point> districts=Town.getdistricts();
				while(x==-1||!World.validatecoordinate(x,y)
						||Terrain.get(x,y).equals(Terrain.WATER)
						||World.get(x,y,actors)!=null||districts.contains(getlocation())){
					x=RPG.r(town.x-distance,town.x+distance);
					y=RPG.r(town.y-distance,town.y+distance);
				}
			}
			super.place();
			WorldScreen.discover(x,y);
		}
	}

	/** Town this quest was generated for. */
	public final Town town;
	/** Encounter level. Default is {@link Town#population}. */
	protected int el;
	/** Quest becomes invalid once it reaches zero. */
	public int daysleft;
	/**
	 * Name of the quest. Used as a locally-exclusive identifier per {@link Town}.
	 */
	public String name;

	/**
	 * Amount of gold to be awarded upon completion.
	 *
	 * @see #reward()
	 */
	public int gold;
	/**
	 * Item reward.
	 *
	 * @see #reward()
	 */
	public Item item;
	/**
	 * Utility value for maximum distance quests should be from their Town.
	 */
	protected int distance;

	/**
	 * For Reflection compatibility, all subclasses should respect this
	 * constructor signature.
	 *
	 * @param t Town this quest is active in.
	 */
	protected Quest(Town t){
		town=t;
		el=t.population;
		daysleft=Javelin.round(RPG.r(7,100));
		distance=town.getdistrict().getradius()*2;
	}

	/** @return If <code>false</code>, don't use this object as a quest. */
	public abstract boolean validate();

	/** A chance to further define details after validation. */
	protected void define(){
		int target=Math.min(town.population,el);
		var min=RewardCalculator.getgold(target-1);
		var max=RewardCalculator.getgold(target+1);
		gold=Math.max(1,Javelin.round(RPG.r(min,max)));
		var items=RewardCalculator.generateloot(gold,1,Item.ITEMS);
		if(!items.isEmpty()){
			gold=0;
			item=items.get(0);
			item.identified=true;
		}
	}

	/**
	 * @return <code>true</code> if this is still listed as active in its
	 *         respective {@link Town}.
	 */
	public boolean isactive(){
		return daysleft>0&&town.quests.contains(this);
	}

	/**
	 * Note that a quest can be fulfilled but if {@link #daysleft} has expired,
	 * players won't be able to complete it as it will have been removed from
	 * {@link #town}.
	 *
	 * @return If <code>true</code>, the quest is considered completed and a
	 *         {@link Squad} may claim the reward.
	 */
	protected abstract boolean checkcomplete();

	/**
	 * @return <code>true</code> if a quest is to be cancelled permanently. For
	 *         example: a location needs to be captured but the location itself is
	 *         removed by some external force.
	 */
	protected boolean cancel(){
		return daysleft<=0;
	}

	/**
	 * @return A descriptive, permanent name for this quest.
	 *
	 * @see #equals(Object)
	 * @see #validate()
	 */
	protected abstract String getname();

	@Override
	public boolean equals(Object obj){
		Quest q=obj instanceof Quest?(Quest)obj:null;
		return q!=null&&q.name.equals(name);
	}

	@Override
	public int hashCode(){
		return name.hashCode();
	}

	@Override
	public String toString(){
		return name;
	}

	/**
	 * @return A brand-new, valid quest or <code>null</code> if couldn't generate
	 *         any.
	 */
	public static Quest generate(Town t){
		Set<Class<? extends Quest>> quests;
		if(Javelin.DEBUG&&DEBUG!=null)
			quests=Set.of(DEBUG);
		else{
			quests=new HashSet<>(QUESTS.get(BASIC));
			for(var trait:t.traits)
				quests.addAll(QUESTS.get(trait));
		}
		try{
			for(var quest:RPG.shuffle(new ArrayList<>(quests))){
				var q=quest.getConstructor(Town.class).newInstance(t);
				if(!q.validate()) continue;
				q.name=q.getname();
				if(t.quests.contains(q)) continue;
				q.define();
				return q;
			}
		}catch(ReflectiveOperationException e){
			if(Javelin.DEBUG)
				throw new RuntimeException("Cannot generate Town quest.",e);
		}
		return null;
	}

	/** @return A player-friendly "expires in x [time unit]" notice. */
	public String getdeadline(){
		int amount;
		String unit;
		if(daysleft>=30){
			amount=Math.round(daysleft/30);
			unit=amount==1?"month":"months";
		}else if(daysleft>=7){
			amount=Math.round(daysleft/7);
			unit=amount==1?"week":"weeks";
		}else{
			amount=daysleft;
			unit=amount==1?"day":"days";
		}
		return "expires in "+amount+" "+unit;
	}

	/** @see ContentSummary */
	public static String printsummary(){
		var total=QUESTS.values().stream()
				.collect(Collectors.summingInt(l->l.size()));
		var traits=new ArrayList<>(QUESTS.keySet());
		traits.remove(BASIC);
		traits.sort(null);
		traits.add(0,BASIC);
		var detailed=traits.stream().map(t->QUESTS.get(t).size()+" "+t)
				.collect(Collectors.joining(", "));
		return total+" town quests ("+detailed+")";
	}

	/** @return Description of {@link #item} or {@link #gold}. */
	public String describereward(){
		return item==null?"$"+Javelin.format(gold):item.toString();
	}

	int modifyreputation(){
		var gain=town.population/town.getrank().rank;
		return RPG.randomize(gain,0,Integer.MAX_VALUE);
	}

	/**
	 * Checks if the quest is expired or invalid, whether the objective is
	 * completed and then rewards the player. Removes itself from
	 * {@link Town#quests} as necessary.
	 */
	public void update(){
		if(cancel()){
			town.quests.remove(this);
			town.diplomacy.reputation-=modifyreputation();
			town.events.add("Quest expired: "+name+".");
		}else if(checkcomplete()) complete();
	}

	/** Completes the quest succesfully. */
	public void complete(){
		town.diplomacy.reputation+=modifyreputation();
		var m="You have completed a quest ("+name+")!\n";
		m+=RewardCalculator.rewardxp(Squad.active.members,el,1)+"\n";
		m+="You are rewarded for your efforts with: "+describereward()+"!\n";
		var s=town.diplomacy.describestatus().toLowerCase();
		m+="Mood in "+town+" is now: "+s+".";
		Javelin.message(m,true);
		Squad.active.gold+=gold;
		if(item!=null) item.grab();
		town.quests.remove(this);
	}
}
