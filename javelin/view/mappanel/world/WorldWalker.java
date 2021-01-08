package javelin.view.mappanel.world;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.walker.overlay.OverlayStep;
import javelin.controller.walker.overlay.OverlayWalker;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

public class WorldWalker extends OverlayWalker{
	public class WorldStep extends OverlayStep{
		public float hours;
		public boolean safe;
		public float totalhours;

		WorldStep(Point p){
			super(p);
		}
	}

	HashSet<Point> safe=Town.getdistricts();
	boolean safeworld=BattleScreen.active instanceof WorldScreen
			&&!World.scenario.worldencounters;

	public WorldWalker(Point from,Point to){
		super(from,to);
	}

	@Override
	protected Point step(Point p,LinkedList<Point> previous){
		float totalhours=previous.isEmpty()?0
				:((WorldStep)previous.getLast()).totalhours;
		WorldStep step=new WorldStep(p);
		step.hours=Squad.active.move(false,Terrain.get(p.x,p.y),p.x,p.y);
		step.totalhours=step.hours+totalhours;
		step.safe=safe.contains(p);
		step.text=Math.round(step.totalhours)+"h";
		if(step.safe)
			step.color=Color.WHITE;
		else if(!safeworld&&step.totalhours>=WorldScreen.HOURSPERENCOUNTER/2.0)
			step.color=Color.RED;
		else
			step.color=Color.GREEN;
		return step;
	}

	@Override
	public boolean validate(Point step,LinkedList<Point> previous){
		WorldStep p=(WorldStep)step;
		if(!checkwater(p)) return false;
		int maxhours=Math.round(safeworld?24:WorldScreen.HOURSPERENCOUNTER);
		if(p.totalhours>maxhours) return false;
		if(p.equals(to)) return WorldPanel.ACTORS.get(p)==null||previous.isEmpty();
		return WorldPanel.ACTORS.get(p)==null
				&&WorldScreen.current.mappanel.tiles[p.x][p.y].discovered;
	}

	boolean checkwater(Point p){
		return !Terrain.get(p.x,p.y).equals(Terrain.WATER)||Squad.active.swim();
	}

	@Override
	public Point resetlocation(){ // TODO
		return new Point(Squad.active.x,Squad.active.y);
	}
}
