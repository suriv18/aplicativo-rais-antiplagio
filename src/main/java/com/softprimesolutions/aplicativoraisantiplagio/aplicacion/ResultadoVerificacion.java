package com.softprimesolutions.aplicativoraisantiplagio.aplicacion;

import com.softprimesolutions.aplicativoraisantiplagio.dominio.EstadoTesis;

/** Lo que RAIS le muestra al docente después de cada paso del filtro. */
public record ResultadoVerificacion(EstadoTesis estado, String mensaje) {
}
