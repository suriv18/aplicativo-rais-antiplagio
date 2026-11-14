package com.softprimesolutions.aplicativoraisantiplagio.aceptacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import com.softprimesolutions.aplicativoraisantiplagio.aplicacion.ResultadoVerificacion;
import com.softprimesolutions.aplicativoraisantiplagio.aplicacion.VerificarSimilitudTesis;
import com.softprimesolutions.aplicativoraisantiplagio.dobles.RelojControlable;
import com.softprimesolutions.aplicativoraisantiplagio.dobles.TurnitinSimulado;
import com.softprimesolutions.aplicativoraisantiplagio.dominio.PoliticaSimilitud;
import com.softprimesolutions.aplicativoraisantiplagio.dominio.Tesis;

/**
 * Pasos de Cucumber. Conectan cada frase del escenario con el código,
 * usando el Turnitin simulado y el reloj controlable (nunca el Turnitin real).
 */
public class PasosFiltroAntiplagio {

    private final TurnitinSimulado turnitin = new TurnitinSimulado();
    private final RelojControlable reloj = new RelojControlable(Instant.parse("2026-11-13T09:00:00Z"));

    private int umbral;
    private Duration tiempoMaximo;
    private VerificarSimilitudTesis casoDeUso;
    private Tesis tesis;
    private ResultadoVerificacion ultimoResultado;

    // ---------- Antecedentes ----------

    @Dado("que el umbral máximo de similitud permitido es {int} %")
    public void elUmbralEs(int porcentaje) {
        umbral = porcentaje;
    }

    @Dado("que el tiempo máximo de espera del análisis es de {int} minutos")
    public void elTiempoMaximoEs(int minutos) {
        tiempoMaximo = Duration.ofMinutes(minutos);
    }

    // ---------- Dado ----------

    @Dado("que el docente subió el PDF de su tesis {string}")
    public void elDocenteSubio(String archivo) {
        subirTesis(archivo);
    }

    @Dado("la tesis fue enviada a Turnitin para el análisis de similitud")
    public void laTesisFueEnviada() {
        assertThat(turnitin.envios()).isEqualTo(1);
        assertThat(tesis.fueEnviada()).isTrue();
    }

    @Dado("que Turnitin no está disponible")
    public void turnitinNoDisponible() {
        turnitin.simularCaida();
    }

    // ---------- Cuando ----------

    @Cuando("el docente sube el PDF de su tesis {string}")
    public void elDocenteSube(String archivo) {
        subirTesis(archivo);
    }

    @Cuando("transcurren {int} minutos sin que Turnitin entregue el porcentaje de similitud")
    public void transcurrenMinutosSinRespuesta(int minutos) {
        turnitin.simularDemora();
        reloj.avanzar(Duration.ofMinutes(minutos));   // 15 minutos "pasan" en un instante
        ultimoResultado = casoDeUso.revisar(tesis);
    }

    @Cuando("Turnitin informa una similitud de {int} %")
    public void turnitinInforma(int porcentaje) {
        turnitin.responderCon(porcentaje);
        ultimoResultado = casoDeUso.revisar(tesis);
    }

    // ---------- Entonces ----------

    @Entonces("el sistema debe mostrar el mensaje {string}")
    public void debeMostrarMensaje(String mensaje) {
        assertThat(ultimoResultado.mensaje()).isEqualTo(mensaje);
    }

    @Entonces("la tesis debe quedar en estado {string}")
    public void debeQuedarEnEstado(String estado) {
        assertThat(tesis.estado().etiqueta()).isEqualTo(estado);
    }

    @Entonces("la tesis no debe ser aprobada ni rechazada")
    public void noDebeSerAprobadaNiRechazada() {
        assertThat(tesis.estado().esDecisionFinal()).isFalse();
    }

    @Entonces("el sistema debe volver a consultar el resultado sin enviar nuevamente el documento a Turnitin")
    public void vuelveAConsultarSinReenviar() {
        int consultasAntes = turnitin.consultas();

        reloj.avanzar(Duration.ofMinutes(5));
        casoDeUso.revisar(tesis);   // siguiente revisión automática

        assertThat(turnitin.consultas()).isGreaterThan(consultasAntes);
        assertThat(turnitin.envios()).isEqualTo(1);   // un solo envío = una sola consulta pagada
    }

    // ---------- Apoyo ----------

    private void subirTesis(String archivo) {
        casoDeUso = new VerificarSimilitudTesis(turnitin, new PoliticaSimilitud(umbral, tiempoMaximo), reloj);
        tesis = new Tesis(archivo, new byte[] {1, 2, 3});
        ultimoResultado = casoDeUso.registrar(tesis);
    }
}
