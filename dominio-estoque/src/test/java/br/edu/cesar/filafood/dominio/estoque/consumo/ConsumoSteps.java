package br.edu.cesar.filafood.dominio.estoque.consumo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.suporte.MundoDoEstoque;
import br.edu.cesar.filafood.dominio.estoque.suporte.PedidoEmTeste;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos da baixa automática por explosão da ficha técnica (F1 RN 4). */
public class ConsumoSteps {

    private final MundoDoEstoque mundo;

    public ConsumoSteps(MundoDoEstoque mundo) {
        this.mundo = mundo;
    }

    @Dado("que o produto {string} tem ficha técnica com {int} {string} e {int} {string} na unidade")
    public void fichaTecnicaComDoisItens(String produto, int primeiraQuantidade, String primeiroInsumo,
            int segundaQuantidade, String segundoInsumo) {
        FichaTecnica ficha = mundo.fichas.definir(produto);
        ficha.definirItem(mundo.insumoAtivo(primeiroInsumo), primeiraQuantidade);
        ficha.definirItem(mundo.insumoAtivo(segundoInsumo), segundaQuantidade);
    }

    @Dado("o pedido tem {int} {string}")
    public void pedidoComItem(int quantidade, String produto) {
        if (mundo.pedido == null) {
            mundo.pedido = new PedidoEmTeste("P1");
        }
        mundo.pedido.adicionarItem(produto, quantidade);
    }

    @Dado("o saldo do insumo {string} igual a {int} e o saldo do insumo {string} igual a {int}")
    public void saldosIguais(String primeiroInsumo, int primeiroSaldo, String segundoInsumo, int segundoSaldo) {
        mundo.garantirSaldo(primeiroInsumo, primeiroSaldo);
        mundo.garantirSaldo(segundoInsumo, segundoSaldo);
    }

    @Quando("a unidade inicia o preparo do pedido")
    public void iniciarPreparo() {
        mundo.iniciarPreparoDoPedido();
    }

    @Então("o pedido fica em preparo")
    public void pedidoEmPreparo() {
        mundo.semRecusa();
        assertEquals(EstadoDoPedido.EM_PREPARO, mundo.pedido.estado());
    }

    @Então("o pedido é recusado com o motivo {string}")
    public void pedidoRecusado(String motivo) {
        assertNotNull(mundo.recusa, "esperava a recusa do pedido");
        assertEquals(motivo, mundo.recusa.getMessage());
        assertEquals(EstadoDoPedido.RECUSADO, mundo.pedido.estado());
    }

    @Então("o saldo do insumo {string} fica {int}")
    public void saldoFica(String nome, int saldo) {
        mundo.semRecusa();
        assertEquals(saldo, mundo.estoque.insumo(nome).saldo());
    }

    @Então("o saldo do insumo {string} continua {int}")
    public void saldoContinua(String nome, int saldo) {
        assertEquals(saldo, mundo.estoque.insumo(nome).saldo());
    }
}
