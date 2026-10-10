package br.edu.cesar.filafood.dominio.estoque.entrada;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.MovimentacaoEstoque;
import br.edu.cesar.filafood.dominio.estoque.suporte.MundoDoEstoque;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos da entrada de insumos por compra (F1 RN 3). */
public class EntradaSteps {

    private final MundoDoEstoque mundo;
    private String insumoDaEntrada;

    public EntradaSteps(MundoDoEstoque mundo) {
        this.mundo = mundo;
    }

    @Dado("o insumo {string} ativo com saldo {int} {string}")
    public void insumoComSaldo(String nome, int saldo, String unidadeDeMedida) {
        mundo.garantirSaldo(nome, saldo);
    }

    @Quando("o gestor da unidade registra uma entrada de {int} {string} do insumo {string} com custo unitário {double}")
    public void registrarEntrada(int quantidade, String unidadeDeMedida, String nome, double custoUnitario) {
        mundo.tentar(() -> mundo.estoque.insumo(nome).receber(quantidade, custoUnitario));
    }

    @Então("o saldo do insumo {string} fica {int} {string}")
    public void saldoFica(String nome, int saldo, String unidadeDeMedida) {
        mundo.semRecusa();
        assertEquals(saldo, mundo.estoque.insumo(nome).saldo());
    }

    @Então("fica registrada uma movimentação de entrada de {int} {string} com custo unitário {double}")
    public void movimentacaoRegistrada(int quantidade, String unidadeDeMedida, double custoUnitario) {
        mundo.semRecusa();
        boolean encontrada = mundo.estoque.insumos().stream()
                .flatMap(insumo -> insumo.movimentacoes().stream())
                .anyMatch(m -> m.tipo() == MovimentacaoEstoque.Tipo.ENTRADA
                        && m.motivo() == MovimentacaoEstoque.Motivo.COMPRA
                        && m.quantidade() == quantidade
                        && m.custoUnitario() == custoUnitario);
        assertTrue(encontrada, "esperava uma movimentação de entrada de " + quantidade + " com custo " + custoUnitario);
    }

    @Então("a entrada é recusada com a mensagem {string}")
    public void entradaRecusada(String mensagem) {
        assertNotNull(mundo.recusa, "esperava uma recusa, mas a entrada foi aceita");
        assertEquals(mensagem, mundo.recusa.getMessage());
    }

    @Dado("uma entrada de {int} {string} registrada para o insumo {string}")
    public void entradaRegistrada(int quantidade, String unidadeDeMedida, String nome) {
        insumoDaEntrada = nome;
        mundo.insumoAtivo(nome).receber(quantidade, 2.00);
    }

    @Quando("o gestor da unidade tenta alterar a quantidade da entrada para {int} {string}")
    public void alterarQuantidadeDaEntrada(int quantidade, String unidadeDeMedida) {
        Insumo insumo = mundo.estoque.insumo(insumoDaEntrada);
        mundo.tentar(() -> insumo.alterarQuantidadeDaMovimentacao(quantidade));
    }

    @Então("a alteração é recusada com a mensagem {string}")
    public void alteracaoRecusada(String mensagem) {
        assertNotNull(mundo.recusa, "esperava a recusa da alteração");
        assertEquals(mensagem, mundo.recusa.getMessage());
    }
}
