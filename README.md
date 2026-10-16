# LinguaGo

<div align="center">

**Aprende idiomas offline, sin fricción, con todo el contenido desde la instalación.**

ES→EN · ES→PT · ES→FR · ES→JA — A1 a B2 (y N5–N3 para japonés)

Código abierto · Sin anuncios · Sin vidas · Sin backend · 100% offline

</div>

---

LinguaGo es una app Android nativa de aprendizaje de idiomas estructurada sobre
Clean Architecture, con módulos Gradle por feature, motor de lecciones basado en
máquina de estados, racha diaria progresiva, repaso espaciado (FSRS) y lectoescritura
de modelos de voz ligeros integrados localmente.

## Principios (invariantes)

1. **Offline-first estricto** — ninguna ruta crítica de aprendizaje necesita red.
2. **Sin backend propio** — la nube es opcional y solo como respaldo (Google Drive/OneDrive).
3. **Sin fricción** — no hay login obligatorio, ni vidas, ni anuncios, ni esperas.
4. **Progreso intocable** — la sincronización nunca elimina avances del usuario.
5. **Identidad original** — cero elementos de terceros en marca, arte o contenido.
6. **Código abierto real** — MIT para código, CC BY-SA 4.0 para contenido.
7. **Testabilidad** — toda lógica de negocio es testeable en aislamiento.
8. **Accesibilidad** — TalkBack, contraste y tamaños dinámicos desde el día uno.

## Módulos

| Módulo | Rol |
|---|---|
| `app` | Composition root, navegación, DI |
| `core/core-domain` | Entidades, contratos y use cases (Kotlin puro) |
| `core/core-ui` | Design system y componentes atómicos |
| `core/core-data` | Room, DataStore, repositorios |
| `core/core-content` | Parser y validador de contenido JSON |
| `core/core-audio` | Estrategias TTS/STT (Piper + Vosk) |
| `core/core-sync` | Sincronización con Drive/OneDrive |
| `core/core-common` | Utilidades transversales |
| `feature/*` | Pantallas por feature |
| `content/*` | Assets del contenido pedagógico |

Ver `ARCHITECTURE.md` para el mapa completo de dependencias.

## Contraer y ejecutar

```bash
# Clonar
git clone https://github.com/MREmmanuel09/LinguaGo.git
cd LinguaGo

# Requisitos: JDK 17+ (o JBR de Android Studio) y Android SDK
# Configura tu ruta al SDK en local.properties si no está en la ruta por defecto
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk

# Compilar
./gradlew assembleDebug

# APK en app/build/outputs/apk/debug/
```

### Modelos de voz (TTS Piper / STT Vosk)

Los modelos de voz **no se commiten** en el repositorio (decisión ADR-006) para
mantenerlo por debajo de 500 MB. Se descargan y se colocan en `models/` según
`models/manifest.json`. Ver `docs/MODELS.md` al respecto. La funcionalidad sin
modelos se degrada sin bloquear la app (los ejercicios de conversación quedan
deshabilitados y la lectura a texto se hace por el proveedor disponible).

## Pipeline

- `./gradlew build` — build, tests y análisis
- `./gradlew ktlintCheck detekt` — estilo y análisis estático
- GitHub Actions en `.github/workflows/ci.yml`

## Licencias

- Código: [MIT](./LICENSE)
- Contenido pedagógico: [CC BY-SA 4.0](./LICENSE-CONTENT)

## Contribuir

Lee [CONTRIBUTING.md](./CONTRIBUTING.md). Comités de decisión quedan registrados
en [docs/DECISIONS.md](docs/DECISIONS.md) como ADRs.
