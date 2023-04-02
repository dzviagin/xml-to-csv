package docdvz.xmlparser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class BillingParser {

    private final File file;
    private final String targetTag;
    private final Set<String> fields = new HashSet<>();
    private final List<Map<String, String>> result = new ArrayList<>();

    public BillingParser(File file, String targetTag) {
        this.file = file;
        this.targetTag = targetTag;
    }

    public void parse() throws IOException, ParserConfigurationException, SAXException {
        fields.clear();
        result.clear();
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream input = new FileInputStream(file);
        Document doc = builder.parse(input);
        Element root = doc.getDocumentElement();
        if (root.getNodeName().equals(targetTag)) {
            traverseTarget(root);
        } else {
            traverseElement(root);
        }
    }

    private void traverseElement(Element element) {
        var childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            var node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals(targetTag)){
                    traverseTarget((Element) node);
                } else {
                    traverseElement((Element) node);
                }
            }
        }
    }

    private void traverseTarget(Element node) {
        Map<String, String> values = new HashMap<>();
        traverseTarget(node, "", values);
        result.add(values);
    }

    private void traverseTarget(Element element, String prefix, Map<String, String> values) {
        var childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            var node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getChildNodes().getLength() == 1) {
                    for (int k = 0; k < 100; k++) {
                        var name = prefix + node.getNodeName() + (k > 0 ? ("-" + k) : "");
                        if (!values.containsKey(name)) {
                            this.fields.add(name);
                            values.put(name, node.getTextContent());
                            break;
                        }
                    }
                } else {
                    var p = prefix + node.getNodeName() + "-";
                    traverseTarget((Element) node, p, values);
                }
            }
        }
    }

    public Set<String> getFields() {
        return fields;
    }

    public List<Map<String, String>> getResult() {
        return result;
    }
}
