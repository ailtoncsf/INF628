package hadoop.ifba;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

public class Util {
	
	public static final String[] REDIS_INSTANCES = { "p0", "p1", "p2", "p3",
		"p4", "p6" };

	// This helper function parses the stackoverflow into a Map for us.
	public static Map<String, String> transformXmlToMap(String xml) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			String[] tokens = xml.trim().substring(5, xml.trim().length() - 3)
					.split("\"");
	
			for (int i = 0; i < tokens.length - 1; i += 2) {
				String key = tokens[i].trim();
				String val = tokens[i + 1];
	
				map.put(key.substring(0, key.length() - 1), val);
			}
		} catch (StringIndexOutOfBoundsException e) {
			System.err.println(xml);
		}
	
		return map;
	}
	
	public static Map<String, String> transformXmlToMap2(String xml) {
	    Document doc = null;
	    try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(false);
	        DocumentBuilder bldr = factory
	                .newDocumentBuilder();

	        doc = bldr.parse(new ByteArrayInputStream(xml.getBytes()));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }

	    Map<String, String> map = new HashMap<String, String>();
	    NamedNodeMap attributeMap = doc.getDocumentElement().getAttributes();

	    for (int i = 0; i < attributeMap.getLength(); ++i) {
	        Attr n = (Attr) attributeMap.item(i);

	        map.put(n.getName(), n.getValue());
	    }

	    return map;
	}	
	
}
