package javelin.controller.challenge.factor;

import javelin.model.unit.Monster;

/**
 * @see HdFactor#gettypedata(Monster)
 * @author alex
 */
public class TypeData {
	/** Challenge rating per HD. */
	public float cr;
	/** How many skill points per HD. */
	public int skillprogression;

	/** Constructor. */
	public TypeData(float cr, int skillprogression) {
		this.cr = cr;
		this.skillprogression = skillprogression;
	}
}