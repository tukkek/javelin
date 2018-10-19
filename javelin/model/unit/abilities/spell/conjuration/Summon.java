package javelin.model.unit.abilities.spell.conjuration;

import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
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
 * @author alex
 */
public class Summon extends Spell{
	static final int[] DISPLACE=new int[]{-1,0,+1};

	static final float CRFACTOR=5f;

	public String monstername;
	float chance;

	public Summon(String monstername,float chance){
		super("Summon "+monstername.toLowerCase(),0,0,Realm.MAGIC);
		assert chance==1;// cannot be a Spell if random
		this.monstername=monstername;
		this.chance=chance;
		castinbattle=true;
		if(!Javelin.MONSTERSBYCR.isEmpty()) postloadmonsters();
		isring=false;
	}

	public Summon(String name){
		this(name,1);
	}

	/**
	 * Chance is applied as a normal %.
	 *
	 * TODO isn't taking into account summoning a group.
	 */
	public static float ratechallenge(String monster,float chance){
		Monster m=Javelin.getmonster(monster);
		float cr=m.cr/((5+2)/2f);
		return chance*cr;
	}

	@Override
	public String cast(Combatant caster2,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		List<Combatant> team=target.getteam(s);
		Monster m=Javelin.getmonster(monstername);
		m.name="Summoned "+m.name.toLowerCase();
		Combatant summoned=new Combatant(m,true);
		place(target,summoned,team,s);
		cn.overlay=new AiOverlay(summoned.getlocation());
		return "";// default message is enough
	}

	static public void place(Combatant summoner,Combatant summoned,
			List<Combatant> team,BattleState s){
		team.add(summoned);
		summoned.summoned=true;
		summoned.automatic=true;
		/*
		 * TODO test if this is called after the CastSpell AP has already been
		 * spent
		 */
		summoned.ap=summoner.ap;
		summoned.initialap=summoned.ap;
		final Square[][] map=s.map;
		int x=summoner.location[0];
		int y=summoner.location[1];
		while(s.getcombatant(x,y)!=null||map[x][y].blocked){
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
		Monster m=Javelin.getmonster(monstername);
		if(m==null) throw new RuntimeException("Unknown summon: "+monstername);
		return ChallengeCalculator.calculatecr(c.source)>=m.cr&&super.apply(c);
	}

	@Override
	public boolean canbecast(Combatant c){
		return !c.summoned&&super.canbecast(c);
	}

	@Override
	public void postloadmonsters(){
		cr=ratechallenge(monstername,chance);
		level=Math.round(Javelin.getmonster(monstername).cr/2);
		casterlevel=calculatecasterlevel(level);
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
}
