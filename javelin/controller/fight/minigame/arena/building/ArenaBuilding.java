package javelin.controller.fight.minigame.arena.building;

import javelin.Javelin;
import javelin.controller.fight.minigame.arena.Arena;
import javelin.model.state.BattleState;
import javelin.model.unit.Building;
import javelin.model.unit.Combatant;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.action.BattleMouseAction;
import javelin.view.screen.BattleScreen;

/**
 * TODO on upgrade start fast healing
 *
 * @author alex
 */
public abstract class ArenaBuilding extends Building{
	protected String actiondescription;

	public ArenaBuilding(String name,String avatar,String description){
		super(name,avatar);
		actiondescription=description;
		source.passive=true;
		setlevel(Building.LEVELS[0]);
		hp=maxhp;
	}

	abstract protected void upgradebuilding();

	@Override
	public BattleMouseAction getmouseaction(){
		return new BattleMouseAction(){
			@Override
			public void onenter(Combatant current,Combatant target,Tile t,
					BattleState s){
				Javelin.message(getactiondescription(current),Javelin.Delay.NONE);
			}

			@Override
			public boolean validate(Combatant current,Combatant target,BattleState s){
				return true;
			}

			@Override
			public Runnable act(final Combatant current,final Combatant target,
					final BattleState s){
				return ()->{
					if(!current.isadjacent(target)){
						MessagePanel.active.clear();
						Javelin.message("Too far away...",Javelin.Delay.WAIT);
					}else{
						if(click(current)) s.clone(current).ap+=1;
						Javelin.app.switchScreen(BattleScreen.active);
					}
				};
			}
		};

	}

	/**
	 * Called not from the {@link Arena} main logic but from the
	 * {@link BattleScreen#perform(Runnable)} callback (boils down to be being the
	 * same pretty much though).
	 *
	 * @param current Unit using this.
	 * @return Wheter to update {@link Combatant#ap} or not. <code>false</code> if
	 *         nothing was actually done or if removing the unit from the Fight.
	 */
	abstract protected boolean click(Combatant current);

	public String getactiondescription(Combatant current){
		return actiondescription;
	}
}