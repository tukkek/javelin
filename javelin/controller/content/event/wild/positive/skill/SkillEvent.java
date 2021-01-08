package javelin.controller.content.event.wild.positive.skill;

import java.util.Set;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.content.event.wild.WildEvent;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

public abstract class SkillEvent extends WildEvent{
	static final boolean DEBUGFUMBLE=false;
	protected String prompt;
	char confirm;
	char cancel;

	int level;
	protected int dc;

	/**
	 * @param name Event name.
	 * @param prompt Confirmation prompt.
	 * @param confirm Confrmation key to proceed.
	 * @param cancel Confirmation key to cancel.
	 */
	public SkillEvent(String name,PointOfInterest l,String prompt,char confirm,
			char cancel){
		super(name,l);
		this.prompt=prompt;
		this.confirm=confirm;
		this.cancel=cancel;
	}

	@Override
	public void define(Squad s,int squadel){
		super.define(s,squadel);
		level=Math.round(RPG.pick(s.members).source.cr);
		dc=10+level+RPG.randomize(10);
	}

	@Override
	public void happen(Squad s){
		var input=Javelin.prompt(prompt,Set.of(confirm,cancel));
		if(input==cancel) return;
		var active=getbest(s);
		var fumble=RPG.r(1,20)==1||Javelin.DEBUG&&DEBUGFUMBLE;
		if(fumble&&fumble(active,location)) return;
		if(!attempt(active)){
			Javelin.message(fail(active),true);
			return;
		}
		location.remove();
		Javelin.message(succeed(active,givereward(s,active)),true);
	}

	String givereward(Squad s,Combatant active){
		var gold=RPG.r(RewardCalculator.getgold(Math.min(1,level-1)),
				RewardCalculator.getgold(level+1));
		var items=RewardCalculator.generateloot(gold,1,Item.ITEMS);
		String reward;
		if(items.isEmpty()){
			gold=Javelin.round(gold);
			reward="$"+Javelin.format(gold);
			s.gold+=gold;
		}else{
			reward=Javelin.group(items).toLowerCase();
			for(var i:items)
				i.identified=true;
			s.equipment.get(active).addAll(items);
		}
		return reward;
	}

	/**
	 * @return Most suited squad member to act.
	 */
	protected abstract Combatant getbest(Squad s);

	/**
	 * Called only when a fumble happens (by default a 1 on a d20).
	 *
	 * @param location TODO
	 *
	 * @return If <code>false</code>, will instead ignore that the fumble
	 *         happened, allowing for events to not support fumbles.
	 */
	protected abstract boolean fumble(Combatant active,PointOfInterest location);

	/**
	 * @return <code>true</code> if succeds at {@link Skill} check.
	 * @see #dc
	 */
	protected abstract boolean attempt(Combatant active);

	/**
	 * Called if {@link #attempt(Combatant)} fails.
	 *
	 * @return Message to be shown to player.
	 */
	protected abstract String fail(Combatant active);

	/**
	 * Called if {@link #attempt(Combatant)} succeds.
	 *
	 * @param reward Gold or items, already added to Squad's stash or
	 *          {@link Combatant}'s bag.
	 * @return Message to be shown to player.
	 *
	 * @see Squad#gold
	 * @see Squad#equipment
	 */
	protected abstract String succeed(Combatant active,String reward);

}