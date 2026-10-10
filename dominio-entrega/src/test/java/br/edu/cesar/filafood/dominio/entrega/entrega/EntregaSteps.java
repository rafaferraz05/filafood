package br.edu.cesar.filafood.dominio.entrega.entrega;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.cesar.filafood.dominio.entrega.suporte.MundoDaEntrega;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos dos estados e do desfecho da entrega (F2 RN 5). */
public class EntregaSteps {

    private static final String OBSERVACAO = "observação do cenário";

    private final MundoDaEntrega mundo;

    public EntregaSteps(MundoDaEntrega mundo) {
        this.mundo = mundo;
    }

    @Dado("uma entrega em aguardando")
    public void umaEntregaEmAguardando() {
        mundo.garantirZona("Centro", 5, 5.00, 2, 1.50, 10.00, 30.00);
        mundo.pedidoPronto("P1", 25.00, MundoDaEntrega.aoNorte(MundoDaEntrega.CENTRO_PADRAO, 2));
        assertEquals(EstadoDaEntrega.AGUARDANDO, mundo.entrega.estado());
    }

    @Dado("uma entrega em rota")
    public void umaEntregaEmRota() {
        ateARota("Ana");
    }

    @Dado("uma entrega em rota com o entregador {string}")
    public void umaEntregaEmRotaCom(String nome) {
        ateARota(nome);
    }

    @Dado("uma entrega entregue")
    public void umaEntregaEntregue() {
        ateARota("Ana");
        mundo.entrega.registrarEntrega("Maria");
    }

    private void ateARota(String nome) {
        umaEntregaEmAguardando();
        mundo.garantirEntregador(nome, "Centro", 3);
        assertTrue(mundo.despacharPedido(), "esperava a entrega despachada");
        mundo.entrega.registrarRetirada();
    }

    @Quando("o sistema despacha a entrega para o entregador {string}")
    public void despacharPara(String nome) {
        mundo.garantirEntregador(nome, "Centro", 3);
        assertTrue(mundo.despacharPedido(), "esperava a entrega despachada");
    }

    @Quando("o entregador {string} registra a retirada")
    public void registrarRetirada(String nome) {
        mundo.tentar(() -> mundo.entrega.registrarRetirada());
    }

    @Quando("o entregador registra a retirada")
    public void registrarRetirada() {
        mundo.tentar(() -> mundo.entrega.registrarRetirada());
    }

    @Quando("o entregador {string} registra a entrega com o recebedor {string}")
    public void registrarEntrega(String nome, String recebedor) {
        mundo.tentar(() -> mundo.entrega.registrarEntrega(recebedor));
    }

    @Quando("o entregador registra a entrega sem informar o recebedor")
    public void registrarEntregaSemRecebedor() {
        mundo.tentar(() -> mundo.entrega.registrarEntrega(""));
    }

    @Quando("o entregador {string} registra uma falha com o motivo {string}")
    public void registrarFalha(String nome, String motivo) {
        mundo.tentar(() -> mundo.consequencias.registrar(mundo.entrega, MundoDaEntrega.motivo(motivo), OBSERVACAO));
    }

    @Quando("o entregador registra uma falha sem informar o motivo")
    public void registrarFalhaSemMotivo() {
        mundo.tentar(() -> mundo.entrega.registrarFalha(null, OBSERVACAO));
    }

    @Quando("o sistema marca a entrega como despachada sem entregador")
    public void despacharSemEntregador() {
        mundo.tentar(() -> mundo.entrega.despachar(null));
    }

    @Então("a entrega fica despachada")
    public void ficaDespachada() {
        mundo.semRecusa();
        assertEquals(EstadoDaEntrega.DESPACHADA, mundo.entrega.estado());
    }

    @Então("a entrega fica em rota")
    public void ficaEmRota() {
        mundo.semRecusa();
        assertEquals(EstadoDaEntrega.EM_ROTA, mundo.entrega.estado());
    }

    @Então("a entrega fica entregue")
    public void ficaEntregue() {
        mundo.semRecusa();
        assertEquals(EstadoDaEntrega.ENTREGUE, mundo.entrega.estado());
    }

    @Então("a entrega fica falha")
    public void ficaFalha() {
        mundo.semRecusa();
        assertEquals(EstadoDaEntrega.FALHA, mundo.entrega.estado());
    }

    @Então("a falha fica registrada com o motivo")
    public void falhaRegistradaComMotivo() {
        assertNotNull(mundo.entrega.motivoDaFalha(), "esperava o motivo da falha registrado");
    }

    @Então("a transição grava data e hora")
    public void transicaoGravaDataEHora() {
        assertFalse(mundo.entrega.transicoes().isEmpty(), "esperava transições registradas");
        TransicaoDaEntrega ultima = mundo.entrega.transicoes().get(mundo.entrega.transicoes().size() - 1);
        assertNotNull(ultima.dataHora(), "esperava data e hora na transição");
    }

    @Então("a transição é recusada com a mensagem {string}")
    public void transicaoRecusada(String mensagem) {
        assertNotNull(mundo.recusa, "esperava a recusa da transição");
        assertEquals(mensagem, mundo.recusa.getMessage());
    }

    @Então("o registro é recusado com a mensagem {string}")
    public void registroRecusado(String mensagem) {
        assertNotNull(mundo.recusa, "esperava a recusa do registro");
        assertEquals(mensagem, mundo.recusa.getMessage());
    }
}
