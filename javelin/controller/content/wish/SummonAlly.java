package javelin.controller.content.wish;

import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.generator.NpcGenerator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.old.RPG;
import javelin.view.screen.SquadScreen;

/**
 * Summons a random, permanent ally, with number of rubies equal to
 * {@link Monster#cr}.
 *
 * @see SquadScreen
 */
public class SummonAlly extends Wish{
	/**
	 * If not <code>null</code> will use this as the challenge rating target.
	 */
	public Float fixed=null;

	/**
	 * Constructor.
	 *
	 * @param s
	 */
	public SummonAlly(Character keyp,WishScreen s){
		super("summon ally",keyp,s.rubies,false,s);
	}

	@Override
	boolean wish(Combatant target){
		int cr=screen.rubies;
		Monster m=getcandidate(cr);
		if(m!=null){
			Squad.active.recruit(m);
			return true;
		}
		while(m==null)
			m=getcandidate(RPG.r(cr/2,cr-1));
		Squad.active.recruit(NpcGenerator.generatenpc(m,cr));
		return true;
	}

	Monster getcandidate(int cr){
		List<Monster> candidates=Monster.BYCR.get(new Float(cr));
		if(candidates==null) return null;
		Collections.shuffle(candidates);
		for(Monster candidate:candidates)
			if(!Squad.active.contains(candidate)) return candidate;
		return null;
	}

	Float findnextlowercr(Float cr){
		Float newcr=-Float.MAX_VALUE;
		for(Float c:Monster.BYCR.keySet())
			if(c<cr) newcr=c;
		return newcr;
	}
}