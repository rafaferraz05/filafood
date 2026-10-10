package br.edu.cesar.filafood.dominio.estoque.insumo;

import java.time.LocalDateTime;

/**
 * Registro de alteração de saldo de um insumo. É imutável: uma vez gravada, a movimentação não pode
 * ser alterada nem excluída.
 *
 * O campo {@code origem} guarda o pedido que causou a movimentação, o que permite devolver
 * exatamente o que foi consumido.
 */
public record MovimentacaoEstoque(
        Tipo tipo,
        Motivo motivo,
        double quantidade,
        double custoUnitario,
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

    public static MovimentacaoEstoque compra(double quantidade, double custoUnitario) {
        return new MovimentacaoEstoque(Tipo.ENTRADA, Motivo.COMPRA, quantidade, custoUnitario, "", LocalDateTime.now());
    }

    public static MovimentacaoEstoque devolucao(double quantidade, String pedido) {
        return new MovimentacaoEstoque(Tipo.ENTRADA, Motivo.DEVOLUCAO, quantidade, 0, pedido, LocalDateTime.now());
    }

    public static MovimentacaoEstoque consumo(double quantidade, String pedido) {
        return new MovimentacaoEstoque(Tipo.SAIDA, Motivo.CONSUMO, quantidade, 0, pedido, LocalDateTime.now());
    }

    public boolean somaAoSaldo() {
        return tipo == Tipo.ENTRADA;
    }

    public boolean ehDoPedido(String pedido) {
        return origem != null && origem.equals(pedido);
    }
}
