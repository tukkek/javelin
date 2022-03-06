package javelin.controller.generator.encounter;

import java.util.List;

import javelin.model.unit.Alignment.Ethics;
import javelin.model.unit.Alignment.Morals;
import javelin.model.unit.Combatant;

/**
 * Checks if a group of {@link Combatant}s is aligned enough to work as a team.
 *
 * @author alex
 */
public class AlignmentDetector{
	public boolean good=false;
	public boolean evil=false;
	public boolean lawful=false;
	public boolean chaotic=false;

	/**
	 * @param foes All combatants that need to be compatible.
	 */
	public AlignmentDetector(List<Combatant> foes){
		for(Combatant c:foes)
			register(c);
	}

	void register(Combatant c){
		var a=c.source.alignment;
		if(a.morals!=Morals.NEUTRAL) if(a.isgood())
			good=true;
		else
			evil=true;
		if(a.ethics!=Ethics.NEUTRAL) if(a.islawful())
			lawful=true;
		else
			chaotic=true;
	}

	/**
	 * @return <code>false</code> if there are good and evil (or lawful and
	 *         chaotic) creatures coexisting on the given group. <code>true</code>
	 *         if all alignments match.
	 */
	public boolean check(){
		if(good&&evil) return false;
		if(lawful&&chaotic) return false;
		return true;
	}
}
