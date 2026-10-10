package br.edu.cesar.filafood.dominio.estoque.devolucao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.insumo.MovimentacaoEstoque;
import br.edu.cesar.filafood.dominio.estoque.suporte.MundoDoEstoque;
import br.edu.cesar.filafood.dominio.estoque.suporte.PedidoEmTeste;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos da devolução ao cancelar o preparo (F1 RN 5). */
public class DevolucaoSteps {

    private static final String PEDIDO = "P1";

    private final MundoDoEstoque mundo;

    public DevolucaoSteps(MundoDoEstoque mundo) {
        this.mundo = mundo;
    }

    @Dado("um pedido em preparo com a baixa de {int} {string} e {int} {string} já lançada")
    public void pedidoEmPreparoComBaixa(int primeiraQuantidade, String primeiroInsumo,
            int segundaQuantidade, String segundoInsumo) {
        mundo.pedido = new PedidoEmTeste(PEDIDO);
        mundo.pedido.marcarEmPreparo();
        lancarBaixa(primeiroInsumo, primeiraQuantidade);
        lancarBaixa(segundoInsumo, segundaQuantidade);
    }

    @Dado("um pedido concluído")
    public void pedidoConcluido() {
        pedidoJaBaixado(EstadoDoPedido.CONCLUIDO);
    }

    @Dado("um pedido recusado")
    public void pedidoRecusado() {
        pedidoJaBaixado(EstadoDoPedido.RECUSADO);
    }

    @Dado("um pedido cancelado")
    public void pedidoCancelado() {
        pedidoJaBaixado(EstadoDoPedido.CANCELADO);
    }

    @Quando("a unidade cancela o preparo do pedido")
    public void cancelarPreparo() {
        mundo.tentar(() -> mundo.devolucao.devolver(PEDIDO, mundo.pedido.estado()));
    }

    @Quando("a unidade cancela o pedido")
    public void cancelarPedido() {
        mundo.tentar(() -> mundo.devolucao.devolver(PEDIDO, mundo.pedido.estado()));
    }

    @Quando("a unidade tenta devolver os insumos do pedido")
    public void tentarDevolver() {
        mundo.tentar(() -> mundo.devolucao.devolver(PEDIDO, mundo.pedido.estado()));
    }

    @Então("fica registrada uma entrada do tipo devolução de {int} {string}")
    public void entradaDevolucao(int quantidade, String insumo) {
        mundo.semRecusa();
        boolean encontrada = mundo.estoque.insumo(insumo).movimentacoes().stream()
                .anyMatch(m -> m.tipo() == MovimentacaoEstoque.Tipo.ENTRADA
                        && m.motivo() == MovimentacaoEstoque.Motivo.DEVOLUCAO
                        && m.quantidade() == quantidade);
        assertTrue(encontrada, "esperava uma devolução de " + quantidade + " de " + insumo);
    }

    @Então("o saldo do insumo {string} volta ao valor anterior à baixa")
    public void saldoVolta(String insumo) {
        mundo.semRecusa();
        assertEquals(mundo.saldoAntesDaBaixa.get(insumo),
                mundo.estoque.insumo(insumo).saldo(), 0.001);
    }

    @Então("nenhuma movimentação de devolução é registrada")
    public void nenhumaDevolucao() {
        boolean existe = mundo.estoque.insumos().stream()
                .flatMap(insumo -> insumo.movimentacoes().stream())
                .anyMatch(m -> m.motivo() == MovimentacaoEstoque.Motivo.DEVOLUCAO);
        assertFalse(existe, "esperava nenhuma movimentação de devolução");
    }

    /** Lança a saída do insumo guardando o saldo anterior, para conferir a volta depois. */
    private void lancarBaixa(String insumo, int quantidade) {
        mundo.garantirSaldo(insumo, 10);
        mundo.saldoAntesDaBaixa.put(insumo, mundo.estoque.insumo(insumo).saldo());
        mundo.estoque.insumo(insumo).consumir(quantidade, PEDIDO);
    }

    /**
     * Monta um pedido já com a baixa lançada, no estado informado. A baixa é lançada de propósito
     * para que a verificação de "nenhuma devolução" tenha o que recusar.
     */
    private void pedidoJaBaixado(EstadoDoPedido estado) {
        mundo.pedido = new PedidoEmTeste(PEDIDO);
        mundo.garantirSaldo("Pão", 10);
        mundo.estoque.insumo("Pão").consumir(3, PEDIDO);
        switch (estado) {
            case CONCLUIDO -> mundo.pedido.concluir();
            case RECUSADO -> mundo.pedido.recusar("recusado pelo teste");
            case CANCELADO -> mundo.pedido.cancelar();
            default -> throw new IllegalArgumentException("estado não previsto: " + estado);
        }
    }
}
