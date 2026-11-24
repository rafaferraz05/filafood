package br.edu.cesar.filafood.dominio.compras;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Item do catálogo do fornecedor (F7 RN 2): o insumo que ele vende, com preço unitário, quantidade
 * mínima de compra e prazo de entrega.
 *
 * <p>O insumo é referenciado só pelo id: o contexto de compras não depende do módulo de estoque. Quem
 * confere se o insumo existe e está ativo é o caso de uso, que conhece a porta do estoque.
 */
public record ItemDoCatalogo(UUID id, UUID insumoId, BigDecimal precoUnitario, BigDecimal quantidadeMinima,
        int prazoDeEntregaEmDias) {

    private static final int ESCALA_DO_PRECO = 2;
    private static final int ESCALA_DA_QUANTIDADE = 3;

    public ItemDoCatalogo {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (insumoId == null) {
            throw new RegraDeNegocioException("informe o insumo");
        }
        if (precoUnitario == null || precoUnitario.signum() <= 0) {
            throw new RegraDeNegocioException("o preço unitário deve ser maior que zero");
        }
        if (quantidadeMinima == null || quantidadeMinima.signum() <= 0) {
            throw new RegraDeNegocioException("a quantidade mínima deve ser maior que zero");
        }
        if (prazoDeEntregaEmDias < 1) {
            throw new RegraDeNegocioException("o prazo de entrega deve ser de ao menos 1 dia");
        }
        precoUnitario = precoUnitario.setScale(ESCALA_DO_PRECO, RoundingMode.HALF_UP);
        quantidadeMinima = quantidadeMinima.setScale(ESCALA_DA_QUANTIDADE, RoundingMode.HALF_UP);
    }

    public static ItemDoCatalogo novo(UUID insumoId, BigDecimal precoUnitario, BigDecimal quantidadeMinima,
            int prazoDeEntregaEmDias) {
        return new ItemDoCatalogo(UUID.randomUUID(), insumoId, precoUnitario, quantidadeMinima,
                prazoDeEntregaEmDias);
    }

    /** Novas condições para o mesmo insumo; ordens já criadas guardam o preço antigo (RN 2). */
    public ItemDoCatalogo comCondicoes(BigDecimal precoUnitario, BigDecimal quantidadeMinima,
            int prazoDeEntregaEmDias) {
        return new ItemDoCatalogo(id, insumoId, precoUnitario, quantidadeMinima, prazoDeEntregaEmDias);
    }
}
