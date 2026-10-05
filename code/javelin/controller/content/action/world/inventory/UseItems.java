package javelin.controller.content.action.world.inventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.collection.CountingSet;
import javelin.controller.content.action.world.WorldAction;
import javelin.model.item.Item;
import javelin.model.item.gear.Gear;
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
  /** Singleton. */
  public static final UseItems INSTANCE=new UseItems();

  static final ArrayList<Character> KEYS=SelectScreen.filterkeys("desq");
  static final String COMMANDS="Press key to use item or (d)iscard item, (e)xchange item, (s)how %s, (q)uit.";
  static final String DISCARD="""
      Discard %s?

      Press d to discard, any other key to cancel...
      """;

  public static boolean skiperror=false;

  boolean stayopen=false;
  /** TODO should probably be tabs in 2.0 */
  String switchto="gear";

  /** Constructor. */
  UseItems(){
    super("Use items",new int[]{},new String[]{"i"});
  }

  /** Constructor for subclasses. */
  protected UseItems(String name,int[] keysp,String[] morekeysp){
    super(name,keysp,morekeysp);
  }

  @Override
  public void perform(WorldScreen worldscreen){
    skiperror=false;
    Squad.active.sort();
    while(true){
      var infoscreen=new InfoScreen("");
      var actions=COMMANDS.formatted(switchto);
      var allitems=new ArrayList<Item>();
      var list=listitems(allitems,true);
      infoscreen.print(actions+"\n"+list);
      if(executecommand(allitems,list,infoscreen)) break;
    }
    Javelin.app.switchScreen(JavelinApp.context);
  }

  /** Toggle between {@link UseItems} and {@link EquipGear}. */
  protected WorldAction switchscreen(){
    return EquipGear.INSTANCE;
  }

  boolean executecommand(ArrayList<Item> allitems,String list,
      InfoScreen infoscreen){
    Javelin.app.switchScreen(infoscreen);
    var input=InfoScreen.feedback();
    if(input=='d'){
      var i=select(allitems,infoscreen);
      if(i==null) return false;
      var name=i.toString().toLowerCase();
      Javelin.app.switchScreen(new InfoScreen(DISCARD.formatted(name)));
      if(InfoScreen.feedback()=='d') Squad.active.equipment.remove(i);
      return false;
    }
    if(input=='e'){
      exchange(allitems,list,infoscreen);
      return false;
    }
    if(input=='s'){
      switchscreen().perform(null);
      return true;
    }
    if(input=='q') return true;// leaves screen
    var selected=select(allitems,input);
    if(selected==null) return false;
    if(use(infoscreen,selected)) return !stayopen;
    if(skiperror) return false;
    var error=selected.describefailure();
    if(error!=null){
      infoscreen.print(infoscreen.text+"\n\n"+error+"...");
      InfoScreen.feedback();
    }
    return false;
  }

  boolean use(InfoScreen infoscreen,Item i){
    if(!i.usedoutofbattle){
      Item.failure="Can only be used in combat";
      return false;
    }
    Combatant target=null;
    if(i.targeted) target=inputmember(
        "Which member will use the "+i.toString().toLowerCase()+"?");
    if(!i.usepeacefully(target)) return false;
    if(i.consumable) i.expend();
    return true;
  }

  Item select(ArrayList<Item> allitems,Character input){
    var index=KEYS.indexOf(input);
    return 0<=index&&index<allitems.size()?allitems.get(index):null;
  }

  void exchange(ArrayList<Item> allitems,String reequiptext,
      InfoScreen infoscreen){
    var i=select(allitems,infoscreen);
    if(i==null) return;
    var owner=findowner(i);
    owner.unequip(i);
    var s=Squad.active;
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
      for(Item i:Squad.active.equipment.get(c)) if(i==selected) return c;
    throw new RuntimeException("Item owner not found #useitems");
  }

  int count(Item it,List<Item> allitems){
    var count=0;
    for(Item i:allitems) if(i.equals(it)) count+=1;
    return count;
  }

  Combatant inputmember(String message){
    ArrayList<Combatant> members=Squad.active.members;
    List<String> choices=members.stream()
        .map(member->member+" ("+member.getstatus()+")")
        .collect(Collectors.toList());
    return members.get(Javelin.choose(message,choices,true,true));
  }

  /** @return Items to be shown on this screen, from all given. */
  protected List<Item> filter(List<Item> items){
    return items.stream().filter(i->i.usedoutofbattle||!(i instanceof Gear))
        .collect(Collectors.toList());
  }

  /**
   * @param allitems Adds items to this list if not <code>null</code>.
   * @param showkeys If <code>true</code> will prepend each item with a key from
   *   #KEYS.
   * @return A textual listing.
   */
  public String listitems(ArrayList<Item> allitems,boolean showkeys){
    var s="";
    var keys=KEYS.iterator();
    for(var c:filtermercenaries(Squad.active.members)){
      var bag=filter(Squad.active.equipment.get(c));
      if(bag.isEmpty()) continue;
      s+="\n"+c+"\n";
      s+=listbag(allitems,showkeys,bag,c,keys);
    }
    return s;
  }

  static String listbag(List<Item> allitems,boolean showkeys,List<Item> bag,
      Combatant c,Iterator<Character> keys){
    var count=new CountingSet();
    count.casesensitive=true;
    var map=new TreeMap<String,Item>();
    for(Item i:bag){
      var description=i.describe(c);
      count.add(description);
      map.put(description,i);
    }
    var s="";
    for(String d:map.keySet()){
      var i=map.get(d);
      if(allitems!=null) allitems.add(i);
      if(showkeys) s+=" ["+keys.next()+"]";
      s+=" "+d;
      var n=count.getcount(d);
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
    var members=new ArrayList<Combatant>(all.size());
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
    var options=new ArrayList<String>(members.size());
    for(Combatant c:members){
      var option=c.toString();
      var invalid=i.canuse(c);
      if(invalid!=null) option+=" ("+invalid.toLowerCase()+")";
      options.add(option);
    }

    return members.get(Javelin.choose(text,options,true,true));
  }
}
