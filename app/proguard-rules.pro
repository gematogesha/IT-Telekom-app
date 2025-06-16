#####################################
# == Общие настройки ==
#####################################

-dontwarn java.lang.invoke.*
-dontwarn kotlin.Metadata
-ignorewarnings

#####################################
# == Gson (включая TypeToken) ==
#####################################
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }
-keepattributes Signature, *Annotation*
-keep class java.util.Map
-keep class java.util.HashMap
-keep class com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class * {@com.google.gson.annotations.SerializedName <fields>;}

#####################################
# == TokenManager ==
#####################################
-keep class com.ittelekom.app.utils.TokenManager { *; }

#####################################
# == Retrofit ==
#####################################
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**

#####################################
# == ViewModel (Lifecycle) ==
#####################################
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

#####################################
# == Kotlin Coroutines ==
#####################################
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

#####################################
# == Jetpack Compose ==
#####################################
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

#####################################
# == AndroidX EncryptedSharedPreferences ==
#####################################
-keep class androidx.security.** { *; }
-dontwarn androidx.security.**

#####################################
# == Parcelable ==
#####################################
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

#####################################
# == EventBus ==
#####################################
-keepclassmembers class ** {
    public void onEvent*(...);
}

#####################################
# == Flutter и Firebase ==
#####################################
-keep class io.flutter.** { *; }
-dontwarn io.flutter.**
-keep class com.google.firebase.** { *; }

#####################################
# == Activity, Application и т.п. ==
#####################################
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

#####################################
# == Конструкторы и View Binding ==
#####################################
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

#####################################
# == Дополнительно (безопасные отключения) ==
#####################################
-dontwarn okio.**
-dontwarn lombok.**
