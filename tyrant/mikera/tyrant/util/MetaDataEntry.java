package tyrant.mikera.tyrant.util;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/**
 *
 * @author  Carsten Muessig <carsten.muessig@gmx.net>
 */

public class MetaDataEntry {
    
    public static final int FIX_VALUE = 0;
    public static final int ANY_VALUE = 1;
    public static final int POSITIVE_VALUE = 2; // including zero
    public static final int NEGATIVE_VALUE = 3;
    public static final int INTERVAL = 4;
    public static final int CERTAIN_VALUES = 5;
    
    public static final int MANDATORY_PROPERTY = 6;
    public static final int OPTIONAL_PROPERTY = 7;
    
    private Object value;
    private ArrayList validValues;
    private int valueCondition;
    private int propertyCondition;
    
    protected MetaDataEntry(Object o, Object[] vv, int v, int p) throws IllegalArgumentException {
        if((valueCondition==INTERVAL) && (vv.length!=2))
            throw new IllegalArgumentException("The INTERVAL condition needs two valid values: [minimum ; maximum]. "+vv.length+" values were found");
        if((valueCondition==CERTAIN_VALUES) && (vv.length<=1))
            throw new IllegalArgumentException("The CERTAIN_VALUES condition needs more than one valid value. "+vv.length+" value(s) were found");
        if(vv!=null) {
            validValues = new ArrayList();
            for(int i=0; i<vv.length; i++)
                validValues.add(vv[i]);
            if(!(vv instanceof MetaData[]))
                Collections.sort(validValues);
        }
        value = o;
        valueCondition = v;
        propertyCondition = p;
    }
    
    protected boolean isMandatory() {
        return propertyCondition==MANDATORY_PROPERTY;
    }
    
    protected Object getValue() {
        return value;
    }
    
    protected String getValueCondition() {
        switch(valueCondition) {
            case FIX_VALUE: return "Fix";
            case ANY_VALUE: return "Any";
            case POSITIVE_VALUE: return "Positive";
            case NEGATIVE_VALUE: return "Negative";
            case INTERVAL: return "Interval";
            case CERTAIN_VALUES: return "Certain values";
            default: return null;
        }
    }
    
    protected ArrayList getValidValues() {
        return validValues;
    }
    
    protected boolean describes(Object o) {
        Iterator it;
        boolean condition = false;
        try {
            if((value instanceof Integer) && (!(o instanceof Integer)))
                o = new Integer((String)o);
            if((value instanceof Double) && (!(o instanceof Double)))
                o = new Double((String)o);
            if(value instanceof MetaData)
                o = (TreeMap)o;
            System.out.println("    "+o+" matches the value type "+value.getClass().getName());
        } catch(Exception e) {e.printStackTrace();
            System.out.println("    "+o+" is a "+o.getClass()+" and doesn't match the value type "+value.getClass()+": "+e.getMessage());
            return false;
        }
        switch(valueCondition) {
            case FIX_VALUE:
                if(o instanceof TreeMap)
                    condition = ((MetaData)value).describes((TreeMap)o, true);
                condition =  value.equals(o);
                break;
            case ANY_VALUE:
                condition = true;
                break;
            case POSITIVE_VALUE:
                if(o instanceof Integer)
                    condition = (new Integer(0).compareTo((Integer)o) <= 0);
                if(o instanceof Double)
                    condition = (new Double(0).compareTo((Double)o) <= 0);
                break;
            case NEGATIVE_VALUE:
                if(o instanceof Integer)
                    condition = (new Integer(0).compareTo((Integer)o) > 0);
                if(o instanceof Double)
                    condition = (new Double(0).compareTo((Double)o) > 0);
                break;
            case INTERVAL:
                it = validValues.iterator();
                if(o instanceof Integer)
                    condition = (((Integer)it.next()).compareTo((Integer)o)>=0) && (((Integer)it.next()).compareTo((Integer)o)<=0);
                if(o instanceof Double)
                    condition = (((Double)it.next()).compareTo((Double)o)>=0) && (((Double)it.next()).compareTo((Double)o)<=0);
                break;
            case CERTAIN_VALUES:
                if(o instanceof TreeMap) {
                    TreeMap properties = (TreeMap)o;
                    it = validValues.iterator();
                    while(it.hasNext()) {
                        if(((MetaData)it.next()).describes(properties, true))
                            condition = true;
                    }
                } else
                    condition = validValues.contains(o);
                break;
            default:
                break;
        }
        return checkCondition(o, condition);
    }
    
    private boolean checkCondition(Object o, boolean condition) {
        if(condition)
            System.out.println("    "+o+" matches the "+getValueCondition().toLowerCase()+" condition");
        else
            System.out.println("    "+o+" doesn't match the "+getValueCondition().toLowerCase()+" condition");
        return condition;
    }
}