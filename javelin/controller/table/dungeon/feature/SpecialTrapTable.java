package javelin.controller.table.dungeon.feature;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.feature.trap.Trap;

/**
 * {@link #rollboolean()} returns <code>true</code> for a special {@link Trap}.
 *
 * @author alex
 * @see Trap#generate(int, boolean, javelin.controller.Point)
 */
public class SpecialTrapTable extends Table{
	/** Constructor. */
	public SpecialTrapTable(){
		add(true,1);
		add(false,4-1,10-1);
	}
}
