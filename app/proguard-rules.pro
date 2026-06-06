# Keep app logic that relies on reflection / kotlinx.serialization untouched.
# (R8 still strips the bulk of androidx material-icons-extended and unused framework code.)
-keep class com.campusmind.app.agent.** { *; }
-keep class com.campusmind.app.ai.** { *; }
-keep class com.campusmind.app.model.** { *; }
-keep class com.campusmind.app.data.** { *; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keep,includedescriptorclasses class com.campusmind.app.**$$serializer { *; }
-keepclasseswithmembers class com.campusmind.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class com.campusmind.app.** { *; }
