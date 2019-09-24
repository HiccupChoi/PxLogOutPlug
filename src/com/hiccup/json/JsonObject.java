package com.hiccup.json;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author chen
 */
public class JsonObject extends JsonValue {

    @SuppressWarnings("unused")
    private final static String TAG = "JsonObject";
    private final HashMap<String, JsonValue> map;

    public JsonObject() {
        map = new HashMap<String, JsonValue>(16);
    }

    public Set<Map.Entry<String, JsonValue>> entrySet() {
        return map.entrySet();
    }

    public Set<String> keySet() {
        return map.keySet();
    }



    public void put(String key, String[] data) {
        map.put(key, stringToArray(data));
    }

    private JsonArray stringToArray(String[] data) {
        JsonArray array = new JsonArray();
        if (data != null)
        {
            for (int i = 0; i < data.length; i++)
            {
                array.add(new JsonString(data[i]));
            }
        }
        return array;
    }

    public void put(String key, String[][] data) {
        JsonArray array = new JsonArray();
        if (data != null)
        {
            for (int i = 0; i < data.length; i++)
            {
                array.add(stringToArray(data[i]));
            }
        }
        map.put(key, array);
    }

    public void put(String key, int[] data) {
        JsonArray array = new JsonArray();
        if (data != null)
        {
            for (int i = 0; i < data.length; i++)
            {
                array.add(new JsonInt(data[i]));
            }
        }
        map.put(key, array);
    }

    public void put(String key, long[] data) {
        JsonArray array = new JsonArray();
        if (data != null)
        {
            for (int i = 0; i < data.length; i++)
            {
                array.add(new JsonLong(data[i]));
            }
        }
        map.put(key, array);
    }

    public void put(String key, Long[] data) {
        JsonArray array = new JsonArray();
        if (data != null)
        {
            for (int i = 0; i < data.length; i++)
            {
                array.add(new JsonLong(data[i]));
            }
        }
        map.put(key, array);
    }

    public void put(String key, String value) {
        //TODO    if (StringTool.isNotEmpty(value))
        {
            map.put(key, new JsonString(value));
        }
    }

    public void put(String key, long value) {
        map.put(key, new JsonLong(value));
    }

    public void put(String key, int value) {
        map.put(key, new JsonInt(value));
    }

    public void put(String key, JsonObject obj) {
        map.put(key, obj);
    }

    public void put(String key, float value) {
        //TODO       if (value != 0)
        {
            map.put(key, new JsonFloat(value));
        }
    }

    public void put(String key, double value) {
        //TODO    if (value != 0)
        {
            map.put(key, new JsonDouble(value));
        }
    }

    public void put(String key, boolean value) {
        map.put(key, new JsonBool(value));
    }

    public void put(String key, JsonValue value) {
        map.put(key, value);
    }



    public void put(String key, short[] array) {
        map.put(key, toJson(array));
    }

    public void put(String key, float[] array) {
        map.put(key, toJson(array));
    }

    public void put(String key, double[] array) {
        map.put(key, toJson(array));
    }

    public static JsonValue toJson(short[] array){
        JsonArray jArray = new JsonArray();
        if (array != null)
        {
            for (short v : array) {
                jArray.add(new JsonInt(v));
            }
        }
        return jArray;
    }
    public static JsonValue toJson(float[] array){
        JsonArray jArray = new JsonArray();
        if (array != null)
        {
            for (float v : array) {
                jArray.add(new JsonFloat(v));
            }
        }
        return jArray;
    }
    public static JsonValue toJson(double[] array){
        JsonArray jArray = new JsonArray();
        if (array != null)
        {
            for (double v : array) {
                jArray.add(new JsonDouble(v));
            }
        }
        return jArray;
    }


    public JsonValue getValue(String key) {
        if (key != null)
        {
            JsonValue value = map.get(key);
            if (value == null)
            {
                value = map.get(key.toLowerCase());
            }
            return value;
        }
        return null;
    }

    public String getString(String key) {
        JsonValue value = getValue(key);
        return value == null ? "" : value.asString();
    }

    public int getIntValue(String key) {
        JsonValue value = getValue(key);
        return value == null ? 0 : value.asInt();
    }

    public int getIntValue(String key, int defaultValue) {
        JsonValue value = getValue(key);
        return value == null ? defaultValue : value.asInt();
    }

    public boolean getBooleanValue(String key) {
        JsonValue value = getValue(key);
        return value == null ? false : value.asBool();
    }

    public long getLongValue(String key) {
        JsonValue value = getValue(key);
        return value == null ? 0 : value.asLong();
    }

    public float getFloatValue(String key) {
        JsonValue value = getValue(key);
        return value == null ? 0 : value.asFloat();
    }

    public double getDoubleValue(String key) {
        JsonValue value = getValue(key);
        return value == null ? 0 : value.asDouble();
    }

    public JsonObject getJSONObject(String key) {
        JsonValue value = getValue(key);
        if (value != null && value.getType() == TYPE_OBJECT)
        {
            return (JsonObject) value;
        }
        return null;
    }

    public JsonArray getJSONArray(String key) {
        JsonValue value = getValue(key);
        if (value != null && value.getType() == TYPE_OBJECT_ARRAY)
        {
            return (JsonArray) value;
        }
        return null;
    }

    public void clear() {
        map.clear();
    }

    public String readUTF(String key) {
        JsonValue value = getValue(key);
        return value == null || value.isNull() ? "" : value.asString();
    }

    public byte readByte(String key) {
        JsonValue value = getValue(key);
        return value == null ? 0 : (byte) value.asInt();
    }

    public short readShort(String key) {
        JsonValue value = getValue(key);
        return value == null ? 0 : (short) value.asInt();
    }

    public int readInt(String key) {
        JsonValue value = getValue(key);
        return value == null ? 0 : value.asInt();
    }

    public long readLong(String key) {
        JsonValue value = getValue(key);
        return value == null ? 0 : value.asLong();
    }

    public int readInt(String key, int defaultValue) {
        JsonValue value = getValue(key);
        return value == null ? defaultValue : value.asInt();
    }

    public long readLong(String key, long defaultValue) {
        JsonValue value = getValue(key);
        return value == null ? defaultValue : value.asLong();
    }


    public short[] readShortArray(String key) {
        JsonValue value = getValue(key);
        if (value != null)
        {
            if (value.getType() == TYPE_OBJECT_ARRAY)
            {
                JsonArray array = (JsonArray) value;
                ArrayList<JsonValue> datas = array.getDatas();
                int len = datas.size();
                short[] ret = new short[len];
                for (int i = 0; i < len; i++)
                {
                    ret[i] = (short)datas.get(i).asInt();
                }
                return ret;
            }
        }
        return null;
    }

    public int[] readIntArray(String key) {
        JsonValue value = getValue(key);
        if (value != null)
        {
            if (value.getType() == TYPE_OBJECT_ARRAY)
            {
                JsonArray array = (JsonArray) value;
                ArrayList<JsonValue> datas = array.getDatas();
                int len = datas.size();
                int[] ret = new int[len];
                for (int i = 0; i < len; i++)
                {
                    ret[i] = datas.get(i).asInt();
                }
                return ret;
            }
        }
        return null;
    }

    public long[] readLongArray(String key) {
        JsonValue value = getValue(key);
        if (value != null)
        {
            if (value.getType() == TYPE_OBJECT_ARRAY)
            {
                JsonArray array = (JsonArray) value;
                ArrayList<JsonValue> datas = array.getDatas();
                int len = datas.size();
                long[] ret = new long[len];
                for (int i = 0; i < len; i++)
                {
                    ret[i] = datas.get(i).asLong();
                }
                return ret;
            }
        }
        return null;
    }


    public float[] readFloatArray(String key) {
        JsonValue value = getValue(key);
        if (value != null)
        {
            if (value.getType() == TYPE_OBJECT_ARRAY)
            {
                JsonArray array = (JsonArray) value;
                ArrayList<JsonValue> datas = array.getDatas();
                int len = datas.size();
                float[] ret = new float[len];
                for (int i = 0; i < len; i++)
                {
                    ret[i] = datas.get(i).asFloat();
                }
                return ret;
            }
        }
        return null;
    }

    public double[] readDoubleArray(String key) {
        JsonValue value = getValue(key);
        if (value != null)
        {
            if (value.getType() == TYPE_OBJECT_ARRAY)
            {
                JsonArray array = (JsonArray) value;
                ArrayList<JsonValue> datas = array.getDatas();
                int len = datas.size();
                double[] ret = new double[len];
                for (int i = 0; i < len; i++)
                {
                    ret[i] = datas.get(i).asDouble();
                }
                return ret;
            }
        }
        return null;
    }

    public Long[] readLLongArray(String key) {
        JsonValue value = getValue(key);
        if (value != null)
        {
            if (value.getType() == TYPE_OBJECT_ARRAY)
            {
                JsonArray array = (JsonArray) value;
                ArrayList<JsonValue> datas = array.getDatas();
                int len = datas.size();
                Long[] ret = new Long[len];
                for (int i = 0; i < len; i++)
                {
                    ret[i] = datas.get(i).asLong();
                }
                return ret;
            }
        }
        return null;
    }

    private String[] arrayToStringArray(JsonArray array) {
        ArrayList<JsonValue> datas = array.getDatas();
        int len = datas.size();
        String[] ret = new String[len];
        for (int i = 0; i < len; i++)
        {
            ret[i] = datas.get(i).asString();
        }
        return ret;
    }

    private String[] valueToStringArray(JsonValue value) {
        if (value != null)
        {
            if (value.getType() == TYPE_OBJECT_ARRAY)
            {
                JsonArray array = (JsonArray) value;
                return arrayToStringArray(array);
            } else
            {
                String values = value.asString();
                if (!values.isEmpty())
                {
                    return values.split(",");
                }
            }
        }
        return null;
    }

    public String[] readStringArray(String key) {
        JsonValue value = getValue(key);
        return valueToStringArray(value);
    }

    public String[][] readStringArrayArray(String key) {
        JsonValue value = getValue(key);
        if (value != null)
        {
            if (value.getType() == TYPE_OBJECT_ARRAY)
            {
                JsonArray array = (JsonArray) value;
                ArrayList<JsonValue> datas = array.getDatas();
                int len = datas.size();
                String[][] ret = new String[len][];
                for (int i = 0; i < len; i++)
                {
                    ret[i] = valueToStringArray(datas.get(i));
                }
                return ret;
            }
        }
        return null;
    }

    public float readFloat(String key) {
        JsonValue value = getValue(key);
        return value == null ? 0 : value.asFloat();
    }

    public double readDouble(String key) {
        JsonValue value = getValue(key);
        return value == null ? 0 : value.asDouble();
    }

    @Override
    public int getType() {
        return TYPE_OBJECT;
    }

    public static JsonObject parse(String jsonStr) {
        return jsonStr == null ? null : parse(jsonStr, 0, jsonStr.length());
    }

    public static JsonObject parse(String jsonStr, int off, int end) {
        JsonParser reader = new JsonParser(jsonStr, off, end);
        return reader.parseObject();
    }

    public static JsonValue parseValue(String jsonStr) {
        return jsonStr == null ? null : parseValue(jsonStr, 0, jsonStr.length());
    }

    public static JsonValue parseValue(String jsonStr, int off, int end) {
        JsonParser reader = new JsonParser(jsonStr, off, end);
        return reader.parseValue();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void remove(String key) {
        map.remove(key);
    }

    @Override
    public JsonObject asObject() {
        return this;
    }

    public Map<String, Object> toMap() {
        Iterator<Map.Entry<String, JsonValue>> it = map.entrySet().iterator();
        Map<String, Object> ret = new HashMap<String, Object>();
        while (it.hasNext())
        {
            Map.Entry<String, JsonValue> entry = it.next();
            JsonValue value = entry.getValue();
            if (value != null)
            {
                ret.put(entry.getKey(), value.toObject());
            }
        }
        return ret;
    }

    @Override
    public Object toObject() {
        return toMap();
    }

}
