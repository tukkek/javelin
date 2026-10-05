package javelin.controller.content.upgrade;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more information.
 */
public class NaturalArmor extends Upgrade{
	/** +2 armor. */
	public static final NaturalArmor LEATHER=new NaturalArmor("leather",2);
	/** +4 armor. */
	public static final NaturalArmor SCALES=new NaturalArmor("scales",4);
	/** +8 armor. */
	public static final NaturalArmor PLATES=new NaturalArmor("plates",8);

	//	/** +14 armor. */
	//	public static final NaturalArmor CLAYGOLEM=new NaturalArmor("clay golem",14);
	//	/** +18 armor. */
	//	public static final NaturalArmor STONEGOLEM=new NaturalArmor("stone golem",
	//			18);
	//	/** +22 armor. */
	//	public static final NaturalArmor IRONGOLEM=new NaturalArmor("iron golem",22);

	final int target;

	/** Constructor. */
	public NaturalArmor(final String name,int target){
		super("Natural armor: "+name);
		this.target=target;
	}

	@Override
	public String inform(final Combatant c){
		return "Current armor class: "+c.source.getrawac();
	}

	@Override
	public boolean apply(final Combatant c){
		Monster m=c.source;
		int from=m.getrawac();
		int to=from+target-m.armor;
		if(target<=m.armor||to>from+10||to>m.dexterity+m.constitution) return false;
		m.armor=target;
		m.setrawac(to);
		return true;
	}
}