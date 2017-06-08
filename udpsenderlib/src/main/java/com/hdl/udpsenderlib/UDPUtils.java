package com.hdl.udpsenderlib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类，不对外提供服务
 * Created by HDL on 2017/6/8.
 */

class UDPUtils {
    /**
     * 判断字符串是否是ipv4地址格式
     *
     * @param ipAddress
     * @return
     */
    public static boolean isIpv4(String ipAddress) {
        String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();

    }
}
