package data;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Attribute implements Serializable {
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

    private final String name;
    private final Relation relation;
    private final String value;

    public Attribute(String name, Relation relation, String value) {
        this.name = name;
        this.relation = relation;
        this.value = value;
    }

    public static Attribute parse(String attr) {
        String[] parts = attr.split("\\s+");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid input for Attribute: " + attr);
        }
        Relation relation = supportedRelations.get(parts[1]);
        if (relation == null) {
            throw new IllegalArgumentException("Unsupported relation: " + parts[1]);
        }
        return new Attribute(parts[0], relation, parts[2]);
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
}
