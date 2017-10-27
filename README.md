# UDPSender  [![](https://jitpack.io/v/huangdali/UDPSender.svg)](https://jitpack.io/#huangdali/UDPSender)

基于NIO的UDP发送-接收器

**输入：**

- 指令byte数组
- 目标端口号

**输出：**

- UDPReuslt结果对象（包含对方IP地址、结果数组[byte类型的数组]）


## 导入
在你项目app/build.gradle中加入如下的代码

```java
    dependencies {
       ...
       compile 'com.jwkj:udpsender:v2.0.2'
    }
```




## 使用方法

### 快速上手

```java
 UDPSender.getInstance()
                .setInstructions(byteAraary)
                .setTargetPort(port)
                .start(new UDPResultCallback() {
                    @Override
                    public void onNext(UDPResult result) {
                        //do something
                    }
                });
```

### 完整demo

```java
UDPSender.getInstance()
                .setInstructions(byteAraary)//设置发送的指令[可为空]
                .setReceiveTimeOut(70 * 1000)//设置接收超时时间[可不写，默认为60s]--超时70s就停止任务
                .setTargetPort(port)//设置发送的端口[必写]
                .setLocalReceivePort(port)//设置本机接收的端口[可不写，默认为目标端口]
                .setTargetIp("192.168.1.150")//设置目标ip地址，[可不写，默认广播]
                .schedule(2, 3000)//执行2次，间隔三秒执行（上一次结束到下一次开始的时间）
                .start(new UDPResultCallback() {

                    @Override
                    public void onStart() {
                        //请求开始的时候回调
                    }

                    @Override
                    public void onNext(UDPResult result) {
                        //每收到一个结果的时候就回调
                    }

                    @Override
                    public void onCompleted() {
                        //请求结束的时候回调
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        //当发生错误的时候回调
                    }
                });
```

### 只接收数据

```java
 UDPSender.getInstance()
                .setLocalReceivePort(9988)//接收端口
                .start(new UDPResultCallback() {
                    @Override
                    public void onStart() {
                        ELog.hdl("开始了");
                    }

                    @Override
                    public void onNext(UDPResult result) {
                        ELog.hdl("收到结果" + result);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        ELog.hdl("出错" + throwable);
                    }

                    @Override
                    public void onCompleted() {
                        ELog.hdl("完成");
                    }
                });
```


## 关闭任务

```java
UDPSender.getInstance().stop();
```

## UDPReslt

```java
public class UDPResult {
    /**
     * 对方ip地址
     */
    private String ip;
    /**
     * 对方返回的结果
     */
    private byte[] resultData;
}
```

## 历史更新记录

### v2.X版

v2.0.2 ( [2017.10.27]() )
- 【修复】数据清理不及时，导致产生脏数据

v2.0.1
- 【修复】只能接收对方端口为自身接收端口的bug
- 【优化】底层库代码优化，已经相对成熟，可用于生产环境

> 温馨提示： 不建议使用v1.X版，建议使用最新版，使用方法不变


### v1.X版

v1.4.0

- 【新增】指定ip时默认只发送一次包(如需多次发送，可通过schedule方法指定)

- 【bug】只能发送方指定端口，bug，不要使用此版本

v1.3.9

- 【修复】调用定时任务时突然停止导致下一次重新开始任务时立刻停止当前的任务

- 【bug】只能发送方指定端口，bug，不要使用此版本

v1.3.8

- 【修复】修复特殊情况下的崩溃

- 【bug】只能发送方指定端口，bug，不要使用此版本

v1.3.7

- 【修复】任务结束时自动关闭相关接口

- 【bug】只能发送方指定端口，bug，不要使用此版本

 v1.3.6

- 【修复】多次调用onCompleted方法


- 【bug】只能发送方指定端口，bug，不要使用此版本

 v1.3.5

- 【修复】关闭任务（UDPSender.getInstance().stop()）不走onCompleted方法

- 【新增】设置目标ip方法(默认广播形式发送)

- 【bug】只能发送方指定端口，bug，不要使用此版本

更多历史版本暂未记录