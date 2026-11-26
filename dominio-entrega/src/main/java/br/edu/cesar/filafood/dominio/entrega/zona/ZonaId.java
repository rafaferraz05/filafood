package br.edu.cesar.filafood.dominio.entrega.zona;

import java.util.UUID;

/** Identidade da zona de entrega. */
public record ZonaId(UUID valor) {

    public ZonaId {
        if (valor == null) {
            throw new IllegalArgumentException("o id da zona é obrigatório");
        }
    }

    public static ZonaId novo() {
        return new ZonaId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}