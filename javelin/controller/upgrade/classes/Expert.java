package javelin.controller.upgrade.classes;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see ClassLevelUpgrade
 */
public class Expert extends ClassLevelUpgrade{
	private static final Level[] TABLE=new Level[]{new Level(0,0,0),
			new Level(0,0,2),new Level(0,0,3),new Level(1,1,3),new Level(1,1,4),
			new Level(1,1,4),new Level(2,2,5),new Level(2,2,5),new Level(2,2,6),
			new Level(3,3,6),new Level(3,3,7),new Level(3,3,7),new Level(4,4,8),
			new Level(4,4,8),new Level(4,4,9),new Level(5,5,9),new Level(5,5,10),
			new Level(05,5,10),new Level(6,6,11),new Level(6,6,11),
			new Level(6,6,12),};
	/** Singleton instance. */
	public static final ClassLevelUpgrade SINGLETON=new Expert();

	Expert(){
		super("Expert",.72f,TABLE,6,6,.65f);
	}

	@Override
	protected void setlevel(int level,Monster m){
		m.expert=level;
	}

	@Override
	public int getlevel(Monster m){
		return m.expert;
	}

	@Override
	public float advancebab(int next){
		return next==1?0:super.advancebab(next);
	}

	@Override
	protected boolean prefer(Combatant c){
		Monster m=c.source;
		int highest=gethighestability(m);
		return highest==m.dexterity||highest==m.intelligence;
	}
}
