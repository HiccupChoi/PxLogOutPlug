package com.hiccup.json;

/**
 *
 * @author chen
 */
public class JsonString extends JsonValue {

    @SuppressWarnings("unused")
    private final static String TAG = "JsonString";
    private final static char[] HEX = "0123456789ABCDEF".toCharArray();
    protected String value;

    public JsonString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return value;
    }



    @Override
    public int getType() {
        return TYPE_STRING;
    }

    @Override
    public Object toObject() {
        return value;
    }

}
