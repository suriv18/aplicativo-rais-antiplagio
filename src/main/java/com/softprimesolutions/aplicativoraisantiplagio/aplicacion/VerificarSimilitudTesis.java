package com.softprimesolutions.aplicativoraisantiplagio.aplicacion;

import java.time.Clock;
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
 * Fase REFACTOR: el comportamiento es el mismo que en GREEN (las pruebas no cambiaron),
 * pero las reglas del 20 % y de los 15 minutos ahora viven en PoliticaSimilitud
 * y cada paso del flujo tiene su propio método.
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
            return resultadoDeDecision(tesis);
        }
        if (!tesis.fueEnviada()) {
            return intentarEnviar(tesis);   // nunca llegó a Turnitin (estaba caído): ahora sí se envía
        }
        return consultarSinRomperse(tesis.idEnvio())
                .map(porcentaje -> decidir(tesis, porcentaje))
                .orElseGet(() -> seguirEsperando(tesis));
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

    private ResultadoVerificacion decidir(Tesis tesis, int porcentaje) {
        tesis.registrarDecision(porcentaje, politica.decidir(porcentaje));
        return resultadoDeDecision(tesis);
    }

    private ResultadoVerificacion seguirEsperando(Tesis tesis) {
        if (politica.esperaAgotada(tesis.enviadaEn(), reloj.instant())) {
            tesis.marcarPendiente();
            return new ResultadoVerificacion(tesis.estado(), MSG_DEMORA);
        }
        return new ResultadoVerificacion(tesis.estado(), MSG_EN_ANALISIS);
    }

    private Optional<Integer> consultarSinRomperse(String idEnvio) {
        try {
            return servicio.consultarPorcentaje(idEnvio);
        } catch (ServicioSimilitudNoDisponibleException e) {
            return Optional.empty();   // se trata igual que "todavía no hay resultado"
        }
    }

    private ResultadoVerificacion resultadoDeDecision(Tesis tesis) {
        String mensaje = tesis.estado() == EstadoTesis.RECHAZADA_POR_SIMILITUD
                ? "Su tesis fue rechazada: la similitud (" + tesis.porcentajeSimilitud()
                        + " %) supera el máximo permitido (" + politica.umbralMaximo() + " %)."
                : "Su tesis superó el filtro antiplagio (similitud: " + tesis.porcentajeSimilitud() + " %).";
        return new ResultadoVerificacion(tesis.estado(), mensaje);
    }
}
