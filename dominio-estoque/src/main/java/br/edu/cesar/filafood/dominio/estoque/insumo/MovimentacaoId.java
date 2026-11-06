package br.edu.cesar.filafood.dominio.estoque.insumo;

import java.util.UUID;

/** Identidade da movimentação de estoque: a linha é imutável, mas precisa de chave própria. */
public record MovimentacaoId(UUID valor) {

    public MovimentacaoId {
        if (valor == null) {
            throw new IllegalArgumentException("o id da movimentação é obrigatório");
        }
    }

    public static MovimentacaoId novo() {
        return new MovimentacaoId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
