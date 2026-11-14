package com.softprimesolutions.aplicativoraisantiplagio.dobles;

import java.util.Optional;

import com.softprimesolutions.aplicativoraisantiplagio.aplicacion.puerto.ServicioSimilitud;
import com.softprimesolutions.aplicativoraisantiplagio.aplicacion.puerto.ServicioSimilitudNoDisponibleException;

/**
 * Simulador de Turnitin para los escenarios de Cucumber.
 *
 * Le decimos cómo comportarse (responder con un porcentaje, demorar o estar caído)
 * y además lleva la cuenta de cuántas veces le enviamos documentos y cuántas veces
 * le consultamos. Así comprobamos que no se pagan envíos de más.
 */
public class TurnitinSimulado implements ServicioSimilitud {

    private boolean disponible = true;
    private Integer porcentajeListo;   // null = todavía "procesando"
    private int envios;
    private int consultas;

    public void simularCaida() {
        disponible = false;
    }

    public void simularDemora() {
        disponible = true;
        porcentajeListo = null;
    }

    public void responderCon(int porcentaje) {
        disponible = true;
        porcentajeListo = porcentaje;
    }

    @Override
    public String enviar(String nombreArchivo, byte[] contenidoPdf) {
        if (!disponible) {
            throw new ServicioSimilitudNoDisponibleException("Turnitin simulado: servicio caído");
        }
        envios++;
        return "sim-envio-" + envios;
    }

    @Override
    public Optional<Integer> consultarPorcentaje(String idEnvio) {
        consultas++;
        if (!disponible) {
            throw new ServicioSimilitudNoDisponibleException("Turnitin simulado: servicio caído");
        }
        return Optional.ofNullable(porcentajeListo);
    }

    public int envios() {
        return envios;
    }

    public int consultas() {
        return consultas;
    }
}
