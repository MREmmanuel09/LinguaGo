# Auditoría global del contenido (4.080 lecciones)

**Fecha:** 2026-11-15
**Estado:** auditoría técnica completa; revisión nativa humana pendiente.
**Alcance:** 4 cursos (ES→EN, ES→PT, ES→FR, ES→JA) × 6 niveles
(A1–C2) × 170 lecciones = **4.080 lecciones** y **38.080 ejercicios**.

> Esta auditoría **no sustituye** una revisión nativa humana. Comprueba lo que
> una máquina puede demostrar (estructura, claves, placeholders, duplicados,
> higiene, coherencia de nivel) y revisa una muestra cualitativa. La naturalidad
> fina, el registro y las traducciones de matiz siguen requiriendo revisión
> nativa.

## 1. Método

1. **Auditoría estructural lección por lección** con
   `tools/content/audit_all.py`: niveles, unidades, número de ejercicios,
   focos gramaticales, `scenario`/`canDo`, bandas léxicas, notas culturales,
   micro-clase, glosario, tipos permitidos por CEFR, feedback, opciones,
   `correctIndex`, placeholders de cloze, tokens, pares, registros,
   `conceptClusters`, `minTokens`, mojibake, espacios, scripts y duplicados.
2. **Comprobaciones específicas** de claves de respuesta, coherencia entre
   feedback y `answerTokens`, metalenguaje inglés en cursos no ingleses y
   cobertura de `conceptClusters`.
3. **Muestra cualitativa** de 72 lecciones (3 por nivel y curso: primera,
   central y última) más casos límite detectados por las herramientas.
4. **Verificación en emulador** de una clave corregida y de la estructura de
   una lección rediseñada.

## 2. Errores corregidos

| Hallazgo | Cantidad | Corrección |
|---|---:|---|
| `ListenChoose` con `correctIndex` que no apuntaba al audio | **1.759** | Se fijó el índice de la opción que coincide con `audioText`; se añadió validación en `CourseValidator` y dos tests |
| Lecciones piloto duplicadas exactamente en PT/FR/JA | **146** | Se convirtieron en repasos mixtos que combinan ejercicios de dos lecciones del mismo nivel; se conservaron conteos y tipos |
| Lecciones históricas B1/B2 de ES→EN sin `microLesson` | **42** | Se generó la micro-clase desde los focos gramaticales, la nota cultural, el glosario y los ejercicios |
| Huecos de cloze con opciones repetidas | **2** | Se deduplicaron las opciones usando respuestas y opciones hermanas |
| `minTokens` superior a la propia respuesta modelo | **121** | Se ajustó al número real de tokens del modelo (el alumno que reproduzca el modelo ya no recibe `TOO_SHORT`) |
| Espacios al inicio/final de términos y pasajes | **47** | Se recortaron todos los strings de contenido |
| «de el» en metadatos españoles | **341** | Se sustituyó por «del» (sin tocar «de El Salvador») |
| Metalenguaje inglés en gramática de PT/FR/JA | miles de cadenas | Se tradujeron 13 etiquetas y 19 descripciones únicas; barrido final: **0** términos ingleses en metadatos |
| Títulos de gramática en inglés en PT/FR/JA | **165** | Se tradujeron los 11 títulos repetidos por nivel (`Going to`, `Present perfect`, `Used to`…) |
| `MatchPairs` de JA con columnas invertidas | **84 pares** | Se normalizó español→japonés en A1/A2; se conservaron kana→romaji y los pares semánticos de B1+ |
| Mezclas puntuales PT/ES (`sobre el assunto`, `las consequências`) | **13** | Se corrigieron a portugués |
| `Eu adoraria obrigado` sin coma | **3** | Se corrigió a `Eu adoraria, obrigado` |
| `読む こと が したい です` (construcción japonesa antinatural) | **3** | Se naturalizó a `読みたい です`, con tokens de `OrderWords` coherentes |
| Títulos repetidos dentro del mismo nivel | **14** | Se marcaron como «· práctica» o «Repaso: …» según el caso |

### 2.1. El fallo más grave: claves de `ListenChoose`

Miles de ejercicios tenían `correctIndex: 0` aunque el audio era la segunda o
tercera opción. Ejemplo real de ES→EN A1:

```json
{
  "type": "ListenChoose",
  "audioText": "I don't say thank you",
  "options": ["I say hello", "I say goodbye", "I don't say thank you"],
  "correctIndex": 0
}
```

El alumno veía como correcta «I say hello». Se corrigieron **1.759** casos en
los cuatro cursos. La regla es demostrable: si `audioText` está entre las
opciones, la opción correcta es exactamente esa. Se añadió a
`CourseValidator.validateListenChoose` y se cubrió con
`CourseValidatorTest` para que no vuelva a colarse.

### 2.2. Lecciones piloto duplicadas

PT/FR/JA contenían unidades `*-p0-*` con lecciones idénticas a las del camino
principal. Se convirtieron en **repasos integradores**: mantienen el id y el
número/tipo de ejercicios, pero combinan ejercicios de dos lecciones del mismo
nivel. Así dejan de ser duplicados exactos y el alumno repasa sin repetir la
misma sesión. Los conteos por nivel y curso no cambiaron.

### 2.3. Localización de la gramática

Los cursos PT/FR/JA se generaron desde especificaciones inglesas y conservaron
etiquetas como `Passive reporting`, `Will have + participio` o `Going to` en
sus micro-clases y descripciones. Se sustituyeron por explicaciones en español
(source language) sin formas verbales de otro idioma. También se eliminaron
mezclas como «Vocabulario básico de as tarefas» mediante plantillas genéricas
(«Vocabulario básico del tema»).

## 3. Verificación

| Comprobación | Resultado |
|---|---|
| `audit_all.py` | **0 errores**; 922 avisos informativos |
| Lecciones por curso / nivel | 1.020 / 170 en los cuatro cursos |
| Ejercicios totales | 38.080 |
| `validateContent` | **OK** (los 4 cursos y fixtures) |
| `:core:core-content:testDebugUnitTest` | **85/85** |
| `ktlintCheck detekt validateContent test` | **BUILD SUCCESSFUL** |
| `:app:assembleFullDebug` | **BUILD SUCCESSFUL** |
| Prueba en emulador | `ListenChoose` «Good evening» → **¡Correcto!** con el feedback del audio |

La prueba en emulador se hizo sobre la lección A1 «Saludos» de ES→EN: se
completaron los cuatro ejercicios previos, se eligió «Good evening» en el
quinto y la app respondió «¡Correcto!», confirmando que la clave corregida se
usa de extremo a extremo.

## 4. Avisos que quedan (no son errores)

- **72 pares de cognados** en `MatchPairs` (`chocolate = chocolate`,
  `médico = médico`, `azul = azul`…). Son válidos, pero en A1 resultan
  demasiado fáciles; la revisión nativa puede sustituirlos por pares con más
  contraste.
- **268 ejercicios de comprensión B1+** con opciones paráfrasis en español
  (`listen-comprehension`). No son claves erróneas: el `correctIndex` se
  revisó en muestras de B1, C1 y C2. La herramienta los marca solo porque el
  audio no aparece literalmente entre las opciones.
- **582 títulos repetidos entre niveles** (F3–F6 derivaron títulos de F2). El
  contenido escala por nivel, pero el título se repite; conviene diferenciarlo
  en la revisión nativa.
- **Cobertura de `conceptClusters`**: la comprobación estricta marca 15
  respuestas modelo en EN, 26 en PT, 19 en FR y 35 en JA que no contienen
  literalmente un término del cluster. Muchas son variantes morfológicas o
  sinónimos que el grader acepta; requiere revisión lingüística, no un fix
  automático.

## 5. Deuda de revisión nativa

La muestra cualitativa detectó puntos que una máquina no debe corregir sola:

- **PT B1**: cuerpos de micro-clase con concatenaciones sin puntuar, p. ej.
  «Vamos começar pelo primeiro ponto? abre; eu gostaria de acrescentar um ponto
  pide la palabra.». Se entiende, pero necesita redacción nativa.
- **JA A2**: algunas etiquetas gramaticales siguen siendo conceptos ingleses
  traducidos al español (`Presente simple`), aunque el contenido enseñe formas
  japonesas. La revisión nativa debe sustituirlas por categorías japonesas
  (por ejemplo, forma ます, て形, 敬語).
- **Traducciones de matiz**: se detectaron casos como «Escribo para solicitar
  una entrevista» → «Estou escrevendo…» (presente continuo) o «Adjunto…» →
  «Anexei…» (pasado). Son naturales en contexto de correo, pero no son
  literales; la revisión nativa debe decidir el criterio.
- **Espaciado japonés**: el contenido separa morfemas con espacios para el
  tokenizador; el feedback a veces usa la frase natural sin espacios. Es
  intencional para el TTS/STT, pero conviene revisar la consistencia visible.
- **Revisión nativa completa de las 4.080 lecciones**: sigue pendiente y es la
  única forma de validar naturalidad, registro y adecuación cultural.

## 6. Invariantes respetadas

- **I1/I2**: no se añadió red ni servicios; todo el contenido sigue empaquetado
  y offline.
- **I4**: no se tocó Room, FSRS ni el esquema de progreso; las gemas siguen en
  DataStore.
- **Conteos**: 1.020 lecciones por curso, 170 por nivel y 38.080 ejercicios.
- **Contenido**: UTF-8 sin BOM; `validateContent` en verde.
- **Sin dependencias nuevas** en la app: las herramientas de auditoría son
  scripts de `tools/content/`.

## 7. Reproducibilidad

```powershell
python tools/content/audit_all.py
python tools/content/audit_english_meta.py
python tools/content/audit_order_feedback.py
python tools/content/audit_clusters.py
python tools/content/sample_lessons.py

./gradlew ktlintCheck detekt validateContent test
./gradlew :app:assembleFullDebug
```

El informe máquina de la auditoría global queda en
`build/reports/audit-all.json`; la muestra legible, en
`build/reports/sample-review.txt`.
