package javelin.model.world.location.dungeon.feature.door;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;
import javelin.controller.table.Table;
import javelin.controller.table.dungeon.door.HiddenDoor;
import javelin.controller.table.dungeon.door.LockedDoor;
import javelin.controller.table.dungeon.door.StuckDoor;
import javelin.controller.table.dungeon.door.TrappedDoor;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.model.item.Item;
import javelin.model.item.key.door.Key;
import javelin.model.item.key.door.MasterKey;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.door.trap.Alarm;
import javelin.model.world.location.dungeon.feature.door.trap.ArcaneLock;
import javelin.model.world.location.dungeon.feature.door.trap.DoorTrap;
import javelin.model.world.location.dungeon.feature.door.trap.HoldPortal;
import javelin.old.RPG;

public class Door extends Feature{
	public static final List<Class<? extends Door>> TYPES=new ArrayList<>();
	static final List<DoorTrap> TRAPS=Arrays.asList(
			new DoorTrap[]{Alarm.INSTANCE,ArcaneLock.INSTANCE,HoldPortal.INSTANCE});

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

	public int unlockdc=RPG.r(20,30)
			+Dungeon.gettable(FeatureModifierTable.class).rollmodifier();

	/** Used if {@link #hidden}. TODO */
	public int searchdc=RPG.r(20,30);
	public int breakdc;
	public Class<? extends Key> key;

	DoorTrap trap=rolltable(TrappedDoor.class)?RPG.pick(TRAPS):null;
	boolean stuck=rolltable(StuckDoor.class);
	boolean locked=rolltable(LockedDoor.class);
	/** @see #searchdc */
	/**
	 * TODO use only #draw, remove
	 */
	boolean hidden=!Debug.bypassdoors&&rolltable(HiddenDoor.class);

	public Door(String avatar,int breakdcstuck,int breakdclocked,
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
		Combatant unlocker=unlock();
		Combatant forcer=force();
		if(locked){
			if(unlocker!=null)
				Javelin.message(unlocker+" unlocks the door!",false);
			else if(forcer!=null)
				Javelin.message(forcer+" forces the lock!",false);
			else if(getkey()==null){
				Javelin.message("The lock is too complex...",false);
				return false;
			}else if(!usekey()) // feedback alredy shown as prompt
				return false;
		}else if(stuck) if(forcer!=null)
			Javelin.message(forcer+" breaks the door open!",false);
		else{
			Javelin.message("The door is too heavy to open...",false);
			return false;
		}
		enter=true;
		Dungeon.active.features.remove(this);
		spring(unlocker==null?forcer:unlocker);
		return true;
	}

	boolean usekey(){
		Item k=getkey();
		if(k==null) return false;
		if(Javelin.prompt("Do you want to use a "+k.name.toLowerCase()+"?\n"
				+"Press ENTER to use or any other key to cancel...",false)!='\n')
			return false;
		Squad.active.equipment.remove(k);
		return true;
	}

	Item getkey(){
		Item k=Squad.active.equipment.get(key);
		if(k==null) k=Squad.active.equipment.get(MasterKey.class);
		return k;
	}

	void spring(Combatant opening){
		if(trap==null) return;
		if(opening==null) for(Combatant c:Squad.active.members)
			if(opening==null||c.hp>opening.hp) opening=c;
		trap.activate(opening);
	}

	Combatant unlock(){
		Combatant expert=Squad.active.getbest(Skill.DISABLEDEVICE);
		return expert.taketen(Skill.DISABLEDEVICE)>=unlockdc?expert:null;
	}

	Combatant force(){
		Combatant strongest=null;
		for(Combatant c:Squad.active.members)
			if(strongest==null||c.source.strength>strongest.source.strength)
				strongest=c;
		int force=10+Monster.getbonus(strongest.source.strength);
		return force>=breakdc?strongest:null;
	}

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
}
