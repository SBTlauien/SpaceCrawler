package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.helper.Validate;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;

public class FormElement extends Element {
    private final Elements elements = new Elements();

    public FormElement(Tag tag, String baseUri, Attributes attributes) {
        super(tag, baseUri, attributes);
    }

    public Elements elements() {
        return elements;
    }
    
    public FormElement addElement(Element element) {
        elements.add(element);
        return this;
    }

    public Connection submit() {
        String action = hasAttr("action") ? absUrl("action") : baseUri();
        Validate.notEmpty(action, "Could not determine a form action URL for submit. Ensure you set a base URI when parsing.");
        Connection.Method method = attr("method").toUpperCase().equals("POST") ? Connection.Method.POST : Connection.Method.GET;
        return Jsoup.connect(action).data(formData()).method(method);
    }

    public List<Connection.KeyVal> formData() {
        ArrayList<Connection.KeyVal> data = new ArrayList<Connection.KeyVal>();
        for (Element el: elements) {
            if (!el.tag().isFormSubmittable()) continue; 
            if (el.hasAttr("disabled")) continue; 
            String name = el.attr("name");
            if (name.length() == 0) continue;
            String type = el.attr("type");
            if ("select".equals(el.tagName())) {
                Elements options = el.select("option[selected]");
                boolean set = false;
                for (Element option: options) {
                    data.add(HttpConnection.KeyVal.create(name, option.val()));
                    set = true;
                }
                if (!set) {
                    Element option = el.select("option").first();
                    if (option != null)
                        data.add(HttpConnection.KeyVal.create(name, option.val()));
                }
            } else if ("checkbox".equalsIgnoreCase(type) || "radio".equalsIgnoreCase(type)) {
                if (el.hasAttr("checked")) {
                    final String val = el.val().length() >  0 ? el.val() : "on";
                    data.add(HttpConnection.KeyVal.create(name, val));
                }
            } else {
                data.add(HttpConnection.KeyVal.create(name, el.val()));
            }
        }
        return data;
    }

}
