# Feather — sistema de diseño de LinguaGo

> "Feather" es el nombre **interno** del sistema de diseño de LinguaGo. No es
> una dependencia externa ni un pack de iconos: no se instala nada de
> terceros (I5). La fuente de verdad de valores es
> `core/core-ui/src/main/kotlin/dev/linguago/core/ui/theme/LinguaTokens.kt`;
> este documento explica el criterio. Cualquier cambio en los tokens es un
> cambio de identidad y requiere ADR (ADR-026).

## 1. Principios

1. **Flat 3D:** los controles tienen cuerpo y borde inferior sólido de 4 dp
   (sin sombras difusas). Da profundidad sin coste de render.
2. **Redondeado y amable:** radios de 16–24 dp en componentes nuevos.
3. **Color con significado:** cada estado del camino tiene un color propio,
   nunca solo color (siempre acompañado de icono o texto).
4. **Movimiento con propósito:** el movimiento confirma acciones y celebra
   logros; nunca retrasa el avance.
5. **Identidad original:** púrpura LinguaGo como ancla; oro para cofres;
   sin copiar paletas ni mascotas de terceros.

## 2. Tokens de color

### Marca y semánticos

| Token | Hex | Uso |
|---|---|---|
| `PRIMARY` | `#6C5CE7` | Acción principal, nodo disponible |
| `PRIMARY_EDGE` | `#5A4BD1` | Borde 3D del primario |
| `ACCENT` | `#00B894` | Acierto, nodo completado, barra de XP |
| `DESTRUCTIVE` | `#E17055` | Error |
| `WARNING` | `#FDCB6E` | Racha, nodo en progreso |
| `INFO` | `#74B9FF` | Información |
| `BG` / `BG_SECONDARY` | `#FFFFFF` / `#F8F9FA` | Superficies claras |
| `FG` / `FG_MUTED` | `#2D3436` / `#636E72` | Texto |
| `BORDER` | `#DFE6E9` | Bordes y nodo bloqueado |

### Extendidos (ADR-026)

| Token | Hex | Uso |
|---|---|---|
| `CHEST_GOLD` | `#FFD700` | Cofre de unidad |
| `CHEST_EDGE` | `#B8860B` | Borde 3D del cofre |
| `HEART_RED` | `#FF6B6B` | Vida llena |
| `HEART_EMPTY` | `#DFE6E9` | Vida vacía |

### Modo oscuro

Los tokens actuales de modo oscuro se conservan
(`DARK_BG_HEX`, `DARK_BG_SECONDARY_HEX`, `DARK_FG_HEX`, `DARK_FG_MUTED_HEX`,
`DARK_BORDER_HEX`). Los tokens extendidos deben tener variante oscura antes
de implementar la Fase 3 (oro más apagado, rojo más profundo, vacío oscuro).

## 3. Formas y profundidad

| Token | Valor | Uso |
|---|---|---|
| `RADIUS_BUTTON_DP` | 16 | Botones (existente) |
| `RADIUS_CARD_DP` | 12 | Tarjetas existentes (no se migran aún) |
| `RADIUS_CARD_LARGE_DP` | 20 | Tarjetas nuevas |
| `RADIUS_NODE_DP` | 24 | Nodos del camino |
| `RADIUS_PILL_DP` | 999 | Chips y píldoras |
| `EDGE_BUTTON_DP` | 4 | Borde inferior 3D |

Regla: el borde inferior se dibuja como una segunda caja del mismo ancho,
desplazada hacia abajo; el componente se desplaza 2 dp al presionarse.

## 4. Tipografía

- **Nunito** (SIL OFL), fuente variable con pesos 400/500/600/700/800.
- Escala en `sp` (respeta la preferencia del usuario, I8).
- **Japonés:** no se empaqueta fuente CJK; el sistema resuelve el fallback.
  Si se decide empaquetar Noto Sans JP (~4 MB), requiere ADR por tamaño de
  APK.

## 5. Movimiento

| Token | Valor | Uso |
|---|---|---|
| `SpringBouncy` | `spring(dampingRatio = 0.55f, stiffness = MediumLow)` | Rebote de nodo al tocar |
| `SpringSnappy` | `spring(dampingRatio = 1f, stiffness = High)` | Presión, contadores, corazones |

- Duraciones cortas: 150–250 ms para interacción; 400–700 ms para
  celebraciones.
- **Reduced motion:** si `ValueAnimator.areAnimatorsEnabled()` es `false`,
  las animaciones decorativas se sustituyen por el estado final (sin
  confeti ni mascota en movimiento).

## 6. Componentes del camino

| Componente | Estado | Color |
|---|---|---|
| `PathNode` | `Completed` | `ACCENT` + check |
| | `Available` | `PRIMARY` + pulso |
| | `InProgress` | `WARNING` |
| | `Locked` | `BORDER` + candado |
| | `Chest` | `CHEST_GOLD` + cofre |

Reglas:
- Nodo de 72 dp (objetivo táctil ≥ 48 dp, I8).
- El nodo `Available` es el único con animación continua.
- `Chest` es un nodo independiente después de cada unidad; la última lección
  de la unidad sigue siendo rejugable.
- Orden del camino: `levels → units → lessons` del `course.json` (rampa de
  dificultad A1→B2).

## 7. HUD del alumno

- **Racha:** llama + días (`StreakIndicator`, animación de pulso).
- **Vidas:** 10 corazones; un solo nodo de accesibilidad ("3 de 10 vidas").
- **XP:** barra líquida hacia el siguiente nivel visual
  (`totalXp / 100 + 1`, sin persistir nivel) y contador animado al ganar XP.

## 8. Accesibilidad (I8)

- Todo control con `contentDescription` y `Role` correctos.
- No comunicar estado solo con color: icono/texto adicional.
- Contraste AA en texto; el texto blanco sobre oro no se usa (bajo
  contraste): el icono del cofre va en tono oscuro.
- Respetar `fontScale` 2.0×, `ANIMATOR_duration_scale` y modo oscuro.
- El camino mantiene un orden de foco lógico y anuncia el estado del nodo.

## 9. Gobernanza

- Los valores canónicos viven en `LinguaTokens.kt`.
- Añadir tokens es aditivo y requiere ADR si altera la identidad (ADR-026 ya
  aprueba los tokens extendidos de este documento).
- Prohibido introducir iconos, ilustraciones o mascotas de terceros.
- Los componentes nuevos se documentan aquí antes de usarse en una pantalla.
