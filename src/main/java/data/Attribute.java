package data;

import com.sun.org.apache.regexp.internal.RE;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Attribute implements Serializable {

    public static final Map<Relation, String> relationToOperator = Stream.of(
        new SimpleEntry<>(Relation.EQ, "="),
        new SimpleEntry<>(Relation.NEQ, "!="),
        new SimpleEntry<>(Relation.LT, "<"),
        new SimpleEntry<>(Relation.LTE, "<="),
        new SimpleEntry<>(Relation.GT, ">"),
        new SimpleEntry<>(Relation.GTE, ">=")
    ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    private static final Map<String, Relation> supportedRelations = Stream.of(
        new SimpleEntry<>("=", Relation.EQ),
        new SimpleEntry<>("!=", Relation.NEQ),
        new SimpleEntry<>("<", Relation.LT),
        new SimpleEntry<>("<=", Relation.LTE),
        new SimpleEntry<>(">", Relation.GT),
        new SimpleEntry<>(">=", Relation.GTE)
    ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    public enum Relation {
        EQ, NEQ, LT, LTE, GT, GTE,
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private Relation relation;
    private String value;
    private String type;

    public void setType(String type) {
        this.type = type;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Attribute(){}

    public Attribute(String name, Relation relation, String value, String type) {
        this.name = name;
        this.relation = relation;
        this.value = value;
        this.type = type;
    }

    public static Attribute parse (String attr) {

        Relation relation = null;
        String splitter = null;
        if(attr.contains("<=")) {
            relation = Relation.LTE;
            splitter = "<=";
        }else if(attr.contains(">=")){
            relation = Relation.GTE;
            splitter = ">=";
        }else if(attr.contains("!=")){
           relation =   Relation.NEQ;
            splitter = "!=";
        }else if (attr.contains("=")){
            relation = Relation.EQ;
            splitter = "=";
        }else if (attr.contains("<")){
            relation = Relation.LT;
            splitter = "<";
        }else if (attr.contains(">")){
            relation = Relation.GT;
            splitter = ">";
        }

        if (relation == null) {
            throw new IllegalArgumentException("Unsupported relation: ");
        }
        String[] parts = attr.split(splitter);

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid input for Attribute: " + attr);
        }

        switch (parts[0].trim()){
            case "name":
                return new Attribute(parts[0], relation, parts[1].trim(),"String");
            case "x":
                return  new Attribute(parts[0], relation, (parts[1]).trim(),"Integer");
            case "y":
                return  new Attribute(parts[0], relation, (parts[1]).trim(),"Integer");
            case "owner":
                return  new Attribute(parts[0], relation, parts[1].trim(),"String");
            case "filesize":
                return new Attribute(parts[0], relation, (parts[1]).trim(),"Long");
            default:
                throw new IllegalArgumentException("Unsupported argument: " + parts[0]);
        }
    }

    public static MetaData convertToMetaData(List<Attribute> attributes) {
        MetaData.Builder builder = new MetaData.Builder();
        for (Attribute attr : attributes) {
            switch (attr.getName()) {
                case "name":
                    builder.name(attr.getValue());
                    break;
                case "owner":
                    builder.ownerID(attr.getValue());
                    break;
                case "x":
                    builder.x(Integer.parseInt(attr.getValue()));
                    break;
                case "y":
                    builder.y(Integer.parseInt(attr.getValue()));
                    break;
                case "filesize":
                    builder.fileLength(Long.parseLong(attr.getValue()));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported attribute name: " + attr.getName());
            }
        }
        return builder.build();
    }

    /**
     * Returns whether the {@code otherValue} matches the attribute (i.e. if the attribute is true for the given value).
     */
    public boolean match(String otherValue) {

        Comparable v=null;
        Comparable oV=null;
        switch (type) {
            case "Integer":
                v = Integer.parseInt(value);
                oV = Integer.parseInt(otherValue);
                break;
            case "Long":
                v= Long.parseLong(value);
                oV=Long.parseLong(otherValue);
                break;
            case "String":
                v =value;
                oV = otherValue;
        }

        if (this.relation == Relation.EQ) {
            return v.equals(oV);
        }
        if (this.relation == Relation.NEQ) {
            return !v.equals(oV);
        }
        switch (this.relation) {
            case LT:
                return v.compareTo(oV) < 0;
            case LTE:
                return v.compareTo(oV) <= 0;
            case GT:
                return v.compareTo(oV) > 0;
            case GTE:
                return v.compareTo(oV) >= 0;
        }
        throw new IllegalArgumentException("Unsupported relation: " + this.relation);
    }

    public String getName() {
        return name;
    }

    public Relation getRelation() {
        return relation;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
