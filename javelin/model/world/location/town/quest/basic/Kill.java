package javelin.model.world.location.town.quest.basic;

import java.util.stream.Collectors;

import javelin.model.world.World;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.quest.Quest;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Kill quest: capture hostile {@link Location}.
 *
 * @see Location#ishostile()
 *
 * @author alex
 */
public class Kill extends Quest{
	Fortification target;

	/** Reflection-friendly constructor. */
	public Kill(Town t){
		super(t);
		var radius=t.getdistrict().getradius();
		for(var i=1;i<=4&&target==null;i++){
			distance=radius*i;
			target=findtarget();
		}
	}

	Fortification findtarget(){
		var source=town.getlocation();
		var fortifications=World.getactors().stream()
				.filter(a->source.distanceinsteps(a.getlocation())<=distance)
				.filter(a->WorldScreen.see(a.getlocation()))
				.filter(a->a instanceof Fortification).map(a->(Fortification)a)
				.filter(f->f.ishostile()).filter(f->f.getel(null)!=null)
				.collect(Collectors.toList());
		if(fortifications.isEmpty()) return null;
		var appropriate=fortifications.stream().filter(f->f.getel(null)<=el)
				.collect(Collectors.toList());
		if(!appropriate.isEmpty()) return RPG.pick(appropriate);
		fortifications.sort((a,b)->Integer.compare(a.getel(null),b.getel(null)));
		return fortifications.get(0);
	}

	@Override
	public boolean validate(){
		return target!=null;
	}

	@Override
	protected void define(){
		super.define();
		el=Math.min(el,target.getel(null));
		WorldScreen.current.mappanel.tiles[target.x][target.y].discovered=true;
	}

	@Override
	protected String getname(){
		return "Capture "+target;
	}

	@Override
	protected boolean checkcomplete(){
		return !target.ishostile()||!World.getactors().contains(target);
	}
}
