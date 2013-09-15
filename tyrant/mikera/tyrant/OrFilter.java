package tyrant.mikera.tyrant;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import tyrant.mikera.engine.Thing;



public class OrFilter implements IThingFilter {
    private List filters = new LinkedList();
    
    public OrFilter(IThingFilter filterA, IThingFilter filterB) {
        addFilter(filterA);
        addFilter(filterB);
    }
    public OrFilter(IThingFilter filterA, IThingFilter filterB, IThingFilter filterC) {
        addFilter(filterA);
        addFilter(filterB);
        addFilter(filterC);
    }
    
    public void addFilter(IThingFilter filter) {
        filters.add(filter);
    }
    
    public boolean accept(Thing thing, String query) {
        for (Iterator iter = filters.iterator(); iter.hasNext();) {
            IThingFilter filter = (IThingFilter) iter.next();
            if(filter.accept(thing, query)) return true;
        }
        return false;
    }
}
