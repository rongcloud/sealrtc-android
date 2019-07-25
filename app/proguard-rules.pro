# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\android-sdks/tools/proguard/proguard-android.txt
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




-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-dontwarn
-dontskipnonpubliclibraryclassmembers
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keepnames class * implements java.io.Serializable
-keepclassmembers class * extends java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keepclassmembers class * implements java.io.Serializable {
    <fields>;
}

 -keepclassmembers class * {
     public <methods>;
 }

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * extends android.app.Activity {
  public void *(android.view.View);
}
-keep class **.R$* {
 *;
}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keep class com.tencent.bugly.**{*;}
-keep class cn.rongcloud.rtc.core.**  { *; }
-keep class cn.rongcloud.rtc.engine.binstack.json.**  { *; }

-keep class de.tavendo.autobahn.**  { *; }
-keep class com.google.gson.**  { *; }

-dontnote android.support.**
-dontwarn android.support.**
-dontwarn com.tencent.bugly.**
-ignorewarnings


-keepattributes Exceptions,InnerClasses

-keepattributes Signature
#RongRTCLib
-keep public class cn.rongcloud.** {*;}

#RongIMLib
-keep class io.rong.** {*;}
-keep class cn.rongcloud.** {*;}
-keep class * implements io.rong.imlib.model.MessageContent {*;}
-dontwarn io.rong.push.**
-dontnote com.xiaomi.**
-dontnote com.google.android.gms.gcm.**
-dontnote io.rong.**

-ignorewarnings

-keep class bailingquic.**{*;}

-keep class go.**{*;}

# here white sdk
-keep class com.herewhite.** { *; }
-keepattributes  *JavascriptInterface*
-keepattributes Signature
