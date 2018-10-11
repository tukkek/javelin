package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.WorldScreen;

/**
 * Cast {@link Spell}s out of battle.
 *
 * @see Spell#castoutofbattle
 * @author alex
 */
public class CastSpells extends WorldAction{

	/** Constructor. */
	public CastSpells(){
		super("Cast spells",new int[0],new String[]{"s"});
	}

	@Override
	public void perform(WorldScreen screen){
		ArrayList<Combatant> casters=filtercasters();
		if(casters.isEmpty()){
			MessagePanel.active.clear();
			Javelin.message("No peaceful spells to cast right now...",
					Javelin.Delay.WAIT);
			return;
		}
		int choice=Javelin.choose("Who?",casters,false,false);
		if(choice==-1){
			MessagePanel.active.clear();
			return;
		}
		Combatant caster=casters.get(choice);
		List<Spell> spells=new ArrayList<>(caster.spells);
		Spell s=selectspell(spells);
		if(s==null){
			MessagePanel.active.clear();
			return;
		}
		Combatant target=null;
		if(s.castonallies){
			int targetindex=selecttarget();
			if(targetindex<0){
				MessagePanel.active.clear();
				return;
			}
			target=Squad.active.members.get(targetindex);
		}
		if(!s.validate(caster,target)){
			Javelin.message("Can't cast this spell right now.",Javelin.Delay.BLOCK);
			return;
		}
		String message=s.castpeacefully(caster,target,Squad.active.members);
		if(message!=null){
			Javelin.app.switchScreen(screen);
			screen.center();
			Javelin.message(message,false);
		}
		s.used+=1;
	}

	int selecttarget(){
		List<String> targets=new ArrayList<>();
		for(Combatant m:Squad.active.members)
			targets.add(m.source.toString());
		int targetindex=Javelin.choose("Cast on...",targets,targets.size()>4,false);
		return targetindex;
	}

	private Spell selectspell(List<Spell> spells){
		ArrayList<String> spellnames=listspells(spells);
		if(spellnames.size()==0){
			MessagePanel.active.clear();
			Javelin.message("All spells already cast! Rest to regain them.",
					Javelin.Delay.BLOCK);
			return null;
		}
		int input=Javelin.choose("Which spell?",spellnames,false,false);
		if(input==-1) return null;
		String name=spellnames.get(input);
		for(Spell s:spells)
			if(s.toString().equals(name)) return s;
		throw new RuntimeException("Should have caught spell name");
	}

	ArrayList<Combatant> filtercasters(){
		ArrayList<Combatant> casters=new ArrayList<>(Squad.active.members);
		for(Combatant m:new ArrayList<>(casters))
			if(listspells(new ArrayList<>(m.spells)).size()==0) casters.remove(m);
		return casters;
	}

	ArrayList<String> listspells(List<Spell> spells){
		ArrayList<String> spellnames=new ArrayList<>();
		for(Spell s:spells)
			if(!s.exhausted()&&s.castoutofbattle) spellnames.add(s.toString());
		return spellnames;
	}
}
