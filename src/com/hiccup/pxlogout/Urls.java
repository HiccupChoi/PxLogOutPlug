package com.hiccup.pxlogout;

/**
 * @Author: Hiccup
 * @Date: 2019/9/24 11:09 上午
 */
public class Urls {
    static final String CODE = "code";
    static final String SUCCESS_CODE = "200";
    /**
     * 2.40以下版本注销地址
     */
    static final String OLD_URL_PATH = "/irs-iface/commons/cloudisk/upload";
    /**
     * 2.40及以上版本注销地址
     */
    static final String NEW_URL_PATH = "/paris-pos/equipment-unbind/unbindPos";
    /**
     * 登录接口
     * 获取sessionId用
     */
    static final String LOGIN_URL = "/irs-iface/om/inf/v1/login";

}
