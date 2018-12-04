package data;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
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

    /**
     * Returns whether the {@code otherValue} matches the attribute (i.e. if the attribute is true for the given value).
     */
    public boolean match(String otherValue) {
        if (this.relation == Relation.EQ) {
            return this.getValue().equals(otherValue);
        }
        if (this.relation == Relation.NEQ) {
            return !this.getValue().equals(otherValue);
        }
        if (!otherValue.trim().matches("\\d+")) {
            // Cannot compare strings using relations other than == and !=.
            throw new IllegalArgumentException("Invalid operation on strings: " + this.relation);
        }
        switch (this.relation) {
            case LT:
                return Long.parseLong(otherValue) < Long.parseLong(this.getValue());
            case LTE:
                return Long.parseLong(otherValue) <= Long.parseLong(this.getValue());
            case GT:
                return Long.parseLong(otherValue) > Long.parseLong(this.getValue());
            case GTE:
                return Long.parseLong(otherValue) >= Long.parseLong(this.getValue());
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
}
