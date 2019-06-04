package javelin.model.world.location.dungeon.feature.door;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.generator.dungeon.template.Template;
import javelin.controller.table.Table;
import javelin.controller.table.dungeon.door.HiddenDoor;
import javelin.controller.table.dungeon.door.LockedDoor;
import javelin.controller.table.dungeon.door.StuckDoor;
import javelin.controller.table.dungeon.door.TrappedDoor;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.model.item.key.door.Key;
import javelin.model.item.key.door.MasterKey;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.DisableDevice;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.door.trap.Alarm;
import javelin.model.world.location.dungeon.feature.door.trap.ArcaneLock;
import javelin.model.world.location.dungeon.feature.door.trap.DoorTrap;
import javelin.model.world.location.dungeon.feature.door.trap.HoldPortal;
import javelin.old.RPG;

/**
 * A {@link Dungeon} door, which may be {@link #locked}, {@link #stuck}, contain
 * a {@link #trap} or not. If none of these are true, it may as well be
 * discarded.
 *
 * @see Key
 * @author alex
 */
public class Door extends Feature{
	static final List<Class<? extends Door>> TYPES=new ArrayList<>();
	static final List<DoorTrap> TRAPS=Arrays.asList(
			new DoorTrap[]{Alarm.INSTANCE,ArcaneLock.INSTANCE,HoldPortal.INSTANCE});
	static final String SPENDMASTERKEY="Do you want to spend a master key to open this door?\n"
			+"Press ENTER to confirm or any other key to cancel...";

	static{
		registerdoortype(WoodenDoor.class,3);
		registerdoortype(GoodWoodenDoor.class,2);
		registerdoortype(ExcellentWoodenDoor.class,2);
		registerdoortype(StoneDoor.class,1);
		registerdoortype(IronDoor.class,1);
	}

	static void registerdoortype(Class<? extends Door> d,int chances){
		for(int i=0;i<chances;i++)
			TYPES.add(d);
	}

	/** DC of {@link DisableDevice} to unlock. */
	public int unlockdc=RPG.r(20,30)
			+Dungeon.gettable(FeatureModifierTable.class).rollmodifier();

	/** Used if {@link #hidden}. TODO */
	public int searchdc=RPG.r(20,30);
	/** DC of {@link Monster#strength} to break down. */
	public int breakdc;
	/** Type of key used to unlock. */
	public Class<? extends Key> key;
	/** Trap present or <code>null</code>. */
	public DoorTrap trap=rolltable(TrappedDoor.class)?RPG.pick(TRAPS):null;
	/** <code>true</code> if stuck. */
	public boolean stuck=rolltable(StuckDoor.class);
	/** <code>true</code> if locked. */
	public boolean locked=rolltable(LockedDoor.class);
	/** @see #searchdc */
	/**
	 * TODO use only #draw, remove
	 */
	boolean hidden=!Debug.bypassdoors&&rolltable(HiddenDoor.class);

	Door(String avatar,int breakdcstuck,int breakdclocked,
			Class<? extends Key> key){
		super(-1,-1,avatar);
		enter=false;
		breakdc=locked?breakdclocked:breakdcstuck;
		breakdc=Math.max(2,breakdc+RPG.randomize(5));
		unlockdc=Math.max(2,unlockdc);
		this.key=key;
		if(trap!=null) trap.generate(this);
		draw=!hidden;
	}

	@Override
	public boolean activate(){
		if(Debug.bypassdoors) return true;
		if(hidden&&!draw) return false;
		Combatant unlocker=getunlocker();
		Combatant forcer=getforcer();
		int strength=Monster.getbonus(forcer.source.strength);
		if(locked) if(unlocker!=null)
			Javelin.message(unlocker+" unlocks the door!",false);
		else if(10+strength>breakdc)
			Javelin.message(forcer+" forces the lock!",false);
		else if(usekey()){
			//fall through
		}else{
			Javelin.message("The lock is too complex to pick...",false);
			return false;
		}
		locked=false;
		if(stuck){
			String prompt="The door is too heavy to open... Do you want to force it?\n";
			prompt+="Press ENTER to force or any other key to cancel...";
			if(Javelin.prompt(prompt)!='\n'||!force(forcer,strength)) return false;
		}
		enter=true;
		remove();
		spring(unlocker==null?forcer:unlocker);
		return true;
	}

	boolean force(Combatant forcer,int strength){
		boolean forced=false;
		boolean alerted=false;
		var attempts=0;
		while(!forced&&!alerted&&attempts<10){
			forced=attemptbreak(strength);
			alerted=RPG.chancein(10);
			attempts+=1;
		}
		String result;
		if(forced){
			result=forcer+" breaks the door down!";
			stuck=false;
		}else
			result=forcer+" could not break the door after "+attempts
					+" attempt(s)...";
		if(alerted) result+="\nThe noise draws someone's attention!";
		Javelin.message(result,false);
		if(alerted) throw new StartBattle(Dungeon.active.encounter());
		return !stuck;
	}

	boolean attemptbreak(int strength){
		if(10+strength>=breakdc) return true;
		int roll=RPG.r(1,20);
		if(roll==20) return true;
		if(roll==1) return false;
		return roll+strength>=breakdc;
	}

	boolean usekey(){
		Key key=null;
		for(var k:Squad.active.equipment.getall(this.key))
			if(k.dungeon==Dungeon.active){
				key=k;
				break;
			}
		if(key!=null){
			var message="You unlock the door with the "+key.toString().toLowerCase()
					+"!";
			Javelin.message(message,false);
			return true;
		}
		key=Squad.active.equipment.get(MasterKey.class);
		if(key!=null&&Javelin.prompt(SPENDMASTERKEY)=='\n'){
			key.expend();
			return true;
		}
		return false;
	}

	void spring(Combatant opening){
		if(trap==null) return;
		if(opening==null) for(Combatant c:Squad.active.members)
			if(opening==null||c.hp>opening.hp) opening=c;
		trap.activate(opening);
	}

	Combatant getunlocker(){
		Combatant expert=Squad.active.getbest(Skill.DISABLEDEVICE);
		return expert.taketen(Skill.DISABLEDEVICE)>=unlockdc?expert:null;
	}

	Combatant getforcer(){
		Combatant strongest=Squad.active.members.get(0);
		for(Combatant c:Squad.active.members)
			if(c.source.strength>strongest.source.strength) strongest=c;
		return strongest;
	}

	/**
	 * @param dungeon Dungeon to place door in.
	 * @param p Position of the door.
	 * @return A randomly-chosen type of door.
	 */
	public static Door generate(Dungeon dungeon,Point p){
		try{
			Door d=RPG.pick(TYPES).getDeclaredConstructor().newInstance();
			if(!d.stuck&&!d.locked&&!(d.trap instanceof Alarm)) return null;
			d.x=p.x;
			d.y=p.y;
			d.unlockdc+=dungeon.level;
			return d;
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public void place(Dungeon d){
		super.place(d);
		if(hidden) d.map[x][y]=Template.WALL;
	}

	@Override
	public void discover(Combatant searching,int searchroll){
		super.discover(searching,searchroll);
		if(!draw&&searchroll>=searchdc){
			Dungeon.active.map[x][y]=Template.FLOOR;
			draw=true;
			hidden=false;
			Javelin.redraw();
			if(searching!=null) Javelin.message("You find a hidden door!",true);
		}
	}

	static boolean rolltable(Class<? extends Table> table){
		return Dungeon.active.tables.get(table).rollboolean();
	}

	@Override
	public String toString(){
		return getClass().getSimpleName();
	}

	@Override
	public void remove(){
		super.remove();
		Dungeon.active.map[x][y]=Template.FLOOR; //make sure path is clear
	}
}
