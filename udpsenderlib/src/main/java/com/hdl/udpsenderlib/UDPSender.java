package com.hdl.udpsenderlib;


import android.os.Handler;
import android.os.Message;


/**
 * UDP管理器，隔离UDP实现层逻辑
 * Created by dali on 2017/4/14.
 */

public class UDPSender {
    private static UDPSender udpSender;

    private UDPSender() {
    }

    public static UDPSender getInstance() {
        if (udpSender == null) {
            synchronized (UDPSender.class) {
                udpSender = new UDPSender();
            }
        }
        return udpSender;
    }

    /**
     * 拿到一个结果
     */
    private static final int WHAT_TASK_NEXT = 711;
    /**
     * 任务结束
     */
    private static final int WHAT_TASK_FINSHED = 95;
    /**
     * 任务错误
     */
    private static final int WHAT_TASK_ERROR = 123;

    /**
     * 接收数据超时时间
     */
    private long receiveTimeOut = 60 * 1000;//默认60s


    /**
     * 目标端口，默认为8899
     */
    private int targetPort = UDPManager.DEFAULT_PORT;

    /**
     * 本机接收端口
     */
    private int localReceivePort = -1;
    /**
     * 指定数组（字节）
     */
    private byte[] instructions;
    /**
     * 发送的次数，默认是1次
     */
    private int sendCount = 1;

    /**
     * 上一次结束时到下一次开始的时间，默认10s
     */
    private long delay = 10 * 1000;
    /**
     * 目标ip地址，默认为广播
     */
    private String targetIp = "255.255.255.255";
    /**
     * 默认是在运行的
     */
    private boolean isRunning = true;

    /**
     * 设置接收超时时间
     *
     * @param receiveTimeOut 超时时间
     * @return
     */
    public UDPSender setReceiveTimeOut(long receiveTimeOut) {
        this.receiveTimeOut = receiveTimeOut;
        return this;
    }

    /**
     * 设置目标ip地址，默认为广播
     *
     * @param targetIp 目标ip地址
     * @return
     */
    public UDPSender setTargetIp(String targetIp) {
        this.targetIp = targetIp;
        return this;
    }

    private Handler handler = new Handler() {
        UDPResult result;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_TASK_NEXT:
                    if (isRunning()) {
                        result = (UDPResult) msg.obj;
                        callback.onNext(result);
                    }
                    break;
                case WHAT_TASK_ERROR:
                    Throwable throwable = (Throwable) msg.obj;
                    callback.onError(throwable);
                    break;
                case WHAT_TASK_FINSHED:
                    isRunning = false;
                    callback.onCompleted();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 设置指令
     *
     * @param instructions 指令字节数组
     * @return
     */
    public UDPSender setInstructions(byte[] instructions) {
        this.instructions = instructions;
        return this;
    }

    /**
     * 设置请求端口
     *
     * @param targetPort 请求端口号，默认为8899，范围是1024-65535
     * @return 当前发送器对象
     */
    public UDPSender setTargetPort(int targetPort) {
        this.targetPort = targetPort;
        return this;
    }

    /**
     * 设置请求端口
     *
     * @param localReceivePort 本机接收端口号，默认为8899，范围是1024-65535
     * @return 当前发送器对象
     */
    public UDPSender setLocalReceivePort(int localReceivePort) {
        this.localReceivePort = localReceivePort;
        return this;
    }

    private int currentCount = 1;

    /**
     * 发送UDP广播
     *
     * @param callback 结果回调
     */
    public synchronized void start(final UDPResultCallback callback) {
        this.callback = callback;
        callback.onStart();
        startTask();
    }

    private UDPResultCallback callback;

    private UDPThread udpThread;

    private void startTask() {
        if (udpThread != null && udpThread.isRuning()) {
            Message msg = handler.obtainMessage();
            msg.obj = new Throwable("Task running");
            msg.what = WHAT_TASK_ERROR;
            handler.sendMessage(msg);
            handler.sendEmptyMessage(WHAT_TASK_FINSHED);
        } else {
            udpThread = new UDPThread();//重新创建对象
            udpThread.setInstructions(instructions);//设置发送的指令
            udpThread.setTargetPort(targetPort);//设置目标端口
            udpThread.setTargetIp(targetIp);//设置目标ip地址
            udpThread.setReceivePort((localReceivePort == -1) ? targetPort : localReceivePort);//设置接收端口(默认为发送端口)
            udpThread.setReceiveTimeOut(receiveTimeOut + (currentCount - 1) * delay);//设置接收超时时间
            udpThread.getCallback(new UDPResultCallback() {
                @Override
                public void onNext(UDPResult result) {
                    Message msg = handler.obtainMessage();
                    msg.obj = result;
                    msg.what = WHAT_TASK_NEXT;
                    handler.sendMessage(msg);
                }

                @Override
                public void onError(Throwable throwable) {
                    Message msg = handler.obtainMessage();
                    msg.obj = throwable;
                    msg.what = WHAT_TASK_ERROR;
                    handler.sendMessage(msg);
                }

                @Override
                public void onCompleted() {
                    currentCount++;
                    if (currentCount <= sendCount) {
                        if (isRunning) {
                            startTask();
                        }
                    } else {
                        currentCount = 0;//要复位
                        stop();
                    }
                }
            });
            udpThread.start();
        }
    }

    /**
     * 是否正在运行
     *
     * @return
     */
    public boolean isRunning() {
        if (udpThread == null) {
            return false;
        }
        return udpThread.isRuning();
    }

    /**
     * 停止运行
     */
    public UDPSender stop() {
        if (isRunning()) {
            handler.sendEmptyMessage(WHAT_TASK_FINSHED);//停止当前任务
            udpThread.stopThread();
        }
        return this;
    }

    /**
     * 发送安排
     *
     * @param sendCount 发送次数
     * @param delay     上一次结束到下一次开始的间隔
     */
    public UDPSender schedule(int sendCount, long delay) {
        this.sendCount = sendCount;
        this.delay = delay;
        return this;
    }
}
