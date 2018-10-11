package javelin.controller.generator.encounter;

import java.util.ArrayList;

import javelin.model.unit.Combatant;

/**
 * Checks if a group of {@link Combatant}s is aligned enough to work as a team.
 *
 * @author alex
 */
public class MisalignmentDetector{
	boolean good=false;
	boolean evil=false;
	boolean lawful=false;
	boolean chaotic=false;

	/**
	 * @param foes All combatants that need to be compatible.
	 */
	public MisalignmentDetector(ArrayList<Combatant> foes){
		for(Combatant c:foes)
			register(c);
	}

	void register(Combatant c){
		if(c.source.good!=null) if(c.source.good)
			good=true;
		else
			evil=true;
		if(c.source.lawful!=null) if(c.source.lawful)
			lawful=true;
		else
			chaotic=true;
	}

	/**
	 * @return <code>false</code> if there are good and evil (or lawful and
	 *         chaotic) creatures coexisting on the given group.
	 */
	public boolean check(){
		if(good&&evil) return false;
		if(lawful&&chaotic) return false;
		return true;
	}

}
