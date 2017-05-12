package com.hdl.udpsenderlib;

import java.util.Arrays;

/**
 * UDP广播发送器返回的结果对象
 */
public class UDPResult {
    /**
     * 目标ip地址
     */
    private String ip;
    /**
     * 目标返回的结果
     */
    private byte[] resultData;

    public UDPResult() {
    }

    public UDPResult(String ip, byte[] resultData) {
        super();
        this.ip = ip;
        this.resultData = resultData;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public byte[] getResultData() {
        return resultData;
    }

    public void setResultData(byte[] resultData) {
        this.resultData = resultData;
    }

    @Override
    public String toString() {
        return "UDPResult [ip=" + ip + ", resultData="
                + Arrays.toString(resultData) + "]";
    }

}
