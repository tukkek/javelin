package javelin.controller.action.ai;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.Action;
import javelin.controller.action.ActionMapping;
import javelin.controller.action.Withdraw;
import javelin.controller.ai.BattleAi;
import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * This is a special {@link AiAction} that is not included in the normal
 * {@link ActionMapping#ACTIONS} catalog. This is only to allow the computer
 * player to flee from hopeless battles.
 *
 * The reason behind this implementation is that the {@link BattleAi} is
 * actually too good to be fun when combats are are almost-over. They'll just
 * run forever to prevent it from losing the game, which is a bore to micro
 * against and can take a long while.to resolve withoua dding any value to the
 * game itself.
 *
 * Fled creatures should not be counted towards XP and gold received but killed
 * ones should.
 *
 * @see Withdraw
 * @author alex
 */
public class Flee extends Action implements AiAction{
	public static final Action SINGLETON=new Flee();

	static final boolean ALLOWFLEE=true;
	static final int FLEEAT=Difficulty.VERYEASY;

	private Flee(){
		super("Flee");
	}

	@Override
	public boolean perform(Combatant active){
		throw new UnsupportedOperationException();
	}

	public static boolean flee(Combatant active,BattleState s){
		if(!ALLOWFLEE||!Javelin.app.fight.canflee||s.isengaged(active))
			return false;
		if(s.blueTeam.isEmpty()||s.redTeam.isEmpty()) return false;
		final int eldifference=ChallengeCalculator.calculateel(s.redTeam)
				-ChallengeCalculator.calculateel(s.blueTeam);
		return eldifference<=FLEEAT&&s.redTeam.contains(s.next);
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(Combatant active,BattleState s){
		ArrayList<List<ChanceNode>> outcomes=new ArrayList<>();
		s=s.clone();
		s.flee(active);
		ArrayList<ChanceNode> chances=new ArrayList<>(1);
		ChanceNode node=new ChanceNode(s,1,active+" flees!",Javelin.Delay.BLOCK);
		AiOverlay overlay=new AiOverlay(active.location[0],active.location[1]);
		overlay.image=AiMovement.MOVEOVERLAY;
		node.overlay=overlay;
		chances.add(node);
		outcomes.add(chances);
		return outcomes;
	}

}
