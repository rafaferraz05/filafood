package br.edu.cesar.filafood.dominio.estoque.insumo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro de alteração de saldo de um insumo. É imutável: uma vez gravada, a movimentação não pode
 * ser alterada nem excluída.
 *
 * O campo {@code origem} guarda o pedido que causou a movimentação, o que permite devolver
 * exatamente o que foi consumido. Quantidade e custo unitário usam {@link BigDecimal}: quantidade
 * com escala 3 e dinheiro com escala 2, como no schema (G-09).
 */
public record MovimentacaoEstoque(
        MovimentacaoId id,
        Tipo tipo,
        Motivo motivo,
        BigDecimal quantidade,
        BigDecimal custoUnitario,
        String origem,
        LocalDateTime dataHora) {

    public enum Tipo {
        ENTRADA,
        SAIDA
    }

    public enum Motivo {
        COMPRA,
        DEVOLUCAO,
        CONSUMO
    }

    public static MovimentacaoEstoque compra(BigDecimal quantidade, BigDecimal custoUnitario) {
        return new MovimentacaoEstoque(MovimentacaoId.novo(), Tipo.ENTRADA, Motivo.COMPRA, quantidade, custoUnitario,
                "", LocalDateTime.now());
    }

    public static MovimentacaoEstoque devolucao(BigDecimal quantidade, String pedido) {
        return new MovimentacaoEstoque(MovimentacaoId.novo(), Tipo.ENTRADA, Motivo.DEVOLUCAO, quantidade,
                BigDecimal.ZERO.setScale(2), pedido, LocalDateTime.now());
    }

    public static MovimentacaoEstoque consumo(BigDecimal quantidade, String pedido) {
        return new MovimentacaoEstoque(MovimentacaoId.novo(), Tipo.SAIDA, Motivo.CONSUMO, quantidade,
                BigDecimal.ZERO.setScale(2), pedido, LocalDateTime.now());
    }

    public boolean somaAoSaldo() {
        return tipo == Tipo.ENTRADA;
    }

    public boolean ehDoPedido(String pedido) {
        return origem != null && origem.equals(pedido);
    }
}