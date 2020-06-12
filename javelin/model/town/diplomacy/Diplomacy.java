package javelin.model.town.diplomacy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;

import javelin.model.diplomacy.mandate.Mandate;
import javelin.model.diplomacy.mandate.RevealAlignment;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.quest.Quest;
import javelin.old.RPG;

/**
 * Offers long-term card-game-like actions to pursue towards {@link Town}s. Each
 * {@link Town} is treated as an independent entity from the player, regardless
 * of it being hostile or not. A town must be discovered, however, before
 * diplomatic actions are allowed on it.
 *
 * Reputation is accrued by completing or failing {@link Quest}s and once
 * {@link #getstatus()} reaches 100%, a card can be chosen and paid for
 * (-100%)..New cards are drawn and old ones discarded periodically. Invalid
 * cards will be removed on demand but not replenished immediately to prevent
 * scumming.
 *
 * TODO with 2.0, when we have html documentation, include overview and card
 * documentation
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
	/**
	 * On reaching 100, enables a diplomatic action. Surplus is lost and should
	 * never be reduced, only increased.
	 */
	public int reputation=0;
	/** Possible diplomatic actions. */
	public TreeSet<Mandate> treaties=new TreeSet<>();
	/** Town these rewards are for. */
	public Town town;
	/**
	 * Town alignment dictates how certain {@link Mandate}s behave, so unlocking
	 * it is a privilege.
	 *
	 * @see RevealAlignment
	 */
	public boolean showalignment=false;

	/** Generates a fresh set of relationships, when a campaign starts. */
	public Diplomacy(Town town){
		this.town=town;
	}

	/** To be called once per day per instance. */
	public void turn(){
		reputation-=town.population/400f;
		validate();
		if(!treaties.isEmpty()&&RPG.chancein(30)){
			var m=RPG.pick(treaties);
			treaties.remove(m);
			town.events.add("Treaty opportunity expired: "+m+".");
		}
		if(treaties.size()<town.getrank().rank&&RPG.chancein(7)){
			var m=Mandate.generate(this);
			if(m!=null) town.events.add("New treaty available: "+m+".");
		}
	}

	/**
	 * Removes invalid entries from {@link #treaties}.
	 *
	 * @see Mandate#validate()
	 */
	public void validate(){
		for(var card:new ArrayList<>(treaties))
			if(!card.validate(this)){
				treaties.remove(card);
				town.events.add("Treaty no longer eligible: "+card+".");
			}
	}

	/**
	 * @return A percentage, where 0 is zero {@link #reputation} and 1 is
	 *         {@link #reputation} == {@link Town#population}.
	 */
	public float getstatus(){
		return reputation/(float)town.population;
	}

	/** @return A human description of {@link #getstatus()}. */
	public String describestatus(){
		var s=getstatus();
		if(s<=-1) return "Hostile";
		if(s<=0) return "Cautious";
		if(s<=.3) return "Neutral";
		if(s<=.7) return "Content";
		if(s<1) return "Happy";
		return "Loyal";
	}

	/**
	 * @return A description of {@link Town#alignment}, taking
	 *         {@link #showalignment} and {@link #showmorals} into account.
	 */
	public String describealignment(){
		return showalignment?town.alignment.toString():"Unknown alignment";
	}

	/** @return <code>true</code> if can claim a {@link Mandate}. */
	public boolean claim(){
		return getstatus()>=1&&!treaties.isEmpty();
	}

	/** @param m Pays for, plays and discards this. */
	public void enact(Mandate m){
		reputation-=town.population;
		m.act(this);
		treaties.remove(m);
	}
}
