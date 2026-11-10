# Fase 1 — Validación de `v2.5.0`

**Fecha:** 2026-11-09
**Dispositivo:** `Medium_Phone_API_37.0` (emulador x86_64, API 37)
**Variante validada:** `full` release universal y `full` debug (R8 y sin R8)

## Recorrido funcional y visual

| Área | Resultado |
|---|---|
| Onboarding / Home | Camino lineal, nodo disponible, racha, vidas, tarjeta de repaso y unidad A1 correctos. |
| Lección (MultipleChoice) | Opción seleccionada legible con borde y tinte de marca; feedback con Lumi correcto. |
| Lección (Translate / OrderWords) | Tablero de fichas por índice; frase `I am from Chile` y `She is my sister` resueltas. |
| Lección (MatchPairs) | Piezas redondeadas 12 dp, encaje central y estado correcto con checks. |
| Lección (ListenChoose) | Audio visible y animado; opción correcta y feedback. |
| Lección (ListenType) | Dictado, validación y `Ver por qué` / `Ver respuesta` correctos. |
| Omitir voz | `Ejercicio omitido` como aviso informativo; sin sonido de error ni `mistakes`; no rompe el combo. Se corrigió el denominador de nota para que el omitido no reste porcentaje. |
| Rotación | La lección y el feedback se conservan; no se expulsa al usuario. |
| `fontScale = 2.0x` | Texto, opciones y CTA legibles en lección. |
| Salida de lección | Diálogo de confirmación con `Salir` / `Seguir practicando`; `Back` abre el mismo diálogo. |
| Resultado | `RewardSheet` con XP, aciertos, errores y porcentaje; `MistakeReviewSheet` coherente. |

### Observaciones para F2

- En horizontal el panel de feedback ocupa la parte inferior y el contenido del ejercicio queda
  recortado; conviene hacer la etapa desplazable o plegar el feedback en landscape.
- El diálogo de confirmación abre una ventana propia: en Android 15+ los iconos de barras
  pueden quedar blancos sobre el scrim. Se mantiene el fix de barras de `MainActivity` para la
  ventana principal.

## Tests instrumentados

- `:core:core-ui:connectedDebugAndroidTest` en verde (accesibilidad, galería,
  puzzle, feedback y free speaking).
- `:core:core-data:connectedDebugAndroidTest` en verde (migraciones Room).
- `:core:core-ui:compileDebugAndroidTestKotlin` y
  `:core:core-data:compileDebugAndroidTestKotlin` en verde.

## Artefactos

| Flavor | Universal | arm64-v8a |
|---|---:|---:|
| `full` | 509,4 MB | 392,3 MB |
| `en` | 262,3 MB | 145,2 MB |
| `pt` | 253,9 MB | 136,7 MB |
| `fr` | 263,2 MB | 146,1 MB |
| `ja` | 206,0 MB | 88,9 MB |

Hashes en `dist/v2.5.0/SHA256SUMS.txt` (ignorado por Git).

## Baseline Profile (`enRelease`)

Generado con `:app:generateEnBaselineProfile`:

- `baseline-prof.txt`: 23.883 → 25.427 reglas (+8,47 %).
- `startup-prof.txt`: 21.314 → 20.650 reglas.
- Salida: `app/src/enRelease/generated/baselineProfiles/`.

## Macrobenchmark (`enBenchmarkRelease`, 5 iteraciones)

| Escenario | Métrica | Mediana | P95 |
|---|---|---|---|
| Arranque en frío con perfil | TTID | **301,2 ms** | — |
| Arranque en frío sin perfil | TTID | 322,7 ms | — |
| Scroll de Home con perfil | Frame CPU | 3,76 ms | 36,6 ms |
| Scroll de Home sin perfil | Frame CPU | 4,64 ms | 39,1 ms |

- TTID objetivo ≤ 450 ms: **cumple** (301,2 ms con perfil).
- El P95 de frame en scroll aún supera 16,6 ms en emulador; se mantiene como deuda de F2
  (el objetivo de < 2 % de frames janky requiere medir en hardware y revisar las animaciones
  restantes de la cabecera).
