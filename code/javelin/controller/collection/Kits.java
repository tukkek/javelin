package javelin.controller.collection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.controller.content.kit.Kit;
import javelin.controller.content.upgrade.Upgrade;
import javelin.controller.content.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.abilities.spell.Spell;

/** @author alex */
public class Kits extends ArrayList<Kit>{
  static class Guide{
    String name(Upgrade u){
      var name=u.name;
      if(u instanceof Spell) name="Spell: %s".formatted(name.toLowerCase());
      return "* %s".formatted(name);
    }

    int compare(Upgrade u1,Upgrade u2){
      if(u1 instanceof ClassLevelUpgrade) return -1;
      return name(u1).compareTo(name(u2));
    }

    String show(){
      var kits=new ArrayList<>(Kit.KITS);
      kits.sort(Comparator.comparing(kit1->kit1.name));
      var lines=new ArrayList<String>();
      lines.add("# Upgrades");
      for(var k:kits){
        lines.add("## %s".formatted(k.name));
        var upgrades=new ArrayList<>(k.getupgrades());
        upgrades.sort(this::compare);
        for(var u:upgrades) lines.add(name(u));
        lines.add("");
      }
      return String.join("\n",lines);
    }
  }

  /** Constructor. */
  public Kits(List<Kit> kits){
    super(kits);
  }

  /// Prints a detailed guide to be pasted to the {@link Kit}-{@link Guide}.
  static public void guide(){
    IO.print(new Guide().show());
  }
}
