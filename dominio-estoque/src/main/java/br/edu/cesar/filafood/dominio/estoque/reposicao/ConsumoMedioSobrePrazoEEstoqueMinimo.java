package br.edu.cesar.filafood.dominio.estoque.reposicao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;

/**
 * Política padrão da RN 6: ponto de pedido é o consumo médio diário vezes o prazo de entrega, mais o
 * estoque mínimo; insumo inativo não é monitorado.
 */
public final class ConsumoMedioSobrePrazoEEstoqueMinimo implements PoliticaDeReposicao {

    private static final int ESCALA_DA_QUANTIDADE = 3;

    @Override
    public BigDecimal pontoDePedido(Insumo insumo, LocalDate referencia) {
        return insumo.consumoMedioDiario(referencia)
                .multiply(BigDecimal.valueOf(insumo.prazoDeEntregaEmDias()))
                .add(insumo.estoqueMinimo())
                .setScale(ESCALA_DA_QUANTIDADE, RoundingMode.HALF_UP);
    }

    @Override
    public boolean estaARepor(Insumo insumo, LocalDate referencia) {
        return insumo.isAtivo() && insumo.saldo().compareTo(pontoDePedido(insumo, referencia)) <= 0;
    }
}