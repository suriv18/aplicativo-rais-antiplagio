package com.softprimesolutions.aplicativoraisantiplagio.dominio;

import java.time.Instant;
import java.util.Objects;

/**
 * La tesis que sube el docente. Guarda en qué punto del filtro antiplagio está.
 */
public class Tesis {

    private final String nombreArchivo;
    private final byte[] contenidoPdf;

    private EstadoTesis estado = EstadoTesis.REGISTRADA;
    private String idEnvio;            // identificador que devuelve Turnitin al recibir el PDF
    private Instant enviadaEn;         // cuándo se envió, para medir la espera
    private Integer porcentajeSimilitud;

    public Tesis(String nombreArchivo, byte[] contenidoPdf) {
        this.nombreArchivo = Objects.requireNonNull(nombreArchivo, "nombreArchivo");
        this.contenidoPdf = Objects.requireNonNull(contenidoPdf, "contenidoPdf");
    }

    public void marcarEnAnalisis(String idEnvio, Instant cuando) {
        this.idEnvio = Objects.requireNonNull(idEnvio, "idEnvio");
        this.enviadaEn = Objects.requireNonNull(cuando, "cuando");
        this.estado = EstadoTesis.EN_ANALISIS;
    }

    public void marcarPendiente() {
        this.estado = EstadoTesis.PENDIENTE_VERIFICACION;
    }

    public void registrarDecision(int porcentaje, EstadoTesis decision) {
        if (!decision.esDecisionFinal()) {
            throw new IllegalArgumentException("Una decisión solo puede ser aprobar o rechazar: " + decision);
        }
        this.porcentajeSimilitud = porcentaje;
        this.estado = decision;
    }

    public boolean fueEnviada() {
        return idEnvio != null;
    }

    public String nombreArchivo() { return nombreArchivo; }
    public byte[] contenidoPdf() { return contenidoPdf; }
    public EstadoTesis estado() { return estado; }
    public String idEnvio() { return idEnvio; }
    public Instant enviadaEn() { return enviadaEn; }
    public Integer porcentajeSimilitud() { return porcentajeSimilitud; }
}
