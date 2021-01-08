package javelin.model.unit.abilities.spell.conjuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.kit.wizard.Conjurer;
import javelin.controller.content.upgrade.Upgrade;
import javelin.controller.db.reader.MonsterReader;
import javelin.model.state.BattleState;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.town.labor.ecological.Henge;
import javelin.model.world.location.unique.SummoningCircle;
import javelin.old.RPG;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * Brings an ally to fight with your team.
 *
 * Upper Krust's method is not followed here since it makes no sense for a
 * Gelugon (CR21) that could summon another Gelugon to be CR21.4.
 *
 * The next attempt has been to have Spell CR equal Summoned CR/5 - the
 * rationale being that a summoned ally would only participate in one fight out
 * of 5 moderate encounters before resting. This has also proved naive.
 *
 * The current method averages the result of the previous one with that of the
 * spell being cast on round 1 by an enemy (who will always have the spell
 * available, unlike a party member). Instead of applying full CR, uses 50% of
 * it instead - first because a monster summoning a copy of itself doesn't
 * double its CR; second because it's not a guarantee that the monster will be
 * able to cast it before being engaged, killed, etc.
 *
 * TODO This should not be a {@link Spell}. See
 * {@link #cast(Combatant, Combatant, boolean, BattleState, ChanceNode)}
 *
 * Since we can't possibly add all {@link Summon} spell to the proper
 * {@link Conjurer} {@link Kit}, other locations are avialable for different
 * {@link MonsterType} summon spells, such as the {@link SummoningCircle} and
 * the {@link Henge}.
 *
 * @see Conjurer#SUMMONS
 * @author alex
 */
public class Summon extends Spell{
	static final int[] DISPLACE=new int[]{-1,0,+1};
	static final float CRFACTOR=5f;

	/**
	 * Summoned {@link Monster#name}. Since this can come from XML,
	 * {@link String#equalsIgnoreCase(String)} is recommended with it.
	 */
	public String monstername;
	float chance;
	/**
	 * Every summoning {@link Spell}, for each {@link Monster} available.
	 *
	 * Since we don't want these to completely overwhelm the kit, only one per
	 * {@link Spell#casterlevel} is registered with the kit iself. More can be
	 * accessed through {@link SummoningCircle}s.
	 */
	public static final List<Summon> SUMMONS=new ArrayList<>();

	/** Constructor. */
	public Summon(String monstername,float chance){
		super("Summon "+monstername.toLowerCase(),0,0);
		if(chance!=1) throw new RuntimeException("Cannot be a Spell if random!");
		this.monstername=monstername;
		this.chance=chance;
		castinbattle=true;
		if(!Monster.BYCR.isEmpty()) postloadmonsters();
		isscroll=false;
	}

	/**
	 * Chance is applied as a normal %.
	 *
	 * TODO isn't taking into account summoning a group.
	 */
	public static float ratechallenge(Monster m,float chance){
		float cr=m.cr/((5+2)/2f);
		return chance*cr;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		var m=Monster.get(monstername);
		var summoned=summon(m,caster,s);
		cn.overlay=new AiOverlay(summoned.getlocation());
		return "";// default message is enough
	}

	/**
	 * Summons a {@link Monster} near the caster. Does not provide player
	 * feedback.
	 *
	 * @return The location the monster was summone at.
	 */
	static public Combatant summon(Monster m,Combatant caster,BattleState s){
		List<Combatant> team=caster.getteam(s);
		m.customName="Summoned "+m.name.toLowerCase();
		Combatant summoned=new Combatant(m,true);
		place(caster,summoned,team,s);
		return summoned;
	}

	static public void place(Combatant summoner,Combatant summoned,
			List<Combatant> team,BattleState s){
		team.add(summoned);
		summoned.summoned=true;
		summoned.automatic=true;
		summoned.rollinitiative(summoner.ap);
		final Square[][] map=s.map;
		int x=summoner.location[0];
		int y=summoner.location[1];
		while(s.isblocked(x,y)){
			x+=DISPLACE[RPG.r(DISPLACE.length)];
			y+=DISPLACE[RPG.r(DISPLACE.length)];
			if(x<0||y<0||x>=map.length||y>=map.length){
				x=summoner.location[0];
				y=summoner.location[1];
			}
		}
		summoned.location[0]=x;
		summoned.location[1]=y;
	}

	@Override
	public int hit(Combatant active,Combatant target,BattleState state){
		return Integer.MIN_VALUE;
	}

	@Override
	public boolean apply(Combatant c){
		Monster m=Monster.get(monstername);
		if(m==null) throw new RuntimeException("Unknown summon: "+monstername);
		return ChallengeCalculator.calculatecr(c.source)>=m.cr&&super.apply(c);
	}

	@Override
	public boolean canbecast(Combatant c){
		return !c.summoned&&super.canbecast(c);
	}

	@Override
	public void postloadmonsters(){
		var m=Monster.get(monstername);
		cr=ratechallenge(m,chance);
		level=Math.round(m.cr/2f);
		if(level<1) level=1;
		casterlevel=getcasterlevel(level);
	}

	@Override
	public void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		targetself(combatant,targets);
	}

	@Override
	public boolean equals(Object obj){
		return super.equals(obj)&&monstername.equals(((Summon)obj).monstername);
	}

	/**
	 * Since there's one {@link Summon} per {@link Monster} in the game, this
	 * method helps summoning kits to offer a sensible amount of spell choices.
	 *
	 * @param pool What pool of Summons to choose from - usually
	 *          {@link Summon#ALLSUMMONS} or a filtered version of it.
	 * @param perlevel How many spell instances to return, for each level. Note
	 *          that this is the maximum limit, as it may not find enough (or any)
	 *          spells to fulfill this target.
	 * @param casterlevel Only gather summons of this given
	 *          {@link Spell#casterlevel}.
	 *
	 * @see Stream#filter(java.util.function.Predicate)
	 */
	public static List<Summon> select(List<Summon> pool,int perlevel,
			final int casterlevel){
		var tier=pool.stream().filter(s->s.level==casterlevel)
				.collect(Collectors.toList());
		return tier.subList(0,Math.min(perlevel,tier.size()));
	}

	/**
	 * @return The result of calling {@link #select(List, int, int)} with
	 *         {@link Spell#casterlevel} 1 through 9.
	 */
	public static ArrayList<Summon> select(List<Summon> pool,int perlevel){
		var summons=new ArrayList<Summon>(9*perlevel);
		for(var casterlevel=1;casterlevel<=9;casterlevel++)
			summons.addAll(select(pool,perlevel,casterlevel));
		return summons;
	}

	/**
	 * Unlike most {@link Upgrade}s, {@link Summon} {@link Spell}s need to be
	 * created after all {@link Monster}s are loaded.
	 *
	 * @see MonsterReader
	 */
	public static void setupsummons(){
		SUMMONS.addAll(Monster.ALL.stream().filter(m->!m.passive)
				.map(m->new Summon(m.name,1)).collect(Collectors.toList()));
		for(var k:Kit.KITS)
			k.finish();
	}
}
