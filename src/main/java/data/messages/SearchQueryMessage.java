package data.messages;

import data.Attribute;

import java.io.Serializable;
import java.util.List;

public class SearchQueryMessage implements Serializable {

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public SearchQueryMessage(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    private List<Attribute> attributes;

    public SearchQueryMessage(){}


}
