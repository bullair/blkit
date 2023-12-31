# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\eclipse\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


-dontshrink

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keep class com.inuker.bluetooth.library.ClientManager {
     *;
}

-keep class com.inuker.bluetooth.library.BluetoothManager$* {
     *;
}

-keep class com.inuker.bluetooth.library.connect.response.BleResponse {
     <methods>;
}

-keep interface * extends com.inuker.bluetooth.library.connect.response.BleResponse

-dontwarn com.jeremyliao.liveeventbus.**
-keep class com.jeremyliao.liveeventbus.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.arch.core.** { *; }