package javelin.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.event.wild.WildEvents;
import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.quest.Quest;
import javelin.model.world.location.unique.UniqueLocation;

/**
 * Procuces a summary log of {@link Upgrade}s, {@link Item}s and general
 * content.
 *
 * @author alex
 */
public class ContentSummary{
	FileWriter out;

	public ContentSummary(){}

	void print(String line) throws IOException{
		out.write(line+"\n");
	}

	void print() throws IOException{
		print("");
	}

	/** stats */
	public void produce(){
		if(!Javelin.DEBUG) return;
		try{
			out=new FileWriter("content.log");
			printmisc();
			print();
			printoptions();
			out.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	void printoptions() throws IOException{
		var upgrades=UpgradeHandler.singleton.getall(false);
		var allupgrades=new ArrayList<>();
		var items=Item.getall();
		for(var realm:Realm.values()){
			var set=realm.getupgrades(UpgradeHandler.singleton);
			print(set,items,realm.getname());
			allupgrades.addAll(set);
		}
		var realms=Arrays.asList(Realm.values()).stream()
				.map(r->r.getname().toLowerCase()).sorted()
				.collect(Collectors.toList());
		for(var name:upgrades.keySet())
			if(!realms.contains(name.toLowerCase())){
				var set=upgrades.get(name);
				print(set,items,name);
				allupgrades.addAll(set);
			}
		for(Kit k:Kit.KITS)
			for(Upgrade u:k.basic)
				if(!(u instanceof Summon)&&!allupgrades.contains(u))
					throw new RuntimeException("Unregistered upgrade: "+u);
	}

	void print(HashSet<Upgrade> upgrades,HashMap<String,ItemSelection> items,
			String realm) throws IOException{
		int count=1;
		print(realm);
		for(Upgrade u:upgrades){
			print("\t"+count+" - "+u);
			count+=1;
		}
		print();
		ItemSelection inventory=items.get(realm);
		if(inventory==null) return;
		for(int i=0;i<inventory.size();i++){
			Item item=inventory.get(i).clone();
			print("\t"+count+" - "+item+" ($"+item.price+")");
			count+=1;
		}
		print();
	}

	static int countsummon(Collection<Spell> spells){
		int summon=0;
		for(Spell s:spells)
			if(s instanceof Summon) summon+=1;
		return summon;
	}

	void printmisc() throws IOException{
		print(Javelin.ALLMONSTERS.size()+" monsters");
		print(Item.ALL.size()-Item.ARTIFACT.size()+" items, "+Item.ARTIFACT.size()
				+" artifacts, 7 relics");
		Collection<Spell> spells=Spell.SPELLS.values();
		var upgrades=UpgradeHandler.singleton;
		upgrades.gather();
		int nskills=upgrades.countskills();
		int nupgrades=upgrades.count()-spells.size()-nskills;
		int nspells=spells.size()-countsummon(spells)+1;
		int nkits=Kit.KITS.size();
		print(nupgrades+" upgrades, "+nspells+" spells, "+nskills+" skills, "+nkits
				+" kits");
		printmaps();
		HashSet<Class<? extends Actor>> locationtypes=new HashSet<>();
		int uniquelocations=0;
		for(Actor a:World.getactors()){
			if(!(a instanceof Location)) continue;
			locationtypes.add(a.getClass());
			if(a instanceof UniqueLocation) uniquelocations+=1;
		}
		print(locationtypes.size()-uniquelocations+" world location types, "
				+uniquelocations+" unique locations");
		print(Deck.getsummary());
		print(Quest.printsummary());
		print(WildEvents.instance.printsummary("wilderness events"));
	}

	void printmaps() throws IOException{
		var total=Arrays.stream(Terrain.ALL)
				.collect(Collectors.summingInt(t->t.getmaps().size()));
		var byterrain=Arrays.stream(Terrain.ALL)
				.sorted((a,b)->a.name.compareTo(b.name))
				.map(t->t.getmaps().size()+" "+t).collect(Collectors.joining(", "));
		print(total+" battle maps ("+byterrain+")");
	}
}
