package com.softprimesolutions.aplicativoraisantiplagio.aplicacion.puerto;

/** El servicio antiplagio no respondió (caída, red, mantenimiento, etc.). */
public class ServicioSimilitudNoDisponibleException extends RuntimeException {

    public ServicioSimilitudNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public ServicioSimilitudNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
