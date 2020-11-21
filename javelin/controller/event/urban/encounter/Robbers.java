package javelin.controller.event.urban.encounter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.fight.Fight;
import javelin.controller.fight.mutator.Mutator;
import javelin.controller.kit.Barbarian;
import javelin.controller.kit.Rogue;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Alignment.Ethics;
import javelin.model.unit.Alignment.Morals;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Period;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * A {@link Squad} in {@link Town} is approach by a group of robbers.
 *
 * @author alex
 */
public class Robbers extends UrbanEncounter{
	static final Set<Upgrade> UPGRADES=new HashSet<>();

	static{
		UPGRADES.addAll(Barbarian.INSTANCE.getupgrades());
		UPGRADES.addAll(Rogue.INSTANCE.getupgrades());
	}

	/** Reflection constructor. */
	public Robbers(Town t){
		super(t,List.of(Trait.CRIMINAL),Rank.VILLAGE,UPGRADES,
				"A band of robbers approaches a squad in "+t+".");
		bribe="Give them cash";
		fight="Defend yourself";
		surrender=null;
	}

	@Override
	protected boolean validate(Monster foe){
		if(Period.MORNING.is()||Period.AFTERNOON.is()) return false;
		var a=foe.alignment;
		return a.ethics!=Ethics.LAWFUL&&a.morals!=Morals.GOOD&&foe.cr<=5
				&&super.validate(foe);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return el>=squadel&&super.validate(s,squadel);
	}

	@Override
	protected void surrender(){
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean rollskill(Squad s,int dc,String message){
		var perceive=s.getbest(Skill.PERCEPTION).roll(Skill.PERCEPTION);
		var stealth=s.getworst(Skill.STEALTH).roll(Skill.STEALTH);
		if(perceive>=dc&&stealth>=dc){
			notify(message
					+"\nYou are, however, able to hide until they decide to chase another target...");
			return true;
		}
		return false;
	}

	@Override
	protected EventFight fight(Squad s,Combatants foes){
		var f=super.fight(s,foes);
		if(town.traits.contains(Trait.MILITARY)&&RPG.r(1,20)<=town.population){
			var rank=town.getrank().toString().toLowerCase();
			notify("A group of "+rank+" guards comes to your aid!");
			var guards=new Guards(town).generatefoes();
			for(var g:guards)
				g.setmercenary(true);
			f.mutators.add(new Mutator(){
				@Override
				public void prepare(Fight f){
					super.prepare(f);
					Fight.state.blueTeam.addAll(guards);
				}
			});
		}
		return f;
	}
}
