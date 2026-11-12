package br.edu.cesar.filafood.dominio.estoque.ficha;

import java.util.UUID;

/**
 * Identidade do produto do cardápio referenciado pela ficha técnica.
 *
 * O produto é do contexto de cardápio (F6); aqui ele é só identidade, nunca dado duplicado.
 */
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
