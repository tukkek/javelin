package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Describer;
import tyrant.mikera.engine.Description;
import tyrant.mikera.engine.Thing;


public class NameFilter implements IThingFilter {
    public boolean accept(Thing thing, String query) {
        if(query == null) return true;
        query = query.trim();
        String name = Describer.describe(null, thing, Description.ARTICLE_NONE);
        return name.indexOf(query) >= 0;
    }
}
