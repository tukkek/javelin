package javelin.model.world.location.town.labor.productive;

import java.util.ArrayList;
import java.util.Collections;

import javelin.controller.Point;
import javelin.controller.terrain.Terrain;
import javelin.model.world.World;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.basic.Growth;
import javelin.old.RPG;

/**
 * Converts a forest tile in the district into 50% extra labor (automatically
 * adds a Growth project if there isn't another project to benefit from this).
 *
 * @author alex
 */
public class Deforestate extends Labor{
	public Deforestate(){
		super("Deforestate",7,Rank.HAMLET);
	}

	@Override
	protected void define(){
		// nothing
	}

	@Override
	public void done(){
		Point forest=getforest(town.getdistrict());
		if(forest==null) return;
		World w=World.getseed();
		int elevatedneighbors=Terrain.search(forest,Terrain.MOUNTAINS,1,w);
		Terrain newterrain=Terrain.PLAIN;
		if(elevatedneighbors>0){ // at least one mountain nearby
			elevatedneighbors+=Terrain.search(forest,Terrain.HILL,1,w);
			if(RPG.r(1,8)<=elevatedneighbors) newterrain=Terrain.HILL;
		}
		w.map[forest.x][forest.y]=newterrain;
	}

	@Override
	public void work(float step){
		float work=Math.min(cost-progress,step)*1.5f;
		ArrayList<Labor> projects=town.getgovernor().getprojects();
		if(projects.size()==1){ // only this labor project
			Labor growth=new Growth().generate(town);
			if(growth.validate(town.getdistrict())){
				growth.start();
				growth.work(work);
			}
		}else{
			work=work/(projects.size()-1);
			for(Labor l:new ArrayList<>(projects))
				if(l!=this) l.work(work);
		}
		super.work(step);
	}

	@Override
	public boolean validate(District d){
		return super.validate(d)&&getforest(d)!=null;
	}

	Point getforest(District d){
		ArrayList<Point> area=new ArrayList<>(d.getarea());
		Collections.shuffle(area);
		for(Point p:area)
			if(Terrain.get(p.x,p.y).equals(Terrain.FOREST)) return p;
		return null;
	}

}
