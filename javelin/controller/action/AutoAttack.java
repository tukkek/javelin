package javelin.controller.action;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class AutoAttack extends Action{
	private static final Comparator<Combatant> SORTBYSTATUS=(o1,
			o2)->o1.getnumericstatus()-o2.getnumericstatus();

	public AutoAttack(){
		super("Auto-attack nearest visible enemy",new String[]{"\t"});
	}

	@Override
	public boolean perform(Combatant active){
		//TODO create a comparator that sorts passive first then other methods
		BattleState s=Fight.state;
		ArrayList<Combatant> melee=s.getsurroundings(active);
		melee.removeAll(active.getteam(s));
		if(!melee.isEmpty()&&!active.source.melee.isEmpty()){
			melee.sort(SORTBYSTATUS);
			active.meleeattacks(melee.get(0),s);
			return true;
		}
		List<Combatant> ranged=s.gettargets(active);
		ranged.removeAll(melee);
		if(!ranged.isEmpty()&&!active.source.ranged.isEmpty()){
			ranged.sort((o1,o2)->{
				Point p=active.getlocation();
				return p.distanceinsteps(o1.getlocation())
						-p.distanceinsteps(o2.getlocation());
			});
			active.meleeattacks(ranged.get(0),s);
			return true;
		}
		Javelin.message("No targets in range...",Javelin.Delay.WAIT);
		throw new RepeatTurn();
	}

	@Override
	public String[] getDescriptiveKeys(){
		return new String[]{"TAB"};
	}
}
