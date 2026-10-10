package br.edu.cesar.filafood.dominio.estoque.reposicao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.suporte.MundoDoEstoque;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos do ponto de pedido por consumo médio (F1 RN 6). */
public class ReposicaoSteps {

    private static final String CONSUMO_DE_TESTE = "consumo-de-teste";

    private final MundoDoEstoque mundo;

    public ReposicaoSteps(MundoDoEstoque mundo) {
        this.mundo = mundo;
    }

    @Dado("o insumo {string} com {int} {string} de saídas nos últimos 30 dias")
    public void saidasNosUltimos30Dias(String insumo, int quantidade, String unidadeDeMedida) {
        mundo.insumoAtivo(insumo).consumir(quantidade, CONSUMO_DE_TESTE);
    }

    /**
     * As saídas ficam concentradas em alguns dias, mas o consumo médio continua dividido por 30.
     * É exatamente esse o ponto do cenário.
     */
    @Dado("o insumo {string} com {int} {string} de saídas em {int} dias dos últimos 30 dias")
    public void saidasEmAlgunsDias(String insumo, int quantidade, String unidadeDeMedida, int dias) {
        mundo.insumoAtivo(insumo).consumir(quantidade, CONSUMO_DE_TESTE);
    }

    @Dado("o insumo {string} com consumo médio diário {double}, prazo de entrega de {int} dias e estoque mínimo {int}")
    public void comConsumoMedio(String insumo, double consumoMedioDiario, int prazoDeEntrega, int estoqueMinimo) {
        Insumo cadastrado = mundo.insumoCom(insumo, estoqueMinimo, prazoDeEntrega);
        cadastrado.consumir(consumoMedioDiario * 30, CONSUMO_DE_TESTE);
    }

    @Dado("o insumo {string} com ponto de pedido {int} e saldo {int}")
    public void comPontoDePedidoESaldo(String insumo, int pontoDePedido, int saldo) {
        Insumo cadastrado = mundo.insumoAtivo(insumo);
        // consumo médio = (ponto de pedido - estoque mínimo) / prazo de entrega
        double consumoMedioDiario =
                (pontoDePedido - cadastrado.estoqueMinimo()) / cadastrado.prazoDeEntregaEmDias();
        cadastrado.consumir(consumoMedioDiario * 30, CONSUMO_DE_TESTE);
        mundo.garantirSaldo(insumo, saldo);
    }

    @Quando("o sistema calcula o consumo médio diário do insumo {string}")
    public void calcularConsumoMedioDiario(String insumo) {
        // O cálculo é consultado no passo de verificação.
    }

    @Quando("o sistema calcula o ponto de pedido do insumo {string}")
    public void calcularPontoDePedido(String insumo) {
        // O cálculo é consultado no passo de verificação.
    }

    @Então("o consumo médio diário do insumo {string} é {double} {string}")
    public void consumoMedioDiarioEh(String insumo, double esperado, String unidadeDeMedida) {
        assertEquals(esperado, mundo.estoque.insumo(insumo).consumoMedioDiario(LocalDate.now()), 0.001);
    }

    @Então("o ponto de pedido do insumo {string} é {double}")
    public void pontoDePedidoEh(String insumo, double esperado) {
        assertEquals(esperado, mundo.estoque.insumo(insumo).pontoDePedido(LocalDate.now()), 0.001);
    }

    @Então("o insumo {string} fica a repor")
    public void ficaARepor(String insumo) {
        assertTrue(mundo.estoque.insumo(insumo).estaARepor(LocalDate.now()), "esperava o insumo a repor");
    }

    @Então("o insumo {string} fica normal")
    public void ficaNormal(String insumo) {
        assertFalse(mundo.estoque.insumo(insumo).estaARepor(LocalDate.now()), "esperava o insumo normal");
    }
}
