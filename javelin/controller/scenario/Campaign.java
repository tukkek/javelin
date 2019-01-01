package javelin.controller.scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.Debug;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.UnbalancedTeams;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.classes.Commoner;
import javelin.model.Realm;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.item.Item;
import javelin.model.item.key.TempleKey;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.governor.HumanGovernor;
import javelin.model.world.location.town.governor.MonsterGovernor;
import javelin.old.RPG;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.world.WorldTile;
import javelin.view.screen.WorldScreen;

/**
 * Main game mode, full on strategy-RPG.
 *
 * @author alex
 */
public class Campaign extends Scenario{
	/** Minimum starting party encounter level. */
	public static final float INITIALEL;

	static final ArrayList<Point> ALLTILES=new ArrayList<>();

	static{
		ArrayList<Float> crs=new ArrayList<>(4);
		for(int i=0;i<4;i++)
			crs.add(1f);
		try{
			INITIALEL=ChallengeCalculator.calculateelfromcrs(crs,false);
		}catch(UnbalancedTeams e){
			throw new RuntimeException(e);
		}
	}

	/** <code>true</code> to allow {@link Diplomacy}. */
	protected boolean diplomacy=true;

	/** Constructor. */
	public Campaign(){
		allowallactors=true;
		lockedtemples=true;
		allowlabor=true;
		asksquadnames=true;
		desertradius=2;
		fogofwar=true;
		helpfile="Campaign";
		easystartingtown=true;
		minigames=true;
		normalizemap=false;
		record=true;
		respawnlocations=true;
		size=30;
		startingdungeons=20;
		statictowns=false;
		towns=Realm.values().length;
		worldencounters=true;
		worldhazards=true;
		dominationwin=false;
		startingfeatures=size*size/5;
		simpletroves=false;
		boost=1;
		randomrealms=false;
		worlddistrict=false;
		spawn=true;
		quests=true;
		diplomacy=true;
		urbanevents=true;
	}

	@Override
	public void upgradesquad(ArrayList<Combatant> squad){
		float startingcr=totalcr(squad);
		while(ChallengeCalculator.calculateel(squad)<INITIALEL){
			ArrayList<Upgrade> u=new ArrayList<>();
			u.add(Commoner.SINGLETON);
			Combatant.upgradeweakest(squad,u);
		}
		Squad.active.gold=RewardCalculator.getgold(totalcr(squad)-startingcr);
	}

	static float totalcr(ArrayList<Combatant> squad){
		int cr=0;
		for(Combatant c:squad)
			cr+=ChallengeCalculator.calculatecr(c.source);
		return cr;
	}

	@Override
	public boolean checkfullsquad(ArrayList<Combatant> squad){
		return ChallengeCalculator.calculateel(squad)>=INITIALEL;
	}

	@Override
	public boolean win(){
		return false;
	}

	@Override
	public List<Location> generatestartinglocations(){
		return Collections.EMPTY_LIST;
	}

	@Override
	public Item openspecialchest(){
		return TempleKey.generate();
	}

	@Override
	public Item openaltar(Temple t){
		return t.relic;
	}

	@Override
	public void ready(){
		if(diplomacy) Diplomacy.instance=new Diplomacy(Town.gettowns());
	}

	@Override
	public void endday(){
		cover(2);
		if(Diplomacy.instance!=null) Diplomacy.instance.turn();
	}

	/** Covers an amount of {@link WorldTile} per day with fog of war. */
	void cover(int amount){
		if(Debug.showmap) return;
		if(ALLTILES.isEmpty()) ALLTILES.addAll(Point.getrange(0,0,size,size));
		var stayrevealed=new HashSet<Point>();
		MapPanel mappanel=WorldScreen.current.mappanel;
		for(Actor a:World.getactors()){
			if(!mappanel.tiles[a.x][a.y].discovered) continue;
			var point=a.getlocation();
			stayrevealed.add(point);
			Location l=a instanceof Location?(Location)a:null;
			if(l==null) continue;
			var vision=l.watch();
			if(vision>0) for(var x=point.x-vision;x<=point.x+vision;x++)
				for(var y=point.y-vision;y<=point.y+vision;y++)
					stayrevealed.add(new Point(x,y));
		}
		for(Point p:RPG.shuffle(ALLTILES)){
			Tile t=mappanel.tiles[p.x][p.y];
			if(!t.discovered||World.seed.roads[t.x][t.y]||stayrevealed.contains(p))
				continue;
			t.cover();
			amount-=1;
			if(amount==0) break;
		}
		Squad.updatevision();
	}

	@Override
	public void populate(Town t,boolean starting){
		t.setgovernor(starting?new HumanGovernor(t):new MonsterGovernor(t));
		if(!starting) t.populate(RPG.r(1,4));
	}
}