-dontwarn com.google.android.gms.**
-keepnames public class * extends io.realm.RealmObject
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.** { *; }
-dontwarn javax.**
-dontwarn io.realm.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keep class rx.internal.util.unsafe.** { *; }
-ignorewarnings

-keep class * {
    public private *;
}
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keepclassmembers class ** {
    public void getName(**);
}
-keepclassmembers class ** {
    public void onEvent*(**);
    void onEvent*(**);
}
-keepclassmembers,includedescriptorclasses class ** { public void onEvent*(**); }

-keep class dmax.dialog.** {
    *;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

