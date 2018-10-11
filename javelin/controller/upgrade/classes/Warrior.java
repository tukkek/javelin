package javelin.controller.upgrade.classes;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see ClassLevelUpgrade
 */
public class Warrior extends ClassLevelUpgrade{
	private static final Level[] TABLE=new Level[]{new Level(0,0,0),
			new Level(2,0,0),new Level(3,0,0),new Level(3,1,1),new Level(4,1,1),
			new Level(4,1,1),new Level(5,2,2),new Level(5,2,2),new Level(6,2,2),
			new Level(6,3,3),new Level(7,3,3),new Level(7,3,3),new Level(8,4,4),
			new Level(8,4,4),new Level(9,4,4),new Level(9,5,5),new Level(10,5,5),
			new Level(10,5,5),new Level(11,6,6),new Level(11,6,6),new Level(12,6,6),};
	public static final ClassLevelUpgrade SINGLETON=new Warrior();

	private Warrior(){
		super("Warrior",1f,TABLE,8,2,.7f);
	}

	@Override
	protected void setlevel(int level,Monster m){
		m.warrior=level;
	}

	@Override
	public int getlevel(Monster m){
		return m.warrior;
	}

	@Override
	protected boolean prefer(Combatant c){
		Monster m=c.source;
		int highest=gethighestability(m);
		return highest==m.strength||highest==m.constitution;
	}
}
