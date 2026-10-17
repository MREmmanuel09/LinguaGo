# Política de seguridad — LinguaGo

## Versiones soportadas

| Versión | Soportada |
|---|---|
| 0.2.x | ✅ |
| < 0.2 | ❌ |

## Cómo reportar una vulnerabilidad

**No abras un issue público para vulnerabilidades.** Usa una de estas vías:

1. **GitHub Security Advisories**: pestaña *Security → Report a vulnerability*
   del repositorio (privado, permite coordinar el fix).
2. Si la advisory no está disponible, contacta a
   [@MREmmanuel09](https://github.com/MREmmanuel09) por mensaje directo.

Incluye: versión afectada, pasos de reproducción, impacto y, si puedes, una
prueba de concepto. Respondemos en un plazo objetivo de **72 horas** y
publicamos el fix con crédito (si lo deseas) en las notas de la release.

## Alcance

- APK y código de este repositorio (todas las variantes).
- Integraciones incluidas: OAuth/Google Drive, cifrado local, carga de
  modelo de voz, parseo de contenido.
- **Fuera de alcance**: dispositivos con root, manipulaciones del APK,
  vulnerabilidades de Android/Room/sherpa-onnx/Vosk sin impacto propio
  (repórtalas aguas arriba), y el proveedor de laboratorio `dev_local`
  (existe solo en builds debug, sin red).

## Diseño de seguridad (resumen)

| Área | Medida |
|---|---|
| Autenticación | OAuth 2.0 + PKCE (RFC 7636) con redirect custom; **sin client secrets** en el APK; tokens con refresh automático |
| Tokens en reposo | Cifrados AES-GCM-256 con clave en **Android Keystore** (ADR-014); migración de texto claro solo una vez |
| Snapshot en la nube | Se cifra en el dispositivo antes de subirlo; un snapshot robado es inútil sin la clave del dispositivo |
| Superficie de red | Solo endpoints de Google OAuth/Drive (`core-auth`); nada más sale del dispositivo |
| Integridad de modelos | SHA-256 obligatorio antes de cargar cualquier voz/modelo (ADR-006) |
| Contenido | Validado contra JSON Schema en CI antes de empaquetarse (ADR-012) |
| Secretos | Nunca en el repo: keystore y client id se pasan por `gradle.properties` local / variables |
| Datos | Solo progreso pedagógico; sin telemetría, sin analytics, sin publicidad |

## Actualizaciones de dependencias

Las dependencias se fijan en `gradle/libs.versions.toml`; el CI compila
debug + release (R8) y corre el gate de calidad en cada push, de modo que una
regresión conocida de seguridad rompería visibilidad antes de publicar.
