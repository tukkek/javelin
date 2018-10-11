package javelin.controller.upgrade;

import javelin.controller.ai.BattleAi;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.Feat;

/**
 * Adds a feat as an upgrade. It can be used by just giving a {@link Feat} to
 * the constructor but most feats usually need more setup, be it due to
 * prerequisites (other feats, ability scores, base attack bonus...),
 * modifications to be introduced during {@link #apply(Combatant)}, etc.
 *
 * Probably better to keep this separate from {@link Feat} to ensure minimum
 * weitght for {@link Monster}s when being processed by the {@link BattleAi}.
 *
 * @author alex
 */
public class FeatUpgrade extends Upgrade{
	/** Feat being upgraded. */
	public final Feat feat;
	Feat prerequisite=null;

	/**
	 * @param featp Builds this upgrade based on this feat.
	 */
	public FeatUpgrade(Feat featp){
		super(featp.name.substring(0,1).toUpperCase()+featp.name.substring(1));
		feat=featp;
		prerequisite=feat.prerequisite;
		usedincombat=feat.arena;
	}

	@Override
	public boolean apply(final Combatant c){
		return feat.upgrade(c);
	}

	@Override
	public String inform(Combatant c){
		return feat.inform(c);
	}
}