package br.edu.cesar.filafood.dominio.entrega.tentativa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.cesar.filafood.dominio.entrega.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;
import br.edu.cesar.filafood.dominio.entrega.entrega.DesfechoDaFalha;
import br.edu.cesar.filafood.dominio.entrega.entrega.Entrega;
import br.edu.cesar.filafood.dominio.entrega.entrega.EstadoDaEntrega;
import br.edu.cesar.filafood.dominio.entrega.suporte.MundoDaEntrega;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos da nova tentativa após falha (F2 RN 6). */
public class TentativaSteps {

    private static final String OBSERVACAO = "observação do cenário";
    private static final String PEDIDO = "P1";

    private final MundoDaEntrega mundo;

    public TentativaSteps(MundoDaEntrega mundo) {
        this.mundo = mundo;
    }

    @Dado("um pedido concluído com uma entrega em rota")
    public void pedidoConcluidoComEntregaEmRota() {
        mundo.garantirZona("Centro", 5, 5.00, 2, 1.50, 10.00, 30.00);
        mundo.cadastrarEntregador("Ana", "Centro", 3);
        mundo.pedidoPronto(PEDIDO, 25.00, MundoDaEntrega.aoNorte(MundoDaEntrega.CENTRO_PADRAO, 2));
        assertTrue(mundo.despacharPedido(), "esperava o pedido despachado");
        mundo.entrega.registrarRetirada();
        assertEquals(EstadoDoPedido.CONCLUIDO, mundo.pedido.estado());
    }

    @Dado("um pedido concluído com a primeira entrega falha e a nova tentativa em rota")
    public void primeiraEntregaFalhaENovaTentativaEmRota() {
        pedidoConcluidoComEntregaEmRota();
        registrarFalha("endereço não encontrado");
        mundo.entrega = mundo.fila.entregaDoPedido(PEDIDO)
                .orElseThrow(() -> new AssertionError("esperava a nova entrega na fila"));
        assertTrue(mundo.despacharPedido(), "esperava a nova tentativa despachada");
        mundo.entrega.registrarRetirada();
    }

    @Dado("o entregador {string} com carga em andamento {int} e uma entrega em rota")
    public void entregadorComCargaEEntregaEmRota(String nome, int cargaEmAndamento) {
        mundo.garantirZona("Centro", 5, 5.00, 2, 1.50, 10.00, 30.00);
        Entregador entregador = mundo.cadastrarEntregador(nome, "Centro", 5);
        // o despacho soma a entrega em rota, então a carga anterior é uma a menos
        MundoDaEntrega.assumir(cargaEmAndamento - 1, entregador);
        mundo.pedidoPronto(PEDIDO, 25.00, MundoDaEntrega.aoNorte(MundoDaEntrega.CENTRO_PADRAO, 2));
        assertTrue(mundo.despacharPedido(), "esperava o pedido despachado");
        mundo.entrega.registrarRetirada();
        assertEquals(cargaEmAndamento, entregador.cargaEmAndamento(), "a carga deveria ser a do cenário");
    }

    @Quando("o entregador registra uma falha com o motivo {string}")
    public void registrarFalha(String motivo) {
        registrarFalhaComObservacao(motivo, OBSERVACAO);
    }

    @Quando("o entregador registra uma falha na nova tentativa com o motivo {string}")
    public void registrarFalhaNaNovaTentativa(String motivo) {
        registrarFalhaComObservacao(motivo, OBSERVACAO);
    }

    @Quando("o entregador registra uma falha com o motivo {string} e sem observação")
    public void registrarFalhaSemObservacao(String motivo) {
        registrarFalhaComObservacao(motivo, "");
    }

    @Quando("o entregador {string} registra uma falha na entrega com o motivo {string}")
    public void registrarFalhaDoEntregador(String nome, String motivo) {
        assertEquals(nome, mundo.entrega.entregador().nome(), "esperava a entrega do entregador do cenário");
        registrarFalhaComObservacao(motivo, OBSERVACAO);
    }

    @Então("o pedido volta para a fila de despacho")
    public void pedidoVoltaParaAFila() {
        mundo.semRecusa();
        assertTrue(mundo.fila.contemPedido(PEDIDO), "esperava o pedido de volta na fila");
    }

    @Então("uma nova entrega do pedido fica em aguardando")
    public void novaEntregaEmAguardando() {
        Entrega nova = mundo.fila.entregaDoPedido(PEDIDO)
                .orElseThrow(() -> new AssertionError("esperava a nova entrega na fila"));
        assertEquals(EstadoDaEntrega.AGUARDANDO, nova.estado());
        assertEquals(2, nova.tentativa());
    }

    @Então("o pedido continua concluído")
    public void pedidoContinuaConcluido() {
        assertEquals(EstadoDoPedido.CONCLUIDO, mundo.pedido.estado());
    }

    @Então("o pedido fica cancelado")
    public void pedidoFicaCancelado() {
        assertEquals(EstadoDoPedido.CANCELADO, mundo.pedido.estado());
    }

    @Então("nenhuma outra entrega é criada para o pedido")
    public void nenhumaOutraEntrega() {
        assertFalse(mundo.fila.contemPedido(PEDIDO), "não esperava outra entrega para o pedido");
    }

    @Então("a carga em andamento do entregador {string} fica {int}")
    public void cargaFica(String nome, int carga) {
        assertEquals(carga, mundo.entregador(nome).cargaEmAndamento());
    }

    /**
     * Registra a falha e aplica o desfecho ao pedido. O pedido pertence ao contexto de pedido, então
     * quem cancela é quem chamou, a partir do desfecho devolvido pelo domínio da entrega.
     */
    private void registrarFalhaComObservacao(String motivo, String observacao) {
        mundo.tentar(() -> {
            DesfechoDaFalha desfecho = mundo.consequencias.registrar(mundo.entrega,
                    MundoDaEntrega.motivo(motivo), observacao);
            if (desfecho == DesfechoDaFalha.PEDIDO_CANCELADO) {
                mundo.pedido.cancelar();
            }
        });
    }
}
