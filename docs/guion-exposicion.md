# Guion de exposición – Grupo 1: Filtro antiplagio con Turnitin

**Duración:** 8 minutos de contenido (el enunciado fija 10 min máximo; la rúbrica menciona 4 min, **confirmar con el docente**). Si son 4 min, ver la versión corta al final.

**Reparto sugerido:** persona A (min 1–2 y 7–8), persona B (min 3–4), persona C (min 5–6).

---

## Minuto 1–2 · Diagnóstico técnico (por qué las horas fracasan)

**Decir:**
- "Cuando el docente sube su tesis, RAIS la envía a Turnitin y rechaza si la similitud excede el 20 %."
- "Programar esto son pocas líneas: enviar, leer un número, comparar. El esfuerzo es bajo."
- "El problema es el riesgo: Turnitin **cobra por cada consulta** y **tarda hasta 10 minutos**. Eso bloquea los despliegues automáticos."
- "Si estimamos en horas, un equipo dice 2 horas y otro dice 3 días. El tiempo real lo decide un tercero. Por eso usamos puntos Fibonacci: **13**, porque combinamos esfuerzo bajo con riesgo máximo."

**Mostrar:** matriz 2x2 con el punto en "riesgo alto / esfuerzo bajo" (`docs/matriz-estimacion.md`, sección 1).

---

## Minuto 3–4 · Escenario Cucumber

**Decir:**
- "Antes de programar, definimos el comportamiento cuando Turnitin falla, en lenguaje de negocio."

**Mostrar:** `src/test/resources/features/filtro_antiplagio.feature`

```gherkin
Escenario: Turnitin no entrega el porcentaje dentro del tiempo máximo de espera
  Dado que el docente subió el PDF de su tesis "tesis_maestria.pdf"
  Y la tesis fue enviada a Turnitin para el análisis de similitud
  Cuando transcurren 15 minutos sin que Turnitin entregue el porcentaje de similitud
  Entonces el sistema debe mostrar el mensaje "El análisis de similitud está demorando más de lo previsto..."
  Y la tesis debe quedar en estado "Pendiente de verificación"
  Y la tesis no debe ser aprobada ni rechazada
```

**Puntos a destacar:**
- Decisión de negocio: ante la demora no se aprueba ni se rechaza; se pospone.
- Hay un segundo escenario para Turnitin caído y un esquema con 19, 20 y 21 %: "excede" significa mayor estricto, así que 20 % pasa.
- Los 15 minutos son una decisión del grupo; el Vicerrectorado fijó solo el 20 %.

**Comando opcional:** `mvn test` → reporte en `target/reporte-cucumber.html`.

---

## Minuto 5–6 · Estrategia TDD + Mock

**Decir:**
- "RAIS no habla con Turnitin: habla con `ServicioSimilitud`, una interfaz **nuestra**. En producción la implementa el adaptador real; en pruebas la reemplazamos."

| Doble | Qué reemplaza | Para qué |
|---|---|---|
| `@Mock ServicioSimilitud` (Mockito) | Turnitin | Verificar que el PDF se envía **una sola vez** |
| `TurnitinSimulado` | Turnitin | Configurar en Cucumber: responde X %, demora o cae |
| `RelojControlable` | Reloj del sistema | Que 15 minutos pasen en un instante |

**Demo en vivo del ciclo** (repositorio `aplicativo-rais-antiplagio`, tags de git; antes de exponer: `git status` limpio y estar en `main`). El historial también muestra que el Gherkin fue el primer commit después del esqueleto (`git log --oneline`):

```bash
git checkout 1-red       && mvn test   # falla: el código asume que Turnitin siempre responde
git checkout 2-green     && mvn test   # pasa: implementación mínima
git checkout 3-refactor  && mvn test   # pasa: las reglas 20 % y 15 min pasan al dominio
git checkout main
```

**Puntos a destacar:**
- Solo simulamos tipos que son nuestros (Freeman y Pryce); si cambia la API de Turnitin, solo cambia el adaptador.
- Resultado: toda la lógica corre en milisegundos y sin pagar una consulta.

---

## Minuto 7–8 · Conclusión ejecutiva

**Mostrar:** la matriz con la flecha **13 → 3** (`docs/matriz-estimacion.md`, secciones 3 y 4).

**Decir:**
- "El esfuerzo nunca cambió; lo que bajó fue el riesgo. Pasamos de 13 a 3 puntos."
- "No decimos que el riesgo es cero: la integración real se valida aparte con pocas pruebas de contrato, de vez en cuando. Lo que sacamos fue el riesgo del día a día del desarrollo."
- Cierre: "El pipeline ya no cuesta dinero ni espera 10 minutos."

---

## Preguntas críticas probables

| Pregunta | Respuesta |
|---|---|
| ¿Por qué 15 minutos? | Turnitin puede tardar hasta 10 min en pruebas; damos 5 min de margen. Es una decisión del grupo y se configura en `PoliticaSimilitud`. |
| ¿Qué pasa con exactamente 20 %? | Pasa. El enunciado dice "excede", es decir, mayor estricto. Está en el esquema de ejemplos. |
| ¿Y si el Mock no se parece a Turnitin real? | Por eso quedan pruebas de contrato contra el servicio real, fuera del pipeline. El Mock valida **nuestra lógica**, no a Turnitin. |
| ¿Por qué no simular el cliente HTTP? | Porque no es nuestro tipo; acoplaría las pruebas a la API de Turnitin. |
| ¿Se cobra dos veces si reintentamos? | No: tras la demora solo se vuelve a consultar con el mismo ID de envío; hay una prueba que verifica un único envío. |
| ¿Por qué 13 si son pocas líneas? | Los puntos miden volumen con incertidumbre, no horas de código. |

---

## Versión corta (si el límite real es 4 minutos)

| Tiempo | Contenido |
|---|---|
| 0:00–0:45 | Diagnóstico: esfuerzo bajo, riesgo máximo, 13 puntos |
| 0:45–1:45 | Escenario Cucumber de la demora (solo ese) |
| 1:45–3:15 | Interfaz propia + Mock + reloj controlable; mostrar `mvn test` verde |
| 3:15–4:00 | Matriz 13 → 3 y cierre |

## Lista de verificación antes de exponer
- [ ] `mvn test` en verde en la máquina de la exposición (JDK 25 en `aplicativo-rais-antiplagio`; `mvn` ya está en el PATH).
- [ ] Tags `1-red`, `2-green`, `3-refactor` presentes en `aplicativo-rais-antiplagio` (`git tag`), y `git checkout main` al terminar la demo.
- [ ] Matriz lista como imagen o diapositiva.
- [ ] Ensayo cronometrado.
