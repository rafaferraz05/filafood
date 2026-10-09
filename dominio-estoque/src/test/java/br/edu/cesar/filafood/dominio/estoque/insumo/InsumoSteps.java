package br.edu.cesar.filafood.dominio.estoque.insumo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.cesar.filafood.dominio.estoque.EstoqueDaUnidade;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;


// Passos do cadastro de insumo (F1 RN 1).

public class InsumoSteps {

    private final EstoqueDaUnidade estoque = new EstoqueDaUnidade();
    private RegraDeNegocioException recusa;

    @Quando("o gestor da unidade cadastra o insumo {string} com unidade de medida {string}, estoque mínimo {int} e prazo de entrega de {int} dias")
    public void cadastrarInsumo(String nome, String unidadeDeMedida, int estoqueMinimo, int prazoDeEntrega) {
        executar(() -> estoque.cadastrarInsumo(nome, unidadeDeMedida, estoqueMinimo, prazoDeEntrega));
    }

    @Quando("o gestor da unidade cadastra um insumo com nome vazio, unidade de medida {string}, estoque mínimo {int} e prazo de entrega de {int} dias")
    public void cadastrarInsumoSemNome(String unidadeDeMedida, int estoqueMinimo, int prazoDeEntrega) {
        executar(() -> estoque.cadastrarInsumo("", unidadeDeMedida, estoqueMinimo, prazoDeEntrega));
    }

    @Dado("o insumo {string} cadastrado e com movimentações de estoque registradas")
    public void insumoComMovimentacoes(String nome) {
        estoque.cadastrarInsumo(nome, "kg", 5, 3).receber(10, 4.00);
    }

    @Dado("o insumo {string} inativo")
    public void insumoInativo(String nome) {
        estoque.cadastrarInsumo(nome, "kg", 5, 3).desativar();
    }

    @Quando("o gestor da unidade desativa o insumo {string}")
    public void desativar(String nome) {
        executar(() -> estoque.insumo(nome).desativar());
    }

    @Quando("o gestor da unidade reativa o insumo {string}")
    public void reativar(String nome) {
        executar(() -> estoque.insumo(nome).reativar());
    }

    @Então("o insumo {string} fica ativo")
    public void insumoFicaAtivo(String nome) {
        semRecusa();
        assertTrue(estoque.insumo(nome).isAtivo(), "esperava o insumo ativo");
    }

    @Então("o insumo {string} fica inativo")
    public void insumoFicaInativo(String nome) {
        semRecusa();
        assertFalse(estoque.insumo(nome).isAtivo(), "esperava o insumo inativo");
    }

    @Então("o saldo do insumo {string} fica zero")
    public void saldoFicaZero(String nome) {
        semRecusa();
        assertEquals(0, estoque.insumo(nome).saldo(), "esperava saldo zero");
    }

    @Então("o histórico de movimentações do insumo {string} continua consultável")
    public void historicoConsultavel(String nome) {
        semRecusa();
        assertFalse(estoque.insumo(nome).movimentacoes().isEmpty(), "esperava movimentações no histórico");
    }

    @Então("o cadastro é recusado com a mensagem {string}")
    public void cadastroRecusado(String mensagem) {
        assertNotNull(recusa, "esperava uma recusa, mas o cadastro foi aceito");
        assertEquals(mensagem, recusa.getMessage());
    }

    @Então("nenhum insumo é cadastrado")
    public void nenhumInsumo() {
        assertEquals(0, estoque.quantidadeDeInsumos(), "esperava o estoque sem insumos");
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
            recusa = null;
        } catch (RegraDeNegocioException e) {
            recusa = e;
        }
    }

    private void semRecusa() {
        assertNull(recusa);
    }

    private static void assertNull(Object valor) {
        org.junit.jupiter.api.Assertions.assertNull(valor, "o sistema recusou a operação e não deveria");
    }
}
