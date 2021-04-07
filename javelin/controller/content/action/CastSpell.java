package javelin.controller.content.action;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.Audio;
import javelin.controller.ai.ActionProvider;
import javelin.controller.ai.AiThread;
import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.ai.AiAction;
import javelin.controller.content.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.old.messagepanel.MessagePanel;

/**
 * Spells with attack rolls are supposed to have critical hits too but for the
 * sake of AI speed this rule is ignored.
 *
 * @author alex
 */
public class CastSpell extends Fire implements AiAction{
	/** Only instance of CastSpell to exist. */
	public static final CastSpell SINGLETON=new CastSpell();
	/** Spell for {@link Fire} to perform. */
	public Spell casting;

	CastSpell(){
		super("Cast spells","s",'s');
	}

	@Override
	public boolean perform(Combatant c){
		MessagePanel.active.clear();
		casting=null;
		List<Spell> castable=c.spells.stream().filter(s->s.castinbattle)
				.collect(Collectors.toList());
		if(castable.isEmpty()){
			Javelin.message(c+" doesn't know any battle spells...",Delay.WAIT);
			return false;
		}
		castable=castable.stream().filter(s->s.canbecast(c))
				.collect(Collectors.toList());
		if(castable.isEmpty()){
			Javelin.message("All battle spells are spent...",Delay.WAIT);
			return false;
		}
		if(Fight.state.isengaged(c)){
			castable=castable.stream().filter(s->!s.provokeaoo||c.concentrate(s))
					.collect(Collectors.toList());
			if(castable.isEmpty()){
				String message="Not enough Concentration to cast any of your current spells while engaged...";
				Javelin.prompt(message+"\nPress any key to continue...");
				return false;
			}
		}
		castable.sort((o1,o2)->o1.name.compareTo(o2.name));
		int i=Javelin.choose("Choose a spell:",castable,castable.size()>4,false);
		if(i==-1) return false;
		return cast(castable.get(i),c);
	}

	/**
	 * Like {@link #perform(Combatant, BattleMap, Thing)} except skips the
	 * selection UI step.
	 */
	public boolean cast(Spell spell,Combatant c){
		casting=spell;
		return super.perform(c);
	}

	@Override
	protected void attack(Combatant combatant,Combatant targetCombatant,
			BattleState s){
		Action.outcome(
				cast(combatant,targetCombatant,combatant.spells.indexOf(casting),s));
	}

	List<ChanceNode> cast(Combatant caster,Combatant target,int spellindex,
			BattleState state){
		state=state.clone();
		caster=state.clone(caster);
		caster.clonesource();
		target=state.cloneifdifferent(target,caster);
		if(target!=caster) target.clonesource();
		Spell spell=null;
		if(casting==null)
			spell=caster.spells.get(spellindex);
		else{
			int i=caster.spells.indexOf(casting);
			spell=i>=0?caster.spells.get(i):casting;
		}
		caster.ap+=spell.apcost;
		spell.used+=1;
		final List<ChanceNode> chances=new ArrayList<>();
		final String prefix=caster+" casts "+spell.name.toLowerCase()+"!\n";
		final int touchtarget=spell.hit(caster,target,state);
		float misschance;
		if(touchtarget==Integer.MIN_VALUE)
			misschance=0;
		else{
			misschance=bind(touchtarget/20f);
			chances.add(new ChanceNode(state,misschance,
					prefix+caster+" misses touch attack.",Javelin.Delay.BLOCK));
		}
		final float hitc=1-misschance;
		final float affectchance=affect(caster,target,state,spell,chances,prefix,
				hitc);
		final float savec=spell.getsavechance(caster,target);
		if(savec!=0) chances
				.add(hit(caster,target,state,spell,savec*affectchance,true,prefix));
		if(savec!=1) chances.add(
				hit(caster,target,state,spell,(1-savec)*affectchance,false,prefix));
		if(Javelin.DEBUG) ActionProvider.validate(chances);
		var audio=new Audio("cast",caster.source);
		for(var chance:chances)
			chance.audio=audio;
		return chances;
	}

	static float affect(Combatant caster,Combatant target,BattleState state,
			final Spell spell,final List<ChanceNode> chances,final String prefix,
			float hitchance){
		if(spell.castonallies||target.source.sr==0||caster.equals(target))
			return hitchance;
		final float resistchance=bind((target.source.sr-spell.casterlevel)/20f)
				*hitchance;
		chances.add(new ChanceNode(state,resistchance,
				prefix+target+" resists spell!",Javelin.Delay.BLOCK));
		return hitchance-resistchance;
	}

	/**
	 * @param target A d20 roll target such as the one provided by
	 *          {@link Spell#getsavetarget(int, Combatant)}.
	 * @return A number between 0 and 1: 0% if target is
	 *         {@link Integer#MAX_VALUE}, 100% if target is
	 *         {@link Integer#MIN_VALUE}, or a number between 5% to 95% - higher
	 *         meaning a more likely chance to succeed with the save.
	 *
	 * @see #bind(float)
	 */
	public static float converttochance(final int target){
		if(target==Integer.MIN_VALUE) return 1;
		if(target==Integer.MAX_VALUE) return 0;
		return 1-bind(target/20f);
	}

	static ChanceNode hit(Combatant active,Combatant target,BattleState state,
			final Spell spell,final float chance,final boolean saved,String prefix){
		state=state.clone();
		active=state.clone(active);
		target=state.cloneifdifferent(target,active);
		ChanceNode cn=new ChanceNode(state,chance,null,Javelin.Delay.BLOCK);
		String message=spell.cast(active,target,saved,state,cn);
		if(message==null||message.isEmpty())
			prefix=prefix.substring(0,prefix.length()-1);
		cn.action=prefix+message;
		return cn;
	}

	@Override
	protected void checkhero(Combatant hero){
		// no check
	}

	@Override
	protected int predictchance(Combatant active,Combatant target,BattleState s){
		return casting.hit(active,target,s);
	}

	@Override
	protected void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		casting.filtertargets(combatant,targets,s);
	}

	@Override
	protected boolean checkengaged(BattleState state,Combatant c){
		return casting.provokeaoo&&!c.concentrate(casting)&&state.isengaged(c);
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final Combatant active,
			final BattleState gameState){
		casting=null;
		final ArrayList<List<ChanceNode>> chances=new ArrayList<>();
		boolean engaged=gameState.isengaged(active);
		final ArrayList<Spell> spells=active.spells;
		for(int i=0;i<spells.size();i++){
			final Spell s=spells.get(i);
			if(s.provokeaoo&&!active.concentrate(s)&&engaged) continue;
			if(!s.castinbattle||!s.canbecast(active)) continue;
			final List<Combatant> targets=gameState.gettargets(active,
					gameState.getcombatants());
			s.filtertargets(active,targets,gameState);
			for(Combatant target:targets)
				chances.add(cast(active,target,i,gameState));
			AiThread.checkinterrupted();
		}
		return chances;
	}
}
