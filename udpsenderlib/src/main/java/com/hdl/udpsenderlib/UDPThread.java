package com.hdl.udpsenderlib;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

/**
 * UDP发送的线程
 * Created by HDL on 2017/6/7.
 */

class UDPThread extends Thread {
    /**
     * 发送的命令
     */
    private byte instructions[];
    /**
     * 拿到结果
     */
    private static final int WHAT_UDPTHREAD_GET_RESULT = 33;
    /**
     * 开始
     */
    private static final int WHAT_UDPTHREAD_START = 34;
    /**
     * 完成
     */
    private static final int WHAT_UDPTHREAD_FINISHED = 35;
    /**
     * 出错
     */
    private static final int WHAT_UDPTHREAD_ERROR = 36;
    /**
     * 发送次数,默认10次
     */
    public int send_time = 10;
    /**
     * 目标端口号
     */
    private int targetPort = 9988;
    /**
     * 接收端口号
     */
    private int receivePort = 8899;
    /**
     * 接收超时时间,
     * <p>当UDPSender接收数据时，如果超过了shakeTimeOut还是没有响应，那么一般就是已经搜索完成了，此时可根据这个值来判断是否要结束搜索 </p>
     * <p>默认8s，5是时间因子</p>
     */
    private long receiveTimeOut = 10 * 1000;
    /**
     * 最后一次接收的时间
     */
    private long lastReciveTime = System.currentTimeMillis();
    /**
     * 任务是否正在执行
     */
    private boolean isRuning;
    /**
     * 目标ip，默认为广播形式，可指定目标ip发送
     */
    private String targetIp = "255.255.255.255";

    private DatagramSocket server;
    private DatagramSocket broadcast;
    private Selector selector;
    private DatagramChannel udpChannel;
    private UDPResultCallback callback;

    /**
     * 设置接收超时时间
     *
     * @param receiveTimeOut
     */
    public void setReceiveTimeOut(long receiveTimeOut) {
        this.receiveTimeOut = receiveTimeOut;
    }

    /**
     * 设置发送的命令
     *
     * @param instructions
     */
    public void setInstructions(byte[] instructions) {
        this.instructions = instructions;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public void setReceivePort(int receivePort) {
        this.receivePort = receivePort;
    }

    /**
     * 设置ip地址
     *
     * @param targetIp
     */
    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (callback == null) {
                return;
            }
            switch (msg.what) {
                case WHAT_UDPTHREAD_START:
                    callback.onStart();
                    break;
                case WHAT_UDPTHREAD_GET_RESULT:
                    UDPResult result = (UDPResult) msg.obj;
                    callback.onNext(result);
                    lastReciveTime = System.currentTimeMillis();//记录最后一次接收的时间
                    break;
                case WHAT_UDPTHREAD_ERROR:
                    Throwable throwable = (Throwable) msg.obj;
                    callback.onError(throwable);
                    break;
                case WHAT_UDPTHREAD_FINISHED:
                    stopThread();
                    break;
            }

        }
    };

    /**
     * 拿到回调
     */
    public void getCallback(UDPResultCallback callback) {
        this.callback = callback;
    }

    /**
     * 发送错误，通知回调
     *
     * @param throwable 错误信息
     */
    private void handlerError(Throwable throwable) {
        isRuning = false;//设置为不在运行状态
        stopThread();//还要关闭通道哦
        Message msg = handler.obtainMessage();
        msg.obj = throwable;
        msg.what = WHAT_UDPTHREAD_ERROR;
        handler.sendMessage(msg);//通知错误信息
        handler.sendEmptyMessage(WHAT_UDPTHREAD_FINISHED);//错误的同时，需要结束任务
    }

    @Override
    public void run() {
        handler.sendEmptyMessage(WHAT_UDPTHREAD_START);
        isRuning = true;
        try {
            System.out.println("start udpthread");
            selector = Selector.open();
            udpChannel = DatagramChannel.open();
            udpChannel.configureBlocking(false);
            server = udpChannel.socket();
            if (!server.isConnected()) {
                server.bind(new InetSocketAddress(targetPort));
            }
            udpChannel.register(selector, SelectionKey.OP_READ);
            ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
            sendBroadcast();//发送广播

            UDPResult result;
            while (isRuning) {
                long currentReciveTime = System.currentTimeMillis();//记录当前接收时间
                if (receiveTimeOut < currentReciveTime - lastReciveTime) {//如果超过了指定的时间，还是没有接收到数据，那么就停止搜索
                    isRuning = false;//结束任务
                    handler.sendEmptyMessage(WHAT_UDPTHREAD_FINISHED);
                }

                int n = selector.select(100);
                if (n > 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    for (SelectionKey key : keys) {
                        keys.remove(key);
                        if (key.isReadable()) {
                            DatagramChannel dc = (DatagramChannel) key.channel();
                            dc.configureBlocking(false);
                            InetSocketAddress client = (InetSocketAddress) dc.receive(receiveBuffer);
                            key.interestOps(SelectionKey.OP_READ);
                            receiveBuffer.flip();
                            if (client.getPort() == receivePort) {//指定端口接收
                                result = new UDPResult();
                                result.setIp(client.getAddress().getHostAddress());
                                result.setResultData(receiveBuffer.array());
                                Message msg = handler.obtainMessage();
                                msg.obj = result;
                                msg.what = WHAT_UDPTHREAD_GET_RESULT;
                                handler.sendMessage(msg);
                            }
                            receiveBuffer.clear();
                        }
                    }

                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            handlerError(e);
        } finally {
            stopThread();
        }

    }

    /**
     * 发送广播
     */
    private void sendBroadcast() {
        if (!UDPUtils.isIpv4(targetIp)) {//判断是否是正确的ip地址
            handlerError(new Throwable("targetIp error"));
            return;
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    int times = 0;
                    broadcast = new DatagramSocket();
                    broadcast.setBroadcast(true);
                    while (times < send_time) {//每隔1s发送一次请求，持续sendtimes次
                        if (!isRuning) {
                            return;
                        }
                        times++;
                        if (instructions != null && instructions.length > 0) {
                            DatagramPacket packet = new DatagramPacket(instructions, instructions.length, InetAddress.getByName(targetIp), targetPort);
                            broadcast.send(packet);
                        }
                        Thread.sleep(1000);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    handlerError(e);
                } finally {
                    try {
                        broadcast.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 停止任务
     */
    public void stopThread() {
        callback.onCompleted();
        if (isRuning) {
            selector.wakeup();
            isRuning = false;
        }
        try {
            if (server != null && server.isConnected()) {
                server.close();
                server = null;
            }
            if (broadcast != null && broadcast.isConnected()) {
                broadcast.close();
                broadcast = null;
            }
            if (udpChannel != null && udpChannel.isOpen()) {
                udpChannel.close();
                udpChannel = null;
            }
            if (selector != null && selector.isOpen()) {
                selector.wakeup();
                selector.close();
                selector = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判读是否正在运行
     *
     * @return
     */
    public boolean isRuning() {
        return isRuning;
    }
}

