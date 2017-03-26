/*
 * Created on 17-Aug-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import java.util.*;

import javelin.controller.old.Game;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;


/**
 * Recipes can be learned by the hero to create useful items
 * 
 * 
 */
public class Recipe {
	public static void init() {
		Thing t=Lib.extend("base recipe","base thing");
		t.set("IsRecipe",1);
		t.set("NoStack",1);
		Lib.add(t);
		
		t=Lib.extend("base recipe scroll","base scroll");
		t.set("IsRecipeScroll",1);
		t.set("OnRead",new ReadRecipeScrollScript());
		t.set("ValueBase",100);
		t.set("Frequency",20);
		Lib.add(t);
		
		initRecipes();
	}
	
    private static class ReadRecipeScrollScript extends Script {
        private static final long serialVersionUID = 3977020660478586934L;

        public boolean handle(Thing s, Event e) {
            Thing user=e.getThing("Reader");
        	
           	String rname=s.getstring("RecipeName");
            Thing r=Lib.create(rname);
            
            String order=r.getstring("RecipeOrder");
            
            Item.identify(s);
            
            if (user.getStat(order)<=0) {
            	Game.messageTyrant("You identify "+s.getTheName());
            	Game.messageTyrant("You must acquire the "+order+" skill in order to learn this recipe");
            	return true;
            }
            
            user.addThing(r);
           	s.remove(1);
        
           	Thing res=recipeResult(r);
           	Item.identify(res);
           	
            Game.messageTyrant("You now know how to make "+res.getAName());

            return true;
        }
    }
    
    /**
     * Chack that a being has the necessary ingredients in its inventory
     * 
     * @param b
     * @param r
     * @return
     */
	public static int checkIngredients(Thing b, String r) {
		if (r==null) return 999999;
		try {
			String[] rs=r.split(",");
			int c=999999;
			for (int i=0; i<rs.length; i++) {
				String s=rs[i].trim();
				int n=1;
				if (Character.isDigit(s.charAt(0))) {
					int sp=s.indexOf(" ");
					n=Integer.parseInt(s.substring(0,sp));
					s=s.substring(sp+1);
				}
				int count=b.countIdentifiedItems(s);
				c=RPG.min(c,count/n);
			}
			return c;
		} catch (Throwable x) {
			Game.warn("Problem checking ingredients ["+r+"]");
			return 0;
		}
	}
	
	/**
	 * Remove ingredeints from a being
	 * 
	 * @param b
	 * @param r
	 * @return
	 */
	public static boolean removeIngredients(Thing b, String r) {
		String[] rs=r.split(",");
		for (int i=0; i<rs.length; i++) {
			String s=rs[i].trim();
			int n=1;
			if (Character.isDigit(s.charAt(0))) {
				int sp=s.indexOf(" ");
				n=Integer.parseInt(s.substring(0,sp));
				s=s.substring(sp+1);
			}
			int count=b.removeItems(s,n);
			if (count!=n) return false;
		}
		return true;
	}	
	

	
	/**
	 * ArrayList for registered items requiring recipes
	 */
	private static ArrayList recs=new ArrayList();
	
	/**
	 * Register a recipe for creation. This will be created later in the init procedure
	 * when initRecipes() is called
	 * 
	 * @param order Skill order required to use the recipe
	 * @param t The thing to create the recipe for
	 * @param ings The ingredient count
	 */
	public static void register(String order, Thing t, int ings) {
		t.set("RecipeOrder",order);
		t.set("RecipeIngredientCount",ings);
		recs.add(t);
		
	}
	
	private static void addRecipe(Thing t) {
		Lib.add(t);
		
		String order=t.getstring("RecipeOrder");
		
		Thing rs=Lib.extend("instructions for a "+recipeResult(t).get("Name"),"base recipe scroll");
		rs.set("NamePlural",rs.get("Name"));
		rs.set("UName","instructions for a strange "+recipeName(t.getstring("RecipeOrder")));
		rs.set("UNamePlural",rs.get("UName"));
		rs.set("NameType",Thing.NAMETYPE_QUANTITY);
		rs.set("RecipeName",t.name());
		rs.set("Level",t.getStat("Level"));
		rs.set("LevelMin",t.getStat("Level"));
		rs.set(recipeScrollTag(order),1);
		Lib.add(rs);
	}
	
	public static boolean isRecipeSkill(String skill) {
		return !orderTag(skill).equals("Any");
	}
	
	private static String orderTag(String order) {
		if (order.equals(Skill.ALCHEMY)) return "Alchemy";
		if (order.equals(Skill.BLACKMAGIC)) return "Black";
		if (order.equals(Skill.RUNELORE)) return "Rune";
		if (order.equals(Skill.HERBLORE)) return "Herb";
		
		return "Any";
	}
	
	private static String ingredientTag(String order) {
		String ot=orderTag(order);
		if (ot!=null) return "Is"+ot+"Ingredient";	
		return null;	
	}
	
	private static String recipeTag(String order) {
		String ot=orderTag(order);
		if (ot!=null) return "Is"+ot+"Recipe";	
		return null;	
	}
	
	private static String recipeScrollTag(String order) {
		String ot=orderTag(order);
		if (ot!=null) return "Is"+ot+"RecipeScroll";	
		return null;	
	}
	
	private static String recipeName(String order) {
		if (order.equals(Skill.ALCHEMY)) return "concoction";
		if (order.equals(Skill.BLACKMAGIC)) return "rite";
		if (order.equals(Skill.RUNELORE)) return "runescript";
			
		return "recipe";
	}
	
	private static Thing chooseIngredient(String rname,String order, int level) {
		Thing t=null;
		int c=0;
		String tag=ingredientTag(order);
		while (t==null) {
			c++;
			t= Lib.createType(tag,level+RPG.d(10)-RPG.d(10));
			if (c>30) {
				Game.warn("Can't choose ingredient for "+rname+" ["+tag+","+level+"]");
				return t;
			}
			if (t.name().equals(rname)) t=null;

		}
		return t;
	}
	
	/**
	 * Build all recipes and associated scrolls
	 * 
	 * Creates recipes for all previously registered items
	 *
	 */
	private static void initRecipes() {
		int n=recs.size();
		
		for (int i=0; i<n; i++) {
			Thing tt=(Thing)recs.get(i);
			
			String order=tt.getstring("RecipeOrder");
			String ingredients=tt.getstring("RecipeIngredients");
			int level=tt.getStat("Level")+3;
			
			String rname=tt.name();
			String name=rname+" "+recipeName(order);
			Thing t=Lib.extend(name,"base recipe");
			t.set("RecipeName",rname);


			int ings=tt.getStat("RecipeIngredientCount");
			if (ings<=0) ings=2;

			// combine full ingredient list
			if ((ingredients==null)||(ingredients.equals(""))) {
				ingredients="";
			} else if (ings>0) {
				ingredients+=",";
			}

			for (int j=0; j<ings; j++) {
				ingredients+=chooseIngredient(rname,order,level).name()+",";
			}
			if (ings>0) ingredients=ingredients.substring(0,ingredients.length()-1);
			
			t.set("RecipeOrder",order);
			t.set("Level",level);
			t.set("LevelMin",level);			
			t.set("Ingredients",ingredients);
			t.set(recipeTag(order),1);
			addRecipe(t);
		}
		
		recs.clear();
	}
	
	public static boolean apply(Thing h, String s) {
		Thing[] ts=h.getFlaggedContents("IsRecipe");
		ArrayList al=new ArrayList();
		HashMap hm=new HashMap();
		for (int i=0; i<ts.length; i++) {
			Thing r=ts[i];
			if (!r.getstring("RecipeOrder").equals(s)) continue;
			String ingredients=r.getstring("Ingredients");
			int ic=checkIngredients(h,ingredients);
			String st=r.getstring("RecipeName")+"  ("+ingredients+"  stock: " +ic+")";
			al.add(st);
			hm.put(st,r);
		}
		
		ListScreen ls=new ListScreen("Select your recipe:",al);
		java.awt.Component gs=QuestApp.getInstance().getScreen();
		QuestApp.getInstance().switchScreen(ls);
		String rec=(String)ls.getObject();
		QuestApp.getInstance().switchScreen(gs);
		if (rec!=null) {
			Thing r=(Thing)hm.get(rec);
			String ingredients=r.getstring("Ingredients");
			int ic=checkIngredients(h,ingredients);
			if (ic<=0) {
				Game.messageTyrant("You do not have the necessary ingredients!");
				Game.messageTyrant("You need "+ingredients);
				return false;
			}
			
			removeIngredients(h,ingredients);
			//TODO: skill checks
	
			Thing t=recipeResult(r);
			Game.messageTyrant("You make "+t.getAName());
			Item.identify(t);
			h.addThing(t);
		}
		
		return false;
	}
	
	protected static Thing recipeResult(Thing r) {
		String name=r.getstring("RecipeName");
		Thing t=Lib.create(name);
		return t;
	}
}
