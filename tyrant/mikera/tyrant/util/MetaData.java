package tyrant.mikera.tyrant.util;

import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;

/**
 *
 * @author  Carsten Muessig <carsten.muessig@gmx.net>
 */

public class MetaData {
    
    private TreeMap metaDataEntries; // key = property name, value = MetaDataEntry
    
    protected MetaData() {
        metaDataEntries = new TreeMap();
    }
    
    protected MetaData(MetaData parent) {
        this();
        if(parent!=null)
            metaDataEntries.putAll(parent.getAll());
    }
    
    protected void add(String propertyName, MetaDataEntry property) {
        metaDataEntries.put(propertyName, property);
    }
    
    protected void add(String propertyName, Object value, Object[] validValues, int valueCondition, int propertyCondition) {
        add(propertyName, new MetaDataEntry(value, validValues, valueCondition, propertyCondition));
    }
    
    protected TreeMap getAll() {
        return metaDataEntries;
    }
    
   protected MetaDataEntry get(String property) {
        return (MetaDataEntry)metaDataEntries.get(property);
    }
    
    protected int numberOfMandatoryProperties() {
        int number = 0;
        Iterator it = metaDataEntries.keySet().iterator();
        while(it.hasNext()) {
            MetaDataEntry tmd = (MetaDataEntry)metaDataEntries.get(it.next());
            if(tmd.isMandatory())
                number++;
        }
        return number;
    }
    
    protected boolean describes(TreeMap properties, boolean isMetaData) {
        int mandatoryPropertiesChecked = 0;
        Set propertyNames = properties.keySet();
        if(metaDataEntries.keySet().containsAll(propertyNames)) {
            Iterator it = propertyNames.iterator();
            while(it.hasNext()) {
                String propertyName = (String)it.next();
                if(isMetaData)
                    System.out.println("   Checking meta data property \""+propertyName+"\"");
                else
                    System.out.println("   Checking property \""+propertyName+"\"");
                MetaDataEntry mde = (MetaDataEntry)metaDataEntries.get(propertyName);
                if((mde!=null)&&(mde.describes(properties.get(propertyName)))) {
                    if(mde.isMandatory())
                        mandatoryPropertiesChecked++;
                    if(isMetaData)
                        System.out.println("   Meta data property \""+propertyName+"\" successful checked");
                    else
                        System.out.println("   Property \""+propertyName+"\" successful checked");
                } else {
                    if(isMetaData)
                        System.out.println("   Meta data property \""+propertyName+"\" not successful checked.");
                    else
                        System.out.println("   Property \""+propertyName+"\" not successful checked.");
                }
            }
        }
        return mandatoryPropertiesChecked==numberOfMandatoryProperties();
    }
}