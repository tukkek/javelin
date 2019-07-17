package javelin.model.world.location.haunt.settlement;

import java.util.List;

import javelin.controller.terrain.Terrain;
import javelin.model.unit.Alignment;
import javelin.model.unit.Alignment.Ethics;
import javelin.model.unit.Alignment.Morals;

/**
 * @see Alignment#ischaotic()
 * @author alex
 */
public class GoodSettlement extends Settlement{
	static final List<Terrain> TERRAINS=List.of(Terrain.HILL,Terrain.PLAIN);

	/** Constructor. */
	public GoodSettlement(){
		super(new Alignment(Ethics.NEUTRAL,Morals.GOOD),TERRAINS);
	}
}
