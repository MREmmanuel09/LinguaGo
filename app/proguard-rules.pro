# Reglas R8/ProGuard de LinguaGo (release).
#
# La app depende de reflexión en serializer/framework y JNI nativo:
# sin estas reglas, R8 elimina clases que existen solo en runtime.

# --- kotlinx.serialization (contenido, snapshot, tokens) ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class dev.linguago.**$$serializer { *; }
-keepclassmembers class dev.linguago.** {
    *** Companion;
}
-keepclasseswithmembers class dev.linguago.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- JNI (sherpa-onnx TTS y Vosk STT) ---
# Los símbolos nativos se resuelven por nombre: no renombrar ni eliminar.
-keep class com.k2fsa.sherpa.onnx.** { *; }
-keep class org.vosk.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# --- JNA (Vosk lo usa para mapear structs nativos) ---
# JNA resuelve field IDs por reflexión (p. ej. com.sun.jna.Pointer.peer);
# sin estas reglas R8 renombra/elimina y el STT falla con UnsatisfiedLinkError.
-dontwarn java.awt.**
-keep class com.sun.jna.** { *; }
-keepclassmembers class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }

# --- Room (entidades/DAOs generados vía KSP) ---
-keep class dev.linguago.core.data.database.** { *; }

# --- AndroidX / seguridad ---
# (las librerías incluyen consumer rules; se conservan por claridad)
-dontwarn javax.annotation.**
