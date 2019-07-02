package javelin.model.unit.abilities.spell.conjuration.healing;

import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.InfoScreen;

/**
 * Also features "restoration", implicitly. See the d20 SRD for more info.
 */
public class RaiseDead extends Spell{
	protected static final float RESTORATIONCR=ChallengeCalculator.ratespell(4);

	/** Constructor. */
	public RaiseDead(){
		super("Raise dead",5,ChallengeCalculator.ratespell(5)+RESTORATIONCR);
		components=5000;
		castinbattle=false;
	}

	public RaiseDead(String name,int levelp,float incrementcost){
		super(name,levelp,incrementcost);
	}

	@Override
	public boolean validate(Combatant caster,Combatant target){
		MessagePanel.active.clear();
		String prompt="Revive "+target+"?\n"
				+"Press y to confirm or n to let go of this unit.";
		Javelin.message(prompt,Javelin.Delay.NONE);
		while(true){
			final Character feedback=InfoScreen.feedback();
			if(feedback=='y') return true;
			if(feedback=='n') return false;
		}
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target,
			List<Combatant> squad){
		target.hp=target.source.hd.count();
		return null;
	}

}
