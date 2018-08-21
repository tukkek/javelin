package javelin.view.mappanel.world;

import java.awt.event.MouseEvent;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.old.Interface;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.MoveOverlay;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.DungeonScreen;
import javelin.view.screen.WorldScreen;

/**
 * Handles mouse events for {@link WorldScreen}.
 *
 * @author alex
 */
public class WorldMouse extends Mouse{
	static class Movement implements Runnable{
		private final MoveOverlay overlay;

		public Movement(MoveOverlay overlay){
			this.overlay=overlay;
		}

		@Override
		public void run(){
			Point interrupted=null;
			for(Point p:overlay.affected)
				if(!WorldMove.move(p.x,p.y)){
					interrupted=overlay.path.resetlocation();
					break;
				}
			BattleScreen.active.mappanel.refresh();
			if(interrupted!=null){
				overlay.reset();
				overlay.path.from.x=interrupted.x;
				overlay.path.from.y=interrupted.y;
				overlay.walk();
				MapPanel.overlay=overlay;
			}
		}
	}

	class Interact implements Runnable{
		private final Actor target;

		public Interact(Actor target){
			this.target=target;
		}

		@Override
		public void run(){
			if(!target.isadjacent(Squad.active)){
				target.accessremotely();
				return;
			}
			Squad s=target instanceof Squad?(Squad)target:null;
			if(s!=null){
				s.join(Squad.active);
				return;
			}
			Location l=target instanceof Location?(Location)target:null;
			if(l!=null&&l.allowentry&&l.discard&&l.garrison.isEmpty())
				WorldMove.place(l.x,l.y);
			target.interact();
		}
	}

	static boolean processing=false;
	static Object LOCK=new Object();

	boolean showingdescription=false;
	long lastcall=-Integer.MIN_VALUE;

	/** Constructor. */
	public WorldMouse(MapPanel panel){
		super(panel);
	}

	@Override
	public void mouseClicked(MouseEvent e){
		if(overrideinput()||!Interface.userinterface.waiting) return;
		final WorldTile t=(WorldTile)gettile(e);
		if(!t.discovered) return;
		if(e.getButton()==MouseEvent.BUTTON1){
			final Actor target=World.get(t.x,t.y);
			if(target==Squad.active) return;
			if(target==null){
				if(move()) return;
			}else{
				BattleScreen.perform(new Interact(target));
				return;
			}
		}
		super.mouseClicked(e);
	}

	/**
	 * Handles movement for {@link WorldScreen} and {@link DungeonScreen}.
	 *
	 * @return <code>true</code> if moved the current {@link Squad}.
	 */
	public static boolean move(){
		final MoveOverlay overlay=(MoveOverlay)MapPanel.overlay;
		if(overlay==null||overlay.steps.isEmpty()) return false;
		BattleScreen.perform(new Movement(overlay));
		return true;
	}

	@Override
	public void mouseMoved(MouseEvent e){
		if(!Interface.userinterface.waiting) return;
		if(MapPanel.overlay!=null) MapPanel.overlay.clear();
		final WorldTile t=(WorldTile)gettile(e);
		if(!t.discovered) return;
		final Actor target=World.get(t.x,t.y);
		if(target==null){
			if(showingdescription){
				showingdescription=false;
				MessagePanel.active.clear();
				((WorldScreen)BattleScreen.active).updateplayerinformation();
				MessagePanel.active.repaint();
			}
			Point from=new Point(Squad.active.x,Squad.active.y);
			Point to=new Point(t.x,t.y);
			MoveOverlay.schedule(new MoveOverlay(new WorldWalker(from,to)));
		}else{
			MessagePanel.active.clear();
			Javelin.message(target.describe(),Javelin.Delay.NONE);
			MessagePanel.active.repaint();
			showingdescription=true;
			if(target instanceof Town){
				MapPanel.overlay=new DistrictOverlay((Town)target);
				MapPanel.overlay.refresh(BattleScreen.active.mappanel);
			}
		}
	}
}
