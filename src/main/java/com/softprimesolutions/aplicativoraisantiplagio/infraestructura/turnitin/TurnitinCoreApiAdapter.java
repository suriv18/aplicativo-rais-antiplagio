package com.softprimesolutions.aplicativoraisantiplagio.infraestructura.turnitin;

import java.util.Optional;

import com.softprimesolutions.aplicativoraisantiplagio.aplicacion.puerto.ServicioSimilitud;

/**
 * Adaptador real hacia la Turnitin Core API (TCA).
 *
 * Según la documentación pública de Turnitin, el flujo es asíncrono:
 *   1. Crear la submission (se obtiene un ID).
 *   2. Subir el archivo a esa submission.
 *   3. Esperar a que termine de procesarse (webhook SUBMISSION_COMPLETE o polling).
 *   4. Solicitar el Similarity Report.
 *   5. Esperar a que el reporte termine (webhook SIMILARITY_COMPLETE o polling).
 *   Fuente: https://developers.turnitin.com/turnitin-core-api
 *
 * IMPORTANTE: esta clase NO se ejecuta en las pruebas del pipeline. Cada llamada real
 * cuesta dinero y puede demorar hasta 10 minutos. Se valida aparte, con pocas pruebas
 * de contrato en un entorno controlado. Queda fuera del alcance del laboratorio.
 */
public class TurnitinCoreApiAdapter implements ServicioSimilitud {

    @Override
    public String enviar(String nombreArchivo, byte[] contenidoPdf) {
        throw new UnsupportedOperationException(
                "Integración real con Turnitin Core API: fuera del alcance del laboratorio");
    }

    @Override
    public Optional<Integer> consultarPorcentaje(String idEnvio) {
        throw new UnsupportedOperationException(
                "Integración real con Turnitin Core API: fuera del alcance del laboratorio");
    }
}
