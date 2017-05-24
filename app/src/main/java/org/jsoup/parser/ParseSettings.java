package org.jsoup.parser;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;


public class ParseSettings {
    
    public static final ParseSettings htmlDefault;
    
    public static final ParseSettings preserveCase;

    static {
        htmlDefault = new ParseSettings(false, false);
        preserveCase = new ParseSettings(true, true);
    }

    private final boolean preserveTagCase;
    private final boolean preserveAttributeCase;

    
    public ParseSettings(boolean tag, boolean attribute) {
        preserveTagCase = tag;
        preserveAttributeCase = attribute;
    }

    String normalizeTag(String name) {
        name = name.trim();
        if (!preserveTagCase)
            name = name.toLowerCase();
        return name;
    }

    String normalizeAttribute(String name) {
        name = name.trim();
        if (!preserveAttributeCase)
            name = name.toLowerCase();
        return name;
    }

    Attributes normalizeAttributes(Attributes attributes) {
        if (!preserveAttributeCase) {
            for (Attribute attr : attributes) {
                attr.setKey(attr.getKey().toLowerCase());
            }
        }
        return attributes;
    }
}
