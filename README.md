# UDPSender  [![](https://jitpack.io/v/huangdali/UDPSender.svg)](https://jitpack.io/#huangdali/UDPSender)

基于NIO的UDP发送-接收器

**输入：**

- 指令byte数组
- 目标端口号

**输出：**

- UDPReuslt结果对象（包含对方IP地址、结果数组[byte类型的数组]）


## 导入

**Step 1.** Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```java
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2.** Add the dependency

```java
dependencies {
	     compile 'com.github.huangdali:UDPSender:v1.3.6'
	}
```


**Step 3.** 添加网络访问权限

AndroidManifest.xml中加入

```xml
<manifest ...>
    <uses-permission android:name="android.permission.INTERNET" />
    <application>
        ...
    </application>

</manifest>
```

## 更新历史

 v1.3.6

- 【修复】多次调用onCompleted方法

 v1.3.5

- 【修复】关闭任务（UDPSender.getInstance().stop()）不走onCompleted方法

- 【新增】设置目标ip方法(默认广播形式发送)

更多历史版本暂未记录

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