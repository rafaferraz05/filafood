package br.edu.cesar.filafood.dominio.estoque.ficha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import br.edu.cesar.filafood.dominio.estoque.suporte.MundoDoEstoque;
import br.edu.cesar.filafood.dominio.estoque.suporte.PedidoEmTeste;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos da ficha técnica do produto (F1 RN 2). */
public class FichaSteps {

    private final MundoDoEstoque mundo;

    public FichaSteps(MundoDoEstoque mundo) {
        this.mundo = mundo;
    }

    @Dado("que o produto {string} existe no cardápio da rede")
    public void produtoNoCardapio(String produto) {
        // O cardápio da rede é do contexto de outro integrante. Este contexto guarda apenas a ficha
        // técnica do produto nesta unidade, então o passo só declara que o produto existe na rede.
    }

    @Dado("o insumo {string} ativo na unidade")
    public void insumoAtivo(String nome) {
        mundo.insumoAtivo(nome);
    }

    @Dado("o insumo {string} ativo e o insumo {string} ativo na unidade")
    public void doisInsumosAtivos(String primeiroInsumo, String segundoInsumo) {
        mundo.insumoAtivo(primeiroInsumo);
        mundo.insumoAtivo(segundoInsumo);
    }

    @Dado("o insumo {string} inativo na unidade")
    public void insumoInativo(String nome) {
        mundo.insumoAtivo(nome).desativar();
    }

    @Quando("o gestor da unidade define a ficha técnica do produto {string} com {int} {string} e {int} {string}")
    public void definirComDoisItens(String produto, int primeiraQuantidade, String primeiroInsumo,
            int segundaQuantidade, String segundoInsumo) {
        mundo.tentar(() -> {
            FichaTecnica ficha = mundo.fichas.definir(produto);
            ficha.definirItem(mundo.insumoAtivo(primeiroInsumo), primeiraQuantidade);
            ficha.definirItem(mundo.insumoAtivo(segundoInsumo), segundaQuantidade);
        });
    }

    @Quando("o gestor da unidade define a ficha técnica do produto {string} com {int} {string}")
    public void definirComUmItem(String produto, int quantidade, String insumo) {
        mundo.tentar(() -> mundo.fichas.definir(produto).definirItem(mundo.insumoAtivo(insumo), quantidade));
    }

    @Então("a ficha técnica do produto {string} na unidade fica com {int} itens")
    public void fichaComItens(String produto, int itens) {
        mundo.semRecusa();
        assertEquals(itens, mundo.fichas.exigirFichaDe(produto).quantidadeDeItens());
    }

    @Então("a ficha técnica é recusada com a mensagem {string}")
    public void fichaRecusada(String mensagem) {
        assertNotNull(mundo.recusa, "esperava uma recusa, mas a ficha técnica foi aceita");
        assertEquals(mensagem, mundo.recusa.getMessage());
    }

    @Dado("que o produto {string} não tem ficha técnica na unidade")
    public void produtoSemFicha(String produto) {
        assertFalse(mundo.fichas.temFichaDe(produto), "esperava o produto sem ficha técnica");
    }

    @Quando("a unidade inicia o preparo de um pedido com {int} {string}")
    public void iniciarPreparoDePedidoCom(int quantidade, String produto) {
        mundo.pedido = new PedidoEmTeste("P1");
        mundo.pedido.adicionarItem(produto, quantidade);
        mundo.iniciarPreparoDoPedido();
    }
}
