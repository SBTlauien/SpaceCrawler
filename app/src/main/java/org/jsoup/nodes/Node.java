package org.jsoup.nodes;

import org.jsoup.SerializationException;
import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public abstract class Node implements Cloneable {
    private static final List<Node> EMPTY_NODES = Collections.emptyList();
    Node parentNode;
    List<Node> childNodes;
    Attributes attributes;
    String baseUri;
    int siblingIndex;

    
    protected Node(String baseUri, Attributes attributes) {
        Validate.notNull(baseUri);
        Validate.notNull(attributes);
        
        childNodes = EMPTY_NODES;
        this.baseUri = baseUri.trim();
        this.attributes = attributes;
    }

    protected Node(String baseUri) {
        this(baseUri, new Attributes());
    }

    
    protected Node() {
        childNodes = EMPTY_NODES;
        attributes = null;
    }

    
    public abstract String nodeName();

    
    public String attr(String attributeKey) {
        Validate.notNull(attributeKey);

        String val = attributes.getIgnoreCase(attributeKey);
        if (val.length() > 0)
            return val;
        else if (attributeKey.toLowerCase().startsWith("abs:"))
            return absUrl(attributeKey.substring("abs:".length()));
        else return "";
    }

    
    public Attributes attributes() {
        return attributes;
    }

    
    public Node attr(String attributeKey, String attributeValue) {
        attributes.put(attributeKey, attributeValue);
        return this;
    }

    
    public boolean hasAttr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributeKey.startsWith("abs:")) {
            String key = attributeKey.substring("abs:".length());
            if (attributes.hasKeyIgnoreCase(key) && !absUrl(key).equals(""))
                return true;
        }
        return attributes.hasKeyIgnoreCase(attributeKey);
    }

    
    public Node removeAttr(String attributeKey) {
        Validate.notNull(attributeKey);
        attributes.removeIgnoreCase(attributeKey);
        return this;
    }

    
    public String baseUri() {
        return baseUri;
    }

    
    public void setBaseUri(final String baseUri) {
        Validate.notNull(baseUri);

        traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                node.baseUri = baseUri;
            }

            public void tail(Node node, int depth) {
            }
        });
    }

    
    public String absUrl(String attributeKey) {
        Validate.notEmpty(attributeKey);

        if (!hasAttr(attributeKey)) {
            return ""; 
        } else {
            return StringUtil.resolve(baseUri, attr(attributeKey));
        }
    }

    
    public Node childNode(int index) {
        return childNodes.get(index);
    }

    
    public List<Node> childNodes() {
        return Collections.unmodifiableList(childNodes);
    }

    
    public List<Node> childNodesCopy() {
        List<Node> children = new ArrayList<Node>(childNodes.size());
        for (Node node : childNodes) {
            children.add(node.clone());
        }
        return children;
    }

    
    public final int childNodeSize() {
        return childNodes.size();
    }
    
    protected Node[] childNodesAsArray() {
        return childNodes.toArray(new Node[childNodeSize()]);
    }

    
    public Node parent() {
        return parentNode;
    }

    
    public final Node parentNode() {
        return parentNode;
    }

    
    public Node root() {
        Node node = this;
        while (node.parentNode != null)
            node = node.parentNode;
        return node;
    }
    
    
    public Document ownerDocument() {
        Node root = root();
        return (root instanceof Document) ? (Document) root : null;
    }
    
    
    public void remove() {
        Validate.notNull(parentNode);
        parentNode.removeChild(this);
    }

    
    public Node before(String html) {
        addSiblingHtml(siblingIndex, html);
        return this;
    }

    
    public Node before(Node node) {
        Validate.notNull(node);
        Validate.notNull(parentNode);

        parentNode.addChildren(siblingIndex, node);
        return this;
    }

    
    public Node after(String html) {
        addSiblingHtml(siblingIndex + 1, html);
        return this;
    }

    
    public Node after(Node node) {
        Validate.notNull(node);
        Validate.notNull(parentNode);

        parentNode.addChildren(siblingIndex + 1, node);
        return this;
    }

    private void addSiblingHtml(int index, String html) {
        Validate.notNull(html);
        Validate.notNull(parentNode);

        Element context = parent() instanceof Element ? (Element) parent() : null;        
        List<Node> nodes = Parser.parseFragment(html, context, baseUri());
        parentNode.addChildren(index, nodes.toArray(new Node[nodes.size()]));
    }

    
    public Node wrap(String html) {
        Validate.notEmpty(html);

        Element context = parent() instanceof Element ? (Element) parent() : null;
        List<Node> wrapChildren = Parser.parseFragment(html, context, baseUri());
        Node wrapNode = wrapChildren.get(0);
        if (wrapNode == null || !(wrapNode instanceof Element)) 
            return null;

        Element wrap = (Element) wrapNode;
        Element deepest = getDeepChild(wrap);
        parentNode.replaceChild(this, wrap);
        deepest.addChildren(this);

        
        if (wrapChildren.size() > 0) {
            for (int i = 0; i < wrapChildren.size(); i++) {
                Node remainder = wrapChildren.get(i);
                remainder.parentNode.removeChild(remainder);
                wrap.appendChild(remainder);
            }
        }
        return this;
    }

    
    public Node unwrap() {
        Validate.notNull(parentNode);

        Node firstChild = childNodes.size() > 0 ? childNodes.get(0) : null;
        parentNode.addChildren(siblingIndex, this.childNodesAsArray());
        this.remove();

        return firstChild;
    }

    private Element getDeepChild(Element el) {
        List<Element> children = el.children();
        if (children.size() > 0)
            return getDeepChild(children.get(0));
        else
            return el;
    }
    
    
    public void replaceWith(Node in) {
        Validate.notNull(in);
        Validate.notNull(parentNode);
        parentNode.replaceChild(this, in);
    }

    protected void setParentNode(Node parentNode) {
        if (this.parentNode != null)
            this.parentNode.removeChild(this);
        this.parentNode = parentNode;
    }

    protected void replaceChild(Node out, Node in) {
        Validate.isTrue(out.parentNode == this);
        Validate.notNull(in);
        if (in.parentNode != null)
            in.parentNode.removeChild(in);
        
        final int index = out.siblingIndex;
        childNodes.set(index, in);
        in.parentNode = this;
        in.setSiblingIndex(index);
        out.parentNode = null;
    }

    protected void removeChild(Node out) {
        Validate.isTrue(out.parentNode == this);
        final int index = out.siblingIndex;
        childNodes.remove(index);
        reindexChildren(index);
        out.parentNode = null;
    }

    protected void addChildren(Node... children) {
        
        for (Node child: children) {
            reparentChild(child);
            ensureChildNodes();
            childNodes.add(child);
            child.setSiblingIndex(childNodes.size()-1);
        }
    }

    protected void addChildren(int index, Node... children) {
        Validate.noNullElements(children);
        ensureChildNodes();
        for (int i = children.length - 1; i >= 0; i--) {
            Node in = children[i];
            reparentChild(in);
            childNodes.add(index, in);
            reindexChildren(index);
        }
    }

    protected void ensureChildNodes() {
        if (childNodes == EMPTY_NODES) {
            childNodes = new ArrayList<Node>(4);
        }
    }

    protected void reparentChild(Node child) {
        if (child.parentNode != null)
            child.parentNode.removeChild(child);
        child.setParentNode(this);
    }
    
    private void reindexChildren(int start) {
        for (int i = start; i < childNodes.size(); i++) {
            childNodes.get(i).setSiblingIndex(i);
        }
    }
    
    
    public List<Node> siblingNodes() {
        if (parentNode == null)
            return Collections.emptyList();

        List<Node> nodes = parentNode.childNodes;
        List<Node> siblings = new ArrayList<Node>(nodes.size() - 1);
        for (Node node: nodes)
            if (node != this)
                siblings.add(node);
        return siblings;
    }

    
    public Node nextSibling() {
        if (parentNode == null)
            return null; 
        
        final List<Node> siblings = parentNode.childNodes;
        final int index = siblingIndex+1;
        if (siblings.size() > index)
            return siblings.get(index);
        else
            return null;
    }

    
    public Node previousSibling() {
        if (parentNode == null)
            return null; 

        if (siblingIndex > 0)
            return parentNode.childNodes.get(siblingIndex-1);
        else
            return null;
    }

    
    public int siblingIndex() {
        return siblingIndex;
    }
    
    protected void setSiblingIndex(int siblingIndex) {
        this.siblingIndex = siblingIndex;
    }

    
    public Node traverse(NodeVisitor nodeVisitor) {
        Validate.notNull(nodeVisitor);
        NodeTraversor traversor = new NodeTraversor(nodeVisitor);
        traversor.traverse(this);
        return this;
    }

    
    public String outerHtml() {
        StringBuilder accum = new StringBuilder(128);
        outerHtml(accum);
        return accum.toString();
    }

    protected void outerHtml(Appendable accum) {
        new NodeTraversor(new OuterHtmlVisitor(accum, getOutputSettings())).traverse(this);
    }

    
    Document.OutputSettings getOutputSettings() {
        Document owner = ownerDocument();
        return owner != null ? owner.outputSettings() : (new Document("")).outputSettings();
    }

    
    abstract void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) throws IOException;

    abstract void outerHtmlTail(Appendable accum, int depth, Document.OutputSettings out) throws IOException;

    
    public <T extends Appendable> T html(T appendable) {
        outerHtml(appendable);
        return appendable;
    }
    
	public String toString() {
        return outerHtml();
    }

    protected void indent(Appendable accum, int depth, Document.OutputSettings out) throws IOException {
        accum.append("\n").append(StringUtil.padding(depth * out.indentAmount()));
    }

    
    @Override
    public boolean equals(Object o) {
        
        return this == o;
    }

    

    public boolean hasSameValue(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return this.outerHtml().equals(((Node) o).outerHtml());
    }

    
    @Override
    public Node clone() {
        Node thisClone = doClone(null); 

        
        LinkedList<Node> nodesToProcess = new LinkedList<Node>();
        nodesToProcess.add(thisClone);

        while (!nodesToProcess.isEmpty()) {
            Node currParent = nodesToProcess.remove();

            for (int i = 0; i < currParent.childNodes.size(); i++) {
                Node childClone = currParent.childNodes.get(i).doClone(currParent);
                currParent.childNodes.set(i, childClone);
                nodesToProcess.add(childClone);
            }
        }

        return thisClone;
    }

    
    protected Node doClone(Node parent) {
        Node clone;

        try {
            clone = (Node) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        clone.parentNode = parent; 
        clone.siblingIndex = parent == null ? 0 : siblingIndex;
        clone.attributes = attributes != null ? attributes.clone() : null;
        clone.baseUri = baseUri;
        clone.childNodes = new ArrayList<Node>(childNodes.size());

        for (Node child: childNodes)
            clone.childNodes.add(child);

        return clone;
    }

    private static class OuterHtmlVisitor implements NodeVisitor {
        private Appendable accum;
        private Document.OutputSettings out;

        OuterHtmlVisitor(Appendable accum, Document.OutputSettings out) {
            this.accum = accum;
            this.out = out;
        }

        public void head(Node node, int depth) {
            try {
				node.outerHtmlHead(accum, depth, out);
			} catch (IOException exception) {
				throw new SerializationException(exception);
			}
        }

        public void tail(Node node, int depth) {
            if (!node.nodeName().equals("#text")) { 
				try {
					node.outerHtmlTail(accum, depth, out);
				} catch (IOException exception) {
					throw new SerializationException(exception);
				}
            }
        }
    }
}
