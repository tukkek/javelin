package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.WorldScreen;

/**
 * Allows members to be removed from a {@link Squad} at any point.
 *
 * @author alex
 */
public class Retire extends WorldAction{
	static final String ALLMERCENARIES="Retire all mercenaries";
	static List<String> CONFIRM=Arrays
			.asList(new String[]{"Yes, I am sure.","Not really, keep unit."});

	/** Constructor. */
	public Retire(){
		super("Retire squad member",new int[]{'R'},new String[]{"R"});
	}

	@Override
	public void perform(WorldScreen screen){
		Squad.active.sort();
		List<Combatant> squad=Squad.active.members;
		ArrayList<String> members=new ArrayList<>(squad.size());
		for(int i=0;i<squad.size();i++){
			Combatant c=squad.get(i);
			String fee="";
			if(c.mercenary) fee=", "+MercenariesGuild.getformattedfee(c);
			members.add(c+" ("+c.getstatus()+fee+")");
		}
		List<Combatant> mercenaries=Squad.active.getmercenaries();
		int dismissmercenaries=Integer.MIN_VALUE;
		if(mercenaries.size()>1){
			dismissmercenaries=squad.indexOf(mercenaries.get(0));
			members.add(dismissmercenaries,ALLMERCENARIES);
		}
		String prompt="Which squad member do you want to retire?";
		int choice=Javelin.choose(prompt,members,true,false);
		if(choice==dismissmercenaries)
			dismissmercenaries(mercenaries);
		else if(choice>=0){
			if(dismissmercenaries!=Integer.MIN_VALUE&&choice>=dismissmercenaries)
				choice-=1;
			dismiss(squad.get(choice));
		}
	}

	void dismissmercenaries(List<Combatant> mercenaries){
		if(confirm("Do you want to retire all of your mercenaries?"))
			for(Combatant c:mercenaries)
			c.dismiss(Squad.active);
	}

	void dismiss(Combatant chosen){
		float dailyupkeep=chosen.source.eat();
		if(chosen.mercenary) dailyupkeep+=chosen.pay();
		final String formattedcost=Javelin.format(Math.round(dailyupkeep));
		if(confirm("Are you sure you want to retire "+chosen
				+", with a daily cost of $"+formattedcost+"?"))
			chosen.dismiss(Squad.active);
	}

	boolean confirm(String prompt){
		return Javelin.choose(prompt,CONFIRM,true,true)==0;
	}
}
