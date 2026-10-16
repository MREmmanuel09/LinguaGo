# Arquitectura — LinguaGo

> Documento de referencia técnica. Toda decisión debe trazarse a este documento
> o a un ADR en `docs/DECISIONS.md`.

## 1. Estilo

**Clean Architecture** con modularización por features y separación estricta
de capas:

```
┌─────────────────────────────────────────────────────────┐
│  PRESENTATION LAYER (Compose + ViewModel)               │
│  → UI declarativa, sin lógica de negocio                │
└─────────────────────────────────────────────────────────┘
                          ↓ (depende de)
┌─────────────────────────────────────────────────────────┐
│  DOMAIN LAYER (UseCases + Entities)              │
│  → Lógica de negocio pura, sin dependencias Android     │
└─────────────────────────────────────────────────────────┘
                          ↑ (implementa)
┌─────────────────────────────────────────────────────────┐
│  DATA LAYER (Repositories + DataSources)                │
│  → Room, DataStore, Assets, Cloud APIs                  │
└─────────────────────────────────────────────────────────┘
```

**Regla de dependencia:** las capas externas dependen de las internas, nunca
al revés. El dominio no conoce Compose, Room ni Drive.

## 2. Módulos Gradle

```
app/                       → Composition root, navegación, Application
core/
  core-domain/             → Entidades, contratos de repositorio, UseCases
  core-ui/                 → Design system, componentes atómicos
  core-data/               → Room, DataStore, repositorios concretos
  core-content/            → Parser JSON, loaders de assets
  core-audio/              → Abstracción TTS/STT + implementaciones
  core-sync/               → Orquestación de sincronización
  core-common/             → Utilidades transversales (Result, dispatchers)
feature/
  feature-onboarding/
  feature-auth/
  feature-home/
  feature-lesson/
  feature-profile/
  feature-sync/
  feature-settings/
content/
  content-es-en/           → Assets del curso inglés
  content-es-pt/
  content-es-fr/
  content-es-ja/
build-logic/               → Convention plugins de Gradle (included build)
```

### Reglas de dependencia entre módulos

| Origen | Puede depender de | No puede depender de |
|---|---|---|
| `feature-*` | `core-*` | otro `feature-*` |
| `core-*` | — | `feature-*` |
| `app` | todos | — |
| `content-*` | — | nada (assets puros) |
| `core-domain` | nada Android | Compose, Room, Drive |

### Conventions de build (`build-logic`)

| Plugin id | Aplica |
|---|---|
| `linguago.android.library` | `com.android.library` + config base (minSdk 26, compileSdk 37, JVM 17) |
| `linguago.android.library.compose` | `linguago.android.library` + Compose compiler + features |
| `linguago.android.application` | `com.android.application` + config base |
| `linguago.android.feature` | library compose + Hilt + deps comunes de feature |
| `linguago.kotlin.library` | `org.jetbrains.kotlin.jvm` para módulos puros |
| `linguago.hilt` | KSP + Hilt compiler |
| `linguago.quality` | ktlint + detekt |
| `linguago.content.module` | `linguago.android.library` + assets |

## 3. Patrones aplicados

| Patrón | Dónde | Justificación |
|---|---|---|
| MVVM + MVI | Presentación | Estado unidireccional, predecible, testeable |
| Repository | Data | Abstrae el origen de datos (local vs cloud) |
| Strategy | Audio, Sync | Permite intercambiar motores TTS/STT y proveedores cloud |
| State Machine | Motor de lección | Los estados de lección son finitos y bien definidos |
| Builder | Ejercicios | Cada tipo de ejercicio se construye con parámetros específicos |
| Observer | Racha, progreso | Flow de Kotlin para reactividad |
| Dependency Injection | Todo | Hilt para inversión de dependencias |

## 4. Stack

| Componente | Versión | Rol |
|---|---|---|
| Kotlin | 2.4.21 | Lenguaje (built-in Kotlin de AGP) |
| AGP | 9.4.1 | Build system |
| Gradle | 9.8.1 | Motor |
| Jetpack Compose | BOM 2026.09.00 | UI declarativa |
| Hilt | 2.60.1 | DI (KSP, sin kapt) |
| Room | 2.8.5 | Persistencia estructurada |
| DataStore | 1.2.1 | Preferencias |
| Navigation | 2.10.2 | Navegación Compose |
| WorkManager | 2.12.0 | Sync en background |
| kotlinx-coroutines | 1.11.0 | Concurrencia |
| kotlinx-serialization | 1.11.0 | JSON de contenido |
| sherpa-onnx | 1.13.x | Runtime TTS/STT offline (ADR-003/006) |

## 5. Audio (resumen — ver ADR-003)

- **TTS:** Piper vía sherpa-onnx, ~14–30 MB por voz (puede ajustarse según voz int8/medium).
- **STT:** Vosk Small ~31–48 MB por idioma.
- Contratos `TtsEngine` / `SttEngine` en `core-audio`, implementaciones intercambiables.
- Modelos descargados en build/CI con verificación de checksum (`models/manifest.json`).

## 6. Sincronización (resumen — ver RFC §6)

- Contrato `CloudProvider` con implementaciones Google Drive (`appDataFolder`)
  y OneDrive (`appfolder`).
- Payload `linguago_progress.json` cifrado AES-256-GCM con clave derivada
  de account UID + Android Keystore.
- Merge por entidad con reglas deterministas; **nunca elimina progreso**.

## 7. Nombres de paquetes

Raíz: `dev.linguago.*` — refleja la ruta del módulo:

| Módulo | Paquete |
|---|---|
| `core/core-domain` | `dev.linguago.core.domain` |
| `core/core-ui` | `dev.linguago.core.ui` |
| `feature/feature-lesson` | `dev.linguago.feature.lesson` |
| `content/content-es-en` | `dev.linguago.content.es_en` |
