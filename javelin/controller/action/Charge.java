package javelin.controller.action;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.ai.AiAction;
import javelin.controller.action.ai.AiMovement;
import javelin.controller.action.ai.attack.AttackResolver;
import javelin.controller.action.ai.attack.MeleeAttack;
import javelin.controller.ai.ChanceNode;
import javelin.controller.fight.Fight;
import javelin.controller.walker.Walker;
import javelin.controller.walker.state.ChargePath;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.condition.Charging;
import javelin.model.unit.condition.Fatigued;
import javelin.model.unit.feat.attack.BullRush;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.overlay.TargetOverlay;
import javelin.view.mappanel.overlay.Overlay;

/**
 * Charging is a special full-round action that allows you to move up to twice
 * your speed and attack. You must move at least 10 feet (2 squares) and may
 * move up to double your speed directly toward the designated opponent. You
 * must have a clear path toward the opponent. After moving, you may make a
 * single melee attack. You get a +2 bonus on the attack roll and take a -2
 * penalty to your AC until the start of your next turn.
 */
public class Charge extends Fire implements AiAction{
	class ChargeOverlay extends Overlay{
		Point target;

		public ChargeOverlay(List<Point> steps,Point target){
			this.target=target;
			affected.add(target);
			affected.addAll(steps);
		}

		@Override
		public void overlay(Tile t){
			Point p=new Point(t.x,t.y);
			if(p.equals(target))
				draw(t,TargetOverlay.TARGET);
			else if(affected.contains(p)) draw(t,AiMovement.MOVEOVERLAY);
		}
	}

	/** Constructor. */
	public Charge(){
		super("Charge","c",'c');
	}

	@Override
	protected void attack(final Combatant combatant,
			final Combatant targetCombatant,final BattleState s){
		Action.outcome(charge(Fight.state,combatant,targetCombatant).get(0));
	}

	ArrayList<List<ChanceNode>> charge(BattleState s,Combatant me,
			Combatant target){
		final ArrayList<List<ChanceNode>> chances=new ArrayList<>();
		if(me.source.melee.isEmpty()||me.hascondition(Fatigued.class)!=null)
			return chances;
		Point from=new Point(me.location[0],me.location[1]);
		s=s.clone();
		me=s.clone(me);
		target=s.clone(target);
		final List<Point> walk=walk(me,target,s);
		final Point destination=walk.get(walk.size()-1);
		if(s.getmeld(destination.x,destination.y)!=null) return chances;
		me.location[0]=destination.x;
		me.location[1]=destination.y;
		charge(me);
		var sequence=me.source.melee.get(0);
		var resolver=new AttackResolver(MeleeAttack.INSTANCE,me,target,
				sequence.get(0),s);
		resolver.attackbonus+=2;
		resolver.ap=1f;
		final List<ChanceNode> move=resolver.attack(me,target,s);
		final boolean bullrush=me.source.hasfeat(BullRush.SINGLETON);
		List<Point> steps=new ArrayList<>(walk.subList(0,walk.size()-1));
		steps.add(from);
		final Overlay o=new ChargeOverlay(steps,new Point(target));
		for(ChanceNode node:move){
			node.action=me+" charges!\n"+node.action;
			node.delay=Javelin.Delay.BLOCK;
			node.overlay=o;
			if(bullrush){
				final BattleState post=(BattleState)node.n;
				final Combatant posttarget=post.clone(target);
				if(posttarget!=null&&posttarget.hp<target.hp){
					final int pushx=Charge.push(me,posttarget,0);
					final int pushy=Charge.push(me,posttarget,1);
					if(!Charge.outoufbounds(post,pushx,pushy)
							&&!s.map[pushx][pushy].blocked&&s.getcombatant(pushx,pushy)==null
							&&s.getmeld(pushx,pushy)==null){
						posttarget.location[0]=pushx;
						posttarget.location[1]=pushy;
					}
				}
			}
		}
		chances.add(move);
		return chances;
	}

	//TODO inline
	void charge(Combatant me){
		me.addcondition(new Charging(me.ap+ActionCost.FULL,me));
	}

	static boolean outoufbounds(final BattleState s,final int x,final int y){
		return x<0||y<0||x>=s.map.length||y>=s.map[0].length;
	}

	static int push(final Combatant me,final Combatant target,final int i){
		return target.location[i]+target.location[i]-me.location[i];
	}

	@Override
	protected int predictchance(Combatant c,Combatant target,BattleState s){
		return target.getac()-(2+c.source.melee.get(0).get(0).bonus);
	}

	@Override
	protected void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		super.filtertargets(combatant,targets,s);
		for(Combatant target:new ArrayList<>(targets)){
			final List<Point> steps=walk(combatant,target,s);
			if(steps==null)
				targets.remove(target);
			else{
				final double distance=steps.size();
				if(distance<2||distance>2*combatant.gettopspeed(s)/5
						||distance>combatant.view(s.period))
					targets.remove(target);
			}
		}
	}

	List<Point> walk(final Combatant me,Combatant target,final BattleState state){
		final Walker walk=new ChargePath(new Point(me.location[0],me.location[1]),
				new Point(target.location[0],target.location[1]),state,
				me.source.swim()>0);
		List<Point> solution=walk.walk();
		if(solution==null) return null;
		final List<Point> threatened=solution;
		threatened.remove(threatened.size()-1);
		final ArrayList<Combatant> opponents=state.blueteam==state.getteam(me)
				?state.redteam
				:state.blueteam;
		for(Point s:threatened)
			for(Point p:Point.getadjacent2()){
				p.x+=s.x;
				p.y+=s.y;
				for(Combatant neighbor:opponents)
					if(neighbor!=target&&p.x==neighbor.location[0]
							&&p.y==neighbor.location[1])
						return null;
			}
		return solution;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(Combatant combatant,
			BattleState gameState){
		ArrayList<List<ChanceNode>> outcomes=new ArrayList<>();
		if(gameState.isengaged(combatant)) return outcomes;
		List<Combatant> targets=gameState.gettargets(combatant);
		filtertargets(combatant,targets,gameState);
		for(Combatant target:targets)
			outcomes.addAll(charge(gameState,combatant,target));
		return outcomes;
	}

	@Override
	protected void checkhero(Combatant hero){
		return;
	}
}
