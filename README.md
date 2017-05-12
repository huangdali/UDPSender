#UDPSender  [![](https://jitpack.io/v/huangdali/UDPSender.svg)](https://jitpack.io/#huangdali/UDPSender)

基于NIO的UDP发送器

## 导入

**Step 1.** Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2.** Add the dependency

```
dependencies {
	        compile 'com.github.huangdali:UDPSender:v1.2.1'
	}
```

## 使用方法

快速上手

```
 UDPSender.getInstance()
                .setInstructions(byteAraary)
                .setTargetPort(port)
                .send(new UDPResultCallback() {
                    @Override
                    public void onNext(UDPResult result) {
                        //do something
                    }
                });
```

demo

```
UDPSender.getInstance()
                .setInstructions(byteAraary)//设置发送的指令[必须，不可为空]
                .setReceiveTimeOut(10 * 1000)//设置接收超时时间[可不写，默认为8s]--超过10s没有接收到设备就视为无设备了就可以停止当前任务了
                .setTargetPort(port)//设置发送的端口[可不写，默认为8899端口]
                .setLocalReceivePort(port)//设置本机接收的端口[可不写，默认为8899端口]
                .schedule(2, 3000)//执行2次，间隔三秒执行
                .send(new UDPResultCallback() {
                    /**
                     * 请求开始的时候回调
                     */
                    @Override
                    public void onStart() {

                    }

                    /**
                     * 每拿到一个结果的时候就回调
                     *
                     * @param result 请求的结果
                     */
                    @Override
                    public void onNext(UDPResult result) {

                    }

                    /**
                     * 请求结束的时候回调
                     */
                    @Override
                    public void onCompleted() {

                    }

                    /**
                     * 当发生错误的时候回调
                     *
                     * @param throwable
                     */
                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
```
