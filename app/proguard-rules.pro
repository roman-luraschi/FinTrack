# Add project specific ProGuard rules here.
-keep class com.fintrack.core.database.entity.** { *; }
-keep class com.fintrack.core.domain.model.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
