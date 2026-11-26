package br.edu.cesar.filafood.dominio.entrega.entregador;

import java.util.UUID;

/** Identidade do entregador: substituta, imutável e independente do nome. */
public record EntregadorId(UUID valor) {

    public EntregadorId {
        if (valor == null) {
            throw new IllegalArgumentException("o id do entregador é obrigatório");
        }
    }

    public static EntregadorId novo() {
        return new EntregadorId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}