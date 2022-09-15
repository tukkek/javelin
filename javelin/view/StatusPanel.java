package javelin.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.collection.CountingSet;
import javelin.controller.content.action.Examine;
import javelin.controller.content.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Maneuver;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.condition.Breathless;
import javelin.old.QuestApp;
import javelin.old.TPanel;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.screen.BattleScreen;

/**
 * Bottom panel for the {@link BattleScreen}.
 *
 * @author alex
 */
public class StatusPanel extends TPanel{
  private static final long serialVersionUID=3905800885761095223L;
  static final int boxborder=1;
  static final Color powercolor=new Color(0,128,60);
  static int charwidth=0;
  static int charheight=0;
  static int charmaxascent=0;
  int nextLine;

  /** Constructor. */
  public StatusPanel(){
    setBackground(QuestApp.PANELCOLOUR);
  }

  @Override
  public Dimension getPreferredSize(){
    return new Dimension(208,272);
  }

  String conditiondata(Combatant hero){
    return listlist("Conditions",hero.liststatus(Fight.state));
  }

  @Override
  public void paint(final Graphics g){
    super.paint(g);
    nextLine=0;
    var hero=BattleState.getcombatant(BattlePanel.current,
        Fight.state.getcombatants());
    if(hero==null||hero.source==null) return;
    if(Examine.lastlooked!=null) hero=Examine.lastlooked;
    var helper="";
    for(final char c:(maininfo(hero)+movementdata(hero)+attackdata(hero)
        +passivedata(hero)+spelldata(hero)+itemdata(hero)+conditiondata(hero))
            .toCharArray()){
      helper+=c;
      if(Character.valueOf('\n').equals(c)||helper.length()==26){
        paintLabel(g,helper,10,getNextLine());
        helper="";
      }
    }
    paintLabel(g,helper,10,getNextLine());
  }

  private String itemdata(Combatant combatant){
    var count=new CountingSet();
    var bag=Fight.current.getbag(combatant);
    bag.stream().filter(i->i.usedinbattle&&i.canuse(combatant)==null)
        .forEach(i->{
          var name=i.toString().replaceAll("Potion of","");
          if(name.length()>19) name=name.substring(0,19).trim();
          count.add(name);
        });
    var items=count.getelements();
    var listing=new ArrayList<String>(items.size());
    for(String i:items){
      var n=count.getcount(i);
      if(n>1){
        if(i.length()>15) i=i.substring(0,15);
        i+=" x"+n;
      }
      listing.add(i);
    }
    return listlist("Items",listing);
  }

  private String spelldata(Combatant combatant){
    var listing=new ArrayList<String>();
    for(Spell s:combatant.spells){
      var uses=s.perday-s.used;
      if(uses==0||s instanceof Summon&&combatant.summoned) continue;
      var spellname=s.name;
      if(spellname.length()>=14) spellname=s.name.substring(0,14);
      if(uses>1) spellname+=" ("+(s.perday-s.used)+")";
      listing.add(spellname);
    }
    return listlist("Spells",listing);
  }

  String listlist(String title,List<String> listing){
    if(listing.isEmpty()) return "";
    var text=title+"\n";
    Collections.sort(listing);
    for(String i:listing) text+=" "+i.trim().toLowerCase()+"\n";
    return "\n"+text;
  }

  String movementdata(final Combatant c){
    var data=new ArrayList<String>(2);
    if(c.source.fly>0) data.add("Fly");
    else if(c.source.swim>0) data.add("Swim");
    if(c.source.burrow>0) data.add(c.burrowed?"Burrowed":"Burrow");
    var output="";
    if(!data.isEmpty()){
      output=data.get(0);
      data.remove(0);
      for(String s:data) output+=", "+s.toLowerCase();
      output+="\n";
    }
    var steps=c.gettopspeed(Fight.state)/5;
    output+=steps+" steps";
    if(BattlePanel.current.equals(c)&&BattleScreen.partialmove!=0){
      var left=(.5f-BattleScreen.partialmove)/.5f;
      left=steps*left;
      output+=" ("+Math.round(left)+" left)";
    }
    return output+"\n\n";
  }

  String attackdata(final Combatant c){
    var s="";
    if(c.hasattacktype(true)) s+="Mêlée\n";
    if(c.hasattacktype(false)) s+="Ranged\n";
    if(!c.source.breaths.isEmpty())
      s+=c.hascondition(Breathless.class)==null?"Breath\n":"Breathless\n";
    if(c.source.touch!=null) s+=c.source.touch.name+"\n";
    var maneuvers=c.disciplines.getmaneuvers();
    if(!maneuvers.isEmpty()){
      s+="Maneveurs\n";
      for(Maneuver m:maneuvers){
        final var spent=m.spent?"*":"";
        var name=m.name;
        final var trim=Math.min(name.length(),20-spent.length());
        s+=" "+name.substring(0,trim)+spent+"\n";
      }
    }
    return s;
  }

  String maininfo(Combatant combatant){
    final var customname=combatant.source.customName;
    final var status=combatant.getstatus();
    final var format=new BigDecimal(combatant.ap).setScale(1,
        RoundingMode.HALF_UP);
    final var ap="AP: "+format+"\n";
    return (customname!=null?customname:combatant.source.name)+"\n"
        +Character.toUpperCase(status.charAt(0))+status.substring(1)+"\n"+ap
        +"\n";
  }

  String passivedata(final Combatant combatant){
    var status="";
    if(combatant.source.fasthealing>0)
      status+="Fast healing "+combatant.source.fasthealing+"\n";
    return status.isEmpty()?"":"\n"+status;
  }

  private int getNextLine(){
    return nextLine+=15;
  }

  static void paintBar(final Graphics g,final int x,final int y,final int w,
      final int h,final Color f,final Color b,float amount){
    if(amount>1) amount=1;
    final var hh=h/4;
    g.setColor(f);
    g.fillRect(x,y,(int)(w*amount),h);
    g.setColor(f.brighter());
    g.fillRect(x,y,(int)(w*amount),hh);
    g.setColor(f.darker());
    g.fillRect(x,y+3*hh,(int)(w*amount),h-3*hh);
    g.setColor(b);
    g.fillRect(x+(int)(w*amount),y,(int)(w*(1-amount)),h);
    paintBox(g,x,y,w,h,false);
  }

  static int paintLabel(final Graphics g,final String s,final int x,
      final int y){
    g.setColor(QuestApp.INFOTEXTCOLOUR);
    g.drawString(s,x,y+charmaxascent-charheight/2);
    return charwidth*s.length();
  }

  // paint a boxed area, raised or lowered
  static void paintBox(final Graphics g,final int x,final int y,final int w,
      final int h,final boolean raised){
    if(raised) g.setColor(QuestApp.PANELHIGHLIGHT);
    else g.setColor(QuestApp.PANELSHADOW);

    g.fillRect(x,y,w,boxborder);
    g.fillRect(x,y,boxborder,h);

    if(!raised) g.setColor(QuestApp.PANELHIGHLIGHT);
    else g.setColor(QuestApp.PANELSHADOW);

    g.fillRect(x+1,y+h-boxborder,w-1,boxborder);
    g.fillRect(x+w-boxborder,y+1,boxborder,h-1);
  }
}
