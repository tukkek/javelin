package javelin.model.world.location.town.governor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javelin.Javelin;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.Trait;
import javelin.model.world.location.town.labor.basic.Growth;
import javelin.old.RPG;

/**
 * Governor for human-captured towns. Auto-manage can be toggled on and off.
 *
 * @author alex
 */
public class HumanGovernor extends Governor{
	static final Comparator<Labor> SORTBYCOST=(o1,o2)->o1.cost-o2.cost;

	/** Constructor. */
	public HumanGovernor(Town t){
		super(t);
	}

	@Override
	public void manage(){
		ArrayList<Labor> hand=new ArrayList<>();
		for(Labor l:gethand())
			if(l.automatic) hand.add(l);
		if(hand.isEmpty()){
			hand.clear();
			redraw();
			return;
		}
		if(hand.size()>1&&town.getrank().rank>=getseason())
			hand.remove(Growth.INSTANCE);
		Labor selected=null;
		if(nexttrait(hand)) selected=picktrait(hand);
		if(selected==null){
			ArrayList<Labor> nontraits=new ArrayList<>(hand);
			for(Labor l:new ArrayList<>(nontraits))
				if(l instanceof Trait) nontraits.remove(l);
			selected=pick(nontraits);
		}
		if(selected==null) /* no smart choice? then any choice! */
			selected=RPG.pick(hand);
		if(selected==null&&Javelin.DEBUG)
			throw new RuntimeException("No trait to pick! #humangovernor");
		selected.start();
	}

	/**
	 * @return <code>true</code> if should pick town's first trait or if every
	 *         option in our hand is basic (and we should thus expand the
	 *         possibilities).
	 */
	private boolean nexttrait(ArrayList<Labor> hand){
		if(town.traits.isEmpty()) return true;
		for(Labor l:hand)
			if(!Deck.isbasic(l)) return false;
		return true;
	}

	public Labor picktrait(ArrayList<Labor> hand){
		Collections.shuffle(hand);
		for(Labor l:hand)
			if(l instanceof Trait) return l;
		return null;
	}
}
