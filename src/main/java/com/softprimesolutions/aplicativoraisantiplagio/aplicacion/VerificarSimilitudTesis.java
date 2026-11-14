package com.softprimesolutions.aplicativoraisantiplagio.aplicacion;

import java.time.Clock;
import java.util.Objects;

import com.softprimesolutions.aplicativoraisantiplagio.aplicacion.puerto.ServicioSimilitud;
import com.softprimesolutions.aplicativoraisantiplagio.dominio.PoliticaSimilitud;
import com.softprimesolutions.aplicativoraisantiplagio.dominio.Tesis;

/**
 * Caso de uso: pasar la tesis por el filtro antiplagio.
 *
 * VERSIÓN INICIAL (fase RED): solo envía el PDF y asume que Turnitin responderá.
 * Todavía no sabe qué hacer si Turnitin demora, se cae o devuelve el porcentaje.
 */
public class VerificarSimilitudTesis {

    public static final String MSG_EN_ANALISIS =
            "Su tesis está en análisis de similitud.";
    public static final String MSG_DEMORA =
            "El análisis de similitud está demorando más de lo previsto. Le notificaremos cuando esté listo.";
    public static final String MSG_NO_DISPONIBLE =
            "El servicio de verificación de similitud no está disponible. Su tesis quedó registrada y será verificada automáticamente.";

    private final ServicioSimilitud servicio;
    private final PoliticaSimilitud politica;
    private final Clock reloj;

    public VerificarSimilitudTesis(ServicioSimilitud servicio, PoliticaSimilitud politica, Clock reloj) {
        this.servicio = Objects.requireNonNull(servicio);
        this.politica = Objects.requireNonNull(politica);
        this.reloj = Objects.requireNonNull(reloj);
    }

    /** Se ejecuta cuando el docente sube el PDF. */
    public ResultadoVerificacion registrar(Tesis tesis) {
        String idEnvio = servicio.enviar(tesis.nombreArchivo(), tesis.contenidoPdf());
        tesis.marcarEnAnalisis(idEnvio, reloj.instant());
        return new ResultadoVerificacion(tesis.estado(), MSG_EN_ANALISIS);
    }

    /** Se ejecuta periódicamente para ver si Turnitin ya respondió. */
    public ResultadoVerificacion revisar(Tesis tesis) {
        // TODO: aún no implementado
        return new ResultadoVerificacion(tesis.estado(), MSG_EN_ANALISIS);
    }
}
