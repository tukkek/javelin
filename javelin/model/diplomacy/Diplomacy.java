package javelin.model.diplomacy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javelin.Javelin;
import javelin.controller.db.StateManager;
import javelin.model.diplomacy.mandate.Mandate;
import javelin.model.diplomacy.mandate.RaiseHandSize;
import javelin.model.diplomacy.mandate.Redraw;
import javelin.model.diplomacy.mandate.RequestGold;
import javelin.model.diplomacy.mandate.RequestMercenaries;
import javelin.model.unit.Alignment;
import javelin.model.world.World;
import javelin.model.world.location.ResourceSite.Resource;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.frame.DiplomacyScreen;
import javelin.view.screen.WorldScreen;

/**
 * Offers long-term card-game-like actions to pursue.
 *
 * Each {@link Town} is treated as an independent entity from the player,
 * regardless of it being hostile or not. A town must be discovered, however,
 * before diplomatic actions are allowed on it.
 *
 * TODO
 *
 * 2x show alignment (influencing to Ally will show both)
 *
 * influence positive and negative (50% of influencing
 * {@link Alignment#iscompatible(Alignment)}
 *
 * RequestTrade {@link Resource}s (requires ally), giving resource to second
 * target and increasing status too
 *
 * RequestAlly (adds a creature pf given EL to squad in district, requires ally)
 *
 * RequestMap (reveals area equal to District#radius+Town#rank, if not entirely
 * revealed already) - requires Friendly
 *
 * @author alex
 * @see Town#ishostile()
 */
public class Diplomacy implements Serializable{
	/** Amount of {@link #reputation} to unlock a diplomatic action. */
	public static final int TRIGGER=100;
	/** Starting maximum amount of cards for {@link #hand}. */
	public static final int HANDSTARTING=3;
	/** Maximum amount of cards for {@link #hand}. */
	public static final int HANDMAX=7;

	/** Generated at campaign start of set by {@link StateManager#load()}. */
	public static Diplomacy instance=null;

	static final String NOTIFICATION="You have earned enough reputation to warrant a diplomatic action.\n"
			+"Press d at any time to consider your options. Note that no further reputation will be gained until you perform an action.";
	static List<Class<? extends Mandate>> MANDATES=new ArrayList<>(
			List.of(RaiseHandSize.class,Redraw.class,RequestGold.class,
					RequestMercenaries.class));

	/** Map of town by relationship data. */
	public Map<Town,Relationship> relationships=new HashMap<>();
	/**
	 * On reaching 100, enables a diplomatic action. Surplus is lost and should
	 * never be reduced, only increased.
	 */
	public int reputation=0;
	/** Possible diplomatic actions. */
	public TreeSet<Mandate> hand=new TreeSet<>();
	/** Maximum amount of cards for {@link #hand}. */
	public int handsize=HANDSTARTING;

	/** Generates a fresh set of relationships, when a campaign starts. */
	public Diplomacy(List<Town> towns){
		for(var t:towns)
			relationships.put(t,new Relationship(t));
	}

	/** To be called once a day to generate {@link #reputation}. */
	public void turn(){
		var total=getdailyprogress(true);
		reputation+=total;
		if(reputation>=TRIGGER){
			draw();
			if(Javelin.prompt(NOTIFICATION,true)=='d') DiplomacyScreen.open();
		}
	}

	/**
	 * @param randomize Whether to randomize the result or not.
	 * @return Amount of {@link #reputation} garnered in a day.
	 * @see RPG#randomize(int)
	 * @see Town#generatereputation()
	 */
	static public int getdailyprogress(boolean randomize){
		var total=0;
		for(Town t:Town.gettowns())
			if(!t.ishostile()){
				var reputation=t.generatereputation();
				if(randomize) reputation+=RPG.randomize(reputation);
				total+=reputation;
			}
		return total;
	}

	/**
	 * Validates current {@link #hand} and tries to generate new ones up to
	 * {@link #handsize}.
	 *
	 * @see #validate()
	 */
	public void draw(){
		validate();
		ArrayList<Town> discovered=new ArrayList<>(getdiscovered().keySet());
		while(hand.size()<handsize&&generate(discovered))
			continue;
	}

	/**
	 * Removes invalid entries from {@link #hand}.
	 *
	 * @see Mandate#validate()
	 */
	public void validate(){
		for(var card:new ArrayList<>(hand))
			if(!card.validate(this)) hand.remove(card);
	}

	boolean generate(List<Town> discovered){
		try{
			for(var type:RPG.shuffle(MANDATES))
				for(var town:RPG.shuffle(discovered)){
					var card=type.getConstructor(Relationship.class)
							.newInstance(relationships.get(town));
					if(!card.validate(this)) continue;
					card.define();
					if(hand.add(card)) return true;
				}
			return false;
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return A copy of {@link #relationships} containing only {@link Town}s
	 *         currently visible in the {@link World} map.
	 * @see WorldScreen#see(javelin.controller.Point)
	 */
	public HashMap<Town,Relationship> getdiscovered(){
		var discovered=new HashMap<Town,Relationship>();
		for(Town t:Town.gettowns())
			if(WorldScreen.see(t.getlocation()))
				discovered.put(t,relationships.get(t));
		return discovered;
	}
}
