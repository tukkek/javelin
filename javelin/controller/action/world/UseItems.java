package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.CountingSet;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;

/**
 * Use an {@link Item} out of battle.
 *
 * @see Item#usedoutofbattle
 * @author alex
 */
public class UseItems extends WorldAction{
	static final ArrayList<Character> KEYS=SelectScreen.filterkeys("deq");

	public static boolean skiperror=false;

	/** Constructor. */
	public UseItems(){
		super("Inventory",new int[]{},new String[]{"i"});
	}

	@Override
	public void perform(WorldScreen worldscreen){
		skiperror=false;
		Squad.active.sort();
		while(true){
			InfoScreen infoscreen=new InfoScreen("");
			String actions;
			actions="Press number to use an item\n";
			actions+="Press d to discard an item\n";
			actions+="Press e to exchange an item\n";
			actions+="Press q to quit the inventory\n";
			ArrayList<Item> allitems=new ArrayList<>();
			String list=listitems(allitems,true);
			infoscreen.print(actions+list);
			if(executecommand(allitems,list,infoscreen)) break;
		}
		Javelin.app.switchScreen(JavelinApp.context);
	}

	boolean executecommand(ArrayList<Item> allitems,String list,
			InfoScreen infoscreen){
		Javelin.app.switchScreen(infoscreen);
		Character input=InfoScreen.feedback();
		if(input=='q') return true;// leaves screen
		if(input=='e'){
			exchange(allitems,list,infoscreen);
			return false;
		}
		if(input=='d'){
			var i=select(allitems,infoscreen);
			if(i==null) return false;
			Javelin.app
					.switchScreen(new InfoScreen("Are you sure you want to discard "+i
							+"?\n\n"+"Press d to discard, any other key to cancel..."));
			if(InfoScreen.feedback()=='d') Squad.active.equipment.remove(i);
			return false;
		}
		Item selected=select(allitems,input);
		return selected!=null&&selected.usedoutofbattle&&use(infoscreen,selected);
	}

	boolean use(InfoScreen infoscreen,Item i){
		boolean isartifact=i instanceof Artifact;
		Combatant target=null;
		if(isartifact)
			target=findowner(i);
		else if(i.targeted) target=inputmember(
				"Which member will use the "+i.toString().toLowerCase()+"?");
		if(i.usepeacefully(target)){
			if(i.consumable) i.expend();
			return !isartifact;
		}
		if(skiperror) return false;
		String error=i.describefailure();
		infoscreen.print(infoscreen.text+"\n\n"+error+"...");
		InfoScreen.feedback();
		return false;
	}

	Item select(ArrayList<Item> allitems,Character input){
		int index=KEYS.indexOf(input);
		return 0<=index&&index<allitems.size()?allitems.get(index):null;
	}

	void exchange(ArrayList<Item> allitems,String reequiptext,
			InfoScreen infoscreen){
		Item i=select(allitems,infoscreen);
		if(i==null) return;
		Combatant owner=findowner(i);
		owner.unequip(i);
		Squad s=Squad.active;
		s.equipment.remove(i);
		var prompt="Transfer "+i.name+" to who?";
		s.equipment.get(selectmember(s.members,i,prompt)).add(i);
	}

	Item select(ArrayList<Item> allitems,InfoScreen infoscreen){
		infoscreen.print(infoscreen.text+"\n\nSelect an item.");
		return select(allitems,InfoScreen.feedback());
	}

	Combatant findowner(Item selected){
		for(Combatant c:Squad.active.members)
			for(Item i:Squad.active.equipment.get(c))
				if(i==selected) return c;
		throw new RuntimeException("Item owner not found #useitems");
	}

	int count(Item it,List<Item> allitems){
		int count=0;
		for(Item i:allitems)
			if(i.equals(it)) count+=1;
		return count;
	}

	Combatant inputmember(String message){
		ArrayList<Combatant> members=Squad.active.members;
		List<String> choices=members.stream()
				.map((member)->member+" ("+member.getstatus()+")")
				.collect(Collectors.toList());
		return members.get(Javelin.choose(message,choices,true,true));
	}

	/**
	 * @param allitems Adds items to this list if not <code>null</code>.
	 * @param showkeys If <code>true</code> will prepend each item with a key from
	 *          #KEYS.
	 * @return A textual listing.
	 */
	static public String listitems(ArrayList<Item> allitems,boolean showkeys){
		String s="";
		var keys=KEYS.iterator();
		for(Combatant c:filtermercenaries(Squad.active.members)){
			s+="\n"+c+":\n";
			ArrayList<Item> bag=Squad.active.equipment.get(c);
			s+=listbag(allitems,showkeys,bag,c,keys);
		}
		return s;
	}

	static String listbag(ArrayList<Item> allitems,boolean showkeys,
			ArrayList<Item> bag,Combatant c,Iterator<Character> keys){
		if(bag.isEmpty()) return "  carrying no items.\n";
		CountingSet count=new CountingSet();
		count.casesensitive=true;
		var map=new TreeMap<String,Item>();
		for(Item i:bag){
			String description=i.describe(c);
			count.add(description);
			map.put(description,i);
		}
		String s="";
		for(String d:map.keySet()){
			Item i=map.get(d);
			if(allitems!=null) allitems.add(i);
			if(showkeys) s+="  ["+keys.next()+"]";
			s+=" "+d;
			int n=count.getcount(d);
			if(n>1) s+=" x"+n;
			s+="\n";
		}
		return s;
	}

	/**
	 * Filters out mercenaries without items to save screen space.
	 *
	 * TODO this is a necessity due to the poor 1,0 series user interface. With
	 * 2.0+ should instead have a "show mercenaries" checkbox, allowing
	 * mercenaries to use items in this way.
	 */
	static ArrayList<Combatant> filtermercenaries(ArrayList<Combatant> all){
		ArrayList<Combatant> members=new ArrayList<>(all.size());
		for(Combatant c:all){
			if(c.mercenary&&Squad.active.equipment.get(c).isEmpty()) continue;
			members.add(c);
		}
		return members.isEmpty()?all:members;
	}

	/**
	 * @param members Eligible members.
	 * @param i Item in question. See {@link Item#canuse(Combatant)}.
	 * @return Selected member.
	 */
	public static Combatant selectmember(ArrayList<Combatant> members,Item i,
			String text){
		ArrayList<String> options=new ArrayList<>(members.size());
		for(Combatant c:members){
			String option=c.toString();
			String invalid=i.canuse(c);
			if(invalid!=null) option+=" ("+invalid.toLowerCase()+")";
			options.add(option);
		}

		return members.get(Javelin.choose(text,options,true,true));
	}
}
