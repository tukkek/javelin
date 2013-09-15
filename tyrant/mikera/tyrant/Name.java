// Class to manage names of creatures and characters

package tyrant.mikera.tyrant;

import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.util.Text;

public class Name  {

public static String[][] names=
    {{"The Tyrant"},
     {"Benn","Kiern","Fayd","Rodrik","Gabe","Jobe","Mert","Janik","Jonn","Joharn","Andrin","Artur","Arthor","Aron","Bod","Bikk"},
     {"Alarna","Bamdia","Carrel","Dula","Sarra","Gail","Jeela","Ciina","Waydia","Kaydia","Faydia","Kassara","Jenn","Keri","Weyla","Gerrie"},
     {"Rutlud","Gombag","Snogi","Grashnak","Gruttug","Gritnak","Gumnak","Shondag","Stugrat","Kretlig","Gromnark","Bitnak"},
     
     {}
    }; 
  

   private static String[] gobboFirstParts={"gom","grash","grut","gum","stug","kret","krom","kar"};
  private static String[] gobboLastParts={"nak","lug","tug","bag","dag","lig","rat","nark"};
  public static String createGoblinName() {
  	String a=RPG.pick(gobboFirstParts);
  	String b=RPG.pick(gobboLastParts);
  	return Text.capitalise(a+b);
  }
  
  public static String createOrcName() {
  	return createGoblinName();
  }
}