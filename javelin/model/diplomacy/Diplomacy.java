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
import javelin.model.diplomacy.mandate.meta.RaiseHandSize;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.frame.DiplomacyScreen;
import javelin.view.screen.WorldScreen;

/**
 * Offers long-term card-game-like actions to pursue towards {@link Town}s. Each
 * {@link Town} is treated as an independent entity from the player, regardless
 * of it being hostile or not. A town must be discovered, however, before
 * diplomatic actions are allowed on it.
 *
 * Reputation is generated daily by {@link Town#generatereputation()} and upon
 * hitting a certain {@link #TRIGGER} will allow a {@link Mandate} to be chosen.
 * If an action is not taken immediately, reputation will still be generated but
 * is reset to 0 once a card is played, meaning that it's a suboptimal play to
 * way too long to choose. The game will remind the player of this daily.
 *
 * A {@link #hand} of {@link Mandate} cards is persistent and will be fully
 * replenished to {@link #handsize} once {@link #TRIGGER} is hit. Invalid cards
 * will be removed on demand but not replenished immediately to prevent
 * scumming.
 *
 * TODO with 2.0, when we have html documentation instead of F1-F12, include
 * overview and card documentation
 *
 * TODO "spontaneous change alignment" mandates
 *
 * @author alex
 * @see Town#ishostile()
 * @see Town#generatereputation()
 * @see Mandate#validate(Diplomacy)
 * @see RaiseHandSize
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
	/**
	 * Map of town by relationship data. Always prefer using
	 * {@link #getdiscovered()}.
	 */
	Map<Town,Relationship> relationships=new HashMap<>();
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

	//TODO add a "lastnotice" to only bother the player once per week
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
	 *
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
		while(hand.size()<handsize&&Mandate.generate(this))
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

	/**
	 * @return A copy of {@link #relationships} containing only {@link Town}s
	 *         currently visible in the {@link World} map.
	 * @see WorldScreen#see(javelin.controller.Point)
	 */
	public HashMap<Town,Relationship> getdiscovered(){
		var discovered=new HashMap<Town,Relationship>(relationships.size());
		for(var t:Town.gettowns()){
			var r=relationships.get(t);
			if(r.isdiscovered()) discovered.put(r.town,r);
		}
		return discovered;
	}
}
