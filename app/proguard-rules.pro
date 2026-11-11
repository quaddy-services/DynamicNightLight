# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Stefan\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Keep the custom Preference that is inflated from res/xml/preferences.xml.
# Classes referenced from XML are instantiated reflectively via their
# (Context, AttributeSet) constructor, so R8 must not remove or rename them.
-keep public class de.quaddyservices.dynamicnightlight.util.ListPreferenceCompat {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context);
}

# Generic safety net: keep the XML-inflation constructors of any custom
# View / Preference subclass referenced from layout or preference XML.
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
