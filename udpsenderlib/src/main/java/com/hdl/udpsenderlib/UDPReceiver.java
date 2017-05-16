package com.hdl.udpsenderlib;

import android.content.Context;

/**
 * UDP接收器
 * Created by dali on 2017/5/16.
 */

public class UDPReceiver {
    private static UDPReceiver receiver;
    private int port;
    private Context mContext;
    private UDPHelper udpHelper;

    private UDPReceiver() {
    }

    public static UDPReceiver getInstance() {

        if (receiver == null) {
            receiver = new UDPReceiver();
        }
        return receiver;
    }

    public UDPReceiver with(Context context) {
        this.mContext = context;
        return this;
    }

    /**
     * 设置端口
     *
     * @param port
     * @return
     */
    public UDPReceiver setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * 发送UDP广播
     *
     * @param callback 结果回调
     */
    public synchronized void receive(final UDPResultCallback callback) {
        callback.onStart();
        udpHelper = new UDPHelper(mContext, port);
        udpHelper.startReciver(callback);
    }

    /**
     * 停止接收
     *
     * @return
     */
    public void stopReceive() {
        udpHelper.stopReceive();
    }
}
