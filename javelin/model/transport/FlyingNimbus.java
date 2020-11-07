package javelin.model.transport;

import javelin.controller.map.terrain.water.DeepWaters;
import javelin.controller.terrain.Terrain;
import javelin.model.item.artifact.Flute;
import javelin.model.unit.Squad;
import javelin.model.world.Period;

/**
 * Vehicle like the {@link Airship} but 10x faster, lasts for a day but will not
 * disappear over {@link DeepWaters}. Restores previous vehicle when done (is
 * also taken with the cloud).
 *
 * @see Flute
 * @author alex
 */
public class FlyingNimbus extends Airship{
	Transport oldtransport;
	long expireat;

	/**
	 * Constructor.
	 *
	 * @param oldtransportp
	 */
	public FlyingNimbus(Transport oldtransportp){
		name="Flying nimbus";
		speed*=10;
		price*=10;
		maintenance=0;
		parkeable=false;
		oldtransport=oldtransportp;
		expireat=Period.gettime()+24;
	}

	@Override
	public void keep(Squad s){
		if(oldtransport!=null){
			s.gold-=oldtransport.maintenance;
			if(s.gold<0) s.gold=0;
		}
		if(s.gettime()>=expireat&&!Terrain.get(s.x,s.y).equals(Terrain.WATER))
			Squad.active.transport=oldtransport;
	}
}
