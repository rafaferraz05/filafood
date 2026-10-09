package br.edu.cesar.filafood.dominio.estoque.insumo;

import java.time.LocalDateTime;

/**
 * Registro de alteração de saldo de um insumo. É imutável: uma vez gravada, a movimentação não pode
 * ser alterada nem excluída.
 */
public record MovimentacaoEstoque(
        Tipo tipo,
        Motivo motivo,
        double quantidade,
        double custoUnitario,
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
        return new MovimentacaoEstoque(Tipo.ENTRADA, Motivo.COMPRA, quantidade, custoUnitario, LocalDateTime.now());
    }

    public static MovimentacaoEstoque devolucao(double quantidade) {
        return new MovimentacaoEstoque(Tipo.ENTRADA, Motivo.DEVOLUCAO, quantidade, 0, LocalDateTime.now());
    }

    public static MovimentacaoEstoque consumo(double quantidade) {
        return new MovimentacaoEstoque(Tipo.SAIDA, Motivo.CONSUMO, quantidade, 0, LocalDateTime.now());
    }

    public boolean somaAoSaldo() {
        return tipo == Tipo.ENTRADA;
    }
}
