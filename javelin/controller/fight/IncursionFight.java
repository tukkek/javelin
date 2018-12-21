package javelin.controller.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.model.unit.Combatant;
import javelin.model.world.Incursion;

/**
 * @see Incursion
 * @author alex
 */
public class IncursionFight extends Fight{
	/** Incursion being fought. */
	public final Incursion incursion;

	/** Constructor. */
	public IncursionFight(final Incursion incursion){
		this.incursion=incursion;
		meld=true;
		hide=false;
		canflee=true;
	}

	@Override
	public Integer getel(final int teamel){
		return incursion.getel();
	}

	@Override
	public ArrayList<Combatant> getfoes(Integer teamel){
		return clone(incursion.squad);
	}

	@Override
	public void bribe(){
		incursion.remove();
	}

	@Override
	public boolean onend(){
		super.onend();
		if(Fight.victory)
			incursion.remove();
		else
			for(Combatant incursant:new ArrayList<>(incursion.squad)){
				Combatant alive=null;
				for(Combatant inbattle:Fight.state.getcombatants())
					if(inbattle.id==incursant.id){
						alive=inbattle;
						break;
					}
				if(alive==null) incursion.squad.remove(incursant);
			}
		return true;
	}

	@Override
	public ArrayList<Combatant> generate(){
		ArrayList<Combatant> foes=super.generate();
		incursion.squad=clone(foes);
		return foes;
	}

	/**
	 * @param from Clones the {@link Combatant}s here into...
	 * @return a new list.
	 * @see Combatant#clone()
	 * @see Combatant#clonesource()
	 */
	static ArrayList<Combatant> clone(List<Combatant> from){
		int size=from.size();
		ArrayList<Combatant> to=new ArrayList<>(size);
		for(int i=0;i<size;i++)
			to.add(from.get(i).clone().clonesource());
		return to;
	}
}