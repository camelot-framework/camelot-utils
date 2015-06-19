package ru.yandex.qatools.camelot.plugin;

import java.io.Serializable;

/**
 * @author Ilya Sadykov smecsia@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class GraphiteValue implements Serializable {

    private String name;
    private double value;
    private long timestamp;

    public GraphiteValue(String name, double value, long timestamp) {
        this.name = name;
        this.value = value;
        this.timestamp = timestamp;
    }

    public GraphiteValue(Object source, double value, long timestamp) {
        this(source.getClass().getName(), value, timestamp);
    }

    /**
     * just a shortcut method for constructor
     */
    @SuppressWarnings("UnusedDeclaration")
    public static GraphiteValue gValue(Object source, double value, long timestamp) {
        return new GraphiteValue(source, value, timestamp);
    }

    /**
     * just a shortcut method for constructor
     */
    @SuppressWarnings("UnusedDeclaration")
    public static GraphiteValue gValue(String name, double value, long timestamp) {
        return new GraphiteValue(name, value, timestamp);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return String.valueOf(value);
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("(%s:%s:%d)", name, value, timestamp);
    }
}
