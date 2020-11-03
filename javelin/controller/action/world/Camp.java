package javelin.controller.action.world;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import javelin.Javelin;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.RandomEncounter;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.view.screen.WorldScreen;

/**
 * Rest in the {@link WorldScreen}. High chance of finding a monster instead.
 *
 * TODO there could be a 1/6 of having a special camp encounter happen instead
 * of a fight
 * http://chaudronchromatique.blogspot.com/2014/09/pc-camp-events.html
 *
 * @author alex
 */
public class Camp extends WorldAction{
	static final String WARNING="Are you sure you want to try to set up camp in this wild area?\n"
			+"Monsters may interrupt you.\n\n";
	static final String PROMPT="Press c to set camp, w to camp for a week or any other key to cancel...";
	static final String PROMPTDEBUG="DEBUG CAMP\n"
			+"(c)amp (d)ay (w)eek (m)onth (s)eason (y)ear?";
	static final String INSIDETOWN="Cannot camp inside a town's district!\n"
			+"Try moving further into the wilderness.\n";
	static final String QUICKHEAL="Do you want to use spells and non-consumable items to heal your party?\n"
			+"Press c to auto-heal or any other key to continue...";

	static final HashMap<Character,int[]> PERIODS=new HashMap<>();

	static{
		final int day=24;
		PERIODS.put('c',new int[]{12,12});
		PERIODS.put('w',new int[]{7*day,12});
		if(Javelin.DEBUG){
			PERIODS.put('d',new int[]{1*day,12});
			PERIODS.put('m',new int[]{30*day,12});
			PERIODS.put('s',new int[]{100*day,12});
			PERIODS.put('y',new int[]{400*day,12});
		}
	}

	/** Constructor. */
	public Camp(){
		super("Camp",new int[]{KeyEvent.VK_C},new String[]{"c"});
	}

	@Override
	public void perform(WorldScreen screen){
		if(quickheal()||Dungeon.active!=null) throw new RepeatTurn();
		Town t=(Town)Squad.active.findnearest(Town.class);
		if(t!=null&&t.getdistrict().getarea().contains(Squad.active.getlocation())){
			Javelin.message(INSIDETOWN,false);
			return;
		}
		String prompt;
		if(Javelin.DEBUG)
			prompt=PROMPTDEBUG;
		else if(World.scenario.worldencounters)
			prompt=WARNING+PROMPT;
		else
			prompt=PROMPT;
		int[] period=PERIODS.get(Javelin.prompt(prompt));
		if(period==null) return;
		final int hours=period[0];
		final int rest=period[1];
		for(int i=0;i<=hours;i++){
			Squad.active.delay(1);
			if(World.scenario.worldencounters)
				RandomEncounter.encounter(1/WorldScreen.HOURSPERENCOUNTER);
			if(i>0&&i%rest==0) Lodge.rest(1,rest,false,Lodge.LODGE);
		}
	}

	boolean quickheal(){
		if(Squad.active.canheal().isEmpty()||Javelin.prompt(QUICKHEAL)!='c')
			return false;
		Squad.active.quickheal();
		return true;
	}
}
