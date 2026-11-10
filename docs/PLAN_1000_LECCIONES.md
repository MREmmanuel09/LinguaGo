# Plan de expansión a 1.020 lecciones por idioma

> **Estado:** propuesta para aprobación (ADR-052).
> **Objetivo:** mínimo 1.000 lecciones por cada curso de destino, con la misma
> cantidad en cada nivel CEFR.
> **Fecha:** 2026-11-09.

## 1. Decisión de cupo

LinguaGo tiene **6 niveles CEFR** (`A1`, `A2`, `B1`, `B2`, `C1`, `C2`), no 4.
Por eso el reparto equivalente no es 250 × 4.

Se propone **170 lecciones por nivel**:

| Nivel | Lecciones |
|---|---:|
| A1 | 170 |
| A2 | 170 |
| B1 | 170 |
| B2 | 170 |
| C1 | 170 |
| C2 | 170 |
| **Por idioma** | **1.020** |

- **4 idiomas:** 1.020 × 4 = **4.080 lecciones**.
- **Meta mínima:** se supera en 20 lecciones por idioma.
- **Unidad de producción:** 10 lecciones → **102 lotes por idioma** y
  **408 lotes totales**.
- **Alternativa exacta de 1.000:** `167 A1 + 167 A2 + 167 B1 + 167 B2 +
  166 C1 + 166 C2`. No se recomienda porque rompe el lote uniforme de 10.

La carga pedagógica sigue subiendo por nivel aunque el número de lecciones sea
el mismo: A1/A2 usan 7 ejercicios; B1, 9; B2, 10; C1, 11; C2, 12. La igualdad
es de **lecciones**, no de minutos de estudio.

## 2. Punto de partida real

| Curso | A1 | A2 | B1 | B2 | C1 | C2 | Total |
|---|---:|---:|---:|---:|---:|---:|---:|
| ES→EN | 10 | 17 | 20 | 22 | 24 | 18 | **111** |
| ES→PT | 10 | 8 | 20 | 22 | 24 | 18 | **102** |
| ES→FR | 10 | 8 | 20 | 22 | 24 | 18 | **102** |
| ES→JA | 10 | 8 | 20 | 22 | 24 | 18 | **102** |

Totales actuales: **417 lecciones / 4.087 ejercicios**.

## 3. Lecciones nuevas necesarias

Objetivo: 170 por nivel.

| Curso | A1 | A2 | B1 | B2 | C1 | C2 | Nuevas | Final |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| ES→EN | +160 | +153 | +150 | +148 | +146 | +152 | **+909** | **1.020** |
| ES→PT | +160 | +162 | +150 | +148 | +146 | +152 | **+918** | **1.020** |
| ES→FR | +160 | +162 | +150 | +148 | +146 | +152 | **+918** | **1.020** |
| ES→JA | +160 | +162 | +150 | +148 | +146 | +152 | **+918** | **1.020** |
| **Total** | **+640** | **+639** | **+600** | **+592** | **+584** | **+608** | **+3.663** | **4.080** |

### Volumen de ejercicios

Si se mantiene la rampa actual (`7/7/9/10/11/12`):

- **9.520 ejercicios por idioma** al terminar.
- **38.080 ejercicios totales** (frente a 4.087 actuales).
- **+33.993 ejercicios nuevos**.

Es el verdadero coste del plan: 3.663 lecciones nuevas implican unas **34.000
unidades de ejercicio** y, con ellas, más audio, glosas, notas culturales y
producción oral.

## 4. Diseño de cada nivel

Cada nivel se divide en **17 bloques de 10 lecciones** (170). Los bloques se
mapean a unidades del curso; las unidades históricas se conservan y no se
renumeran ids.

### Secuencia de un bloque de 10 lecciones

| Lección | Función |
|---:|---|
| 1 | Consolidación y reciclaje del bloque anterior |
| 2 | Entrada contextual + audio |
| 3 | Léxico y colocaciones |
| 4 | Foco gramatical 1 |
| 5 | Foco gramatical 2 |
| 6 | Interacción / rol comunicativo |
| 7 | Comprensión auditiva o lectora |
| 8 | Producción guiada |
| 9 | Integración de lo aprendido |
| 10 | Evaluación formativa del bloque |

- **Reciclaje:** 30–40 % del léxico y gramática del bloque previo.
- **Micro-clase:** una por lección, obligatoria en B1+.
- **Glosario:** 6–14 términos según nivel.
- **Nota cultural:** al menos una por lección en B1+.
- **Ids nuevos:** nunca se reutiliza un id histórico; el patrón nuevo será
  `es-<idioma>-<nivel>-u<nn>-l<nn>` y los ejercicios mantendrán su jerarquía.
- **Compatibilidad:** se mantiene `schemaVersion: 2`; no se toca Room, FSRS ni
  el formato de progreso.

### Mezcla de ejercicios por nivel

Se respeta el contrato actual de `ContentRampTest`:

| Nivel | Ejercicios/lección | FreeSpeaking | AudioOnly | Gramática |
|---|---:|---:|---:|---|
| A1 | 7 | 0 | 0 | 0 |
| A2 | 7 | 0 | 0 | ≤ 2 |
| B1 | 9 | 1 | opcional | 2 |
| B2 | 10 | 1 | 1 | 2 |
| C1 | 11 | 1 | 1 | 2 |
| C2 | 12 | 2 | 1 | 2 |

Cada bloque debe incluir al menos 6 tipos de ejercicio distintos, con
producción oral, audio y una tarea de comprensión por bloque. No se añaden
tipos nuevos de ejercicio: se reutilizan los 14 existentes.

## 5. Fábrica de contenido

Para producir 3.663 lecciones sin perder calidad se necesita una cadena, no
una acumulación de PRs manuales.

### 5.1 Fuente de verdad

1. **Matriz curricular** por nivel: 17 bloques × 4 cursos, con objetivo
   comunicativo, `canDo`, 2 focos gramaticales, banda léxica, escenario y
   reciclaje.
2. **Plantilla de lección** con los campos obligatorios y la mezcla de
   ejercicios.
3. **Guía de estilo por idioma** (EN/PT/FR/JA) con registro, variante
   dialectal y tabúes culturales.
4. **Banco de escenarios y notas culturales** para evitar duplicados.

### 5.2 Generación y revisión

- Borrador asistido por plantilla/IA sobre la matriz, nunca publicación
  directa.
- **Piloto humano** de un bloque por nivel antes de replicar.
- Revisión nativa del bloque.
- Edición de los `course.json` en lotes de 10 lecciones.
- Commits `content:` independientes por lote y por curso.

### 5.3 Código y gates a ampliar

| Cambio | Dónde |
|---|---|
| Cupos por nivel y totales 1.020 | `ContentRampTest`, `PackagedContentTest` |
| Conteo de lecciones/ejercicios por CEFR en `validateContent` | `build.gradle.kts` |
| Test de lotes: 10 lecciones, ids únicos y sin reciclaje | `core-content` |
| Test de contrato oral/audio por nivel | `core-content` / `core-domain` |
| ADR-052 y actualización de `CONTENT_FORMAT.md` | `docs/` |

No se requiere cambiar la base de datos ni el motor de lecciones.

## 6. Calidad y revisión nativa

### Automático (100 %)

- JSON Schema v2 y UTF-8 sin BOM.
- Rampa monotónica A1→C2 y cupos 170/170/170/170/170/170.
- Ids únicos, sin reciclaje, y referencias `grammarRuleRefs` válidas.
- `modelAnswer` de todos los `FreeSpeakingPrompt` aprobado por el grader.
- Tokens máximos: 8/12/24/30/38/48.
- Glosario, micro-clase, `scenario`, `canDo`, `culturalNotes` y
  `feedbackOnError` completos.
- Audio/TTS: `audioText` no vacío y longitud dentro de presupuesto.
- Detección de duplicados semánticos entre lecciones.

### Revisión humana/nativa (obligatoria antes de release)

- **100 %** de micro-clases.
- **100 %** de `FreeSpeakingPrompt`, `AudioOnlyPrompt` y `ListenType`.
- **100 %** de notas culturales y glosarios.
- **20 %** estratificado del resto de ejercicios (por nivel, tipo y curso).
- Cualquier incidencia de native review reabre el lote completo.

Con 38.080 ejercicios, revisar el 100 % de todo es inviable; el muestreo
anterior es el mínimo defendible. Si el presupuesto permite más, se sube el
muestreo antes de ampliar el cupo.

## 7. Calendario, fases y releases

La unidad de avance es el **lote de 10 lecciones**. Cada release completa un
nivel en los 4 cursos y no avanza si el gate o la revisión nativa no están
verdes.

La ejecución se organiza en **7 fases** (F0–F6) y **14 olas internas**:

- **Ola A:** se escribe y valida el nivel en **ES→EN** (curso insignia).
- **Ola B:** se adapta a **PT, FR y JA** con la plantilla semántica, no con
  traducción literal.
- La ola B de un nivel no empieza hasta que la ola A pase el gate completo.
- Ninguna fase acumula deudas: si falla calidad, se corrige antes de abrir la
  siguiente.

| Fase | Release | Contenido | Lecciones nuevas | Olas |
|---|---|---|---:|---|
| **F0** | **3.1.0** | Fábrica + piloto: 1 bloque por nivel y curso | 240 | A: EN · B: PT/FR/JA |
| **F1** | **3.2.0** | A1 completo en los 4 cursos | ~600 | A: EN · B: PT/FR/JA |
| **F2** | **3.3.0** | A2 completo | ~599 | A: EN · B: PT/FR/JA |
| **F3** | **3.4.0** | B1 completo | ~560 | A: EN · B: PT/FR/JA |
| **F4** | **3.5.0** | B2 completo | ~552 | A: EN · B: PT/FR/JA |
| **F5** | **3.6.0** | C1 completo | ~544 | A: EN · B: PT/FR/JA |
| **F6** | **4.0.0** | C2 completo y meta 1.020/idioma | ~568 | A: EN · B: PT/FR/JA |

### Tres puertas de calidad por fase

1. **Puerta de lote (cada 10 lecciones):** schema, rampa, ids únicos,
   `modelAnswer`, tokens, glosario y `feedbackOnError`.
2. **Puerta de ola (al cerrar A o B):** revisión nativa de la sección 6,
   audio/TTS, duplicados semánticos y smoke en emulador.
3. **Puerta de release:** gate Gradle completo, contenido comprimido, peso del
   APK, scroll del camino con 1.020 nodos y firma de calidad.

### Auditorías transversales

- **Auditoría de mitad de plan:** al cerrar **F4 (B2)**. Se revisan los cuatro
  cursos completos y, si la tasa de incidencias supera el umbral acordado, se
  pausa la expansión y se corrige antes de C1.
- **Auditoría final:** antes de **F6/4.0.0**. Revisión independiente de una
  muestra nueva, todas las incidencias abiertas y verificación de que las
  1.020 lecciones por idioma conservan la rampa y la calidad nativa.

### Estimación de esfuerzo

| Equipo | Ritmo | Duración aproximada |
|---|---|---|
| 1 persona | 1 lote/semana | 20–24 meses |
| 4 células (una por idioma), 1 lote/semana cada una | 4 lotes/semana | 12 meses |
| 4 células, 2 lotes/semana cada una | 8 lotes/semana | 6–7 meses |

La revisión nativa y la validación manual de audio son el cuello de botella,
no la generación de JSON.

## 8. Criterios de aceptación

- **1.020 lecciones por idioma**, 170 por nivel, en los 4 cursos.
- **4.080 lecciones** y **38.080 ejercicios** con la rampa actual.
- 0 ids reciclados; 0 BOM; 0 errores de schema.
- 100 % de `modelAnswer` y `AudioOnlyPrompt` validados.
- Cobertura de revisión nativa de la sección 6.
- `ktlintCheck detekt validateContent test` en verde.
- APK `full` dentro del presupuesto de crecimiento (objetivo: +8 MB máx. de
  contenido comprimido) y scroll del camino probado con 1.020 nodos.
- Smoke en emulador API 37: A1, B2 y C2; claro/oscuro y `fontScale 2.0x`.

## 9. Riesgos

| Riesgo | Mitigación |
|---|---|
| Deriva de calidad o contenido repetitivo | Matriz cerrada, plantilla, piloto y muestreo nativo. |
| Revisión nativa insuficiente | 100 % oral/audio/micro-clase; 20 % del resto; no publicar lote sin revisión. |
| Tests con totales hardcodeados | Centralizar cupos en una constante de test y actualizar por release. |
| Crecimiento del APK | Validar peso por release; comprimir assets de contenido si hace falta. |
| Camino lento con 1.020 nodos | Macrobenchmark de scroll; mantener LazyColumn y limitar animaciones. |
| Traducciones literales entre idiomas | Revisión nativa por idioma; plantilla semántica, no copia literal. |
| Fatiga del equipo | Lotes pequeños, releases por nivel y criterios de salida claros. |

## 10. ADR-052 propuesto

**Título:** expansión controlada a 1.020 lecciones por idioma con paridad
CEFR.

**Decisión:** objetivo de 170 lecciones por nivel en los 4 cursos; lotes de
10 lecciones; ids nuevos sin reciclar; schema v2 sin cambios; gates
automáticos y revisión nativa muestreada; releases por nivel hasta 4.0.0.

**Consecuencias:** 4.080 lecciones y 38.080 ejercicios; mayor coste de autoría,
revisión y QA; el contenido sigue 100 % offline y el progreso histórico queda
intacto.
