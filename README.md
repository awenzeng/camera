# CameraDemo

[![JitPack](https://jitpack.io/v/awenzeng/CameraDemo.svg)](https://jitpack.io/#awenzeng/CameraDemo)
[![Downloads](https://jitpack.io/v/awenzeng/CameraDemo/month.svg)](https://jitpack.io/#awenzeng/CameraDemo)

A simple camera app.Please feel free to use this. (Welcome to Star and Fork)

# Demo：
[Download apk](https://github.com/awenzeng/CameraDemo/blob/master/app/app-Awen_release-release.apk?raw=true)

![](https://github.com/awenzeng/CameraDemo/blob/master/resource/camera_demo.gif)

# Download
You can download a jar from GitHub's [releases page](https://github.com/awenzeng/CameraDemo/releases).

Or use Gradle.
```java
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  ```
  ```java
  	dependencies {
	        compile 'com.github.awenzeng:CameraDemo:1.0.1'
	}

```
Or Maven:
```java
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
  ```
  ```java
  	<dependency>
	    <groupId>com.github.awenzeng</groupId>
	    <artifactId>CameraDemo</artifactId>
	    <version>1.0.1</version>
	</dependency>
```
For info on using the bleeding edge, see the [Snapshots](https://jitpack.io/#awenzeng/CameraDemo) wiki page.

# ProGuard
Depending on your ProGuard (DexGuard) config and usage, you may need to include the following lines in your proguard.cfg 

```java
## app proguard
-keep class com.awen.camera.widget.**{*;}

##Rxjava
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

```
# How do I use Camera?
Simple use cases with camera's generated API will look something like this:

Init in your application:
```java
public class CameraDemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CameraApplication.init(this,true);
    }
}
```
In your Activity:
```java
        PermissionsModel permissionsModel = new PermissionsModel(this);
        permissionsModel.checkCameraPermission(new PermissionsModel.PermissionListener() {
            @Override
            public void onPermission(boolean isPermission) {
                if (isPermission) {
                    Intent intent = new Intent(MainActivity.this, TakePhotoActivity.class);
                    startActivityForResult(intent, TakePhotoActivity.REQUEST_CAPTRUE_CODE);
                }
            }
        });
        
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TakePhotoActivity.REQUEST_CAPTRUE_CODE: {
                    String path = data.getStringExtra(TakePhotoActivity.RESULT_PHOTO_PATH);
                    showTakePhotoImg.setImageBitmap(BitmapUtil.getBitmap(path));
                    Log.v(TAG, "图片地址：" + path);
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

```
# Thanks
[Rxjava](https://github.com/ReactiveX/RxJava)

[RxAndroid](https://github.com/ReactiveX/RxAndroid)

[RxPermissions](https://github.com/tbruyelle/RxPermissions)

# License
```java
Copyright 2017 AwenZeng

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```


