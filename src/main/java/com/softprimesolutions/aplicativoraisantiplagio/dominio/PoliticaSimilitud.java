package com.softprimesolutions.aplicativoraisantiplagio.dominio;

import java.time.Duration;

/**
 * Las dos reglas que definió el Vicerrectorado: el umbral de similitud (20 %)
 * y cuánto estamos dispuestos a esperar a Turnitin (15 minutos, decisión del grupo).
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
}
