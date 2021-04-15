package javelin.model.unit.feat.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.skill.Skill;

public class Acrobatic extends Feat{
	public static final Feat SINGLETON=new Acrobatic();
	public static final int BONUS=+4;

	Acrobatic(){
		super("Acrobatic");
	}

	@Override
	public void read(Monster m){
		super.read(m);
		normalize(Skill.ACROBATICS,BONUS,m);
	}

	public static final void normalize(Skill s,int bonus,Monster m){
		int ranks=s.getranks(m);
		if(ranks>0) s.raise(-Math.min(ranks,bonus),m);
	}
}
