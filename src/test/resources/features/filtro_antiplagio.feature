# language: es
Característica: Filtro antiplagio automático de tesis en RAIS
  Como Vicerrectorado de Investigación
  Quiero que toda tesis subida a RAIS pase por el análisis de similitud de Turnitin
  Para rechazar de inmediato las que superen el 20 % de similitud

  Antecedentes:
    Dado que el umbral máximo de similitud permitido es 20 %
    Y que el tiempo máximo de espera del análisis es de 15 minutos

  Escenario: Turnitin no entrega el porcentaje dentro del tiempo máximo de espera
    Dado que el docente subió el PDF de su tesis "tesis_maestria.pdf"
    Y la tesis fue enviada a Turnitin para el análisis de similitud
    Cuando transcurren 15 minutos sin que Turnitin entregue el porcentaje de similitud
    Entonces el sistema debe mostrar el mensaje "El análisis de similitud está demorando más de lo previsto. Le notificaremos cuando esté listo."
    Y la tesis debe quedar en estado "Pendiente de verificación"
    Y la tesis no debe ser aprobada ni rechazada
    Y el sistema debe volver a consultar el resultado sin enviar nuevamente el documento a Turnitin

  Escenario: Turnitin no está disponible al momento de enviar la tesis
    Dado que Turnitin no está disponible
    Cuando el docente sube el PDF de su tesis "tesis_maestria.pdf"
    Entonces el sistema debe mostrar el mensaje "El servicio de verificación de similitud no está disponible. Su tesis quedó registrada y será verificada automáticamente."
    Y la tesis debe quedar en estado "Pendiente de verificación"

  Esquema del escenario: Decisión según el porcentaje de similitud
    Dado que el docente subió el PDF de su tesis "tesis_maestria.pdf"
    Cuando Turnitin informa una similitud de <porcentaje> %
    Entonces la tesis debe quedar en estado "<estado>"

    Ejemplos:
      | porcentaje | estado                  |
      | 19         | Similitud aprobada      |
      | 20         | Similitud aprobada      |
      | 21         | Rechazada por similitud |
