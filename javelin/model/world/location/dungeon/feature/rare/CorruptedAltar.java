package javelin.model.world.location.dungeon.feature.rare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.consumable.Eidolon;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.focus.Rod;
import javelin.model.item.focus.Staff;
import javelin.model.item.focus.Wand;
import javelin.model.item.gear.rune.Rune;
import javelin.model.item.gear.rune.RuneGear;
import javelin.model.item.potion.Flask;
import javelin.model.item.potion.Potion;
import javelin.model.item.precious.PreciousObject;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.old.RPG;

/**
 * Allows you to sacrifice one or more items and have it "chaotically" generate
 * a new item. The main objective of these Altars are:
 *
 * - Thematic. They're very risky but also can deliver any item in the game,
 * while Shops and treasure are limited by nature. They even (very loosely)
 * mimic the concept of addiction as even on a good result you can be tempted to
 * try again for better odds (keeping in mind that even a lower-value item can
 * be more powerful in the right circumstances).
 *
 * - It's a "junk disposal" system that works as an alternative for simply going
 * back to Town and selling things - to the point players may want to find or go
 * back to an Altar rather than selling. Selling is good if you know what you
 * want to buy with the gold received (including services) but this is not only
 * objectively better returns (~90% over ~50%) but it can return you any item in
 * the game, theoretically, even if the chances for the best-case-scenario are
 * much smaller compared to just being it if you can find it in a Shop (also
 * slim in themselves). So, basically, an alternative "vendoring" system with
 * plenty of virtues and flaws that should stand on its own.
 *
 * - Similar to the last but as an "upgrade system" rather than "junk disposal"
 * - is your Staff or Rune Gear getting underpowered after being used for 10
 * levels straight? Put it in an Altar, throw some precious gems in there for
 * good measure and you have a good chance of getting something better suited to
 * your current power level on the other side of it.
 *
 * @author alex
 */
public class CorruptedAltar extends Feature{
	static final String PROMPT="Do you want to sacrifice items at this altar of corruption (%s bias)?\n\n"
			+"Press ENTER to select items or any other key to cancel...";
	static final List<Bias> BIASES=List.of(
			new Bias("Gear",List.of(RuneGear.class,Rune.class)),
			new Bias("Focus",List.of(Rod.class,Wand.class,Staff.class)),
			new Bias("Consumable",
					List.of(Potion.class,Flask.class,Scroll.class,Eidolon.class)));
	static final String CONFIRM="Are you sure you want to sacrifice these items?\n"
			+"%s.\n"+"Press s to sacrifice and any other key to cancel...";

	static class Bias implements Serializable{
		List<Class<? extends Item>> items;
		String name;

		Bias(String name,List<Class<? extends Item>> items){
			this.name=name;
			this.items=items;
		}

		boolean matches(Item i){
			for(var item:items)
				if(item.isInstance(i)) return true;
			return false;
		}

		static Bias get(Item i){
			for(var b:BIASES)
				if(b.matches(i)) return b;
			return null;
		}

		ItemSelection getcandidates(){
			var candidates=new ItemSelection();
			for(var i:items)
				candidates.addAll(Item.getall(i));
			return RPG.shuffle(candidates);
		}
	}

	Bias bias=RPG.pick(BIASES);
	boolean revealed=false;
	int knowledgedc;

	/** Constructor. */
	public CorruptedAltar(DungeonFloor f){
		super("Altar of corruption","corruptedaltar");
		knowledgedc=getdc(f);
		remove=false;
	}

	List<Item> choose(){
		var sacrifice=new ArrayList<Item>();
		var e=Squad.active.equipment;
		var candidates=e.getall().stream().filter(i->i.sell())
				.collect(Collectors.toList());
		while(!candidates.isEmpty()){
			var descriptions=candidates.stream()
					.map(c->e.getowner(c)+"'s "+c.toString().toLowerCase())
					.collect(Collectors.toList());
			var choice=Javelin.choose("Sacrifice which items?",descriptions,true,
					false);
			if(choice<0) break;
			sacrifice.add(candidates.remove(choice));
		}
		return sacrifice;
	}

	boolean confirm(List<Item> sacrifice){
		var items=String.join(", ",
				sacrifice.stream().map(s->s.toString()).collect(Collectors.toList()));
		return Javelin.prompt(String.format(CONFIRM,items))=='s';
	}

	Item sacrifice(List<Item> sacrifice){
		Bias b=null;
		if(RPG.chancein(2))
			b=bias;
		else{
			var nonprecious=sacrifice.stream()
					.filter(s->!(s instanceof PreciousObject))
					.collect(Collectors.toList());
			if(!nonprecious.isEmpty()) b=Bias.get(RPG.pick(nonprecious));
		}
		var unbiased=Item.randomize(Item.NONPRECIOUS);
		List<Item> candidates=b==null?unbiased:b.getcandidates();
		var value=0;
		for(var s:sacrifice)
			value+=s.price;
		var from=value;
		var to=value;
		while(from>1){
			from*=.5;
			to*=.9;
			var fromfinal=from;
			var tofinal=to;
			var match=candidates.stream()
					.filter(c->fromfinal<=c.price&&c.price<=tofinal).findAny()
					.orElse(null);
			if(match==null&&candidates!=unbiased)
				match=unbiased.stream().filter(u->fromfinal<=u.price&&u.price<=tofinal)
						.findAny().orElse(null);
			if(match!=null) return match;
		}
		return null;
	}

	@Override
	public boolean activate(){
		var bias="unknown";
		if(!revealed) revealed=Squad.active.getbest(Skill.KNOWLEDGE)
				.taketen(Skill.KNOWLEDGE)>=knowledgedc;
		if(revealed) bias=this.bias.name.toLowerCase();
		if(Javelin.prompt(String.format(PROMPT,bias))!='\n') return false;
		var offering=choose();
		if(offering.isEmpty()||!confirm(offering)) return false;
		var result=sacrifice(offering);
		if(result==null){
			Javelin.message("Nothing happens...",true);
			return false;
		}
		for(var o:offering)
			Squad.active.equipment.remove(o);
		result.grab();
		return true;
	}

	public static void test(DungeonFloor f,boolean matching,boolean verbose){
		var random=BIASES.stream()
				.flatMap(b->b.getcandidates().subList(0,9).stream())
				.collect(Collectors.toSet());
		for(var b:BIASES){
			var a=new CorruptedAltar(f);
			a.bias=b;
			System.out.println("Bias: "+b.name);
			var items=matching?b.getcandidates():random;
			var biased=0;
			for(var i:items){
				var sacrifice="  Sacrifice %s: get %s";
				var result=a.sacrifice(List.of(i));
				sacrifice=String.format(sacrifice,i,result);
				if(verbose) System.out.println(sacrifice);
				if(b.matches(result)) biased+=1;
			}
			var matches=String.format("  %s%% match bias",100*biased/items.size());
			System.out.println(matches);
			System.out.println();
		}
	}
}
