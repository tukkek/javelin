package javelin.model.world.location.town.quest;

import javelin.model.world.location.town.Town;
import javelin.view.screen.WorldScreen;

/**
 * Liberate a hostile {@link Town}.
 *
 * @see Town#ishostile()
 *
 * @author alex
 */
public class War extends Quest{
	Town target=null;

	/** Reflection constructor. */
	public War(Town town){
		super(town);
		var source=town.getlocation();
		var targets=Town.gettowns().stream()
				.filter(t->t.ishostile()&&WorldScreen.see(t.getlocation())
						&&t.population<=town.population)
				.sorted((a,b)->Double.compare(source.distance(a.getlocation()),
						source.distance(b.getlocation())));
		target=targets.findFirst().orElse(null);
	}

	@Override
	public boolean validate(){
		return target!=null;
	}

	@Override
	protected String getname(){
		return "Liberate: "+target;
	}

	@Override
	public boolean complete(){
		return !target.ishostile();
	}
}
