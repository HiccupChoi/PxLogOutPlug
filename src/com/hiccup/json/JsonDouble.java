package com.hiccup.json;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 *
 * @author chen
 */
public class JsonDouble extends JsonValue {

    @SuppressWarnings("unused")
    private final static String TAG = "JsonDouble";
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
    private double value;
    
    static {
        NUMBER_FORMAT.setGroupingUsed(false);
    }

    public JsonDouble(double value) {
        this.value = value;
        if (Double.isNaN(value))
        {
            this.value = 0;
        }
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return toString(value);
    }

    @Override
    public double asDouble() {
        return value;
    }

    @Override
    public float asFloat() {
        return (float) value;
    }

    @Override
    public long asLong() {
        return (long) value;
    }

    @Override
    public int asInt() {
        return (int) value;
    }

    @Override
    public boolean asBool() {
        return BigDecimal.valueOf(value).compareTo(BigDecimal.valueOf(1d))==0;
    }

    @Override
    public void write(StringBuilder sb, boolean encode) {
        sb.append(toString(value));
    }

    @Override
    public int getType() {
        return TYPE_DOUBLE;
    }

    @Override
    public Object toObject() {
        return value;
    }
    
    public static String toString(double value){
        return NUMBER_FORMAT.format(value);
    }
}
