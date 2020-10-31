package javelin.model.unit;

import java.awt.Image;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.action.world.WorldMove;
import javelin.controller.ai.BattleAi;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.CombatantHealthComparator;
import javelin.controller.comparator.CombatantsByNameAndMercenary;
import javelin.controller.comparator.SpellLevelComparator;
import javelin.controller.db.reader.fields.Skills;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.IncursionFight;
import javelin.controller.terrain.Terrain;
import javelin.model.Equipment;
import javelin.model.item.Item;
import javelin.model.transport.Transport;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.Images;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.BribingScreen;
import javelin.view.screen.NamingScreen;
import javelin.view.screen.WorldScreen;

/**
 * A group of units that the player controls as a overworld game unit. If a
 * player loses all his squads the game ends.
 *
 * TODO when this breaks the 1000 line limit an easy fix is to turn
 * {@link #members} into a SquadMemmbers class. See {@link #sort()}.
 *
 * @author alex
 */
public class Squad extends Actor implements Cloneable,Iterable<Combatant>{
	/**
	 * See {@link Javelin#act()}.
	 */
	static public Squad active;

	/** Which units are in this squad. */
	public Combatants members=new Combatants();
	/** Gold pieces (currency). 1GP = 10 silver pieces. 1SP=10 copper pieces. */
	public int gold=0;
	/**
	 * {@link Item}s carried by each of the {@link #members}. Since the
	 * {@link BattleAi} doesn't use items having this as a Squad filed is the best
	 * choice performance-wise.
	 */
	public Equipment equipment=new Equipment(this);
	/** See {@link Transport}. */
	public Transport transport=null;
	/**
	 * Last visited town by a squad. Ideally this should always be a {@link Town}
	 * but in practice should never be <code>null</code>.
	 */
	public Town lasttown=null;

	/**
	 * <code>false</code> will never prompt to skip battles.
	 */
	public Boolean strategic=false;
	/** Terrain type this squad is coming from after movement. */
	public Terrain lastterrain=null;

	transient Image image=null;
	long time;

	/**
	 * @param xp Starting location (x).
	 * @param yp Starting location (y).
	 * @param hourselapsedp See {@link #time}.
	 * @param lasttownp See {@link #lasttown}.
	 */
	public Squad(final int xp,final int yp,final long hourselapsedp,
			Town lasttownp){
		x=xp;
		y=yp;
		time=hourselapsedp;
		lasttown=lasttownp;
	}

	/** Removes this squad from the game. */
	public void disband(){
		var squads=World.getall(Squad.class);
		squads.remove(this);
		Javelin.lose();
		if(Dungeon.active!=null) Dungeon.active.leave();
	}

	@Override
	public void place(){
		super.place();
		updateavatar();
	}

	/**
	 * Updates {@link Actor#visual}, taking {@link #transport} into account.
	 */
	public void updateavatar(){
		if(members.isEmpty()) return;
		if(transport!=null&&Dungeon.active==null){
			image=Images.get(transport.name.replaceAll(" ","").toLowerCase());
			return;
		}
		ArrayList<Combatant> squad=new Combatants(members);
		squad.removeAll(getmercenaries());
		if(squad.isEmpty()) squad=members;
		Combatant leader=null;
		for(Combatant c:squad)
			if(leader==null||c.source.cr>leader.source.cr) leader=c;
		image=Images.get(List.of("monster",leader.source.avatarfile));
	}

	@Override
	public Image getimage(){
		updateavatar();
		return image;
	}

	/**
	 * Sorts alphabetically, with all mercenaries to the final of the
	 * {@link #members} list, making it easier to overcome some current UI
	 * limitations.
	 *
	 * TODO this shouldn't be public but ensure by the architecure. A solution
	 * would be to make {@link #members} a class of its own (not a List) which
	 * exposes a singgle add methods like {@link #add(Combatant)} and
	 * {@link #add(Combatant, List)}. This needs some rewriting in the code and
	 * also making sure Cloning works as intended both for shallow and deep
	 * oprateions.
	 */
	public void sort(){
		members.sort(CombatantsByNameAndMercenary.SINGLETON);
	}

	/**
	 * @param s Takes this squad, {@link #disband()} it and join with the current
	 *          instance.
	 */
	public void join(final Squad s){
		members.addAll(s.members);
		sort();
		gold+=s.gold;
		time=Math.max(time,s.time);
		for(final Combatant c:s.members)
			equipment.put(c,s.equipment.get(c));
		if(transport==null||s.transport!=null&&s.transport.speed>transport.speed)
			transport=s.transport;
		strategic=strategic&&s.strategic;
		s.disband();
	}

	/**
	 * @return The sum of {@link Monster#size()} for this squad.
	 */
	public float eat(){
		float sum=0;
		for(final Combatant c:members)
			sum+=c.source.eat();
		return sum;
	}

	@Override
	public void turn(long time,WorldScreen world){
		paymercenaries();
		int survival=Skill.SURVIVAL.getbonus(getbest(Skill.SURVIVAL));
		survival+=Terrain.get(x,y).survivalbonus;
		float foodfound=survival/(4f*members.size());
		if(foodfound>1)
			foodfound=1;
		else if(foodfound<0) foodfound=0;
		gold-=Math.round(Math.ceil(eat()*(1-foodfound)));
		if(gold<0) gold=0;
		if(transport!=null) transport.keep(this);
	}

	void paymercenaries(){
		for(Combatant c:new ArrayList<>(members)){
			if(!c.mercenary) continue;
			int fee=c.pay();
			if(gold>=fee)
				gold-=fee;
			else{
				MessagePanel.active.clear();
				Javelin.message(
						c+" is not paid, abandons your ranks!\n\nPress ENTER to coninue...",
						Javelin.Delay.NONE);
				while(Javelin.input().getKeyChar()!='\n'){
					// wait for enter
				}
				MessagePanel.active.clear();
				c.dismiss(this);
			}
		}
	}

	/**
	 * Will automatically {@link #sort()} after inclusion..
	 *
	 * @param c Adds this unit to {@link #members}.
	 * @param equipmentp Unit's equipment to be added to {@link #equipment}.
	 */
	public void add(Combatant c,List<Item> equipmentp){
		members.add(c);
		sort();
		equipment.put(c,new ArrayList<>(equipmentp));
	}

	/**
	 * Like {@link #add(Combatant, List)} but adds an empty item list.
	 */
	public void add(Combatant c){
		add(c,new ArrayList<Item>(0));
	}

	/**
	 * @param c Removes this unit and {@link #disband()} if necessary.
	 */
	public void remove(Combatant c){
		members.remove(c);
		equipment.clean();
		if(members.isEmpty()) disband();
	}

	@Override
	public void move(int tox,int toy){
		super.move(tox,toy);
	}

	@Override
	public String toString(){
		return members.toString();
	}

	@Override
	public Boolean destroy(Incursion i){
		if(Javelin.DEBUG&&Debug.disablecombat) return false;
		Javelin.message("You are attacked by: "+i.toString().toLowerCase()+"!",
				true);
		Squad.active=this;
		throw new StartBattle(new IncursionFight(i));
	}

	/**
	 * @return <code>true</code> if all the squad can fly, <code>false</code> if
	 *         at least one can fly or <code>null</code> otherwise.
	 */
	public boolean fly(){
		if(Squad.active.transport!=null&&Squad.active.transport.flies) return true;
		for(Combatant c:members)
			if(c.source.fly==0) return false;
		return true;
	}

	/**
	 * @param periodbonus
	 * @return a {@link Skills#perception} roll.
	 */
	public int perceive(boolean flyingbonus,boolean weatherpenalty,
			boolean periodbonus){
		return perceive(flyingbonus,weatherpenalty,periodbonus,members);
	}

	public static int perceive(boolean flyingbonus,boolean weatherpenalty,
			boolean periodbonus,List<Combatant> squad){
		if(Javelin.DEBUG&&squad.isEmpty()) throw new InvalidParameterException();
		int best=Integer.MIN_VALUE;
		for(Combatant c:squad){
			int roll=10+c.perceive(flyingbonus,weatherpenalty,periodbonus);
			if(roll>best) best=roll;
		}
		return best;
	}

	/**
	 * Spot a group of enemies.
	 *
	 * @param target If not <code>null</code>, will only shown information if
	 *          adjacent in the {@link WorldScreen}.
	 * @return A list with the name of the given {@link Combatant}s, replaced with
	 *         "?" when failed to {@link #perceive()} properly.
	 */
	public String spotenemies(List<Combatant> opponents,Actor target){
		if(target!=null&&distanceinsteps(target.x,target.y)>1) return "";
		int spot=perceive(true,true,true);
		var spotted=opponents.stream().filter(c->spot>=c.taketen(Skill.STEALTH))
				.collect(Collectors.toList());
		return Javelin.group(spotted);
	}

	/**
	 * Represents a situation in which a group of hostile {@link Combatant} s is
	 * approaching this group. The foes must first be heard successfully and then
	 * a {@link #hide()} attempt is performed.
	 *
	 * @return <code>true</code> if hide is successful and player gives input to
	 *         stay hidden (confirmation).
	 */
	public boolean hide(List<Combatant> foes){
		// needs to pass on a listen check to notice enemy
		boolean outside=Dungeon.active==null;
		boolean flying=outside&&!Terrain.current().equals(Terrain.FOREST);
		int listenroll=Squad.active.perceive(flying,outside,outside);
		boolean listen=false;
		for(Combatant foe:foes)
			if(listenroll>=foe.taketen(Skill.STEALTH)){
				listen=true;
				break;
			}
		if(!listen) return false; // doesn't hear them coming
		int hideroll=getworst(Skill.STEALTH).roll(Skill.STEALTH);
		for(Combatant foe:foes)
			if(10+foe.perceive(flying,outside,outside)>=hideroll) return false; // spotted!
		// hidden
		char input=' ';
		final String prompt="You have hidden from a "+Difficulty.describe(foes)
				+" group of enemies!\n"
				+"Press s to storm them or w to wait for them to go away...\n\n"
				+"Enemies: "+Squad.active.spotenemies(foes,null)+".";
		while(input!='w'&&input!='s')
			input=Javelin.prompt(prompt);
		return input=='w';
	}

	/**
	 * The squad tries to parley with the enemy {@link Combatant}s, possibly
	 * bribing or hirimg them to avoid the fight.
	 *
	 * Hiring mercenaries this way in a {@link Dungeon} costs 10 times more,
	 * because since a dungeon crawl occurs in a much faster time frame, Javelin
	 * doesn't bother applying daily fees while in there and that is very
	 * exploitable. The in-game reasoning is that since dungeon encounters occur
	 * on a fixed per-level table, that would mean a group of intelligent monsters
	 * agreeing to turn on their fellows for money, which obviously takes more
	 * incentive... Note than that's only for hiring them on-the-spot, once you're
	 * outside, fees are calculated normally.
	 *
	 * @return <code>false</code> if the fight is to proceed.
	 */
	public boolean bribe(List<Combatant> foes){
		boolean intelligent=false;
		for(Combatant c:foes)
			if(c.source.think(-1)){
				intelligent=true;
				break;
			}
		if(!intelligent) return false;
		int diplomac=getbest(Skill.DIPLOMACY).roll(Skill.DIPLOMACY);
		int highest=Integer.MIN_VALUE;
		int dailyfee=0;
		for(Combatant foe:foes){
			int will=foe.source.getwill();
			if(RPG.r(1,20)+will>=diplomac) return false;// no deal!
			if(will>highest) highest=will;
			dailyfee+=foe.pay();
		}
		if(Dungeon.active!=null) dailyfee*=10;
		final int bribe=Math.max(1,RewardCalculator.receivegold(foes)/2);
		final boolean canhire=diplomac>=highest+5;
		boolean b=new BribingScreen().bribe(foes,dailyfee,bribe,canhire);
		Javelin.app.switchScreen(BattleScreen.active);
		return b;
	}

	/**
	 * @param x {@link World} coordinate.
	 * @param y {@link World} coordinate.
	 * @return Like {@link #speed()} but return time in hours.
	 */
	public float move(boolean ellapse,Terrain t,int x,int y){
		var hours=WorldMove.TIMECOST*(30f*WorldMove.NORMALMARCH)/speed(t,x,y);
		if(hours<1) hours=1;
		if(ellapse) delay(Math.round(hours));
		return hours;
	}

	/**
	 * @param x {@link World} coordinate.
	 * @param y {@link World} coordinate.
	 * @return The land speed movement overland in miles per hour. This is the
	 *         amount covered in an hour but the correspoinding movement per day
	 *         is less since it has to account for sleep, resting, etc.
	 */
	public int speed(Terrain t,int x,int y){
		int snow=t.getweather()==Terrain.SNOWING?2:1;
		if(transport!=null){
			int transportspeed=transport.getspeed(members)/snow;
			return transport.flies?transportspeed:t.speed(transportspeed,x,y);
		}
		int speed=Integer.MAX_VALUE;
		boolean allfly=true;
		for(Combatant c:members){
			Monster m=c.source;
			speed=Math.min(speed,
					Terrain.WATER.equals(t)?Math.max(m.fly,m.swim):c.gettopspeed(null));
			if(m.fly==0) allfly=false;
		}
		return Math
				.round(WorldMove.NORMALMARCH*(allfly?speed:t.speed(speed,x,y))/snow);
	}

	/**
	 * A squad cannot move on water if any of the combatants can't swim.
	 * Alternatively a boat or airship will let non-swimming squads move on water.
	 *
	 * @return <code>true</code> if this squad can move on water.
	 * @see #MISSING()
	 */
	public boolean swim(){
		if(transport!=null&&(transport.sails||transport.flies)) return true;
		if(!World.scenario.crossrivers) return false;
		for(Combatant c:members)
			if(c.source.swim()==0) return false;
		return true;
	}

	/**
	 * Discover a {@link World} area in a radius around current position.
	 *
	 * @param vision Perceive roll with circumstance bonuses.
	 * @return Distance the Squad was able to see (minimum of 1).
	 * @see WorldScreen#discovered
	 * @see Squad#perceive(boolean)
	 */
	public int seesurroundings(int vision){
		vision=Math.max(1,vision/5);
		Outpost.discover(x,y,vision);
		return vision;
	}

	@Override
	public List<Combatant> getcombatants(){
		return members;
	}

	@Override
	public String describe(){
		String members="";
		for(Combatant c:this.members)
			members+=c+", ";
		return "Squad ("+members.substring(0,members.length()-2)+")";
	}

	/**
	 * @return All squads in the {@link World}.
	 */
	public static ArrayList<Squad> getsquads(){
		ArrayList<Actor> actors=World.getall(Squad.class);
		ArrayList<Squad> squads=new ArrayList<>(actors.size());
		for(Actor a:actors)
			squads.add((Squad)a);
		return squads;
	}

	public boolean skipcombat(int diffifculty){
		if(!strategic) return false;
		Character input=' ';
		while(input!='\n'&&input!='s'){
			Javelin.app.switchScreen(BattleScreen.active);
			final String difficulty=Difficulty.describe(diffifculty);
			final String prompt="Do you want to skip this "+difficulty+" battle?\n\n" //
					+"Press ENTER to open the battle screen.\n"
					+"Press s to skip it and calculate results automatically.";
			input=Javelin.prompt(prompt);
		}
		return input=='s';
	}

	public void seesurroudings(){
		boolean onairship=Transport.AIRSHIP.equals(transport);
		int vision=perceive(!onairship,true,true);
		if(onairship)
			vision+=+4;
		else
			vision+=Terrain.get(x,y).visionbonus;
		seesurroundings(vision);
	}

	public static void updatevision(){
		for(Squad s:Squad.getsquads())
			s.seesurroudings();
	}

	/**
	 * prevents players from cheating the strategic combat system by never buying
	 * items, which would have no effect in the outcome of battle.
	 *
	 * @see StartBattle
	 */
	public String wastegold(float resourcesused){
		int spent=Math.round(Squad.active.gold*resourcesused);
		if(spent<=0) return "";
		Squad.active.gold-=spent;
		return "$"+Javelin.format(spent)+" in resources lost.\n\n";
	}

	@Override
	protected boolean cancross(int tox,int toy){
		return swim()||super.cancross(tox,toy);
	}

	@Override
	public Squad clone(){
		try{
			/* shallow clone */
			return (Squad)super.clone();
		}catch(CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return Daily mercenary cost in gold.
	 * @see Combatant#mercenary
	 */
	public int getupkeep(){
		int upkeep=0;
		for(Combatant c:members)
			if(c.mercenary) upkeep+=c.pay();
		return upkeep;
	}

	/**
	 * 100XP = 1CR.
	 *
	 * @return Total of XP between all active {@link Squad} members.
	 */
	public int sumxp(){
		BigDecimal sum=new BigDecimal(0);
		for(Combatant c:Squad.active.members)
			sum=sum.add(c.xp);
		return Math.round(sum.floatValue()*100);
	}

	/**
	 * @return <code>true</code> if can use any available spell to heal
	 *         {@link #members}.
	 * @see #quickheal()
	 */
	public boolean canheal(){
		for(Spell s:getavailablespells())
			for(Combatant c:members)
				if(s.canheal(c)) return true;
		return false;

	}

	ArrayList<Spell> getavailablespells(){
		ArrayList<Spell> available=new ArrayList<>();
		for(Combatant c:members)
			for(Spell s:c.spells)
				if(!s.exhausted()) available.add(s);
		return available;
	}

	/**
	 * Uses available spells to heal your party.
	 *
	 * @see #canheal()
	 */
	public void quickheal(){
		ArrayList<Combatant> members=new ArrayList<>(this.members);
		members.sort(CombatantHealthComparator.SINGLETON);
		ArrayList<Spell> spells=getavailablespells();
		spells.sort(SpellLevelComparator.SINGLETON);
		for(Spell s:spells)
			for(Combatant c:members)
				while(s.canheal(c)&&!s.exhausted()){
					s.castpeacefully(null,c,members);
					s.used+=1;
				}
	}

	@Override
	public Integer getel(Integer attackerel){
		return ChallengeCalculator.calculateel(members);
	}

	/**
	 * @return A new list contianing all mercenaries in this squad.
	 * @see Combatant#mercenary
	 */
	public List<Combatant> getmercenaries(){
		return members.stream().filter(c->c.mercenary).collect(Collectors.toList());
	}

	public Combatant getbest(Skill s){
		Combatant best=null;
		for(Combatant c:members){
			if(!s.canuse(c)) continue;
			if(best==null||s.getbonus(c)>s.getbonus(best)) best=c;
		}
		return best==null?members.get(0):best;
	}

	public Combatant getworst(Skill s){
		Combatant worst=null;
		for(Combatant c:Squad.active.members)
			if(worst==null||s.getbonus(c)<s.getbonus(worst)) worst=c;
		return worst;
	}

	/**
	 * @return The highest take 10 roll of the current {@link Squad}. Creatures
	 *         that are injured or worse won't be able to heal others. If nobody
	 *         can heal, returns {@link Integer#MIN_VALUE}.
	 *
	 * @see Combatant#getstatus()
	 */
	public int heal(){
		int heal=Integer.MIN_VALUE;
		for(Combatant c:Squad.active.members)
			if(c.getnumericstatus()>Combatant.STATUSINJURED)
				heal=Math.max(heal,c.taketen(Skill.HEAL));
		return heal;
	}

	public int getel(){
		return ChallengeCalculator.calculateel(members);
	}

	/**
	 * @return <code>true</code> if there's a non-mercenary monster of this type
	 *         in the current squad.
	 *
	 * @see Combatant#mercenary
	 */
	public boolean contains(Monster m){
		for(Combatant c:members)
			if(!c.mercenary&&c.source.name.equals(m.name)) return true;
		return false;
	}

	/** @return Recuirts the supplised unit permanently and asks for a name. */
	public Combatant recruit(Combatant c){
		boolean askname=World.scenario!=null&&World.scenario.asksquadnames;
		if(askname&&!Javelin.DEBUG)
			c.source.customName=NamingScreen.getname(c.toString());
		add(c);
		/*
		 * night-only is largely cosmetic so just don't appear for player units
		 */
		c.source.nightonly=false;
		return c;
	}

	/**
	 * @param m Source statistics to make an unit from.
	 * @return An actual unit with said statistics.
	 * @see Combatant#clone()
	 * @see NamingScreen
	 */
	public Combatant recruit(Monster m){
		return recruit(new Combatant(m.clone(),true));
	}

	@Override
	public boolean interact(){
		if(this==Squad.active||Squad.active.distanceinsteps(x,y)!=1) return false;
		join(Squad.active);
		return true;
	}

	@Override
	public Iterator<Combatant> iterator(){
		return members.iterator();
	}

	/**
	 * @param s Rolls this skill for all members, returning the highest result.
	 * @see Combatant#roll(Skill)
	 */
	public int roll(Skill s){
		var highest=Integer.MIN_VALUE;
		for(var member:members)
			highest=Math.max(highest,member.roll(s));
		return highest;
	}

	/**
	 * @return Time, used as game clock for each Squad, not only determining who
	 *         should act first and for how long but also current {@link World}
	 *         time.
	 */
	public long gettime(){
		return time;
	}

	/** @param Advances time by this many hours. */
	public void delay(long hours){
		if(hours<=0) return;
		time+=hours;
		for(var c:new ArrayList<>(members))
			c.terminateconditions((int)hours);
		equipment.refresh((int)hours);
	}

	/** @see #gettime() */
	public void settime(long time){
		this.time=time;
	}
}
