package javelin.model.unit;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.CountingSet;
import javelin.controller.Point;
import javelin.controller.SpellbookGenerator;
import javelin.controller.Weather;
import javelin.controller.action.Action;
import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.attack.AttackResolver;
import javelin.controller.action.ai.attack.MeleeAttack;
import javelin.controller.action.ai.attack.RangedAttack;
import javelin.controller.ai.BattleAi;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.db.reader.fields.Skills;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.Fight;
import javelin.controller.kit.Kit;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.walker.Walker;
import javelin.controller.wish.Ressurect;
import javelin.model.TeamContainer;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.focus.Wand;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.state.Square;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.abilities.discipline.Disciplines;
import javelin.model.unit.abilities.discipline.Maneuver;
import javelin.model.unit.abilities.discipline.serpent.TearingFang.Bleeding;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Spells;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.condition.Condition.Effect;
import javelin.model.unit.condition.Melding;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.feat.attack.Cleave;
import javelin.model.unit.feat.attack.GreatCleave;
import javelin.model.unit.skill.Perception;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.old.RPG;
import javelin.view.mappanel.battle.action.BattleMouseAction;
import javelin.view.screen.BattleScreen;

/**
 * A Combatant is an in-game unit, like the enemies in the battlefield or the
 * named player characters. It contains the data that changes frequently during
 * the course of battle, speeding up the cloning process for BattleNode
 * replication by sharing the same reference to {@link Monster} among cloned
 * instances.
 *
 * @author alex
 */
public class Combatant implements Serializable,Cloneable{
	/** TODO turn into {@link Enum}. */
	public static final int STATUSDEAD=-2;
	public static final int STATUSUNCONSCIOUS=-1;
	public static final int STATUSDYING=0;
	public static final int STATUSINJURED=1;
	public static final int STATUSWOUNDED=2;
	public static final int STATUSHURT=3;
	public static final int STATUSSCRATCHED=4;
	public static final int STATUSUNHARMED=5;
	/** TODO proper dying process + healing phase at the end of combat */
	public static final int DEADATHP=-8;

	static final BigDecimal XPFORMAT=new BigDecimal(100);

	/** The statistics for this monster. */
	public Monster source;
	/**
	 * Action points. 1 represent a full-round action, .5 a move-equivalent or
	 * attack action and so on.
	 */
	public float ap=0;
	/**
	 * XY coordenates. Better used as an array since this enabl es more
	 * programmatic way of handling directions, like deltas.
	 */
	public int[] location=new int[2];
	static private int ids=1;
	/** Last time {@link #refresh()} was invoked. */
	public float lastrefresh=-Float.MAX_VALUE;
	/** TODO implement this as a {@link Condition} instead. */
	public float initialap;
	/** Current hit points. */
	public int hp;
	/** Maxium hitpoints. */
	public int maxhp;
	/**
	 * Unique identifier of unit. Several different units (with unique ids) can
	 * share a same {@link #source} with a same id.
	 */
	public int id=STATUSUNCONSCIOUS;
	/** Temporary modifier to {@link Monster#ac}. */
	public int acmodifier=0;
	/** List of current active {@link Condition}s on this unit. */
	private Conditions conditions=new Conditions();
	/** See {@link Discipline}. */
	public Disciplines disciplines=new Disciplines();
	/**
	 * Canonical representation of the spells this unit has.
	 *
	 * @see Monster#spells
	 * @see Monster#spellcr
	 */
	public Spells spells=new Spells();
	/**
	 * Experience points as unspent challenge rating value.
	 *
	 * @see #learn(float)
	 * @see #gethumanxp()
	 */
	public BigDecimal xp=new BigDecimal(0);
	/**
	 * <code>true</code> if this unit is the result of a {@link Summon} spell.
	 * This means it cannot summon other creatures.
	 */
	public boolean summoned=false;
	/** Equipment this unit is currently wearing. */
	public ArrayList<Artifact> equipped=new ArrayList<>(0);
	/** See {@link MercenariesGuild} */
	public boolean mercenary=false;
	/** See {@link Monster#burrow}. */
	public boolean burrowed=false;
	/** Is a player unit that should be controlled by {@link BattleAi}. */
	public boolean automatic=false;
	/** Global {@link Skill} modifier. */
	public int skillmodifier=0;

	/**
	 * @param generatespells if true will create spells for this monster based on
	 *          his {@link Monster#spellcr}. This is essentially a temporary
	 *          measure to allow 1.0 monster who would have spell powers to use
	 *          the currently implemented spells TODO
	 */
	public Combatant(Monster sourcep,boolean generatespells){
		super();
		source=sourcep.clone();
		newid();
		ap=0;
		hp=source.hd.roll(source);
		maxhp=hp;
		for(Feat f:source.feats)
			f.update(this);
		for(Spell s:source.spells)
			spells.add(s.clone());
		if(generatespells&&source.spellcr>0) SpellbookGenerator.generate(this);
	}

	/**
	 * Generates an unique identity number for this Combatant.
	 *
	 * @see Actor#getcombatants()
	 */
	public void newid(){
		ids+=1;
		while(checkidcollision())
			ids+=1;
		id=ids;
	}

	/**
	 * TODO add WorldActor#getcombatants to do this
	 */
	private boolean checkidcollision(){
		if(World.seed==null) return false;
		for(Actor a:World.getactors()){
			List<Combatant> combatants=a.getcombatants();
			if(combatants!=null) for(Combatant c:combatants)
				if(c.id==ids) return true;
		}
		return false;
	}

	@Override
	public Combatant clone(){
		try{
			Combatant c=(Combatant)super.clone();
			c.location=location.clone();
			c.conditions=conditions.clone();
			c.spells=(Spells)spells.clone();
			c.disciplines=disciplines.clone();
			c.xp=c.xp.add(new BigDecimal(0));
			if(c.equipped!=null) c.equipped=(ArrayList<Artifact>)c.equipped.clone();
			return c;
		}catch(CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
	}

	/** @see MeleeAttack */
	public void meleeattacks(Combatant target,BattleState state){
		var a=chooseattack(source.melee);
		var resolver=new AttackResolver(MeleeAttack.INSTANCE,this,target,a.get(0),a,
				state);
		Action.outcome(resolver.attack());
	}

	/** @see RangedAttack */
	public void rangedattacks(Combatant target,BattleState state){
		var a=chooseattack(source.ranged);
		var resolver=new AttackResolver(RangedAttack.INSTANCE,this,target,a.get(0),
				a,state);
		Action.outcome(resolver.attack());
	}

	public List<AttackSequence> getattacks(boolean melee){
		return melee?source.melee:source.ranged;
	}

	public boolean isally(Combatant c,TeamContainer tc){
		return getteam(tc)==c.getteam(tc);
	}

	public List<Combatant> getteam(TeamContainer tc){
		List<Combatant> red=tc.getredteam();
		return red.contains(this)?red:tc.getblueteam();
	}

	@Override
	public String toString(){
		String s=source.toString();
		return mercenary?s+" mercenary":s;
	}

	public boolean hasattacktype(boolean meleeonly){
		return !getattacks(meleeonly).isEmpty();
	}

	public void checkattacktype(boolean meleeonly){
		if(!hasattacktype(meleeonly)){
			Javelin.message("No "+(meleeonly?"mẽlée":"ranged")+" attacks.",
					Javelin.Delay.WAIT);
			throw new RepeatTurn();
		}
	}

	public void refresh(){
		if(lastrefresh==-Float.MAX_VALUE)
			lastrefresh=ap;
		else if(ap!=lastrefresh){
			ap-=.01;
			float turns=ap-lastrefresh;
			if(source.fasthealing>0) heal(Math.round(source.fasthealing*turns),false);
			/*
			 * don't clone the list here or you'll be acting on different
			 * Conditions than the ones on this Combatant instance!
			 */
			for(Condition c:new ArrayList<>(conditions))
				/*
				 * this second check is needed because some conditions like
				 * Bleeding may remove other conditions during this loop
				 */
				if(conditions.contains(c)) c.expireinbattle(this);
			lastrefresh=ap;
		}
	}

	/**
	 * @return If the unit is being affected by the given condition type, returns
	 *         its instance - otherwise <code>null</code>.
	 */
	public Condition hascondition(Class<? extends Condition> clazz){
		for(Condition c:conditions)
			if(c.getClass().equals(clazz)) return c;
		return null;
	}

	/**
	 * Theoretically total reduction should be allowed but since we're making all
	 * energy resistance universal and all damage reduction impenetrable this is a
	 * measure to avoid monsters from becoming invincible.
	 */
	public void damage(int damagep,BattleState s,int reduce){
		if(reduce==Integer.MAX_VALUE) return;
		int damage=damagep-reduce;
		if(damage<1) damage=1;
		int tenth=damagep/10;
		if(damage<tenth) damage=tenth;
		hp-=damage;
		if(hp<=0){
			if(mercenary) hp=Math.min(hp,DEADATHP);
			Javelin.app.fight.die(this,s);
		}
	}

	/**
	 * % are against AC only, no bonuses included except attack bonus
	 */
	public AttackSequence chooseattack(List<AttackSequence> attacks){
		if(attacks.size()==1) return attacks.get(0);
		var i=Javelin.choose("Start which attack sequence?",attacks,false,false);
		if(i==-1) throw new RepeatTurn();
		return attacks.get(i);
	}

	/**
	 * @return 0 if not surprised or the corresponding AC penalty if flat-footed.
	 */
	public int surprise(){
		if(ap>initialap) return 0;
		int dexbonus=Monster.getbonus(source.dexterity);
		return dexbonus>0?-dexbonus:0;
	}

	/**
	 * Rolls a d20, sums {@link Monster#initiative} and initializes {@link #ap}
	 * according to the result.
	 *
	 * This also adds a random positive or negative value (below 1% of an action
	 * point) to help keep the initiative order stable by preventing collisions
	 * (such as 2 units having exactly 0 {@link #ap}). Given that AP values are
	 * padded to the user and that normal initiative works in 5% steps and normal
	 * actions don't usually go finer than 10% AP cost, this is entirely harmless
	 * when it comes to game balance.
	 *
	 * If you want an unit to enter battle mid-way, you can set a starting value
	 * to {@link #ap} representing the current point in time and then call this
	 * method.
	 */
	public void rollinitiative(){
		ap+=10/20f-(RPG.r(1,20)+source.initiative)/20f;
		ap+=RPG.r(-444,+444)/100000f;
		initialap=ap;
		lastrefresh=-Float.MAX_VALUE;
	}

	/**
	 * @return Same as {@link #getstatus()} but returns a constant instead.
	 * @see #STATUSUNHARMED
	 */
	public int getnumericstatus(){
		int maxhp=getmaxhp();
		if(hp>=maxhp) return STATUSUNHARMED;
		if(hp>1) return Math.round(4.0f*hp/maxhp);
		if(hp==1) return STATUSDYING;
		if(hp>Combatant.DEADATHP) return STATUSUNCONSCIOUS;
		return STATUSDEAD;
	}

	/**
	 * @return {@link #maxhp}, taking into consideration {@link Monster#poison}.
	 */
	public int getmaxhp(){
		return maxhp+source.poison/2*source.hd.count();
	}

	/**
	 * @return String describing {@link #hp} condition.
	 * @see #getnumericstatus()
	 */
	public String getstatus(){
		switch(getnumericstatus()){
			case STATUSUNHARMED:
				return "unharmed";
			case STATUSSCRATCHED:
				return "scratched";
			case STATUSHURT:
				return "hurt";
			case STATUSWOUNDED:
				return "wounded";
			case STATUSINJURED:
				return "injured";
			case STATUSDYING:
				return "dying";
			case STATUSUNCONSCIOUS:
				return "unconscious";
			case STATUSDEAD:
				return "killed";
			default:
				throw new RuntimeException("Unknown possibility: "+getnumericstatus());
		}
	}

	/**
	 * Not to be confused with {@link Skills#perceive(Monster, boolean)}.
	 *
	 * @param period objective period of the day
	 * @return subjective period of the day
	 */
	public String perceive(String period){
		switch(source.vision){
			case 0:
				return period;
			case 2:
				return Javelin.app.fight.denydarkvision?Javelin.PERIODEVENING
						:Javelin.PERIODNOON;
			case 1:
				if(period==Javelin.PERIODNIGHT) return Javelin.PERIODEVENING;
				if(period==Javelin.PERIODEVENING)
					return Javelin.app.fight.denydarkvision?Javelin.PERIODEVENING
							:Javelin.PERIODNOON;
		}
		return period;
	}

	/**
	 * Not to be confused with {@link Skills#perceive(Monster, boolean)}.
	 *
	 * @param period Objective period.
	 * @return monster's vision in squares (5 feet)
	 * @see #perceive(String)
	 */
	public int view(String period){
		if(source.vision==2||source.vision==1&&period==Javelin.PERIODEVENING)
			return 12;
		if(period==Javelin.PERIODEVENING||source.vision==1) return 8;
		if(period==Javelin.PERIODNIGHT) return 4;
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean equals(Object obj){
		return id==((Combatant)obj).id;
	}

	@Override
	public int hashCode(){
		return id;
	}

	public void cleave(float ap){
		if(source.hasfeat(GreatCleave.SINGLETON))
			this.ap-=ap;
		else if(source.hasfeat(Cleave.SINGLETON)) this.ap-=ap/2f;
	}

	public ArrayList<String> liststatus(BattleState s){
		var statuslist=new ArrayList<String>();
		if(s.isengaged(this)){
			statuslist.add("engaged");
			if(isflanked(s)) statuslist.add("flanked");
		}
		if(surprise()!=0) statuslist.add("flat-footed");
		var v=s.haslineofsight(s.next,this);
		if(RangedAttack.iscovered(v,this,s))
			statuslist.add("covered");
		else if(v==Vision.BLOCKED) statuslist.add("blocked");
		if(source.fly==0&&s.map[location[0]][location[1]].flooded)
			statuslist.add("on water");
		for(Condition c:conditions)
			statuslist.add(c.toString().toLowerCase());
		statuslist.sort(null);
		return statuslist;
	}

	boolean isflanked(BattleState s){
		ArrayList<Combatant> team=s.blueTeam.contains(this)?s.redTeam:s.blueTeam;
		for(Combatant c:team)
			if(c.flank(this,s)) return true;
		return false;
	}

	public boolean ispenalized(BattleState s){
		if(surprise()!=0||s.map[location[0]][location[1]].flooded&&source.swim()==0)
			return true;
		for(Condition c:conditions)
			if(c.effect==Effect.NEGATIVE) return true;
		return false;
	}

	public boolean isbuffed(){
		for(Condition c:conditions)
			if(c.effect==Effect.POSITIVE) return true;
		return false;
	}

	/**
	 * 1/5 HD to up one {@link #getstatus()}
	 */
	public void meld(){
		addcondition(new Melding(this));
	}

	public void escape(BattleState s){
		s.flee(this);
	}

	/**
	 * Validates, merge (if necessary) and if everything checks add
	 * {@link Condition}.
	 */
	public void addcondition(Condition c){
		if(source.passive||!c.validate(this)) return;
		Condition previous=hascondition(c.getClass());
		if(previous==null||previous.stack){
			c.start(this);
			conditions.add(c);
		}else
			previous.merge(this,c);
	}

	/**
	 * @param time Hours spend.
	 * @see Condition#terminate(int, Combatant)
	 */
	public void terminateconditions(int time){
		for(var c:new ArrayList<>(conditions)){
			c.terminate(time,this);
			if(hp<=DEADATHP){
				var s=this+" dies from being "+c.description+"!";
				Javelin.message(s,true);
				Squad.active.remove(this);
				return;
			}
		}
		if(hp<=0) hp=1;
	}

	/**
	 * @param c Tries to remove this exact instance from {@link #conditions}
	 *          first. If fails, resorts to {@link List#remove(Object)}, which
	 *          will look for an equal object.
	 */
	public void removecondition(Condition c){
		c.end(this);
		for(int i=0;i<conditions.size();i++)
			if(c==conditions.get(i)){
				conditions.remove(i);
				return;
			}
		conditions.remove(c);
	}

	public void finishconditions(BattleState s,BattleScreen screen){
		for(Condition co:conditions){
			co.finish(s);
			screen.block();
		}
	}

	/**
	 * @return a copy of the current conditions in effect.
	 */
	public ArrayList<Condition> getconditions(){
		return new ArrayList<>(conditions);
	}

	/**
	 * @param detox Remove this many points of {@link #poison} damage. Note that
	 *          this receives a modifier (1 modifier = 2 ability points).
	 */
	public void detox(int detox){
		if(detox>0&&source.poison>0){
			detox=Math.min(detox*2,source.poison);
			source.poison-=detox;
			source.changeconstitutionscore(this,+detox);
		}
	}

	/**
	 * Internally clones {@link #source}.
	 *
	 * @return this instance. To allow the following syntax:
	 *         combatant.clone().clonesource()
	 *
	 * @see #clone()
	 */
	public Combatant clonesource(){
		source=source.clone();
		return this;
	}

	/**
	 * Updates {@link Monster#cr} internally.
	 *
	 * @param r Applies one {@link Upgrade} from this set to the given
	 *          {@link Combatant}.
	 * @return <code>true</code> if an upgrade has been successfully applied.
	 * @see Upgrade#upgrade(Combatant)
	 */
	public boolean upgrade(){
		var kit=RPG.pick(Kit.getpreferred(source,source.cr>=5));
		return kit.upgrade(this);
	}

	/** @return All squares that are visible by this unit. */
	public HashSet<Point> calculatevision(BattleState s){
		var seen=new HashSet<Point>();
		var perception=perceive(s.period);
		var range=view(s.period);
		var map=Javelin.app.fight.map.map;
		if(range==Integer.MAX_VALUE) range=Math.max(map.length,map[0].length);
		var here=new Point(location[0],location[1]);
		var fromx=Math.max(0,here.x-range);
		var fromy=Math.max(0,here.y-range);
		var tox=Math.min(here.x+range,map.length-1);
		var toy=Math.min(here.y+range,map[0].length-1);
		for(var x=fromx;x<=tox;x++)
			for(var y=fromy;y<=toy;y++){
				Point p=new Point(x,y);
				if(seen.contains(p)) continue;
				if(s.haslineofsight(here,p,range,perception)==Vision.BLOCKED) continue;
				seen.add(p);
				if(BattleState.lineofsight!=null) seen.addAll(BattleState.lineofsight);
			}
		return seen;
	}

	/**
	 * TODO at some point update with reach attacks
	 *
	 * @return <code>true</code> if can reach the target with a mêlée attack.
	 * @see Monster#melee
	 */
	public boolean isadjacent(Combatant target){
		return Math.abs(location[0]-target.location[0])<=1
				&&Math.abs(location[1]-target.location[1])<=1;
	}

	/**
	 * @return XP in human readeable format (ex: 150XP).
	 */
	public String gethumanxp(){
		return xp.multiply(XPFORMAT).setScale(0,RoundingMode.HALF_UP)+"XP";
	}

	/**
	 * Locates an enemy by sound during battle.
	 *
	 * @see Skills#perceive(Monster, boolean)
	 */
	public void detect(){
		if(Fight.state.redTeam.contains(this)) return;
		int listen=perceive(true,true,true);
		for(Combatant c:Fight.state.redTeam)
			if(listen>=c.taketen(Skill.STEALTH)+(Walker.distance(this,c)-1))
				BattleScreen.active.mappanel.tiles[c.location[0]][c.location[1]].discovered=true;
	}

	public void setlocation(Point p){
		location[0]=p.x;
		location[1]=p.y;
	}

	public String wastespells(float resourcesused){
		String cast="";
		for(Spell s:spells){
			int ncast=0;
			for(int i=s.used;i<s.perday;i++)
				if(RPG.random()<resourcesused){
					s.used+=1;
					ncast+=1;
				}
			if(ncast>0) cast+=s.name+" ("+ncast+"x), ";
		}
		if(!cast.isEmpty()) cast=" Cast: "+cast.substring(0,cast.length()-2)+".";
		return cast;
	}

	public void unequip(Item i){
		if(equipped.remove(i)) ((Artifact)i).remove(this);
	}

	public boolean equip(Artifact a){
		return a.equip(this);
	}

	/**
	 * @param ismercenary Sets {@link #mercenary} and {@link #automatic} to this
	 *          value.
	 */
	public void setmercenary(boolean ismercenary){
		mercenary=ismercenary;
		automatic=ismercenary;
	}

	/**
	 * @param xpgained Adds this amount to {@link #xp}.
	 */
	public void learn(double xpgained){
		if(!mercenary) xp=xp.add(new BigDecimal(xpgained));
	}

	public void ready(Maneuver m){
		ap+=ActionCost.FULL;
		m.spent=false;
	}

	/**
	 * Adds this {@link Discipline}'s {@link Maneuver} to the given
	 * {@link Combatant}.
	 *
	 * @param d TODO
	 * @param m TODO
	 * @param disciplines TODO
	 * @return <code>false</code> if {@link Maneuver#validate(Combatant)} fails or
	 *         <code>true</code> otherwise.
	 */
	public boolean addmaneuver(Discipline d,Maneuver m){
		if(!m.validate(this)) return false;
		disciplines.add(d,m.clone());
		return true;
	}

	public String printstatus(BattleState state){
		var statuslist=liststatus(state);
		var output=new ArrayList<String>();
		var count=new CountingSet();
		for(var s:statuslist)
			count.add(s);
		for(var status:count.getelements()){
			int n=count.getcount(status);
			if(n>1) status+=" (x"+n+")";
			output.add(status);
		}
		output.add(0,getstatus());
		return String.join(", ",output);
	}

	/**
	 * @param c Removes all {@link Condition} instances of this type.
	 */
	public void clearcondition(Class<? extends Condition> c){
		for(Condition co=hascondition(c);co!=null;co=hascondition(c))
			removecondition(co);
	}

	public void heal(int amount,boolean magical){
		hp+=amount;
		if(hp>maxhp) hp=maxhp;
		if(magical) clearcondition(Bleeding.class);
	}

	/**
	 * Simpler version of {@link #damage(int, BattleState, int)}. Just takes the
	 * given amount from {@link #hp} while making sure it stays positive.
	 *
	 * @param reduction Usually {@link Monster#dr} or
	 *          {@link Monster#energyresistance}, or 0 to ignore.
	 */
	public void damage(int damage,int reduction){
		damage-=reduction;
		if(damage<1) damage=1;
		hp-=damage;
		if(hp<1) hp=1;
	}

	/**
	 * Some {@link Upgrade}s need further interaction from a human after they are
	 * applied. This method deals with all of these cleanups.
	 *
	 * @see #postupgradeautomatic(boolean, Upgrade)
	 */
	public void postupgrade(){
		for(Feat f:source.feats)
			f.postupgrade(this);
	}

	/**
	 * Like {@link #postupgrade(boolean, Upgrade)} but this handles all of these
	 * scenarios automatically - either because the player might not care enoug to
	 * hand-pick the the outcome or because we are upgrading an NPC like with
	 * {@link #upgrade(Collection)}.
	 */
	public void postupgradeautomatic(){
		for(Feat f:source.feats)
			f.postupgradeautomatic(this);
	}

	/**
	 * Passive {@link Monster}s don't rely on BattleAi, and use this instead.
	 *
	 * @see Monster#passive.
	 *
	 * @return Outcomes.
	 */
	public void act(BattleState s){
		// do nothing by default
	}

	public Point getlocation(){
		return new Point(location[0],location[1]);
	}

	public BattleMouseAction getmouseaction(){
		return null;
	}

	/**
	 * @param s If not <code>null</code>, will verify the current position and if
	 *          {@link Square#flooded}, returns {@link Monster#swim}. If
	 *          <code>null</code>, will never return swimming speed.
	 * @return The highest relevant spped available (in feet).
	 *
	 * @see Monster#burrow
	 * @see Monster#fly
	 * @see Monster#walk
	 */
	public int gettopspeed(BattleState s){
		if(burrowed) return source.burrow;
		if(source.fly!=0) return source.fly;
		if(s!=null&&s.map[location[0]][location[1]].flooded&&source.swim!=0)
			return source.swim;
		return source.walk;
	}

	/**
	 * @return Rolls a d20 and adds the given
	 *         {@link Skill#getbonus(Combatant)}.Returns 0 if cannot use this
	 *         skill.
	 *
	 * @see Skill#canuse(Combatant)
	 * @see #taketen(Skill)
	 */
	public int roll(Skill s){
		return s.canuse(this)?RPG.r(1,20)+s.getbonus(this):0;
	}

	/**
	 * This should be preferred to {@link #roll(Skill)} on any circumstance where
	 * retrying is possible. We don't want players to bore/game/grind themselves
	 * by retrying an action 20 times until they reach the best possible result.
	 *
	 * @return Takes an automatic 10 on a d20 ("take 10" rule) and adds the given
	 *         {@link Skill#getbonus(Combatant)}. Returns 0 if cannot use this
	 *         skill.
	 *
	 * @see Skill#canuse(Combatant)
	 */
	public int taketen(Skill s){
		return s.canuse(this)?10+s.getbonus(this):0;
	}

	/**
	 * @return <code>true</code> if can decioher a {@link Spell} from a
	 *         {@link Scroll} or {@link Wand}.
	 */
	public boolean decipher(Spell s){
		if(taketen(Skill.USEMAGICDEVICE)>=15+s.level) return true;
		if(!source.think(-2)) return false;
		int spellcraft=Skill.SPELLCRAFT.getranks(this);
		int ability=Math.max(source.intelligence,source.wisdom);
		ability=Math.max(ability,source.charisma);
		return ability+spellcraft/2>=10+s.level;
	}

	public boolean concentrate(Spell s){
		return Skill.CONCENTRATION.getbonus(this)>=s.casterlevel;
	}

	/**
	 * @param flyingbonus <code>true</code> if flying creatures get a bonus for
	 *          seeing farther.
	 * @param weatherpenalty <code>true</code> if {@link Weather} should influence
	 *          this roll.
	 * @param periodpenalty Penalty according to {@link Monster#vision} and
	 *          {@link Javelin#getperiod()}.
	 * @return Total {@link Perception} roll bonus modifier as in
	 *         {@link Perception#getbonus(Combatant)}.
	 */
	public int perceive(boolean flyingbonus,boolean weatherpenalty,
			boolean periodpenalty){
		int p=Skill.PERCEPTION.getbonus(this);
		if(flyingbonus&&source.fly>0) p+=2;
		if(weatherpenalty&&Weather.current!=Weather.CLEAR)
			p+=Weather.current==Weather.STORM?-4:-2;
		if(periodpenalty) /* half because they apply only to vision, not listening */
			p+=source.see()/2;
		return p;
	}

	/**
	 * @return Effective armor class.
	 */
	public int getac(){
		return source.ac+acmodifier;
	}

	/**
	 * See d20 flanking rules.
	 *
	 * @param target Unit that is potentially being flanked.
	 * @return <code>true</code> if there is a third unit flanking the target.
	 */
	public boolean flank(Combatant target,BattleState s){
		if(burrowed||getlocation().distanceinsteps(target.getlocation())!=1)
			return false;
		List<Combatant> team=getteam(s);
		if(team.contains(target)) return false;
		int deltax=target.location[0]-location[0];
		int deltay=target.location[1]-location[1];
		int flankx=target.location[0]+deltax;
		int flanky=target.location[1]+deltay;
		Combatant flank=s.getcombatant(flankx,flanky);
		return flank!=null&&!flank.burrowed&&team.contains(flank);
	}

	/**
	 * @param u Applies this, taking into consideration validation (using an
	 *          internal clone) and {@link #xp}.
	 * @return <code>false</code> if couldn't be upgraded or not enough XP. If
	 *         <code>true</code>, also substracts {@link #xp} and updates
	 *         {@link Monster#cr}.
	 */
	public boolean upgrade(Upgrade u){
		Combatant clone=clone().clonesource();
		if(!u.upgrade(clone)) return false;
		float xpcost=ChallengeCalculator.calculaterawcr(clone.source)[1]
				-ChallengeCalculator.calculaterawcr(source)[1];
		if(xpcost>xp.floatValue()) return false;
		xp=new BigDecimal(xp.floatValue()-xpcost);
		u.upgrade(this);
		ChallengeCalculator.calculatecr(source);
		return true;
	}

	/**
	 * @param ap Rolls initative on top of this Action Point value then updates
	 *          {@link #ap} and {@link #initialap}.
	 */
	public void rollinitiative(float ap){
		rollinitiative();
		this.ap+=ap;
		initialap=this.ap;
	}

	/**
	 * Note that this does not deduct from {@link Squad#gold} at all.
	 *
	 * @return How much to pay daily if this is a {@link #mercenary}. 0 otherwise.
	 *
	 * @see MercenariesGuild#getfee(Monster)
	 * @see Squad#paymercenaries()
	 */
	public int pay(){
		return MercenariesGuild.getfee(source);
	}

	/**
	 * Called when unit dies in battle.
	 *
	 * @see EndBattle
	 */
	public void bury(){
		ArrayList<Item> bag=Squad.active.equipment.get(this);
		Squad.active.remove(this);
		//TODO expire all effects
		//TODO unequip artifacts as well
		if(Fight.victory) for(Item i:bag)
			i.grab();
		MercenariesGuild.die(this);
		if(!summoned&&!mercenary) Ressurect.dead=this;
	}

	/**
	 * Removes a squad member and returns it to the {@link MercenariesGuild} if a
	 * mercenary.
	 */
	public void dismiss(Squad s){
		s.members.remove(this);
		for(Item i:s.equipment.get(this))
			i.grab();
		s.remove(this);
		for(MercenariesGuild g:s.sortbydistance(MercenariesGuild.getguilds()))
			if(g.all.contains(this)){
				g.receive(this);
				return;
			}
	}

	/**
	 * Depends on {@link Monster#armor} being kept up-to-date.
	 *
	 * @return Armor Class disregarding any armour.
	 */
	public int gettouchac(){
		return getac()-source.armor;
	}
}
