-dontwarn **CompatHoneyComb
-dontwarn **CompatHoneyCombMR2
-dontwarn **CompatCreatorHoneyCombMR2

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}