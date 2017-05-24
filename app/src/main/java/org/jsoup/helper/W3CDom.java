package org.jsoup.helper;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.HashMap;

public class W3CDom {
    protected DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    public Document fromJsoup(org.jsoup.nodes.Document in) {
        Validate.notNull(in);
        DocumentBuilder builder;
        try {
        	
        	factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
            Document out = builder.newDocument();
            convert(in, out);
            return out;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    public void convert(org.jsoup.nodes.Document in, Document out) {
        if (!StringUtil.isBlank(in.location()))
            out.setDocumentURI(in.location());

        org.jsoup.nodes.Element rootEl = in.child(0); 
        NodeTraversor traversor = new NodeTraversor(new W3CBuilder(out));
        traversor.traverse(rootEl);
    }

    protected static class W3CBuilder implements NodeVisitor {
        private static final String xmlnsKey = "xmlns";
        private static final String xmlnsPrefix = "xmlns:";
        private final Document doc;
        private final HashMap<String, String> namespaces = new HashMap<String, String>(); 
        private Element dest;

        public W3CBuilder(Document doc) {
            this.doc = doc;
        }

        public void head(org.jsoup.nodes.Node source, int depth) {
            if (source instanceof org.jsoup.nodes.Element) {
                org.jsoup.nodes.Element sourceEl = (org.jsoup.nodes.Element) source;
                String prefix = updateNamespaces(sourceEl);
                String namespace = namespaces.get(prefix);
                Element el = doc.createElementNS(namespace, sourceEl.tagName());
                copyAttributes(sourceEl, el);
                if (dest == null) { 
                    doc.appendChild(el);
                } else {
                    dest.appendChild(el);
                }
                dest = el; 
            } else if (source instanceof org.jsoup.nodes.TextNode) {
                org.jsoup.nodes.TextNode sourceText = (org.jsoup.nodes.TextNode) source;
                Text text = doc.createTextNode(sourceText.getWholeText());
                dest.appendChild(text);
            } else if (source instanceof org.jsoup.nodes.Comment) {
                org.jsoup.nodes.Comment sourceComment = (org.jsoup.nodes.Comment) source;
                Comment comment = doc.createComment(sourceComment.getData());
                dest.appendChild(comment);
            } else if (source instanceof org.jsoup.nodes.DataNode) {
                org.jsoup.nodes.DataNode sourceData = (org.jsoup.nodes.DataNode) source;
                Text node = doc.createTextNode(sourceData.getWholeData());
                dest.appendChild(node);
            } else {
                
            }
        }

        public void tail(org.jsoup.nodes.Node source, int depth) {
            if (source instanceof org.jsoup.nodes.Element && dest.getParentNode() instanceof Element) {
                dest = (Element) dest.getParentNode(); 
            }
        }

        private void copyAttributes(org.jsoup.nodes.Node source, Element el) {
            for (Attribute attribute : source.attributes()) {
                
                String key = attribute.getKey().replaceAll("[^-a-zA-Z0-9_:.]", "");
                if (key.matches("[a-zA-Z_:]{1}[-a-zA-Z0-9_:.]*"))
                    el.setAttribute(key, attribute.getValue());
            }
        }
        
        private String updateNamespaces(org.jsoup.nodes.Element el) {
            Attributes attributes = el.attributes();
            for (Attribute attr : attributes) {
                String key = attr.getKey();
                String prefix;
                if (key.equals(xmlnsKey)) {
                    prefix = "";
                } else if (key.startsWith(xmlnsPrefix)) {
                    prefix = key.substring(xmlnsPrefix.length());
                } else {
                    continue;
                }
                namespaces.put(prefix, attr.getValue());
            }
            int pos = el.tagName().indexOf(":");
            return pos > 0 ? el.tagName().substring(0, pos) : "";
        }
    }
    
    public String asString(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

}