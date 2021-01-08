package javelin.model.world.location.haunt.settlement;

import java.util.List;

import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Alignment;
import javelin.model.unit.Alignment.Ethics;
import javelin.model.unit.Alignment.Morals;

/**
 * @see Alignment#ischaotic()
 * @author alex
 */
public class ChaoticSettlement extends Settlement{
	static final List<Terrain> TERRAINS=List.of(Terrain.MOUNTAINS);

	/** Constructor. */
	public ChaoticSettlement(){
		super(new Alignment(Ethics.CHAOTIC,Morals.NEUTRAL),TERRAINS);
	}
}
