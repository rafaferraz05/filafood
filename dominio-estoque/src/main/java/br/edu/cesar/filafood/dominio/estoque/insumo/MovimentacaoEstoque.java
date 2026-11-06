package br.edu.cesar.filafood.dominio.estoque.insumo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro de alteração de saldo de um insumo. É imutável: uma vez gravada, a movimentação não pode
 * ser alterada nem excluída.
 *
 * O campo {@code origem} guarda o pedido que causou a movimentação, o que permite devolver
 * exatamente o que foi consumido. O custo unitário é dinheiro, em {@link BigDecimal}.
 */
public record MovimentacaoEstoque(
        MovimentacaoId id,
        Tipo tipo,
        Motivo motivo,
        double quantidade,
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

    public static MovimentacaoEstoque compra(double quantidade, BigDecimal custoUnitario) {
        return new MovimentacaoEstoque(MovimentacaoId.novo(), Tipo.ENTRADA, Motivo.COMPRA, quantidade, custoUnitario,
                "", LocalDateTime.now());
    }

    public static MovimentacaoEstoque devolucao(double quantidade, String pedido) {
        return new MovimentacaoEstoque(MovimentacaoId.novo(), Tipo.ENTRADA, Motivo.DEVOLUCAO, quantidade,
                BigDecimal.ZERO.setScale(2), pedido, LocalDateTime.now());
    }

    public static MovimentacaoEstoque consumo(double quantidade, String pedido) {
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
