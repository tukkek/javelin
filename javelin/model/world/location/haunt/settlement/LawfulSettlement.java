package javelin.model.world.location.haunt.settlement;

import java.util.List;

import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Alignment;
import javelin.model.unit.Alignment.Ethics;
import javelin.model.unit.Alignment.Morals;

/**
 * @see Alignment#islawful()
 * @author alex
 */
public class LawfulSettlement extends Settlement{
	static final List<Terrain> TERRAINS=List.of(Terrain.DESERT);

	/** Constructor. */
	public LawfulSettlement(){
		super(new Alignment(Ethics.LAWFUL,Morals.NEUTRAL),TERRAINS);
	}
}
