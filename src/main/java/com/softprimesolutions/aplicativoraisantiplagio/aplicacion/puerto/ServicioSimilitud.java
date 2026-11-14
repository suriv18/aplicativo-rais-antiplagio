package com.softprimesolutions.aplicativoraisantiplagio.aplicacion.puerto;

import java.util.Optional;

/**
 * Puerto de salida: es la "puerta" por la que RAIS habla con el servicio antiplagio.
 *
 * Esta interfaz es NUESTRA, no de Turnitin. Por eso podemos simularla en las pruebas
 * sin pagar consultas ni esperar 10 minutos. En producción la implementa el adaptador
 * que llama a la Turnitin Core API.
 */
public interface ServicioSimilitud {

    /**
     * Envía el PDF al servicio externo.
     *
     * @return identificador del envío, para consultar el resultado después
     * @throws ServicioSimilitudNoDisponibleException si el servicio no responde
     */
    String enviar(String nombreArchivo, byte[] contenidoPdf);

    /**
     * Consulta si el análisis ya terminó.
     *
     * @return el porcentaje de similitud si ya está listo; vacío si todavía se procesa
     * @throws ServicioSimilitudNoDisponibleException si el servicio no responde
     */
    Optional<Integer> consultarPorcentaje(String idEnvio);
}
