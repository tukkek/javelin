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
public class EvilSettlement extends Settlement{
	static final List<Terrain> TERRAINS=List.of(Terrain.MARSH);

	/** Constructor. */
	public EvilSettlement(){
		super(new Alignment(Ethics.NEUTRAL,Morals.EVIL),TERRAINS);
	}
}
