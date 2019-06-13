package javelin.controller.event.wild.neutral;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.event.wild.Wanderer;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.condition.GenericBuff;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

/**
 * 17 4 2 4 1 0 If fed, the halfling will give a mood boost for 24 hours.
 *
 * @author alex
 */
public class WanderingHalflings extends Wanderer{
	static final String TEXT="You sit and eat with the halfings for a while.\n"
			+"As they devour as much food as they can, they sing gleefully and share stories, jokes and rumours.\n"
			+"Upon departure, they leave your team in a good mood!";
	static final String DECLINE="Decline";
	static final String ATTACK="Attack";

	static class Joyful extends GenericBuff{
		public Joyful(Combatant c){
			super(c,+1,"Joyful",24);
		}
	}

	/** Reflection-frienndly constructor. */
	public WanderingHalflings(PointOfInterest l){
		super("Wandering halfling",l);
	}

	@Override
	public void happen(Squad s){
		var nhalflings=RPG.rolldice(4,4);
		var halfling=Monster.get("lightfoot");
		var price=Javelin.round(Math.round(halfling.eat()*nhalflings))+s.eat();
		if(price<1) price=1;
		var feed="Feed them for $"+Javelin.format(price)+" (you have $"
				+Javelin.format(s.gold)+")";
		List<String> choices=List.of(feed,DECLINE,ATTACK);
		var choice=choices.get(Javelin.choose("You come across a group of "
				+nhalflings+" halflings. They ask you for food.",choices,true,true));
		if(choice==DECLINE) return;
		if(choice==ATTACK){
			var foes=new ArrayList<Combatant>(nhalflings);
			while(foes.size()<nhalflings)
				foes.add(new Combatant(halfling,true));
			throw new StartBattle(new EventFight(foes,location));
		}
		if(s.gold<price){
			Javelin.message("You don't have enough food to share...",false);
			return;
		}
		s.gold-=price;
		s.hourselapsed+=4;
		Javelin.message(TEXT,false);
		for(var member:s)
			member.addcondition(new Joyful(member));
	}
}
