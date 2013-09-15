package tyrant.mikera.tyrant.util;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author Carsten Muessig <carsten.muessig@gmx.net>
 */

public class LibMetaData {

	private static LibMetaData instance = null;
	private TreeMap metaData; // key = thing name, value = MetaData of thing

	private LibMetaData() {
		metaData = new TreeMap();
	}

	protected static LibMetaData instance() {
		if (instance == null) {
			instance = new LibMetaData();
			LibMetaDataHandler.createLibMetaData(instance);
		}
		return instance;
	}

	protected void add(String thingName, MetaData thing) {
		metaData.put(thingName, thing);
	}

	protected TreeMap getAll() {
		return metaData;
	}

	protected MetaData get(String thingName) {
		return (MetaData) metaData.get(thingName);
	}

	protected String describes(TreeMap item) {
		ArrayList metaDataNames = new ArrayList();
		Iterator it = metaData.keySet().iterator();
		while (it.hasNext()) {
			String libItemName = (String) it.next();
			MetaData libItem = (MetaData) metaData.get(libItemName);
			if (libItem.describes(item, false))
				metaDataNames.add(libItemName);
		}
		if (metaDataNames.size() == 0)
			System.out.println(" The item doesn't match any library meta data");
		if (metaDataNames.size() == 1) {
			System.out.println(" The item matches the library meta data \""	+ (String) metaDataNames.get(0) + "\"");
			return (String) metaDataNames.get(0);
		}
		if (metaDataNames.size() > 1) {
			System.out.println(" The item is ambiguous, "
					+ metaDataNames.size()
					+ " matching descriptions were found: " + metaDataNames);
			System.out
					.println(" Reasons: Faulty plug-in data or faulty meta data inside the program");
			System.out
					.println(" Please contact the Tyrant developers if you're not sure and provide your plug-in file");
		}
		return null;
	}

	public static boolean isKnownProperty(String property) {
		TreeMap metaData = instance().getAll();
		Iterator it = metaData.keySet().iterator();
		while (it.hasNext()) {
			String thingName = (String) it.next();
			MetaData meta = (MetaData) metaData.get(thingName);
			if (meta.get(property) != null)
				return true;
		}
		return false;
	}

	public static boolean isValidProperty(String property, Object value) {
		TreeMap metaData = instance().getAll();
		Iterator it = metaData.keySet().iterator();
		MetaDataEntry med = null;
		while (it.hasNext()) {
			String thingName = (String) it.next();
			MetaData meta = (MetaData) metaData.get(thingName);
			if (meta.get(property) != null) {
				System.out.println(meta.getAll().keySet());
				med = meta.get(property);
				System.out.println(med.getValue());
				break;
			}
		}
		if (med != null)
			return med.describes(value);
		System.out.println("No meta data for "+property+" available");
		return false;
	}
	
	public static String getPropertyDescription(String property) {
		return (String)LibMetaDataHandler.createPropertyDescriptions().get(property);
	}
}