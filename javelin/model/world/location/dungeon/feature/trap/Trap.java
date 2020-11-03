package javelin.model.world.location.dungeon.feature.trap;

import javelin.Javelin;
import javelin.controller.action.world.WorldMove;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.divination.FindTraps.FindingTraps;
import javelin.model.unit.skill.DisableDevice;
import javelin.model.unit.skill.Perception;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Furniture;
import javelin.model.world.location.dungeon.feature.chest.Chest;

/**
 * A trap is a semi-permanent strategic dungeon feature. It is balanced by
 * generating more {@link Chest}.
 *
 * In d20 traps are supposed to award XP as well as gold. This isn't done here
 * partly because since the traps are permanent (so as to be strategic) it would
 * be easy to continually step on traps to plunder XP.
 *
 * Traps are now always hidden on {@link Furniture} - allowing players to
 * somewhat strategically avoid them or "search" them for high-rish high-reward
 * outcomes (as {@link Chest}s are also hidden).
 *
 * @see DisableDevice
 * @author alex
 */
public abstract class Trap extends Feature{
	static final String CONFIRM="Are you sure you want to step in to this trap?\n"
			+"Press ENTER to confirm or any other key to quit...";

	/** Challenge rating. */
	public final int cr;
	/** Difficulty class to save against. */
	public int savedc;
	/** See {@link DisableDevice}. */
	public int disarmdc;
	/** See {@link Perception}. */
	public int searchdc;

	public Trap(int cr,String avatarfilep){
		super("trap",avatarfilep);
		this.cr=cr;
		//draw=!Dungeon.gettable(TrapVisibilityTable.class).rollboolean();
		draw=false;
		remove=false;
		searchdc=10+cr;
		disarmdc=searchdc;
		savedc=searchdc;
	}

	@Override
	public boolean activate(){
		if(!draw){
			draw=true;
			spring();
			return true;
		}
		Combatant disabler=Squad.active.getbest(Skill.DISABLEDEVICE);
		int disable=disabler.taketen(Skill.DISABLEDEVICE);
		if(disable>=disarmdc){
			Javelin.prompt(disabler+" disarms the trap!");
			Dungeon.active.features.remove(this);
			return true;
		}
		if(disable<=disarmdc-5||Javelin.prompt(CONFIRM).equals('\n')){
			spring();
			return true;
		}
		WorldMove.abort=true;
		return false;
	}

	@Override
	public boolean discover(Combatant searching,int searchroll){
		if(draw) return true;
		for(var c:Squad.active.members){
			int bonus=c.hascondition(FindingTraps.class)==null?0:+5;
			if(searching.taketen(Skill.PERCEPTION)+bonus>=searchdc){
				draw=true;
				return true;
			}
		}
		return false;
	}

	protected abstract void spring();

	public static Trap generate(int cr,boolean special){
		return special||cr<MechanicalTrap.MINIMUMCR?new TeleporterTrap(cr)
				:new MechanicalTrap(cr);
	}

	@Override
	public boolean reveal(boolean found){
		if(found) return true;
		draw=true;
		Furniture.revealmessage="You stumble upon a hidden trap!";
		Furniture.onreveal=()->spring();
		return true;
	}
}