package javelin.controller.content.action.maneuver;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.Action;
import javelin.controller.content.action.ai.AiAction;
import javelin.controller.content.action.ai.attack.AttackResolver;
import javelin.controller.content.action.ai.attack.MeleeAttack;
import javelin.controller.content.action.target.Target;
import javelin.controller.exception.RepeatTurn;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Size;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.feat.Feat;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * This greatly helps feature maneuvers in Javelin - it was adopted with some
 * modifications.
 *
 * http://www.dnd-wiki.org/wiki/Simplified_Special_Attacks_%283.
 * 5e_Variant_Rule%29#Trip
 *
 * For the human version of these see {@link ExecuteManeuver}.
 *
 * TODO this will probably be better handled in a spell-like fashion, together
 * with martial disciplines
 *
 * @author alex
 */
public abstract class ExpertiseAction extends Target implements AiAction{
	private final Feat prerequisite;
	private int featbonus;

	public ExpertiseAction(String name,String key,Feat prerequisite,
			int featbonus){
		super(name);
		this.prerequisite=prerequisite;
		this.featbonus=featbonus;
		confirmkey='m';
	}

	@Override
	protected boolean checkengaged(BattleState state,Combatant c){
		return false;// engaged is fine
	}

	@Override
	protected void attack(Combatant combatant,Combatant targetCombatant,
			BattleState battleState){
		Action.outcome(maneuver(combatant,targetCombatant,battleState));
	}

	@Override
	protected void checkhero(Combatant hero){
		if(!hero.source.hasfeat(prerequisite)){
			Javelin.message("Needs the "+prerequisite+" feat...",Javelin.Delay.WAIT);
			throw new RepeatTurn();
		}
	}

	@Override
	protected void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		super.filtertargets(combatant,targets,s);
		for(final Combatant target:new ArrayList<>(targets)){
			if(target.source.passive){
				targets.remove(target);
				continue;
			}
			final int deltax=combatant.location[0]-target.location[0];
			final int deltay=combatant.location[1]-target.location[1];
			if(-1<=deltax&&deltax<=+1&& //
					-1<=deltay&&deltay<=+1&& //
					validatetarget(target))
				continue;
			targets.remove(target);
		}
	}

	/**
	 * TODO now that {@link Condition#stacks} is implemented should adding the
	 * same maneuver condition be allowed? the effect would be to transparently
	 * extend {@link Condition#expireat}.
	 */
	abstract boolean validatetarget(Combatant target);

	@Override
	public List<List<ChanceNode>> getoutcomes(final Combatant combatant,
			final BattleState gameState){
		final ArrayList<List<ChanceNode>> outcomes=new ArrayList<>();
		if(!combatant.source.hasfeat(prerequisite)) return outcomes;
		final ArrayList<Combatant> targets=gameState.getcombatants();
		filtertargets(combatant,targets,gameState);
		for(final Combatant target:targets)
			outcomes.add(maneuver(combatant,target,gameState));
		return outcomes;
	}

	float getfailchance(Combatant c,Combatant target,BattleState s){
		var savechance=calculatesavechance(c,calculatesavebonus(target));
		var misschance=getmisschance(c,target,s,0);
		return savechance+(1-savechance)*misschance;
	}

	List<ChanceNode> maneuver(Combatant combatant,Combatant target,BattleState s){
		s=s.clone();
		combatant=s.clone(combatant);
		target=s.clone(target);
		combatant.ap+=.5f;
		var failurechance=getfailchance(combatant,target,s);
		var chances=new ArrayList<ChanceNode>();
		chances.add(mark(miss(combatant,target,s,failurechance),target));
		chances.add(mark(hit(combatant,target,s,1-failurechance),target));
		return chances;
	}

	ChanceNode mark(ChanceNode hit,Combatant target){
		hit.overlay=new AiOverlay(target.location[0],target.location[1]);
		return hit;
	}

	public float calculatesavechance(Combatant c,final int bonus){
		var dc=10+c.source.getbab()/2+getattackerbonus(c)+size(c)+featbonus;
		return calculatesavechance(dc,bonus);
	}

	public int calculatesavebonus(Combatant target){
		return getsavebonus(target)-size(target);
	}

	abstract ChanceNode miss(Combatant combatant,Combatant target,
			BattleState battleState,float chance);

	abstract ChanceNode hit(Combatant combatant,Combatant targetCombatant,
			BattleState battleState,float chance);

	public float calculatesavechance(final int dc,final int bonus){
		return Action.bind(1-(dc-bonus)/20f);
	}

	float getmisschance(Combatant c,Combatant target,BattleState s,int bonus){
		var attack=c.source.melee.get(0).get(0);
		var r=new AttackResolver(MeleeAttack.INSTANCE,c,target,attack,s);
		var dexbonus=Monster.getbonus(target.source.dexterity);
		if(dexbonus>0) bonus+=dexbonus;
		r.attackbonus+=bonus;
		r.preview(target);
		return r.misschance;
	}

	static int size(final Combatant combatant){
		return combatant.source.size-Size.MEDIUM;
	}

	abstract int getsavebonus(Combatant targetCombatant);

	abstract int getattackerbonus(Combatant combatant);

	@Override
	protected int predictchance(Combatant c,Combatant target,BattleState s){
		return Math.round(20*getfailchance(c,target,s));
	}
}
