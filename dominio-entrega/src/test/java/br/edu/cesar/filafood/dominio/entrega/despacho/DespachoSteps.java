package br.edu.cesar.filafood.dominio.entrega.despacho;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;
import br.edu.cesar.filafood.dominio.entrega.suporte.MundoDaEntrega;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos do despacho para o entregador elegível de menor carga (F2 RN 4). */
public class DespachoSteps {

    private final MundoDaEntrega mundo;
    private boolean despachou;

    public DespachoSteps(MundoDaEntrega mundo) {
        this.mundo = mundo;
    }

    @Dado("um pedido pronto com endereço na zona {string}")
    public void pedidoProntoNaZona(String zona) {
        mundo.garantirZona(zona, 5, 5.00, 2, 1.50, 10.00, 30.00);
        mundo.pedidoPronto("P1", 25.00, MundoDaEntrega.aoNorte(MundoDaEntrega.CENTRO_PADRAO, 2));
        assertEquals(zona, mundo.entrega.zona().nome(), "esperava o endereço na zona do cenário");
    }

    @Dado("o entregador {string} ativo, que atende a zona {string}, com carga máxima {int} e carga em andamento {int}")
    public void entregadorElegivel(String nome, String zona, int cargaMaxima, int cargaEmAndamento) {
        Entregador entregador = mundo.garantirEntregador(nome, zona, cargaMaxima);
        MundoDaEntrega.assumir(cargaEmAndamento, entregador);
    }

    @Dado("o entregador {string} ativo, que não atende a zona {string}, com carga máxima {int} e carga em andamento {int}")
    public void entregadorDeOutraZona(String nome, String zona, int cargaMaxima, int cargaEmAndamento) {
        Entregador entregador = mundo.garantirEntregador(nome, "Outra zona", cargaMaxima);
        MundoDaEntrega.assumir(cargaEmAndamento, entregador);
        assertFalse(entregador.atende(zona), "esperava o entregador fora da zona do cenário");
    }

    @Dado("o entregador {string} inativo, que atende a zona {string}, com carga máxima {int} e carga em andamento {int}")
    public void entregadorInativo(String nome, String zona, int cargaMaxima, int cargaEmAndamento) {
        Entregador entregador = mundo.garantirEntregador(nome, zona, cargaMaxima);
        MundoDaEntrega.assumir(cargaEmAndamento, entregador);
        entregador.desativar();
    }

    @Dado("a última entrega do entregador {string} é mais antiga que a do entregador {string}")
    public void ultimaEntregaMaisAntiga(String maisAntigo, String maisRecente) {
        mundo.entregador(maisRecente).registrarUltimaEntregaEm(LocalDateTime.now());
        mundo.entregador(maisAntigo).registrarUltimaEntregaEm(LocalDateTime.now().minusHours(2));
    }

    @Dado("que não há entregador ativo que atende a zona {string}")
    public void nenhumEntregadorAtivo(String zona) {
        assertTrue(mundo.entregadores.stream().noneMatch(entregador -> entregador.isAtivo() && entregador.atende(zona)),
                "esperava nenhum entregador ativo na zona");
    }

    @Quando("o sistema despacha o pedido")
    public void despachar() {
        despachou = mundo.despacharPedido();
    }

    @Quando("o sistema tenta despachar o pedido")
    public void tentarDespachar() {
        despachou = mundo.despacharPedido();
    }

    @Então("o pedido é despachado para o entregador {string}")
    public void despachadoPara(String nome) {
        assertTrue(despachou, "esperava o pedido despachado");
        assertEquals(nome, mundo.entrega.entregador().nome());
    }

    @Então("o pedido permanece na fila de despacho")
    public void permaneceNaFila() {
        assertFalse(despachou, "não esperava o despacho");
        assertTrue(mundo.fila.contemPedido("P1"), "esperava o pedido na fila de despacho");
    }
}
