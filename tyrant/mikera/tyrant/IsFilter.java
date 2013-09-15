package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Thing;


public class IsFilter implements IThingFilter {
    public boolean accept(Thing thing, String query) {
        if(query == null || query.length() < 1) return false;
        char firstLetter = query.charAt(0);
        char lastLetter = query.charAt(query.length()-1);
        if(firstLetter != '[' && Character.toUpperCase(firstLetter) != 'I') return false;
        String name = query;
        if((firstLetter == '[')&&(lastLetter==']')) {
            name = query.substring(1,query.length()-1);
        }
        return thing.getFlag(name);
    }
}
