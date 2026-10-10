package br.edu.cesar.filafood.dominio.entrega.taxa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.cesar.filafood.dominio.entrega.suporte.MundoDaEntrega;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaDeEntrega;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos do cálculo da taxa de entrega (F2 RN 3). */
public class TaxaSteps {

    private final MundoDaEntrega mundo;
    private ZonaDeEntrega zonaCalculada;
    private double taxaCalculada;

    public TaxaSteps(MundoDaEntrega mundo) {
        this.mundo = mundo;
    }

    @Dado("um pedido de {double} com endereço a {double} km do centro das duas zonas")
    public void pedidoNoCentroComum(double valorDoPedido, double distanciaKm) {
        mundo.pedidoPronto("P1", valorDoPedido,
                MundoDaEntrega.aoNorte(MundoDaEntrega.CENTRO_PADRAO, distanciaKm));
    }

    @Dado("um pedido de {double} com endereço a {double} km do centro da zona {string}")
    public void pedidoNoCentroDaZona(double valorDoPedido, double distanciaKm, String zona) {
        mundo.pedidoPronto("P1", valorDoPedido, MundoDaEntrega.aoNorte(mundo.zona(zona).centro(), distanciaKm));
    }

    @Quando("o sistema calcula a taxa de entrega do pedido")
    public void calcularTaxa() {
        zonaCalculada = mundo.zonas.zonaDoEndereco(mundo.pedido.endereco());
        taxaCalculada = zonaCalculada.taxaPara(mundo.pedido.endereco(), mundo.pedido.valorDoPedido());
    }

    @Então("a taxa de entrega é calculada pela zona {string}")
    public void calculadaPelaZona(String zona) {
        assertNotNull(zonaCalculada, "esperava a taxa calculada antes de conferir a zona");
        assertEquals(zona, zonaCalculada.nome());
    }

    @Então("a taxa de entrega é {double}")
    public void taxaEh(double esperada) {
        assertEquals(esperada, taxaCalculada, 0.001);
    }

    @Dado("um pedido despachado com taxa de entrega {double} pela zona {string}")
    public void pedidoDespachado(double taxaEsperada, String zona) {
        mundo.pedidoPronto("P1", 25.00, MundoDaEntrega.aoNorte(mundo.zona(zona).centro(), 4));
        mundo.garantirEntregador("Ana", zona, 3);
        assertTrue(mundo.despacharPedido(), "esperava o pedido despachado");
        assertEquals(taxaEsperada, mundo.entrega.taxa(), 0.001, "a taxa gravada deveria ser a do cenário");
    }

    @Quando("o gestor da unidade altera o valor base da zona {string} para {double}")
    public void alterarValorBase(String zona, double valorBase) {
        mundo.zona(zona).alterarValorBase(valorBase);
    }

    @Então("a taxa de entrega do pedido continua {double}")
    public void taxaContinua(double esperada) {
        assertEquals(esperada, mundo.entrega.taxa(), 0.001);
    }
}
