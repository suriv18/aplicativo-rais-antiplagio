package com.softprimesolutions.aplicativoraisantiplagio.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** La política se prueba sola: no necesita Turnitin, ni Mock, ni reloj. */
class PoliticaSimilitudTest {

    private final PoliticaSimilitud politica = new PoliticaSimilitud(20, Duration.ofMinutes(15));
    private final Instant envio = Instant.parse("2026-11-13T09:00:00Z");

    @Test
    @DisplayName("20 % exacto no excede el umbral: se aprueba")
    void veinteExactoSeAprueba() {
        assertThat(politica.decidir(20)).isEqualTo(EstadoTesis.SIMILITUD_APROBADA);
    }

    @Test
    @DisplayName("21 % excede el umbral: se rechaza")
    void veintiunoSeRechaza() {
        assertThat(politica.decidir(21)).isEqualTo(EstadoTesis.RECHAZADA_POR_SIMILITUD);
    }

    @Test
    @DisplayName("A los 14:59 todavía no se agota la espera; a los 15:00 sí")
    void limiteDeEspera() {
        assertThat(politica.esperaAgotada(envio, envio.plusSeconds(15 * 60 - 1))).isFalse();
        assertThat(politica.esperaAgotada(envio, envio.plusSeconds(15 * 60))).isTrue();
    }
}
