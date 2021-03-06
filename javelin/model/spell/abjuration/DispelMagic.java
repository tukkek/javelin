package javelin.model.spell.abjuration;

import java.util.ArrayList;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.condition.Condition;
import javelin.model.spell.Summon;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * http://www.d20srd.org/srd/spells/dispelMagicGreater.htm
 * 
 * @author alex
 */
public class DispelMagic extends Spell {
	/** Constructor. */
	public DispelMagic() {
		super("Greater dispel magic", 6, SpellsFactor.ratespelllikeability(6),
				Realm.MAGIC);
		isscroll = true;
		castoutofbattle = true;
		castinbattle = true;
		castonallies = true;
		isritual = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		if (target.summoned && casterlevel > new Summon(target.source.name,
				1).casterlevel) {
			s.remove(target);
			return target + " goes back to its plane of existence!";
		}
		ArrayList<Condition> dispelled = new ArrayList<Condition>();
		for (Condition c : target.getconditions()) {
			if (c.casterlevel != null && casterlevel > c.casterlevel) {
				c.dispel();
				target.removecondition(c);
				dispelled.add(c);
			}
		}
		return printconditions(dispelled);
	}

	/**
	 * @return A formatted message informing dispelled conditions, or a proper
	 *         message if given list is empty.
	 */
	static public String printconditions(ArrayList<Condition> dispelled) {
		if (dispelled.isEmpty()) {
			return "No conditions were dispelled...";
		}
		String result = "";
		for (Condition c : dispelled) {
			result += c.toString() + ", ";
		}
		return "The following conditions are dispelled: "
				+ result.substring(0, result.length() - 2) + "!";
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		return cast(caster, target, null, true);
	}
}
