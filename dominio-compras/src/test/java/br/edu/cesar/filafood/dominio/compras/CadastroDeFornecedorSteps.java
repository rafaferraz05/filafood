package br.edu.cesar.filafood.dominio.compras;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import br.edu.cesar.filafood.dominio.compras.suporte.MundoDasCompras;
import io.cucumber.java.Before;
import io.cucumber.java.pt.*;

/** Passos da F7 RN 1 (cadastro de fornecedor) e RN 2 (catálogo do fornecedor). */
public class CadastroDeFornecedorSteps {

    private final MundoDasCompras mundo;

    public CadastroDeFornecedorSteps(MundoDasCompras mundo) {
        this.mundo = mundo;
    }

    @Before
    public void limpar() {
        mundo.fornecedor = null;
        mundo.insumos.clear();
        mundo.erro = null;
    }

    @Dado("que o fornecedor {string} está cadastrado")
    public void fornecedorCadastrado(String nome) {
        mundo.fornecedor = new Fornecedor(FornecedorId.novo(), nome, "11222333000181", "81999990000", "");
    }

    @Quando("o gestor cadastra o fornecedor {string} com CNPJ {string}, telefone {string} e e-mail {string}")
    public void cadastrar(String nome, String cnpj, String telefone, String email) {
        mundo.tentar(() -> mundo.fornecedor = new Fornecedor(FornecedorId.novo(), nome, cnpj, telefone, email));
    }

    @Quando("o gestor desativa o fornecedor")
    public void desativar() {
        mundo.fornecedor.desativar();
    }

    @Quando("o gestor reativa o fornecedor")
    public void reativar() {
        mundo.fornecedor.reativar();
    }

    @Então("o fornecedor {string} fica ativo com CNPJ {string}")
    public void ficaAtivoComCnpj(String nome, String cnpj) {
        assertNull(mundo.erro);
        assertEquals(nome, mundo.fornecedor.nome());
        assertEquals(cnpj, mundo.fornecedor.cnpj().valor());
        assertTrue(mundo.fornecedor.isAtivo());
    }

    @Então("o fornecedor fica inativo")
    public void ficaInativo() {
        assertFalse(mundo.fornecedor.isAtivo());
    }

    @Então("o fornecedor fica ativo")
    public void ficaAtivo() {
        assertTrue(mundo.fornecedor.isAtivo());
    }

    @Dado("o insumo {string} está {string} no estoque")
    public void insumoNoEstoque(String nome, String estado) {
        mundo.insumos.put(nome, new MundoDasCompras.InsumoDoEstoque(java.util.UUID.randomUUID(), estado.equals("ativo")));
    }

    @Dado("o gestor inclui {string} no catálogo com preço {double}, mínimo {int} e prazo {int} dia(s)")
    public void incluir(String insumo, double preco, int minimo, int prazo) {
        MundoDasCompras.InsumoDoEstoque doEstoque = mundo.insumo(insumo);
        mundo.tentar(() -> mundo.fornecedor.incluirItem(doEstoque.id(), doEstoque.ativo(),
                BigDecimal.valueOf(preco), BigDecimal.valueOf(minimo), prazo));
    }

    @Quando("o gestor altera o item de {string} para preço {double}, mínimo {int} e prazo {int} dia(s)")
    public void alterar(String insumo, double preco, int minimo, int prazo) {
        ItemDoCatalogo item = itemDe(insumo);
        mundo.tentar(() -> mundo.fornecedor.alterarItem(item.id(), BigDecimal.valueOf(preco),
                BigDecimal.valueOf(minimo), prazo));
    }

    @Quando("o gestor remove {string} do catálogo")
    public void remover(String insumo) {
        ItemDoCatalogo item = itemDe(insumo);
        mundo.tentar(() -> mundo.fornecedor.removerItem(item.id()));
    }

    @Então("o catálogo do fornecedor tem {int} item/itens")
    public void tamanhoDoCatalogo(int quantidade) {
        assertEquals(quantidade, mundo.fornecedor.catalogo().size());
    }

    @Então("o item de {string} tem preço {double}, mínimo {int} e prazo {int} dia(s)")
    public void conferirItem(String insumo, double preco, int minimo, int prazo) {
        assertNull(mundo.erro);
        ItemDoCatalogo item = itemDe(insumo);
        assertEquals(0, BigDecimal.valueOf(preco).compareTo(item.precoUnitario()));
        assertEquals(0, BigDecimal.valueOf(minimo).compareTo(item.quantidadeMinima()));
        assertEquals(prazo, item.prazoDeEntregaEmDias());
    }

    @Então("a operação de compras é recusada por {string}")
    public void recusada(String mensagem) {
        assertEquals(mensagem, mundo.erro);
    }

    private ItemDoCatalogo itemDe(String insumo) {
        return mundo.fornecedor.itemDoInsumo(mundo.insumo(insumo).id())
                .orElseThrow(() -> new AssertionError("o catálogo não tem item de " + insumo));
    }
}
