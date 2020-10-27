package javelin.model.unit.feat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.factor.FeatsFactor;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.abilities.discipline.Maneuver;
import javelin.model.unit.abilities.discipline.Maneuvers;
import javelin.model.unit.skill.Skill;
import javelin.old.RPG;

/**
 * TODO how to allow player to select the maneuvers to learn?
 *
 * Javelin allows you to take as many feats as you want right up the bat so we
 * need to add some validation here for when the assumption you'll only get a
 * feat every x levels is false. However we don't want to limit this too much as
 * we have this count for 2 feats with each buy.
 *
 * @author alex
 */
public class MartialTraining extends Feat{
	/**
	 * At first will only implement up to lavel 4 TODO
	 */
	private static final int MAXLEVEL=4;

	private static final String[] RANK=new String[]{"Novice","Student","Teacher",
			"Master","Grandmaster","Legend",};

	/**
	 * Stored as a name to prevent circular dependency with {@link Discipline}.
	 *
	 * @see Discipline#ALL
	 */
	String discipline;
	int level=0;
	/**
	 * Number of {@link Maneuver}s that can be learned after upgrading this.
	 *
	 * @see Combatant#postupgrade(boolean, javelin.controller.upgrade.Upgrade)
	 * @see Combatant#postupgradeautomatic(boolean,
	 *      javelin.controller.upgrade.Upgrade)
	 */
	public int slots=2;

	public MartialTraining(Discipline d){
		super(d.name+" training");
		discipline=d.name;
	}

	@Override
	public boolean upgrade(Combatant c){
		c.source=c.source.clone();
		int i=c.source.feats.indexOf(this);
		MartialTraining mt=i==-1?this:(MartialTraining)c.source.feats.get(i);
		if(!mt.validate(c)) return false;
		if(i==-1) return super.upgrade(c);
		mt.slots+=2;
		return true;
	}

	boolean validate(Combatant c){
		Monster m=c.source;
		if(m.feats.count()+slots>=FeatsFactor.getnormalprogression(m)) return false;
		int nextlevel=level+slots/2;
		if(nextlevel>=MAXLEVEL) return false;
		int minimum=1+nextlevel*2;
		int bab=m.getbab();
		int knowledge=Skill.KNOWLEDGE.getranks(c);
		return bab>=minimum||knowledge>=minimum;
	}

	Discipline getdiscipline(){
		return Discipline.ALL.get(discipline);
	}

	ArrayList<Maneuver> gettrainable(Combatant c,int picks){
		var d=getdiscipline();
		Maneuvers trainable=d.getmaneuvers(level);
		Maneuvers known=c.disciplines.get(d);
		if(known!=null) trainable.removeAll(known);
		if(trainable.size()<picks){
			final String error="Not enough trainable maneuvers for discipline "+d
					+" at level "+level+"!";
			throw new RuntimeException(error);
		}
		return trainable;
	}

	@Override
	public void postupgrade(Combatant c){
		while(slots>0){
			level+=1;
			int pick=2;
			while(pick>0){
				ArrayList<Maneuver> trainable=gettrainable(c,pick);
				trainable.sort((a,b)->a.level-b.level);
				var d=getdiscipline();
				String prompt="You have advanced to being a "+getrank().toLowerCase()
						+" in the path of the "+d+"!\n"+"You can now select "+pick
						+" extra maneuver(s). What will you learn?";
				List<String> options=trainable.stream().map(m->m+" (level "+m.level+")")
						.collect(Collectors.toList());
				int choice=Javelin.choose(prompt,options,true,true);
				c.addmaneuver(d,trainable.get(choice));
				pick-=1;
				slots-=1;
			}
		}
	}

	@Override
	public void postupgradeautomatic(Combatant c){
		while(slots>0){
			level+=1;
			int pick=2;
			ArrayList<Maneuver> trainable=gettrainable(c,pick);
			int level=trainable.get(0).level;
			while(pick>0)
				if(pick(trainable,level,c)){
					slots-=1;
					pick-=1;
				}else
					level-=1;
		}
	}

	boolean pick(ArrayList<Maneuver> trainable,int level,Combatant c){
		Maneuvers tier=new Maneuvers();
		for(Maneuver m:trainable)
			if(m.level==level&&m.validate(c)) tier.add(m);
		if(tier.isEmpty()) return false;
		Maneuver m=RPG.pick(tier);
		boolean failed=!c.addmaneuver(getdiscipline(),m);
		if(Javelin.DEBUG&&failed){
			var error="Invalid maneuver "+m+" for "+c;
			throw new RuntimeException(error);
		}
		return true;
	}

	@Override
	public String toString(){
		final String rank=level==0?"":" ("+getrank().toLowerCase()+")";
		return name+rank;
	}

	/**
	 * @return A description the current training level.
	 * @see #RANK
	 */
	public String getrank(){
		return RANK[level-1];
	}

	@Override
	public Feat generate(String name2){
		throw new RuntimeException("Internal feat");
	}

	@Override
	public int count(){
		return level*2;
	}
}
