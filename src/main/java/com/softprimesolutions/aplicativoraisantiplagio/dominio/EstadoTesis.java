package com.softprimesolutions.aplicativoraisantiplagio.dominio;

public enum EstadoTesis {

    REGISTRADA("Registrada"),
    EN_ANALISIS("En análisis"),
    PENDIENTE_VERIFICACION("Pendiente de verificación"),
    SIMILITUD_APROBADA("Similitud aprobada"),
    RECHAZADA_POR_SIMILITUD("Rechazada por similitud");

    private final String etiqueta;

    EstadoTesis(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }

    // Solo estos dos estados son una decisión tomada; el resto sigue esperando a Turnitin
    public boolean esDecisionFinal() {
        return this == SIMILITUD_APROBADA || this == RECHAZADA_POR_SIMILITUD;
    }
}
