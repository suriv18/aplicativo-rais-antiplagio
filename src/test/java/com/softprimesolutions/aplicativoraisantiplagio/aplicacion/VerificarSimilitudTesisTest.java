package com.softprimesolutions.aplicativoraisantiplagio.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.softprimesolutions.aplicativoraisantiplagio.aplicacion.puerto.ServicioSimilitud;
import com.softprimesolutions.aplicativoraisantiplagio.aplicacion.puerto.ServicioSimilitudNoDisponibleException;
import com.softprimesolutions.aplicativoraisantiplagio.dobles.RelojControlable;
import com.softprimesolutions.aplicativoraisantiplagio.dominio.EstadoTesis;
import com.softprimesolutions.aplicativoraisantiplagio.dominio.PoliticaSimilitud;
import com.softprimesolutions.aplicativoraisantiplagio.dominio.Tesis;

/**
 * Pruebas del caso de uso SIN Turnitin real.
 * El puerto ServicioSimilitud es un Mock de Mockito y el tiempo lo maneja RelojControlable.
 */
@ExtendWith(MockitoExtension.class)
class VerificarSimilitudTesisTest {

    private static final Duration QUINCE_MINUTOS = Duration.ofMinutes(15);

    @Mock
    private ServicioSimilitud turnitin;

    private RelojControlable reloj;
    private VerificarSimilitudTesis casoDeUso;
    private Tesis tesis;

    @BeforeEach
    void preparar() {
        reloj = new RelojControlable(Instant.parse("2026-11-13T09:00:00Z"));
        casoDeUso = new VerificarSimilitudTesis(turnitin, new PoliticaSimilitud(20, QUINCE_MINUTOS), reloj);
        tesis = new Tesis("tesis_maestria.pdf", new byte[] {1, 2, 3});
    }

    // ---------------------------------------------------------------------------------
    // Escenario principal del lienzo: Turnitin no responde dentro de los 15 minutos
    // ---------------------------------------------------------------------------------

    @Test
    @DisplayName("Si Turnitin no responde en 15 minutos, la tesis queda pendiente y no se reenvía")
    void turnitinNoRespondeATiempo() {
        when(turnitin.enviar(anyString(), any())).thenReturn("envio-001");
        when(turnitin.consultarPorcentaje("envio-001")).thenReturn(Optional.empty());

        casoDeUso.registrar(tesis);
        reloj.avanzar(QUINCE_MINUTOS);
        ResultadoVerificacion resultado = casoDeUso.revisar(tesis);

        assertThat(resultado.estado()).isEqualTo(EstadoTesis.PENDIENTE_VERIFICACION);
        assertThat(resultado.mensaje()).isEqualTo(VerificarSimilitudTesis.MSG_DEMORA);
        assertThat(tesis.estado().esDecisionFinal()).isFalse();   // ni aprobada ni rechazada

        // Más tarde el sistema vuelve a preguntar... pero sin volver a enviar (ni pagar) el PDF
        reloj.avanzar(Duration.ofMinutes(5));
        casoDeUso.revisar(tesis);

        verify(turnitin, times(1)).enviar(anyString(), any());
        verify(turnitin, times(2)).consultarPorcentaje("envio-001");
    }

    @Test
    @DisplayName("Antes de los 15 minutos la tesis sigue en análisis (no se alarma al docente)")
    void antesDelLimiteSigueEnAnalisis() {
        when(turnitin.enviar(anyString(), any())).thenReturn("envio-001");
        when(turnitin.consultarPorcentaje("envio-001")).thenReturn(Optional.empty());

        casoDeUso.registrar(tesis);
        reloj.avanzar(QUINCE_MINUTOS.minusSeconds(1));
        ResultadoVerificacion resultado = casoDeUso.revisar(tesis);

        assertThat(resultado.estado()).isEqualTo(EstadoTesis.EN_ANALISIS);
    }

    // ---------------------------------------------------------------------------------
    // Regla del 20 %: "excede" = mayor estricto
    // ---------------------------------------------------------------------------------

    @Test
    @DisplayName("Con 21 % de similitud la tesis se rechaza")
    void conVeintiunoSeRechaza() {
        when(turnitin.enviar(anyString(), any())).thenReturn("envio-001");
        when(turnitin.consultarPorcentaje("envio-001")).thenReturn(Optional.of(21));

        casoDeUso.registrar(tesis);
        ResultadoVerificacion resultado = casoDeUso.revisar(tesis);

        assertThat(resultado.estado()).isEqualTo(EstadoTesis.RECHAZADA_POR_SIMILITUD);
        assertThat(tesis.porcentajeSimilitud()).isEqualTo(21);
    }

    @Test
    @DisplayName("Con exactamente 20 % la tesis pasa el filtro")
    void conVeinteExactoPasa() {
        when(turnitin.enviar(anyString(), any())).thenReturn("envio-001");
        when(turnitin.consultarPorcentaje("envio-001")).thenReturn(Optional.of(20));

        casoDeUso.registrar(tesis);
        ResultadoVerificacion resultado = casoDeUso.revisar(tesis);

        assertThat(resultado.estado()).isEqualTo(EstadoTesis.SIMILITUD_APROBADA);
    }

    // ---------------------------------------------------------------------------------
    // Turnitin caído al momento de subir la tesis
    // ---------------------------------------------------------------------------------

    @Test
    @DisplayName("Si Turnitin está caído al subir la tesis, queda pendiente y no se pierde")
    void turnitinCaidoAlSubir() {
        when(turnitin.enviar(anyString(), any()))
                .thenThrow(new ServicioSimilitudNoDisponibleException("caído"));

        ResultadoVerificacion resultado = casoDeUso.registrar(tesis);

        assertThat(resultado.estado()).isEqualTo(EstadoTesis.PENDIENTE_VERIFICACION);
        assertThat(resultado.mensaje()).isEqualTo(VerificarSimilitudTesis.MSG_NO_DISPONIBLE);
        verify(turnitin, never()).consultarPorcentaje(anyString());
    }

    @Test
    @DisplayName("Cuando Turnitin vuelve, la tesis pendiente por caída se envía una sola vez")
    void turnitinVuelveYSeEnvia() {
        when(turnitin.enviar(anyString(), any()))
                .thenThrow(new ServicioSimilitudNoDisponibleException("caído"))
                .thenReturn("envio-002");

        casoDeUso.registrar(tesis);                       // falla: queda pendiente
        ResultadoVerificacion resultado = casoDeUso.revisar(tesis);   // reintento automático

        assertThat(resultado.estado()).isEqualTo(EstadoTesis.EN_ANALISIS);
        assertThat(tesis.idEnvio()).isEqualTo("envio-002");
        verify(turnitin, times(2)).enviar(anyString(), any());
    }
}
