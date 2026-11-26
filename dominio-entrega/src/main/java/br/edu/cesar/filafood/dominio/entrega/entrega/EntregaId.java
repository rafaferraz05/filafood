package br.edu.cesar.filafood.dominio.entrega.entrega;

import java.util.UUID;

/** Identidade da entrega: cada tentativa de um pedido é uma entrega diferente. */
public record EntregaId(UUID valor) {

    public EntregaId {
        if (valor == null) {
            throw new IllegalArgumentException("o id da entrega é obrigatório");
        }
    }

    public static EntregaId novo() {
        return new EntregaId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}