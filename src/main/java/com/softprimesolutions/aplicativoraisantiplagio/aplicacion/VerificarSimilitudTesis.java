package com.softprimesolutions.aplicativoraisantiplagio.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import com.softprimesolutions.aplicativoraisantiplagio.aplicacion.puerto.ServicioSimilitud;
import com.softprimesolutions.aplicativoraisantiplagio.aplicacion.puerto.ServicioSimilitudNoDisponibleException;
import com.softprimesolutions.aplicativoraisantiplagio.dominio.EstadoTesis;
import com.softprimesolutions.aplicativoraisantiplagio.dominio.PoliticaSimilitud;
import com.softprimesolutions.aplicativoraisantiplagio.dominio.Tesis;

/**
 * Caso de uso: pasar la tesis por el filtro antiplagio.
 *
 * Fase GREEN: lo mínimo para que pasen las pruebas. Maneja la demora de Turnitin,
 * la regla del 20 % y la caída del servicio, sin reenviar nunca un PDF ya enviado.
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
        return intentarEnviar(tesis);
    }

    /** Se ejecuta periódicamente para ver si Turnitin ya respondió. */
    public ResultadoVerificacion revisar(Tesis tesis) {
        if (tesis.estado().esDecisionFinal()) {
            return new ResultadoVerificacion(tesis.estado(), mensajeDeDecision(tesis));
        }
        if (!tesis.fueEnviada()) {
            return intentarEnviar(tesis);   // nunca llegó a Turnitin (estaba caído): ahora sí se envía
        }

        Optional<Integer> porcentaje = consultarSinRomperse(tesis.idEnvio());
        if (porcentaje.isPresent()) {
            int p = porcentaje.get();
            EstadoTesis decision = p > politica.umbralMaximo()
                    ? EstadoTesis.RECHAZADA_POR_SIMILITUD
                    : EstadoTesis.SIMILITUD_APROBADA;
            tesis.registrarDecision(p, decision);
            return new ResultadoVerificacion(decision, mensajeDeDecision(tesis));
        }

        Instant limite = tesis.enviadaEn().plus(politica.tiempoMaximoEspera());
        if (!reloj.instant().isBefore(limite)) {
            tesis.marcarPendiente();
            return new ResultadoVerificacion(tesis.estado(), MSG_DEMORA);
        }
        return new ResultadoVerificacion(tesis.estado(), MSG_EN_ANALISIS);
    }

    private ResultadoVerificacion intentarEnviar(Tesis tesis) {
        try {
            String idEnvio = servicio.enviar(tesis.nombreArchivo(), tesis.contenidoPdf());
            tesis.marcarEnAnalisis(idEnvio, reloj.instant());
            return new ResultadoVerificacion(tesis.estado(), MSG_EN_ANALISIS);
        } catch (ServicioSimilitudNoDisponibleException e) {
            tesis.marcarPendiente();
            return new ResultadoVerificacion(tesis.estado(), MSG_NO_DISPONIBLE);
        }
    }

    private Optional<Integer> consultarSinRomperse(String idEnvio) {
        try {
            return servicio.consultarPorcentaje(idEnvio);
        } catch (ServicioSimilitudNoDisponibleException e) {
            return Optional.empty();   // se trata igual que "todavía no hay resultado"
        }
    }

    private String mensajeDeDecision(Tesis tesis) {
        if (tesis.estado() == EstadoTesis.RECHAZADA_POR_SIMILITUD) {
            return "Su tesis fue rechazada: la similitud (" + tesis.porcentajeSimilitud()
                    + " %) supera el máximo permitido (" + politica.umbralMaximo() + " %).";
        }
        return "Su tesis superó el filtro antiplagio (similitud: " + tesis.porcentajeSimilitud() + " %).";
    }
}
