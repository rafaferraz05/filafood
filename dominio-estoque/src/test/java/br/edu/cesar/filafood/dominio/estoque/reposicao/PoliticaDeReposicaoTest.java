package br.edu.cesar.filafood.dominio.estoque.reposicao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;

/** Bordas da RN 6 na política padrão (consumo médio × prazo + mínimo). */
class PoliticaDeReposicaoTest {

    private static final String UNIDADE = "unidade-teste";
    private static final LocalDate HOJE = LocalDate.now();

    private final PoliticaDeReposicao politica = new ConsumoMedioSobrePrazoEEstoqueMinimo();

    @Test
    void pontoDePedidoEhConsumoMedioVezesPrazoMaisMinimo() {
        Insumo insumo = insumo("5", 3);
        insumo.receber(new BigDecimal("18.200"), BigDecimal.ONE);
        insumo.consumir(new BigDecimal("12"), "pedido");

        assertEquals(0, new BigDecimal("6.2").compareTo(politica.pontoDePedido(insumo, HOJE)));
    }

    @Test
    void ficaAReporComSaldoIgualAoPontoDePedido() {
        Insumo insumo = insumo("5", 3);
        insumo.receber(new BigDecimal("18.200"), BigDecimal.ONE);
        insumo.consumir(new BigDecimal("12"), "pedido");

        assertTrue(politica.estaARepor(insumo, HOJE));
    }

    @Test
    void ficaNormalComSaldoAcimaDoPontoDePedido() {
        Insumo insumo = insumo("5", 3);
        insumo.receber(new BigDecimal("18.201"), BigDecimal.ONE);
        insumo.consumir(new BigDecimal("12"), "pedido");

        assertFalse(politica.estaARepor(insumo, HOJE));
    }

    @Test
    void insumoInativoNaoEhMonitorado() {
        Insumo insumo = insumo("5", 3);
        insumo.receber(new BigDecimal("2"), BigDecimal.ONE);
        insumo.desativar();

        assertFalse(politica.estaARepor(insumo, HOJE), "insumo inativo não entra na conta de reposição");
    }

    @Test
    void semSaidasNosUltimos30DiasOPontoEhOMinimo() {
        Insumo insumo = insumo("5", 3);

        assertEquals(0, new BigDecimal("5").compareTo(politica.pontoDePedido(insumo, HOJE)));
    }

    @Test
    void semReposicaoNuncaSinaliza() {
        Insumo insumo = insumo("5", 3);
        insumo.receber(new BigDecimal("1"), BigDecimal.ONE);
        PoliticaDeReposicao semReposicao = new SemReposicao();

        assertEquals(0, BigDecimal.ZERO.compareTo(semReposicao.pontoDePedido(insumo, HOJE)));
        assertFalse(semReposicao.estaARepor(insumo, HOJE));
    }

    private static Insumo insumo(String estoqueMinimo, int prazoDeEntregaEmDias) {
        return new Insumo(InsumoId.novo(), UNIDADE, "Pão", "un", new BigDecimal(estoqueMinimo),
                prazoDeEntregaEmDias);
    }
}