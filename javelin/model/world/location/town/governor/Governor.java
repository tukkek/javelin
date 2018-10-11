package javelin.model.world.location.town.governor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import javelin.Javelin;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * Holds the {@link Labor} options for each {@link Town} and possibly
 * auto-manages it.
 *
 * TODO promote specializing in one trait at a time in subclasses
 *
 * @author alex
 */
public abstract class Governor implements Serializable{
	static final int STARTINGHAND=3;
	static final Comparator<? super Labor> SORTYBYNAME=(o1,o2)->o1.name
			.compareTo(o2.name);

	/** Current labor. */
	private ArrayList<Labor> projects=new ArrayList<>(0);
	private ArrayList<Labor> hand=new ArrayList<>(STARTINGHAND);

	final Town town;
	int nprojects=1;

	/** Constructor. */
	public Governor(Town t){
		town=t;
	}

	public int redraw(){
		int drawn=0;
		while(!isfull()&&draw())
			drawn+=1;
		return drawn;
	}

	public boolean draw(){
		District d=town.getdistrict();
		for(Labor l:Deck.generate(town)){
			l=l.generate(town);
			if(!hand.contains(l)&&!projects.contains(l)&&l.validate(d)){
				hand.add(l);
				return true;
			}
		}
		return false;
	}

	/**
	 * Processes the current {@link #projects}.
	 *
	 * @param labor Labor to be distributed among the {@link #projects}.
	 * @param d
	 *
	 * @return <code>false</code> if there is no current project.
	 */
	public void work(float labor,District d){
		float step=labor/projects.size();
		validate(d);
		for(Labor l:new ArrayList<>(projects))
			l.work(step);
		always(hand);
		if(projects.size()<nprojects&&!hand.isEmpty()){
			manage();
			if(Javelin.DEBUG&&projects.isEmpty())
				System.err.println(town+": empty project list ("+town.traits+")");
		}
	}

	protected void always(ArrayList<Labor> hand2){

	}

	/**
	 * Selects next task for {@link #projects}.
	 */
	public abstract void manage();

	/**
	 * The maximum number of cards is 2 + {@link Town#getrank()} (3 minimum, 7
	 * maximum).
	 *
	 * @return <code>true</code> if currently has the maximum number of cards in
	 *         hand.
	 */
	public boolean isfull(){
		return hand.size()>=gethandsize();
	}

	public int gethandsize(){
		return town.getrank().rank-1+STARTINGHAND;
	}

	public void validate(District d){
		for(Labor l:new ArrayList<>(hand))
			if(!l.validate(d)) hand.remove(l);
		for(Labor l:new ArrayList<>(projects))
			if(l.progress>=l.cost||!l.validate(d)) projects.remove(l);
		redraw();
	}

	/**
	 * @return A hand of cards sorted by name, including any building upgrades in
	 *         the {@link District}.
	 *
	 * @see Location#getupgrades()
	 */
	public ArrayList<Labor> gethand(){
		District d=town.getdistrict();
		validate(d);
		ArrayList<Labor> hand=new ArrayList<>(this.hand);
		for(Location l:d.getlocations())
			for(Labor upgrade:l.getupgrades(d)){
				if(hand.contains(upgrade)) continue;
				upgrade=upgrade.generate(town);
				if(upgrade.validate(d)) hand.add(upgrade);
			}
		hand.sort(SORTYBYNAME);
		return hand;
	}

	/**
	 * @return Ongoing projects.
	 */
	public ArrayList<Labor> getprojects(){
		projects.sort(SORTYBYNAME);
		return projects;
	}

	public void removeproject(Labor labor){
		projects.remove(labor);
	}

	public void removefromhand(Labor l){
		hand.remove(l);
	}

	public void addproject(Labor l){
		projects.add(l);
	}

	public int getprojectssize(){
		return projects.size();
	}

	/**
	 * Pretty weird, somewhat lazy but very random normal algorithm that allows a
	 * computer player to select {@link Labor} with a higher chance if they are
	 * cheaper.
	 *
	 * @return <code>null</code> if there are no option, otherwise a labor card.
	 */
	protected Labor pick(ArrayList<Labor> cards){
		if(cards.isEmpty()) return null;
		if(cards.size()==1) return cards.get(0);
		float total=0;
		Labor min=null;
		for(Labor l:cards){
			total+=l.cost;
			if(!(l instanceof Trait)&&(min==null||l.cost<min.cost)) min=l;
		}
		if(town.getrank().rank<getseason()) return min;
		float[] chances=new float[cards.size()];
		for(int i=0;i<cards.size();i++)
			/*
			 * inverted cost-chance array: 0 cost means 100% chance, total cost
			 * means 0% chance. Minimum of 10% to prevent potential infinite
			 * loop edge cases.
			 */
			chances[i]=Math.max(.1f,(total-cards.get(i).cost)/total);
		Labor selected=null;
		while(selected==null){
			selected=RPG.pick(cards);// pick random card
			if(RPG.random()>chances[cards.indexOf(selected)]) selected=null; // chance roll failed
		}
		return selected;
	}

	long getseason(){
		return Math.min(4,Squad.active.hourselapsed/(24*100));
	}
}
