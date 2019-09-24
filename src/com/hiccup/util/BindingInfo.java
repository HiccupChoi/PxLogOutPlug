package com.hiccup.util;


import com.hiccup.json.JsonObject;

/**
 * 绑定信息（是否单机判断）
 */
@SuppressWarnings("unused")
public class BindingInfo{

    private static final String TAG = "BindingInfo";

    public static final BindingInfo READER = new BindingInfo();
    /**
     * windows
     */
    public static final byte WINDOWS = 1;
    /**
     * android
     */
    public static final byte ANDROID = 2;
    /**
     * 在线
     */
    public static final int STATE_OK = 1;
    /**
     * 单机
     */
    public static final int STATE_OFFLINE_USE = 2;
    /**
     * 网络异常接口异常（未知）
     */
    public static final int STATE_NETWORK_ANOMALY = 3;

    /**
     * 1：windows  2:android
     */
    private byte equipmentType;
    /**
     * 安卓序列号，equipmentType == 2时 必传
     */
    private String androidNo;
    /**
     * windows网卡mac地址，equipmentType==1 必传
     */
    private String [] macAddr;
    /**
     * 客户端版本号
     */
    private String clientVersion;
    /**
     * 服务端版本号
     */
    private String serverVersion;
    /**
     * 操作系统名称
     */
    private String osName;
    /**
     * 操作系统版本（非必传）
     */
    private String osVersion;
    /**
     * 操作人id
     */
    private long userId;
    /**
     * 操作人名称
     */
    private String userName;


    public JsonObject write(JsonObject dout) {
        try {
            dout.put("equipmentType", this.equipmentType);
            dout.put("androidNo", this.androidNo);
            dout.put("macAddr", this.macAddr);
            dout.put("clientVersion",this.clientVersion);
            dout.put("serverVersion", this.serverVersion);
            dout.put("osName", this.osName);
            dout.put("osVersion", this.osVersion);
            dout.put("userId", this.userId);
            dout.put("userName", this.userName);
            return dout;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(byte equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getAndroidNo() {
        return androidNo;
    }

    public void setAndroidNo(String androidNo) {
        this.androidNo = androidNo;
    }

    public String[] getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String[] macAddr) {
        this.macAddr = macAddr;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
