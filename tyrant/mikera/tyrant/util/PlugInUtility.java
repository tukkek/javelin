package tyrant.mikera.tyrant.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javelin.controller.old.Game;
import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.tyrant.Hero;

/**
 * 
 * @author Carsten Muessig <carsten.muessig@gmx.net>
 */

public class PlugInUtility {

	private final static String XML_BEGIN = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
	private final static String XML_LIBRARY_STYLESHEET = "<?xml-stylesheet type=\"text/xsl\" href=\"tyrantLibraryItems.xsl\" ?>";
	private final static String XML_SPECIFICATION_STYLESHEET = "<?xml-stylesheet type=\"text/xsl\" href=\"tyrantLibrarySpecification.xsl\" ?>";
	private final static String XML_OPEN_LIBRARY = "<TyrantLibrary ProgramVersion=\""+ Game.VERSION + "\">";
	private final static String XML_CLOSE_LIBRARY = "</TyrantLibrary>";
	private final static String XML_OPEN_SPECIFICATION = "<TyrantLibrarySpecification ProgramVersion=\""+ Game.VERSION + "\">";
	private final static String XML_CLOSE_SPECIFICATION = "</TyrantLibrarySpecification>";
	private final static String XML_OPEN_ITEM = "<Item>";
	private final static String XML_CLOSE_ITEM = "</Item>";
	private final static String XML_OPEN_PROPERTY = "<Property>";
	private final static String XML_CLOSE_PROPERTY = "</Property>";
	private final static String XML_OPEN_NAME = "<Name>";
	private final static String XML_CLOSE_NAME = "</Name>";
	private final static String XML_OPEN_TYPE = "<Type>";
	private final static String XML_CLOSE_TYPE = "</Type>";
	private final static String XML_OPEN_DESCRIPTION = "<Description>";
	private final static String XML_CLOSE_DESCRIPTION = "</Description>";
	private final static String XML_OPEN_VALUE = "<Value>";
	private final static String XML_CLOSE_VALUE = "</Value>";
	private final static String XML_OPEN_METADATA = "<MetaData>";
	private final static String XML_CLOSE_METADATA = "</MetaData>";
	private final static String XML_OPEN_MANDATORY = "<IsMandatory>";
	private final static String XML_CLOSE_MANDATORY = "</IsMandatory>";
	private final static String XML_OPEN_CONDITION = "<ValueCondition>";
	private final static String XML_CLOSE_CONDITION = "</ValueCondition>";
	private final static String XML_OPEN_VALID_VALUES = "<ValidValues>";
	private final static String XML_CLOSE_VALID_VALUES = "</ValidValues>";
	private final static String XML_OPEN_PROPERTY_SPECIFICATION = "<PropertySpecification>";
	private final static String XML_CLOSE_PROPERTY_SPECIFICATION = "</PropertySpecification>";
	private final static String XML_OPEN_ITEM_SPECIFICATION = "<ItemSpecification>";
	private final static String XML_CLOSE_ITEM_SPECIFICATION = "</ItemSpecification>";
	private final static TreeMap LIBMETADATA = LibMetaData.instance().getAll();
		
	public static void writeLibrary(String fileName, boolean withStylesheet) {
		prepareWriteAction(new File(fileName), withStylesheet, false);
	}

	public static void writeSpecification(String fileName) {
		prepareWriteAction(new File(fileName), true, true);
	}

	public static void readPlugIn(String fileName) {
		File[] files = null;
		File file = new File(fileName);
		try {
			if (file.isDirectory())
				files = file.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".xml");
					}
				});
			if (file.isFile()) {
				files = new File[1];
				files[0] = file;
			}
			LinkedHashMap itemAndMetaData = new LinkedHashMap();
			ArrayList items = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);
			Document doc = dbf.newDocumentBuilder().parse(new FileInputStream(files[i]));
			NodeList plugIn = doc.getElementsByTagName("TyrantPlugIn");
			if (plugIn.getLength() == 0)
				System.out.println(files[i]+" doesn't contain any items");
			else {
				NodeList itemList = doc.getElementsByTagName("Item");
				System.out.println(files[i]+" contains "+itemList.getLength() + " items");
				items.addAll(readItems(itemList));
				System.out.println(items.size()+" items were successful loaded from "+file.getName());
				itemAndMetaData.putAll(checkItemData(items));
				items.clear();
				LibMetaDataHandler.createLibraryItems(itemAndMetaData);
				}
			}
		} catch (Exception e) {
			System.out.println("Error while reading from"+file.toString()+":");
			System.out.println(e.getMessage());
		}		
	}

	private static ArrayList readItems(NodeList itemList) {
		ArrayList items = new ArrayList();
		for (int i = 0; i < itemList.getLength(); i++) {
			Node item = itemList.item(i);
			if (item.hasChildNodes()) {
				System.out.println(" Loading item " + (i + 1));
				items.add(readItem(item.getChildNodes()));
			} else
				System.out.println(" Item " + (i + 1)
						+ " contains unvalid data:\n" + item);
		}
		return items;
	}

	private static TreeMap readItem(NodeList itemData) {
		TreeMap properties = new TreeMap();
		for (int i = 0; i < itemData.getLength(); i++) {
			Node data = itemData.item(i);
			if (data.getNodeType() == Node.ELEMENT_NODE)
				properties.putAll(readProperties(data, false));
		}
		System.out.println(properties);
		return properties;
	}

	private static TreeMap readProperties(Node propertyData, boolean isMetaData) {
		TreeMap properties = new TreeMap(), metaData = new TreeMap();
		String name = null, value = null, nodeName = propertyData.getNodeName();
		if ((propertyData.getNodeType() == Node.ELEMENT_NODE)
				&& (nodeName != null) && (nodeName.length() > 0)
				&& (nodeName.indexOf("#") == -1)) {
			name = propertyData.getNodeName();
			value = propertyData.getFirstChild().getNodeValue();
			if ((value != null) && (value.trim().length() > 0)) {
				String prefix;
				if (isMetaData)
					prefix = "   ";
				else
					prefix = "  ";
				System.out.println(prefix + "Property \"" + name + "\" successful loaded");
				properties.put(name, value);
			} else {
				System.out.println("  Loading meta data");
				NodeList meta = propertyData.getChildNodes();
				for (int j = 0; j < meta.getLength(); j++)
					metaData.putAll(readProperties(meta.item(j), true));
				if (metaData.size() > 0) {
					System.out.println("  Meta data successful loaded");
					properties.put(name, metaData);
				} else {
					System.out.println("  Missing or unvalid meta data:");
					System.out.println("  " + metaData);
					return new TreeMap();
				}
			}
		}
		if (properties.size() > 0)
			return properties;
		if ((properties.size() == 0) && (!isMetaData)) {
			System.out.println("  Missing or unvalid property data:");
			System.out.println("  " + properties);
		}
		return new TreeMap();
	}

	private static LinkedHashMap checkItemData(ArrayList items) {
		LibMetaData libMetaData = LibMetaData.instance();
		LinkedHashMap itemAndMetaData = new LinkedHashMap();
		Iterator it = items.iterator();
		int i = 0;
		System.out.println("Checking " + items.size()
				+ " item(s) for matching library meta data");
		while (it.hasNext()) {
			i++;
			TreeMap item = (TreeMap) it.next();
			System.out.println(" Checking item " + i);
			String metaDataName = libMetaData.describes(item);
			if (metaDataName != null)
				itemAndMetaData.put(getTimeStamp() + "$" + metaDataName, item);
		}
		System.out.println(itemAndMetaData.size()
				+ " item(s) matched the library meta data");
		if (itemAndMetaData.size() != items.size())
			System.out.println((items.size() - itemAndMetaData.size())
					+ " item(s) didn't match the library meta data");
		return itemAndMetaData;
	}

	private static void prepareWriteAction(File file, boolean withStylesheet,
			boolean isSpecification) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileOutputStream(file), true);
			if (isSpecification)
				processLibMetaData(writer);
			else
				processLibrary(writer, withStylesheet);
		} catch (Exception e) {
			System.out.println("Error while writing to" + file.toString() + ":");
			System.out.println(e.getMessage());
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	private static void processLibrary(PrintWriter writer, boolean withStylesheet) {
		//get the library items and write the first XML line
		Hero.createHero("dummy", "human", "sorceror");
		Lib lib = Lib.instance();
		List libItems = lib.getAll();
		MetaData metaData=null;
		
		writer.println(XML_BEGIN);
		if (withStylesheet)
			writer.println(XML_LIBRARY_STYLESHEET);
		writer.println(XML_OPEN_LIBRARY);
		// loop through all item
		for (int i = 0; i < libItems.size(); i++) {
			TreeMap props = new TreeMap(((BaseObject) libItems.get(i)).getCollapsedMap());

			// write the output and start with the item name
			writer.println(getSpaces(1) + XML_OPEN_ITEM);
			writer.println(getSpaces(2) + XML_OPEN_NAME + props.remove("Name") + XML_CLOSE_NAME);
			// now the other properties
			Iterator it = props.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				Object entry = props.get(key);
				// we only display the important part of class name and cut the
				// rest off
				if ((entry != null) && (entry.toString().indexOf("@") > 0)){
					metaData = LibMetaDataHandler.createMetaDataFromObject(entry);
					if(metaData==null)
						entry = entry.toString().substring(0, entry.toString().lastIndexOf("@"));
				}
				if(metaData != null) {
					TreeMap meta = metaData.getAll();
					Iterator jt = meta.keySet().iterator();
					// to do: sometimes getValue() returns numbers which are unvalid XML tags
					//        don't know what's wrong with it, maybe due to recent Tyrant API changes  
					writer.println("<"+getValue(entry)+">");
					writer.println("<MetaData>");
					while(jt.hasNext()) {
						String property = (String)jt.next();
						MetaDataEntry mde = (MetaDataEntry)meta.get(property);
						writer.println("<"+property+">"+mde.getValue()+"</"+property+">");
					}
					writer.println("</MetaData>");
					// to do: sometimes getValue() returns numbers which are unvalid XML tags
					//        don't know what's wrong with it, maybe due to recent Tyrant API changes					
					writer.println("</"+getValue(entry)+">");
				} else
				   writer.println(getSpaces(2) + "<" + key + ">" + entry + "</" + key + ">");
			}
			writer.println(getSpaces(1) + XML_CLOSE_ITEM);
		}
		writer.println(XML_CLOSE_LIBRARY);
	}

	private static void processLibMetaData(PrintWriter writer) {
		TreeMap metaData = new TreeMap(), properties;
		TreeMap descriptions = LibMetaDataHandler.createPropertyDescriptions();
		// look for different properties and store them in the TreeMap metaData
		Iterator it = LIBMETADATA.keySet().iterator();
		while (it.hasNext()) {
			String thingName = (String) it.next();
			MetaData md = (MetaData) LIBMETADATA.get(thingName);
			properties = md.getAll();
			Iterator j = properties.keySet().iterator();
			while (j.hasNext()) {
				String propName = (String) j.next();
				if (!metaData.containsKey(propName))
					metaData.put(propName, properties.get(propName));
			}
		}
		// write the output, this happens in two parts
		writer.println(XML_BEGIN);
		writer.println(XML_SPECIFICATION_STYLESHEET);
		writer.println(XML_OPEN_SPECIFICATION);
		// first the property specification
		writer.println(getSpaces(1) + XML_OPEN_PROPERTY_SPECIFICATION);
		it = metaData.keySet().iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			processMetaDataEntry(writer, false, 2, name,
					(MetaDataEntry) metaData.get(name), descriptions);
		}
		writer.println(getSpaces(1) + XML_CLOSE_PROPERTY_SPECIFICATION);
		// then the thing specification
		writer.println(getSpaces(1) + XML_OPEN_ITEM_SPECIFICATION);
		it = LIBMETADATA.keySet().iterator();
		while (it.hasNext()) {
			String thingName = (String) it.next();
			MetaData md = (MetaData) LIBMETADATA.get(thingName);
			properties = md.getAll();
			writer.println(getSpaces(2) + XML_OPEN_ITEM);
			writer.println(getSpaces(3) + XML_OPEN_NAME + thingName
					+ XML_CLOSE_NAME);
			Iterator j = properties.keySet().iterator();
			while (j.hasNext()) {
				String propName = (String) j.next();
				processMetaDataEntry(writer, true, 4, propName,
						((MetaDataEntry) properties.get(propName)),
						descriptions);
			}
			writer.println(getSpaces(2) + XML_CLOSE_ITEM);
		}
		writer.println(getSpaces(1) + XML_CLOSE_ITEM_SPECIFICATION);
		writer.println(XML_CLOSE_SPECIFICATION);
	}

	private static void processMetaDataEntry(PrintWriter writer,
			boolean isItemSpecification, int level, String propertyName,
			MetaDataEntry mde, TreeMap descriptions) {
		Object value = mde.getValue();
		ArrayList validValues = mde.getValidValues();

		writer.println(getSpaces(level) + XML_OPEN_PROPERTY);
		writer.println(getSpaces(level) + XML_OPEN_NAME + propertyName
				+ XML_CLOSE_NAME);

		if (isItemSpecification) {
			writer.println(getSpaces(level) + XML_OPEN_MANDATORY
					+ mde.isMandatory() + XML_CLOSE_MANDATORY);
			writer.println(getSpaces(level) + XML_OPEN_VALUE + getValue(value)
					+ XML_CLOSE_VALUE);
			writer.println(getSpaces(level) + XML_OPEN_CONDITION
					+ mde.getValueCondition() + XML_CLOSE_CONDITION);
		} else {
			writer.println(getSpaces(level) + XML_OPEN_TYPE
					+ value.getClass().getName() + XML_CLOSE_TYPE);
			writer.println(getSpaces(level) + XML_OPEN_DESCRIPTION
					+ getDescription((String) descriptions.get(propertyName))
					+ XML_CLOSE_DESCRIPTION);
		}
		// properties can be described by meta data, if so process the meta
		// data's properties
		if (value instanceof MetaData) {
			TreeMap tmd = ((MetaData) value).getAll();
			// if a property has to have certain values the meta data is stored
			// there
			if (tmd.keySet().size() > 0) {
				if (isItemSpecification)
					writer.println(getSpaces(level + 1) + XML_OPEN_METADATA);
				Iterator it = tmd.keySet().iterator();
				while (it.hasNext()) {
					String propName = (String) it.next();
					processMetaDataEntry(writer, isItemSpecification,
							(level + 2), propName, (MetaDataEntry) tmd.get(propName), descriptions);
				}
				if (isItemSpecification)
					writer.println(getSpaces(level + 1) + XML_CLOSE_METADATA);
			}
		}

		if (validValues != null) {
			Iterator it = validValues.iterator();
			writer.println(getSpaces(level + 1) + XML_OPEN_VALID_VALUES);
			while (it.hasNext()) {
				Object o = it.next();
				// if a property is described by meta data process the meta
				// data's properties
				if (o instanceof MetaData) {
					writer.println(getSpaces(level + 2) + XML_OPEN_METADATA);
					TreeMap tmd = ((MetaData) o).getAll();
					Iterator j = tmd.keySet().iterator();
					while (j.hasNext()) {
						String name = (String) j.next();
						processMetaDataEntry(writer, isItemSpecification,
								(level + 3), name, (MetaDataEntry) tmd
										.get(name), descriptions);
					}
					writer.println(getSpaces(level + 2) + XML_CLOSE_METADATA);
				} else
					writer.println(getSpaces(level + 2) + XML_OPEN_VALUE + o
							+ XML_CLOSE_VALUE);
			}
			writer.println(getSpaces(level + 1) + XML_CLOSE_VALID_VALUES);
		}
		writer.println(getSpaces(level) + XML_CLOSE_PROPERTY);
	}

	private static String getValue(Object o) {
		String s = o.getClass().getName(), t=null;
		if (s.indexOf("mikera.tyrant") >= 0) {
			if (s.indexOf("@") >= 0) {
				t= s.substring(s.lastIndexOf("."), s.indexOf("@"));}
			else
				t= s.substring(s.lastIndexOf(".") + 1);
		}
		if((t!=null)&&(t.indexOf(";")>=0))
			t=t.substring(0, t.length()-1);
		if(t!=null)
			return t;
		return o.toString();
	}

	private static String getDescription(String s) {
		if (s == null)
			return "";
		return s;
	}

	private static String getSpaces(int i) {
		StringBuffer sb = new StringBuffer();
		while (i-- > 0)
			sb.append("   ");
		return sb.toString();
	}

	private static long getTimeStamp() {
		return System.currentTimeMillis();
	}
}