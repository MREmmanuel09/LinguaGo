# Fase 2 — Navegación, caminos y economía FSRS

**Fecha:** 2026-11-09
**Dispositivo:** `Medium_Phone_API_37.0` (emulador x86_64, API 37)
**Variante:** `full` debug y release universal

## Validación en vivo

| Área | Resultado |
|---|---|
| 4 pestañas | `Inicio · Repaso · Progreso · Ajustes` con badge numérico en Repaso. |
| Cambio rápido de curso | Hoja con los 4 cursos desde Home y desde Ajustes; EN → FR actualiza Home al instante y conserva el progreso separado. |
| Títulos del camino | Cada nodo muestra su lección; el cofre muestra «Cofre». |
| FAB | «Ir a mi lección» abre el nodo activo sin buscar en el camino. |
| Cofre | Unidad completada → cofre dorado → `+20 XP`; al reabrir muestra «Cofre ya reclamado» y no vuelve a sumar. |
| Badge de repaso | 3 tarjetas → badge 3; al calificar baja a 2 y a 0 al terminar. |
| Repaso restaurado | Al volver a la pestaña se recargan las tarjetas vencidas (antes se quedaba vacío). |
| Recuperación de vida | Con 9/10 vidas, 5 tarjetas «Bien» → 10/10. Indicador `n/5` visible. |
| Audio | Reproductor con 1x y 0.75x en el reverso. |
| `fontScale 2.0x` | Calificación en cuadrícula 2×2; barra inferior pasa a iconos para no romper etiquetas. |

## Tests automatizados

- `HeartsRulesTest`: `recoverOne` suma una vida, respeta el máximo y aplica
  primero la regeneración por tiempo.
- `ReviewHeartRulesTest`: 4 tarjetas no recuperan; la 5.ª recupera y reinicia
  el contador.
- Gate `ktlintCheck detekt validateContent test` en verde.

## Observaciones para F3

- Los ids de cofres reclamados viven en DataStore a la espera de la migración
  Room v5, que los integrará con el progreso.
- El FAB puede solaparse con la cabecera de unidad en listas cortas; se puede
  convertir en botón compacto o elevarlo al hacer scroll en una iteración
  posterior.
