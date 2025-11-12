# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK.

# Keep Room entities
-keep class com.snappet.persistence.entities.** { *; }

# Keep Lottie
-keep class com.airbnb.lottie.** { *; }

# Keep TarsosDSP (disabled: dependency removed)
# -keep class be.tarsos.dsp.** { *; }
