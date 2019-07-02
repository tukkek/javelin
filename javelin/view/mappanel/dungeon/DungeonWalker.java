package javelin.view.mappanel.dungeon;

import java.awt.Color;
import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;
import javelin.controller.walker.overlay.OverlayStep;
import javelin.controller.walker.overlay.OverlayWalker;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.trap.MechanicalTrap;
import javelin.view.screen.BattleScreen;

public class DungeonWalker extends OverlayWalker{
	class DungeonStep extends OverlayStep{
		public float encounterchance;

		public DungeonStep(Point p){
			super(p);
		}
	}

	public DungeonWalker(Point from,Point to){
		super(from,to);
	}

	@Override
	protected Point step(Point p,LinkedList<Point> previous){
		DungeonStep step=new DungeonStep(p);
		float chance=previous.isEmpty()?0
				:((DungeonStep)previous.getLast()).encounterchance;
		chance+=1f/Dungeon.active.stepsperencounter;//
		step.encounterchance=chance;
		step.text=Math.round((chance>1?1:chance)*100)+"%";
		step.color=chance>=.5?Color.RED:Color.GREEN;
		return step;
	}

	@Override
	public boolean validate(Point step,LinkedList<Point> previous){
		DungeonStep p=(DungeonStep)step;
		if(Dungeon.active.map[p.x][p.y]==Template.WALL) return false;
		if(p.encounterchance>1) return false;
		if(p.equals(to)) return !Dungeon.active.squadlocation.equals(step);
		if(!BattleScreen.active.mappanel.tiles[p.x][p.y].discovered) return false;
		final Feature f=Dungeon.active.features.get(p.x, p.y);
		return f==null||f instanceof MechanicalTrap;
	}

	@Override
	public Point resetlocation(){ // TODO
		return Dungeon.active==null?null:Dungeon.active.squadlocation;
	}
}
