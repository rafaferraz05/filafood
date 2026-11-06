package br.edu.cesar.filafood.dominio.estoque.insumo;

import java.util.UUID;

/**
 * Identidade do insumo: substituta, imutável e independente do nome (docs/dominio.md, seção 5).
 */
public record InsumoId(UUID valor) {

    public InsumoId {
        if (valor == null) {
            throw new IllegalArgumentException("o id do insumo é obrigatório");
        }
    }

    public static InsumoId novo() {
        return new InsumoId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
