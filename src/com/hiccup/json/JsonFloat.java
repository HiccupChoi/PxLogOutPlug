package com.hiccup.json;

import java.math.BigDecimal;

/**
 *
 * @author chen
 */
public class JsonFloat extends JsonValue {

    @SuppressWarnings("unused")
    private final static String TAG = "JsonFloat";
    private float value;

    public JsonFloat(float value) {
        this.value = value;
        if (Float.isNaN(value))
        {
            this.value = 0;
        }
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return JsonDouble.toString(value);
    }

    @Override
    public double asDouble() {
        return value;
    }

    @Override
    public float asFloat() {
        return value;
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
        return BigDecimal.valueOf(value).compareTo(BigDecimal.valueOf(1f))==0;
    }

    @Override
    public void write(StringBuilder sb, boolean encode) {
        sb.append(JsonDouble.toString(value));
    }

    @Override
    public int getType() {
        return TYPE_FLOAT;
    }

    @Override
    public Object toObject() {
        return value;
    }
}
