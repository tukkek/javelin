package javelin.controller.content.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.Point;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.RepeatTurn;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Period;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Town;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;

/** Split squad into two. */
public class Divide extends WorldAction{
  static final int TOWNBUFFER=1;

  /** Constructor. */
  public Divide(){
    super("Divide squad",new int[]{},new String[]{"d"});
  }

  boolean swap(List<Combatant> indexreference,char input,
      List<Combatant> oldsquad,List<Combatant> newsquad){
    Combatant swap;
    try{
      swap=indexreference.get(SelectScreen.convertkeytoindex(input));
    }catch(final IndexOutOfBoundsException|NumberFormatException e){
      return false;
    }
    List<Combatant> from;
    List<Combatant> to;
    if(oldsquad.contains(swap)){
      from=oldsquad;
      to=newsquad;
    }else{
      to=oldsquad;
      from=newsquad;
    }
    from.remove(swap);
    to.add(swap);
    return true;
  }

  /** TODO would be nice to highlight and use the mouse */
  Point walk(){
    Javelin.message("Select a direction to move into...",Delay.NONE);
    var to=Javelin.input();
    var action=WorldAction.press(to.getKeyChar(),to.getKeyCode());
    var m=action instanceof WorldMove?(WorldMove)action:null;
    if(m==null) return null;
    var l=Squad.active.getlocation();
    l.x+=m.deltax;
    l.y+=m.deltay;
    if(!l.validate(World.SIZE,World.SIZE)) return null;
    if(World.get(l.x,l.y,World.getactors())!=null){
      clear();
      Javelin.message("Destination is not empty...",Delay.WAIT);
      return null;
    }
    return l;
  }

  @Override
  public void perform(final WorldScreen screen){
    if(Dungeon.active!=null) throw new RepeatTurn();
    clear();
    final var in="""
        Press each member's number to switch his destination squad.
        Press c to cancel or ENTER when done.
        The left column is your current squad, the right one is the new squad.
        To join two squads later just place them in the same square.
        """;
    Javelin.promptscreen(in);
    var input=' ';
    var indexreference=new ArrayList<>(Squad.active.members);
    var oldsquad=new Combatants(Squad.active.members);
    var newsquad=new Combatants();
    while(input!='\n'){
      clear();
      var oldcolumn=new ArrayList<String>();
      var newcolumn=new ArrayList<String>();
      log(indexreference,oldsquad,oldcolumn);
      log(indexreference,newsquad,newcolumn);
      input=Javelin.promptscreen(formatcolumns(oldcolumn,newcolumn));
      if(input=='c') return;
      if(input!='\n'&&!swap(indexreference,input,oldsquad,newsquad)) continue;
    }
    if(oldsquad.isEmpty()||newsquad.isEmpty()) return;
    Javelin.app.switchScreen(BattleScreen.active);
    BattleScreen.active.center();
    var to=walk();
    if(to==null) throw new RepeatTurn();
    var gold=transfergold(newsquad);
    spawn(oldsquad,newsquad,to,gold);
  }

  void spawn(final Combatants oldsquad,Combatants newsquad,Point to,int gold){
    var a=Squad.active;
    var s=new Squad(to.x,to.y,Period.gettime(),a.lasttown);
    var t=Terrain.get(to.x,to.y);
    s.members=newsquad;
    if(!t.enter(s,to.x,to.y)){
      clear();
      Javelin.message("Units can't swim...",Delay.WAIT);
      throw new RepeatTurn();
    }
    s.gold=gold;
    s.strategic=a.strategic;
    s.move(true,t,to.x,to.y);
    a.members=oldsquad;
    a.gold-=gold;
    s.place();
    for(var c:newsquad){
      var items=a.equipment.get(c);
      a.equipment.remove(c);
      s.equipment.put(c,items);
    }
    a.updateavatar();
    throw new RepeatTurn();
  }

  int transfergold(ArrayList<Combatant> newsquad){
    var gold=Squad.active.gold*newsquad.size()/Squad.active.members.size();
    var increment=Squad.active.gold/10;
    var input=' ';
    while(input!='\n'){
      clear();
      var prompt="How much gold do you want to transfer to the new squad? Use the + and - keys to change and ENTER to confirm.\n"
          +Javelin.format(gold);
      input=Javelin.prompt(prompt);
      if(input=='+'){
        gold+=increment;
        if(gold>Squad.active.gold) gold=Squad.active.gold;
      }else if(input=='-'){
        gold-=increment;
        if(gold<0) gold=0;
      }
    }
    return gold;
  }

  String formatcolumns(final ArrayList<String> oldcolumn,
      final ArrayList<String> newcolumn){
    var text="";
    var nlines=Math.max(oldcolumn.size(),newcolumn.size());
    for(var i=0;i<nlines;i++){
      var oldtd=i<oldcolumn.size()?oldcolumn.get(i):"";
      while(oldtd.length()<WorldScreen.SPACER.length()) oldtd+=" ";
      final var newtd=i<newcolumn.size()?newcolumn.get(i):"";
      text+=oldtd+newtd+"\n";
    }
    return text;
  }

  Actor findtown(int xp,int yp){
    for(Town t:Town.gettowns()){
      var district=t.getdistrict().getarea();
      for(var x=xp-1;x<=xp+1;x++) for(var y=yp-1;y<=yp+1;y++)
        if(district.contains(new Point(x,y))) return t;
    }
    return null;
  }

  static void clear(){
    BattleScreen.active.messagepanel.clear();
  }

  void log(final List<Combatant> indexreference,final List<Combatant> oldsquad,
      final List<String> oldcolumn){
    if(oldsquad.isEmpty()) oldcolumn.add("Empty");
    else for(final Combatant m:oldsquad){
      var i=indexreference.indexOf(m);
      oldcolumn.add("["+SelectScreen.getkey(i)+"] "+m+" ("+m.getstatus()+")");
    }
  }

  /**
   * @param townbufferenabled If <code>true</code> will also return
   *   <code>true</code> if too close to a {@link Town}.
   * @return <code>true</code> if there is a {@link Town} in this coordinate
   *   already.
   */
  static public boolean istown(final int x,final int y,
      boolean townbufferenabled){
    if(World.get(x,y)!=null) return true;
    var towns=World.getall(Town.class);
    if(townbufferenabled){
      for(final Actor p:towns)
        for(var townx=p.x-TOWNBUFFER;townx<=p.x+TOWNBUFFER;townx++)
          for(var towny=p.y-TOWNBUFFER;towny<=p.y+TOWNBUFFER;towny++)
            if(townx==x&&towny==y) return true;
    }else for(final Actor p:towns) if(p.x==x&&p.y==y) return true;
    return false;
  }
}
