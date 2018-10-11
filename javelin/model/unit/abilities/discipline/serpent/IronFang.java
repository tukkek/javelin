package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.attack.DamageChance;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.necromancy.Poison;
import javelin.model.unit.attack.Attack;
import javelin.old.RPG;

/**
 * http://www.d20pfsrd.com/path-of-war/disciplines-and-maneuvers/steel-serpent-maneuvers/#TOC-Iron-Fang
 *
 * TODO in theory this also adds +2DC to other "prana" maneuvers, not only
 * {@link Poison}.
 *
 * @author alex
 */
public class IronFang extends Strike{
	static final int BONUSDAMAGE=RPG.average(2,6);
	int originaldr;

	public IronFang(){
		super("Iron fang",2);
		ap=ActionCost.STANDARD;
	}

	@Override
	public void preattacks(Combatant current,Combatant target,Attack a,
			BattleState s){
		modifypoisondc(a,+2);
		final Monster m=target.source.clone();
		target.source=m;
		originaldr=m.dr;
		m.dr=0;
	}

	@Override
	public void postattacks(Combatant current,Combatant target,Attack a,
			BattleState s){
		target.source.dr+=originaldr;
		modifypoisondc(a,-2);
	}

	@Override
	public void prehit(Combatant active,Combatant target,Attack a,DamageChance dc,
			BattleState s){
		dc.damage+=BONUSDAMAGE;
	}

	void modifypoisondc(Attack a,int dcbonus){
		Spell effect=a.geteffect();
		if(effect instanceof Poison){
			Poison p=(Poison)effect;
			p.dcbonus+=dcbonus;
		}
	}

	@Override
	public void posthit(Combatant attacker,Combatant target,Attack a,
			DamageChance dc,BattleState s){}
}
