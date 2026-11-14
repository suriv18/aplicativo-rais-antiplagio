package com.softprimesolutions.aplicativoraisantiplagio.dominio;

import java.time.Duration;
import java.time.Instant;

/**
 * Las dos reglas que definió el Vicerrectorado: el umbral de similitud (20 %)
 * y cuánto estamos dispuestos a esperar a Turnitin (15 minutos, decisión del grupo).
 *
 * Refactor: antes estas reglas vivían dentro del caso de uso; ahora son del dominio
 * y se pueden probar solas, sin Turnitin y sin reloj.
 */
public record PoliticaSimilitud(int umbralMaximo, Duration tiempoMaximoEspera) {

    public PoliticaSimilitud {
        if (umbralMaximo < 0 || umbralMaximo > 100) {
            throw new IllegalArgumentException("El umbral debe estar entre 0 y 100: " + umbralMaximo);
        }
        if (tiempoMaximoEspera == null || tiempoMaximoEspera.isNegative() || tiempoMaximoEspera.isZero()) {
            throw new IllegalArgumentException("El tiempo máximo de espera debe ser positivo");
        }
    }

    /**
     * El enunciado dice "si excede el 20 %, se rechaza". Excede = mayor estricto,
     * así que una tesis con exactamente 20 % pasa.
     */
    public EstadoTesis decidir(int porcentajeSimilitud) {
        return porcentajeSimilitud > umbralMaximo
                ? EstadoTesis.RECHAZADA_POR_SIMILITUD
                : EstadoTesis.SIMILITUD_APROBADA;
    }

    /** ¿Ya se cumplió el tiempo máximo de espera desde que enviamos el PDF? */
    public boolean esperaAgotada(Instant enviadaEn, Instant ahora) {
        return !ahora.isBefore(enviadaEn.plus(tiempoMaximoEspera));
    }
}
