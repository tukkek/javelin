package javelin.model.world.location.town.diplomacy.quest;

import java.awt.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.ItemsByPrice;
import javelin.controller.content.ContentSummary;
import javelin.controller.content.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.Diplomacy;
import javelin.model.world.location.town.diplomacy.quest.find.Connect;
import javelin.model.world.location.town.diplomacy.quest.find.Discover.DiscoverHaunt;
import javelin.model.world.location.town.diplomacy.quest.find.Discover.DiscoverTemple;
import javelin.model.world.location.town.diplomacy.quest.find.Discover.DiscoverTown;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;

/**
 * A task that can be completed for rewards and {@link Diplomacy#reputation}.
 * Non-hostile towns will have an active number of them equal to their
 * {@link Rank}.
 *
 * Design-wise, quests have a few gameplay objectives:
 *
 * 1. Provide lightweight but still unique gameplay not found elsewhere.
 *
 * 2. Nudge the player towards less obvious tasks they may overlook, with the
 * allure of a reward - thus promoting variety.
 *
 * 3. An alternate way to level up characters - not fully pacifist or otherwise
 * different to that degree but still...
 *
 * 4. Make the world feel alive, with things happening and opportunities coming
 * and going regardless of what the player is choosing to do. Since strategy is
 * continuous planning under evolcing circumstances, this is key to strategy.
 *
 * @author alex
 */
public abstract class Quest implements Serializable{
	/** All available quest templates. */
	public final static Map<String,List<Class<? extends Quest>>> QUESTS=new HashMap<>();
	/** Short-term quests expire within a week on average (default). */
	protected static final int SHORT=7;
	/** Long-term quests expire within a month on average. */
	protected static final int LONG=30;
	static final String COMPLETE="You have completed a quest (%s)!\n"+"%s\n"+"%s"
			+"Mood in %s is now: %s.";
	static final List<Class<? extends Quest>> ALL=new ArrayList<>(8);
	static final Class<? extends Quest> DEBUG=null;

	static{
		//TODO QUESTS.put(Trait.CRIMINAL,List.of(Heist.class));
		QUESTS.put(Trait.EXPANSIVE,
				List.of(DiscoverTown.class,DiscoverTemple.class,DiscoverHaunt.class));
		//TODO QUESTS.put(Trait.MAGICAL,List.of(Clear.class));
		QUESTS.put(Trait.MERCANTILE,List.of(Connect.class));
		//TODO QUESTS.put(Trait.MILITARY,List.of(Raid.class));
		//TODO QUESTS.put(Trait.NATURAL,List.of(FetchGem.class));
		//TODO QUESTS.put(Trait.RELIGIOUS,List.of(FetchArt.class));
		for(var quests:QUESTS.values())
			ALL.addAll(quests);
	}

	/**
	 * Simple, generic {@link World} actor to use with quests. Ideally subclasses
	 * need only override {@link #interact()}.
	 *
	 * Sharing a same image, it reduces cognitive overload and dependency on art
	 * assets while providing a simple, standard "this is related to a
	 * {@link Town} quest and nothing more" visual feedback.
	 *
	 * Obviously, not all quests need to employ markers (such as those that target
	 * existing {@link Location}s).
	 *
	 * TODO quests markers should, by default always show the difficulty level, as
	 * in {@link Location#headsup(String)}.
	 *
	 * @author alex
	 */
	protected abstract class Marker extends Actor{
		boolean inside;

		/**
		 * @param inside If <code>true</code>, place it inside the {@link District},
		 *          otherwise outside, nearby.
		 */
		protected Marker(boolean inside){
			super();
			this.inside=inside;
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
			return town+"quest ("+Quest.this.toString().toLowerCase()+").";
		}

		@Override
		public Integer getel(Integer attackerel){
			return el;
		}

		@Override
		public void place(){
			if(x==-1){
				var positions=new HashSet<Point>();
				var d=town.getdistrict();
				if(inside)
					positions.addAll(d.getfreespaces());
				else{
					int r=d.getradius()*2;
					positions.addAll(Point.getrange(town.x-r,town.y-r,town.x+r,town.y+r));
					positions.removeAll(d.getarea());
				}
				setlocation(RPG.pick(positions.stream()
						.filter(p->World.validatecoordinate(p.x,p.y)
								&&!Terrain.get(p.x,p.y).equals(Terrain.WATER))
						.collect(Collectors.toList())));
			}
			super.place();
			WorldScreen.discover(x,y);
		}
	}

	/** Town this quest was generated for. */
	public Town town;
	/**
	 * Name of the quest. Used as a locally-exclusive identifier per {@link Town}.
	 */
	public String name;
	/**
	 * Amount of gold to be awarded upon completion.
	 *
	 * @see #reward()
	 */
	public int gold=0;
	/**
	 * Item reward.
	 *
	 * @see #reward()
	 */
	public Item item;
	/** When <code>true</code> will not expire the quest until redeemed. */
	public boolean completed=false;
	/** Encounter level, between 1 and {@link Town#population}. */
	protected int el;
	/**
	 * @see #SHORT
	 * @see #LONG
	 */
	protected int duration=SHORT;

	/** @return If <code>false</code>, cancel or skip this quest type. */
	public boolean validate(){
		return name!=null;
	}

	/** TODO would be cool to have trait-based rewards */
	void reward(){
		var min=RewardCalculator.getgold(el-1);
		var max=RewardCalculator.getgold(el+1);
		gold=Javelin.round(RPG.r(min,max));
		var items=RewardCalculator.generateloot(gold,1,Item.ITEMS);
		if(!items.isEmpty()){
			gold=0;
			items.sort(ItemsByPrice.SINGLETON.reversed());
			item=items.get(0);
			item.identified=true;
		}
	}

	/** A chance to further define details after validation. */
	protected void define(Town t){
		town=t;
		el=Math.max(RPG.r(1,t.population),RPG.r(1,t.population));
		reward();
	}

	/**
	 * @return If <code>true</code>, the quest is considered completed and a
	 *         {@link Squad} may claim the reward.
	 */
	protected abstract boolean complete();

	@Override
	public boolean equals(Object o){
		var q=o instanceof Quest?(Quest)o:null;
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
		var quests=new ArrayList<Class<? extends Quest>>();
		if(Javelin.DEBUG&&DEBUG!=null)
			quests.add(DEBUG);
		else{
			for(var trait:t.traits)
				if(QUESTS.get(trait)!=null) //TODO only needed until redesign is done
					quests.addAll(QUESTS.get(trait));
			RPG.shuffle(quests);
			quests.addAll(RPG.shuffle(ALL));
		}
		try{
			for(var quest:quests){
				var q=quest.getConstructor().newInstance();
				q.define(t);
				if(q.validate()&&!t.diplomacy.quests.contains(q)) return q;
			}
		}catch(ReflectiveOperationException e){
			if(Javelin.DEBUG)
				throw new RuntimeException("Cannot generate Town quest.",e);
		}
		return null;
	}

	/** @see ContentSummary */
	public static String printsummary(){
		var total=QUESTS.values().stream()
				.collect(Collectors.summingInt(l->l.size()));
		var traits=QUESTS.keySet().stream().map(t->QUESTS.get(t).size()+" "+t)
				.collect(Collectors.joining(", "));
		return total+" town quests ("+traits+")";
	}

	/** @return Description of {@link #item} or {@link #gold}. */
	public String describereward(){
		if(item!=null) return item.toString();
		if(gold==0) return "";
		return "$"+Javelin.format(gold);
	}

	/**
	 * @return If <code>true</code>, notify player when quest is generated or on
	 *         {@link #cancel()}. By default only <code>true</code> if
	 *         {@link #duration} is {@link #LONG}.
	 */
	public boolean alert(){
		return duration==LONG;
	}

	/**
	 * Removes this from {@link Town#quests} and lowers
	 * {@link Diplomacy#reputation}.
	 */
	public void cancel(){
		town.diplomacy.quests.remove(this);
		town.diplomacy.reputation-=1;
		if(alert()) town.events.add("Quest expired: "+name+".");
	}

	/**
	 * Updates the quest state, possibly making it {@link #completed} or
	 * {@link #cancel()}.
	 *
	 * @param expire If <code>true</code>, will also roll a daily chance to
	 *          expire.
	 * @see #duration
	 */
	public void update(boolean expire){
		if(completed)
			return;
		else if(complete())
			completed=true;
		else if(!validate()||expire&&RPG.chancein(duration)) cancel();
	}

	/** @return Message show to player during successful {@link #claim()}. */
	protected String message(){
		var xp=RewardCalculator.rewardxp(Squad.active.members,el,1);
		var reward=describereward();
		if(!reward.isEmpty())
			reward="You are rewarded for your efforts with: "+reward+"!\n";
		var reputation=town.diplomacy.describestatus().toLowerCase();
		return String.format(COMPLETE,name,xp,reward,town,reputation);
	}

	/** Completes the quest succesfully. */
	public void claim(){
		update(false);
		if(!completed) return;
		var p=town.population;
		town.diplomacy.reputation+=RPG.randomize(p/town.getrank().rank,0,p);
		Javelin.message(message(),true);
		if(gold>0) Squad.active.gold+=gold;
		if(item!=null) item.grab();
		town.diplomacy.quests.remove(this);
	}

}
