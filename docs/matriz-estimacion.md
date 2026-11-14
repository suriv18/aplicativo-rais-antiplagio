# Matriz Esfuerzo vs. Riesgo – Filtro antiplagio con Turnitin (Grupo 1)

Laboratorio 8.1 · Entornos Metodológicos de Desarrollo de Software · UNMSM

**Reto:** cuando el docente sube el PDF de su tesis, RAIS lo envía a Turnitin, espera el porcentaje de similitud y, si excede el 20 %, rechaza la tesis de inmediato.

## 1. Estimación inicial (antes de aplicar ingeniería)

|  | Esfuerzo bajo | Esfuerzo alto |
|---|---|---|
| **Riesgo alto** | **● RETO (inicial) → 13 pts** | Dividir / investigar |
| **Riesgo bajo** | Tarea rutinaria (1–2 pts) | Trabajo grande pero conocido (5–8 pts) |

**Posición:** cuadrante superior izquierdo → esfuerzo bajo, riesgo máximo.
**Puntaje: 13 (Fibonacci).**

### Justificación

**Esfuerzo bajo.** El código en sí es corto: enviar un archivo, leer un número y compararlo con 20. No hay algoritmos complejos; la similitud la calcula Turnitin, no nosotros.

**Riesgo alto**, por factores externos que no controlamos:

| Fuente de incertidumbre | Efecto sobre el equipo |
|---|---|
| Cada consulta real **cobra dinero** | Cada ejecución de pruebas tiene costo; el pipeline automático gastaría presupuesto sin parar |
| El análisis tarda **hasta 10 minutos** en el entorno de pruebas | Cada ciclo de TDD dura 10 min; los despliegues automáticos quedan bloqueados |
| No sabemos cuánto demorará ni si responderá | No se puede prometer una fecha: la tarea es impredecible |

Por qué una estimación en horas fallaría: el trabajo de programar son pocas horas, pero el tiempo real lo dicta un tercero. Un equipo estimaría 2 horas, otro 3 días de "esperas y reintentos". Los puntos Fibonacci capturan esa incertidumbre; las horas no. Por eso el puntaje sube a 13 aunque el esfuerzo de código sea bajo.

## 2. Mitigación técnica aplicada

| Técnica | Qué neutraliza |
|---|---|
| **BDD (Cucumber)**: el comportamiento ante demora o caída queda escrito antes de programar | Ambigüedad: todos saben qué debe pasar si Turnitin no responde |
| **Puerto `ServicioSimilitud`** (interfaz nuestra) | Acoplamiento: RAIS ya no depende de Turnitin directamente |
| **Mock (Mockito) / `TurnitinSimulado`** reemplazan a Turnitin | Costo por consulta y 10 min de espera: las pruebas corren en milisegundos y gratis |
| **`RelojControlable`** | Los "15 minutos de espera" pasan en un instante |
| **TDD Red-Green-Refactor** | Cada regla (20 %, 15 min) nace probada |

## 3. Estimación final (después)

|  | Esfuerzo bajo | Esfuerzo alto |
|---|---|---|
| **Riesgo alto** | ~~13~~ (posición inicial) | |
| **Riesgo controlado / bajo** | **● RETO (final) → 3 pts** | |

**Posición:** el reto baja al cuadrante de riesgo controlado, esfuerzo bajo.
**Puntaje: 3 (Fibonacci).**

Por qué 3 y no 1: queda un esfuerzo pequeño pero real (el puerto, la política del 20 % y el reintento sin reenviar). Por qué no 0 de riesgo: la integración real con Turnitin se valida aparte con pocas pruebas de contrato (ver README, "¿Y el riesgo de verdad desaparece?").

## 4. Diagrama del movimiento

```
Riesgo
  ^
A |  [13] ● Reto inicial
L |       |
T |       |  TDD + Mock + Cucumber
O |       v
  |
B |  [ 3] ● Reto final
A |
J +----------------------------------> Esfuerzo
O     bajo                    alto
```

## 5. Resumen para la diapositiva

> **13 → 3 puntos.** El esfuerzo de código nunca fue el problema; lo era el riesgo externo (costo y 10 min de Turnitin). Al aislar Turnitin tras una interfaz propia y simularlo, el riesgo salió del día a día del desarrollo.
