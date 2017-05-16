package com.hdl.udpsenderlib;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

class UDPHelper {
    public Boolean IsThreadDisable = false;
    private final static String UDPHelper = "UDPHelper";
    public int port;
    InetAddress mInetAddress;

    MulticastSocket datagramSocket = null;
    public static final int WHAT_HANDLER_MESSAGE_BIND_ERROR = 0x01;
    private static final int WHAT_RECEIVE_NEXT = 0x198;
    private WeakReference<Context> mActivityReference;
    private WifiManager.MulticastLock lock;
    private boolean isStartSuccess = false;
    private UDPResultCallback callback;

    public UDPHelper(Context mContext, int port) {
        this.port = port;
        mActivityReference = new WeakReference<>(mContext);
        WifiManager manager = (WifiManager) mActivityReference.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lock = manager.createMulticastLock(UDPHelper);
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_RECEIVE_NEXT:
                    UDPResult result = (UDPResult) msg.obj;
                    callback.onNext(result);
                    break;
                case WHAT_HANDLER_MESSAGE_BIND_ERROR:
                    callback.onError(new Throwable("port bind error"));
                    break;
            }

        }
    };

    /**
     * 开始接收
     */
    public void startReciver(UDPResultCallback callback) {
        this.callback = callback;
        new Thread() {
            @Override
            public void run() {
                isStartSuccess = false;
                while (!isStartSuccess) {
                    listen();
                    try {
                        sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void listen() {
        // 接收的字节大小，客户端发送的数据不能超过这个大小
        byte[] message = new byte[1024];
        try {
            // 建立Socket连接
            try {
                datagramSocket = new MulticastSocket(port);
            } catch (Exception e) {
                port = 57521;
                datagramSocket = new MulticastSocket(port);
                e.printStackTrace();
            }
            datagramSocket.setBroadcast(true);
            datagramSocket.setSoTimeout(120 * 1000);
            DatagramPacket datagramPacket = new DatagramPacket(message,
                    message.length);
            isStartSuccess = true;
            while (!IsThreadDisable) {
                // 准备接收数据
                multicastLock();
                datagramSocket.receive(datagramPacket);
                mInetAddress = datagramPacket.getAddress();
                byte[] data = datagramPacket.getData();
                UDPResult result = new UDPResult();
                result.setIp(mInetAddress.getHostAddress());
                result.setResultData(data);
                Message msg = mHandler.obtainMessage();
                msg.what = WHAT_RECEIVE_NEXT;
                msg.obj = result;
                mHandler.sendMessage(msg);
                multicastUnLock();
            }
        } catch (SocketException e) {
            e.printStackTrace();
            //如果是此异常，isStartSuccess没有置为true，还会重新监听，所以这里不处理
        } catch (Exception e) {
            e.printStackTrace();
            IsThreadDisable = true;
            if (null != mHandler) {
                mHandler.sendEmptyMessage(WHAT_HANDLER_MESSAGE_BIND_ERROR);
            }
        } finally {
            multicastUnLock();
            if (null != datagramSocket) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }
    }

    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16) | ((src[offset + 3] & 0xFF) << 24);
        return value;
    }

    public void stopReceive() {
        this.IsThreadDisable = true;
        this.isStartSuccess = true;
        if (null != datagramSocket) {
            datagramSocket.close();
            datagramSocket = null;
        }
    }

    private void multicastLock() {
        if (this.lock != null) {
            try {
                this.lock.acquire();
            } catch (Exception e) {
                Log.e("UDPReciever", "multicastLock error");
            }
        }
    }

    private void multicastUnLock() {
        if (this.lock != null) {
            try {
                this.lock.release();
            } catch (Exception e) {
                Log.e("UDPReciever", "multicastUnLock error");
            }
        }
    }
}
