package javelin.controller.scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Debug;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.UnbalancedTeams;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.classes.Commoner;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.key.TempleKey;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.world.WorldTile;
import javelin.view.screen.WorldScreen;

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
		startingpopulation=1;
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
	public List<Location> generatestartinglocations(World seed){
		return Collections.EMPTY_LIST;
	}

	@Override
	public Item openspecialchest(Dungeon d){
		return TempleKey.generate();
	}

	@Override
	public Item openaltar(Temple t){
		return t.relic;
	}

	@Override
	public void endday(double day){
		cover(2);
	}

	/** Covers an amount of {@link WorldTile} per day with fog of war. */
	void cover(int amount){
		if(Debug.showmap) return;
		if(ALLTILES.isEmpty()) ALLTILES.addAll(Point.getrange(0,0,size,size));
		Collections.shuffle(ALLTILES);
		ArrayList<Location> locations=new ArrayList<>();
		ArrayList<Location> friendlylocations=new ArrayList<>();
		MapPanel mappanel=WorldScreen.current.mappanel;
		for(Actor a:World.getactors()){
			Location l=a instanceof Location?(Location)a:null;
			if(l!=null&&mappanel.tiles[a.x][a.y].discovered){
				locations.add(l);
				if(l.view()) friendlylocations.add(l);
			}
		}
		for(Point p:ALLTILES){
			Tile t=mappanel.tiles[p.x][p.y];
			if(checkcoverable(t,locations,friendlylocations)){
				t.cover();
				amount-=1;
				if(amount==0) break;
			}
		}
		Squad.updatevision();
	}

	boolean checkcoverable(Tile t,ArrayList<Location> locations,
			ArrayList<Location> friendlylocations){
		if(!t.discovered||World.seed.roads[t.x][t.y]) return false;
		for(Location l:locations)
			if(t.x==l.x&&t.y==l.y
					||friendlylocations.contains(l)&&l.distanceinsteps(t.x,t.y)<=l.vision)
				return false;
		return true;
	}
}