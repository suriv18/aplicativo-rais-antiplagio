package com.softprimesolutions.aplicativoraisantiplagio.dobles;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Doble de prueba tipo FAKE: un reloj que nosotros adelantamos.
 * Con esto "pasan" 15 minutos en un instante, sin esperar de verdad.
 */
public class RelojControlable extends Clock {

    private Instant ahora;
    private final ZoneId zona;

    public RelojControlable(Instant inicio) {
        this(inicio, ZoneOffset.UTC);
    }

    private RelojControlable(Instant inicio, ZoneId zona) {
        this.ahora = inicio;
        this.zona = zona;
    }

    public void avanzar(Duration tiempo) {
        ahora = ahora.plus(tiempo);
    }

    @Override
    public Instant instant() {
        return ahora;
    }

    @Override
    public ZoneId getZone() {
        return zona;
    }

    @Override
    public Clock withZone(ZoneId otraZona) {
        return new RelojControlable(ahora, otraZona);
    }
}
