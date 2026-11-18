package br.edu.cesar.filafood.dominio.cardapio;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import br.edu.cesar.filafood.dominio.cardapio.suporte.MundoDoCardapio;
import io.cucumber.java.pt.*;

public class ProdutoSteps {

    private final MundoDoCardapio mundo;

    public ProdutoSteps(MundoDoCardapio mundo) {
        this.mundo = mundo;
    }

    @Dado("que o produto {string} está cadastrado na categoria {string} com preço base de {double} reais")
    public void produtoCadastrado(String nome, String categoria, double precoBase) {
        mundo.produto = new Produto(ProdutoId.novo(), "U01", nome, categoria, "", BigDecimal.valueOf(precoBase));
    }

    @Quando("o gestor cadastra o produto {string} na categoria {string} com descrição {string} e preço base de {double} reais")
    public void cadastrar(String nome, String categoria, String descricao, double precoBase) {
        mundo.tentar(() -> mundo.produto = new Produto(
                ProdutoId.novo(), "U01", nome, categoria, descricao, BigDecimal.valueOf(precoBase)));
    }

    @Quando("o gestor edita o produto para nome {string}, categoria {string}, descrição {string} e preço base de {double} reais")
    public void editar(String nome, String categoria, String descricao, double precoBase) {
        mundo.tentar(() -> mundo.produto.alterarDados(nome, categoria, descricao, BigDecimal.valueOf(precoBase)));
    }

    @Quando("o gestor desativa o produto")
    public void desativar() {
        mundo.produto.desativar();
    }

    @Quando("o gestor reativa o produto")
    public void reativar() {
        mundo.produto.reativar();
    }

    @Então("o produto {string} fica ativo com preço base de {double} reais")
    public void conferirProdutoCriado(String nome, double precoEsperado) {
        assertNull(mundo.erro);
        assertEquals(nome, mundo.produto.nome());
        assertTrue(mundo.produto.isAtivo());
        assertEquals(0, mundo.produto.precoBase().compareTo(BigDecimal.valueOf(precoEsperado)));
    }

    @Então("o produto passa a se chamar {string} com preço base de {double} reais")
    public void conferirProdutoEditado(String nome, double precoEsperado) {
        assertNull(mundo.erro);
        assertEquals(nome, mundo.produto.nome());
        assertEquals(0, mundo.produto.precoBase().compareTo(BigDecimal.valueOf(precoEsperado)));
    }

    @Então("o produto fica inativo")
    public void ficaInativo() {
        assertFalse(mundo.produto.isAtivo());
    }

    @Então("o produto fica ativo")
    public void ficaAtivo() {
        assertTrue(mundo.produto.isAtivo());
    }

    @Então("a operação do produto é recusada por {string}")
    public void operacaoRecusada(String mensagem) {
        assertEquals(mensagem, mundo.erro);
    }
}