package br.edu.cesar.filafood.dominio.cardapio;

import java.util.UUID;

public record ProdutoId(UUID valor) {

    public ProdutoId {
        if (valor == null) {
            throw new IllegalArgumentException("o id do produto é obrigatório");
        }
    }

    public static ProdutoId novo() {
        return new ProdutoId(UUID.randomUUID());
    }

    public static ProdutoId de(String texto) {
        return new ProdutoId(UUID.fromString(texto));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}