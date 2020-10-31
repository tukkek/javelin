package javelin.model.world.location.town.labor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.ContentSummary;
import javelin.controller.kit.Fighter;
import javelin.controller.kit.dragoon.BlackDragoon;
import javelin.controller.kit.dragoon.BlueDragoon;
import javelin.controller.kit.dragoon.GreenDragoon;
import javelin.controller.kit.dragoon.RedDragoon;
import javelin.controller.kit.dragoon.WhiteDragoon;
import javelin.model.unit.abilities.discipline.serpent.SteelSerpent;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Cancel;
import javelin.model.world.location.town.labor.basic.Growth;
import javelin.model.world.location.town.labor.basic.Lodge.BuildLodge;
import javelin.model.world.location.town.labor.basic.MiniatureParlor.BuildMiniatureParlor;
import javelin.model.world.location.town.labor.basic.Redraw;
import javelin.model.world.location.town.labor.basic.Shop.BuildShop;
import javelin.model.world.location.town.labor.criminal.Sewers.BuildSewers;
import javelin.model.world.location.town.labor.criminal.Slums.BuildSlums;
import javelin.model.world.location.town.labor.criminal.ThievesGuild.BuildThievesGuild;
import javelin.model.world.location.town.labor.cultural.BardsGuild.BuildBardsGuild;
import javelin.model.world.location.town.labor.cultural.MagesGuild.BuildMagesGuild;
import javelin.model.world.location.town.labor.cultural.RuneShop.BuildRuneShop;
import javelin.model.world.location.town.labor.ecological.ArcheryRange.BuildArcheryRange;
import javelin.model.world.location.town.labor.ecological.Henge.BuildHenge;
import javelin.model.world.location.town.labor.ecological.MeadHall.BuildMeadHall;
import javelin.model.world.location.town.labor.expansive.BuildHighway;
import javelin.model.world.location.town.labor.expansive.BuildRoad;
import javelin.model.world.location.town.labor.expansive.Hub.BuildTransportHub;
import javelin.model.world.location.town.labor.military.Monastery.BuildMonastery;
import javelin.model.world.location.town.labor.productive.Deforestate;
import javelin.model.world.location.town.labor.productive.Mine.BuildMine;
import javelin.model.world.location.town.labor.religious.Sanctuary.BuildSanctuary;
import javelin.model.world.location.town.labor.religious.Shrine.BuildShrine;
import javelin.model.world.location.unique.Artificer.BuildArtificer;
import javelin.model.world.location.unique.MercenariesGuild.BuildMercenariesGuild;
import javelin.model.world.location.unique.NinjaDojo.BuildNinjaDojo;
import javelin.model.world.location.unique.SummoningCircle.BuildSummoningCircle;

/**
 * This class provides the deck-building mini-game logic for {@link Labor}
 * cards. It is not a model entity, making it easier to develop as it can be
 * more freely altered during the course of a game. It is then fully loaded and
 * processed when the game starts.
 *
 * @author alex
 */
public class LaborDeck extends ArrayList<Labor>{
	/** All {@link Town} {@link Trait}s. */
	public static final List<Trait> TRAITS=new ArrayList<>(7);
	static final boolean DEBUG=false;

	static final Labor[] BASE=new Labor[]{
			/*new BuildDwelling(),*/new BuildLodge(),new Cancel(),new Growth(),
			new Redraw(),new BuildShop(),new BuildMiniatureParlor(),new BuildMine()};
	static final Labor[] CRIMINAL=new Labor[]{new BuildNinjaDojo(),
			new BuildSewers(),new BuildSlums(),new BuildThievesGuild(),
			BlackDragoon.INSTANCE.buildguild()};
	static final Labor[] MAGICAL=new Labor[]{new BuildMagesGuild(),
			new BuildArtificer(),new BuildSummoningCircle(),new BuildBardsGuild(),
			new BuildRuneShop()};
	static final Labor[] NATURAL=new Labor[]{new BuildHenge(),
			new BuildArcheryRange(),new BuildMeadHall(),SteelSerpent.LABOR,
			GreenDragoon.INSTANCE.buildguild(),BlueDragoon.INSTANCE.buildguild(),
			WhiteDragoon.INSTANCE.buildguild()};
	static final Labor[] EXPANSIVE=new Labor[]{new BuildRoad(),new BuildHighway(),
			new BuildTransportHub()};
	static final Labor[] MILITARY=new Labor[]{new BuildMercenariesGuild(),
			new BuildMonastery(),Fighter.INSTANCE.buildguild(),
			RedDragoon.INSTANCE.buildguild()};
	static final Labor[] MERCANTILE=new Labor[]{new Deforestate()};
	static final Labor[] RELIGIOUS=new Labor[]{new BuildShrine(),
			new BuildSanctuary()};

	/** List of {@link LaborDeck}s by {@link Trait} names. */
	public static final HashMap<String,LaborDeck> DECKS=new HashMap<>();
	static final LaborDeck DEFAULT=new LaborDeck();

	static{
		populate(DEFAULT,null,BASE);
		if(World.scenario.allowlabor){
			populate(new LaborDeck(),Trait.EXPANSIVE,EXPANSIVE);
			populate(new LaborDeck(),Trait.MERCANTILE,MERCANTILE);
			populate(new LaborDeck(),Trait.MILITARY,MILITARY);
			populate(new LaborDeck(),Trait.MAGICAL,MAGICAL);
			populate(new LaborDeck(),Trait.CRIMINAL,CRIMINAL);
			populate(new LaborDeck(),Trait.RELIGIOUS,RELIGIOUS);
			populate(new LaborDeck(),Trait.NATURAL,NATURAL);
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

	/**
	 * @return A {@link Labor} deck with all of the {@link Town} {@link Trait}
	 *         decks shuffled into tt. If the town has no {@link Trait}s, will
	 *         only have the {@link #DEFAULT} {@link Labor} cards.
	 */
	public static ArrayList<Labor> generate(Town t){
		var d=new LaborDeck();
		d.addAll(DEFAULT);
		for(String trait:t.traits)
			d.addAll(DECKS.get(trait));
		Collections.shuffle(d);
		if(Javelin.DEBUG&&DEBUG&&!t.ishostile()) d.add(0,new Redraw());
		return d;
	}

	static void populate(LaborDeck d,String title,Labor[] cards){
		for(Labor l:cards)
			d.add(l);
		if(title!=null) DECKS.put(title,d);
	}

	/**
	 * @return <code>true</code> if {@link Labor} is from the {@link #DEFAULT}
	 *         deck.
	 */
	public static boolean isbasic(Labor card){
		for(Labor l:DEFAULT)
			if(card.getClass().equals(l.getClass())) return true;
		return false;
	}

	/** @see ContentSummary */
	public static String getsummary(){
		int count=DEFAULT.size();
		for(var d:DECKS.values())
			count+=d.size();
		var detailed=DECKS.keySet().stream().sorted()
				.map(t->DECKS.get(t).size()+" "+t).collect(Collectors.joining(", "));
		detailed=BASE.length+" basic, "+detailed;
		return DECKS.size()+" town traits, "+count+" district projects ("+detailed
				+")";
	}
}
