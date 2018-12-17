package javelin.model.world.location.town.labor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.model.unit.abilities.discipline.serpent.SteelSerpent;
import javelin.model.world.World;
import javelin.model.world.location.fortification.RealmAcademy.BuildRealmAcademy;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Cancel;
import javelin.model.world.location.town.labor.basic.Dwelling.BuildDwelling;
import javelin.model.world.location.town.labor.basic.Growth;
import javelin.model.world.location.town.labor.basic.Lodge.BuildLodge;
import javelin.model.world.location.town.labor.basic.Redraw;
import javelin.model.world.location.town.labor.criminal.Sewers.BuildSewers;
import javelin.model.world.location.town.labor.criminal.Slums.BuildSlums;
import javelin.model.world.location.town.labor.criminal.ThievesGuild.BuildThievesGuild;
import javelin.model.world.location.town.labor.cultural.BardsGuild.BuildBardsGuild;
import javelin.model.world.location.town.labor.cultural.MagesGuild.BuildMagesGuild;
import javelin.model.world.location.town.labor.ecological.ArcheryRange.BuildArcheryRange;
import javelin.model.world.location.town.labor.ecological.Henge.BuildHenge;
import javelin.model.world.location.town.labor.ecological.MeadHall.BuildMeadHall;
import javelin.model.world.location.town.labor.expansive.BuildHighway;
import javelin.model.world.location.town.labor.expansive.BuildRoad;
import javelin.model.world.location.town.labor.expansive.Hub.BuildTransportHub;
import javelin.model.world.location.town.labor.military.MartialAcademy.BuildMartialAcademy;
import javelin.model.world.location.town.labor.military.Monastery.BuildMonastery;
import javelin.model.world.location.town.labor.productive.Deforestate;
import javelin.model.world.location.town.labor.productive.Mine.BuildMine;
import javelin.model.world.location.town.labor.productive.Shop.BuildShop;
import javelin.model.world.location.town.labor.religious.Sanctuary.BuildSanctuary;
import javelin.model.world.location.town.labor.religious.Shrine.BuildShrine;
import javelin.model.world.location.unique.Artificer.BuildArtificer;
import javelin.model.world.location.unique.AssassinsGuild.BuildAssassinsGuild;
import javelin.model.world.location.unique.MercenariesGuild.BuildMercenariesGuild;
import javelin.model.world.location.unique.SummoningCircle.BuildSummoningCircle;

/**
 * This class provides the deck-building mini-game logic for {@link Labor}
 * cards. It is not a model entity, making it easier to develop as it can be
 * more freely altered during the course of a game. It is then fully loaded and
 * processed when the game starts.
 *
 * @author alex
 */
public class Deck extends ArrayList<Labor>{
	public static final List<Trait> TRAITS=new ArrayList<>(7);

	static final Labor[] BASE=new Labor[]{new BuildDwelling(),new BuildLodge(),
			new Cancel(),new Growth(),new Redraw(),new BuildShop(),
			new BuildRealmAcademy()};
	static final Labor[] CRIMINAL=new Labor[]{new BuildAssassinsGuild(),
			new BuildSewers(),new BuildSlums(),new BuildThievesGuild()};
	static final Labor[] MAGICAL=new Labor[]{new BuildMagesGuild(),
			new BuildArtificer(),new BuildSummoningCircle(),new BuildBardsGuild()};
	static final Labor[] NATURAL=new Labor[]{new BuildHenge(),
			new BuildArcheryRange(),new BuildMeadHall(),SteelSerpent.LABOR};
	static final Labor[] EXPANSIVE=new Labor[]{new BuildRoad(),new BuildHighway(),
			new BuildTransportHub()};
	static final Labor[] MILITARY=new Labor[]{new BuildMartialAcademy(),
			new BuildMercenariesGuild(),new BuildMonastery()};
	static final Labor[] MERCANTILE=new Labor[]{new BuildMine(),
			new Deforestate()};
	static final Labor[] RELIGIOUS=new Labor[]{new BuildShrine(),
			new BuildSanctuary()};

	public static final HashMap<String,Deck> DECKS=new HashMap<>();
	static final Deck DEFAULT=new Deck();

	static{
		populate(DEFAULT,null,BASE);
		if(World.scenario.allowlabor){
			populate(new Deck(),Trait.EXPANSIVE,EXPANSIVE);
			populate(new Deck(),Trait.MERCANTILE,MERCANTILE);
			populate(new Deck(),Trait.MILITARY,MILITARY);
			populate(new Deck(),Trait.MAGICAL,MAGICAL);
			populate(new Deck(),Trait.CRIMINAL,CRIMINAL);
			populate(new Deck(),Trait.RELIGIOUS,RELIGIOUS);
			populate(new Deck(),Trait.NATURAL,NATURAL);
			for(String title:new ArrayList<>(DECKS.keySet())){
				/*
				 * TODO just a placeholder to get rid on unused sets during
				 * development:
				 */
				if(DECKS.get(title).isEmpty()){
					DECKS.remove(title);
					continue;
				}
				Trait t=new Trait(title,DECKS.get(title));
				TRAITS.add(t);
				DEFAULT.add(t);
			}
		}
	}

	public static ArrayList<Labor> generate(Town t){
		Deck d=new Deck();
		d.addAll(DEFAULT);
		for(String trait:t.traits)
			d.addAll(DECKS.get(trait));
		Collections.shuffle(d);
		if(Javelin.DEBUG&&!t.ishostile()) d.add(0,new Redraw());
		return d;
	}

	static void populate(Deck d,String title,Labor[] cards){
		for(Labor l:cards)
			d.add(l);
		if(title!=null) DECKS.put(title,d);
	}

	public static boolean isbasic(Labor card){
		for(Labor l:DEFAULT)
			if(card.getClass().equals(l.getClass())) return true;
		return false;
	}

	public static String getsummary(){
		int count=DEFAULT.size();
		for(Deck d:DECKS.values())
			count+=d.size();
		var detailed=DECKS.keySet().stream().sorted()
				.map(t->DECKS.get(t).size()+" "+t).collect(Collectors.joining(", "));
		detailed=BASE.length+" basic, "+detailed;
		return DECKS.size()+" town traits, "+count+" district projects ("+detailed
				+")";
	}
}
