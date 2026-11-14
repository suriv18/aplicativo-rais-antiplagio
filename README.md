# RAIS – Filtro antiplagio con Turnitin (Grupo 1)

Laboratorio 8.1 – Estimación y TDD · Entornos Metodológicos de Desarrollo de Software · UNMSM

## De qué se trata

Cuando un docente sube el PDF de su tesis a RAIS, el sistema lo manda a Turnitin y espera el porcentaje de similitud. Si pasa del 20 %, se rechaza. El problema es que cada consulta real a Turnitin cuesta dinero y en su entorno de pruebas puede tardar hasta 10 minutos. Si nuestras pruebas dependieran de Turnitin, cada ciclo de TDD duraría 10 minutos y cada ejecución del pipeline costaría plata.

Nuestra solución fue no depender de Turnitin para programar ni para probar. RAIS no habla directamente con Turnitin: habla con una interfaz nuestra, `ServicioSimilitud`. En producción esa interfaz la implementa el adaptador real; en las pruebas la reemplazamos por un Mock o por un simulador. Así probamos toda la lógica (la demora, la caída, el umbral del 20 %) en milisegundos y sin pagar una sola consulta.

## Cómo correrlo

Necesitan Java 25 y Maven 3.9 o superior.

```bash
mvn test
```

Al final queda un reporte de los escenarios en `target/reporte-cucumber.html`.

## Demo en vivo del ciclo Red-Green-Refactor

El historial de git tiene una etiqueta por cada fase, así que en la exposición se puede mostrar en vivo:

```bash
git checkout 1-red        # pruebas escritas, código ingenuo
mvn test                  # → falla (RED)

git checkout 2-green      # implementación mínima
mvn test                  # → todo pasa (GREEN)

git checkout 3-refactor   # reglas movidas al dominio, mismas pruebas
mvn test                  # → sigue pasando

git checkout main         # volver a la versión final
```

En la fase RED las pruebas de demora, umbral y caída fallan porque el caso de uso todavía asume que Turnitin siempre responde a tiempo. Ese es justamente el supuesto que vuelve riesgosa la tarea.

## Qué simulamos y por qué

| Doble de prueba | Tipo (Meszaros) | Qué reemplaza | Para qué |
|---|---|---|---|
| `@Mock ServicioSimilitud` (Mockito) | Mock | Turnitin | Verificar interacciones: que el PDF se envía **una sola vez** y que se vuelve a consultar sin reenviar |
| `TurnitinSimulado` | Fake con registro de llamadas | Turnitin | Configurar su comportamiento en los escenarios Cucumber: responde X %, demora o está caído |
| `RelojControlable` | Fake | El reloj del sistema | Hacer que "pasen" 15 minutos en un instante |

Simulamos nuestra propia interfaz y no el cliente HTTP de Turnitin, siguiendo la regla de Freeman y Pryce: *only mock types that you own*. Si mañana cambia la API de Turnitin, solo cambia el adaptador; las pruebas y la lógica de negocio quedan igual.

## Estructura

```
src/main/java/com/softprimesolutions/aplicativoraisantiplagio/
├── dominio/            Tesis, EstadoTesis, PoliticaSimilitud (regla del 20 % y de los 15 min)
├── aplicacion/         VerificarSimilitudTesis (caso de uso) y ResultadoVerificacion
│   └── puerto/         ServicioSimilitud: la interfaz que aísla a Turnitin
└── infraestructura/
    └── turnitin/       TurnitinCoreApiAdapter (esqueleto, fuera del alcance del lab)

src/test/
├── java/.../aplicacion/  pruebas unitarias con Mockito
├── java/.../dominio/     pruebas de la política (sin dobles)
├── java/.../aceptacion/  pasos de Cucumber + runner
├── java/.../dobles/      TurnitinSimulado y RelojControlable
└── resources/features/   filtro_antiplagio.feature (Gherkin en español)
```

## ¿Y el riesgo de verdad desaparece?

No del todo, y conviene decirlo. Lo que hicimos es sacar el riesgo del día a día del desarrollo. La integración real con Turnitin se valida aparte, con pocas pruebas de contrato que se ejecutan de vez en cuando en un entorno controlado, no en cada commit. El riesgo pasa de "bloquea todo el desarrollo" a "se revisa en un punto acotado y conocido".

## Referencias

- Beck, K. (2003). *Test-Driven Development: By Example*. Addison-Wesley.
- Fowler, M. (2007). *Mocks Aren't Stubs*. https://martinfowler.com/articles/mocksArentStubs.html
- Freeman, S., y Pryce, N. (2009). *Growing Object-Oriented Software, Guided by Tests*. Addison-Wesley. Cap. 8, "Only Mock Types That You Own".
- Meszaros, G. (2007). *xUnit Test Patterns: Refactoring Test Code*. Addison-Wesley.
- Turnitin. *Turnitin Core API – Developer Guides*. https://developers.turnitin.com/turnitin-core-api
