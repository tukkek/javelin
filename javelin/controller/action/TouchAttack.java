package javelin.controller.action;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurn;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * An attack that reaches out only 5 feet, like from the Digester or Shocker
 * Lizard.
 *
 * TODO add hit/miss sound
 *
 * @author alex
 */
public class TouchAttack extends Fire implements AiAction{
	/** Constructor. */
	public TouchAttack(){
		super("Touch attack","t",'t');
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(Combatant combatant,
			BattleState gameState){
		List<List<ChanceNode>> outcomes=new ArrayList<>();
		if(combatant.source.touch==null) return outcomes;
		ArrayList<Combatant> opponents=gameState.getcombatants();
		filtertargets(combatant,opponents,gameState);
		for(Combatant target:opponents)
			outcomes.add(touchattack(combatant,target,gameState));
		return outcomes;
	}

	private List<ChanceNode> touchattack(Combatant active,final Combatant target,
			BattleState s){
		s=s.clone();
		active=s.clone(active);
		active.ap+=.5;
		var attack=active.source.touch;
		var name=attack.name.toLowerCase();
		var rolltarget=predictchance(active,target,s);
		var miss=Action.bind(rolltarget/20f);
		List<ChanceNode> nodes=new ArrayList<>(3);
		nodes.add(new ChanceNode(s,miss,active+" misses "+name+"...",Delay.WAIT));
		var hit=1-miss;
		int damage=attack.damage[0]*attack.damage[1]/2;
		String action=active+" hits "+name+"!\n";
		float savechance=CastSpell.converttochance(attack.savedc-active.source.ref);
		nodes.add(registerdamage(s,action+target+" resists, is ",hit*savechance,
				target,damage/2,active));
		nodes.add(registerdamage(s,action+target+" is ",hit*(1-savechance),target,
				damage,active));
		return nodes;
	}

	ChanceNode registerdamage(BattleState gameState,String action,float chance,
			Combatant target,int damage,Combatant active){
		gameState=gameState.clone();
		target=gameState.clone(target);
		target.damage(damage,gameState,target.source.energyresistance);
		action+=action+target.getstatus()+".";
		var n=new ChanceNode(gameState,chance,action,Javelin.Delay.BLOCK);
		n.overlay=new AiOverlay(target);
		return n;
	}

	@Override
	protected void attack(Combatant combatant,Combatant targetCombatant,
			BattleState battleState){
		Action.outcome(touchattack(combatant,targetCombatant,battleState));
	}

	@Override
	protected void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		ArrayList<Combatant> opponents=s.blueteam.contains(combatant)?s.redteam
				:s.blueteam;
		for(Combatant target:new ArrayList<>(targets))
			if(!opponents.contains(target)
					||Math.abs(target.location[0]-combatant.location[0])>1
					||Math.abs(target.location[1]-combatant.location[1])>1)
				targets.remove(target);
	}

	@Override
	protected void checkhero(Combatant hero){
		if(hero.source.touch==null){
			Javelin.message(hero+" doesn't have a touch attack...",
					Javelin.Delay.WAIT);
			throw new RepeatTurn();
		}
	}

	@Override
	protected boolean checkengaged(BattleState state,Combatant c){
		return false;// engaged is fine
	}

	@Override
	protected int predictchance(Combatant c,Combatant target,BattleState s){
		return target.getac()-target.source.armor-c.source.getbab();
	}
}
