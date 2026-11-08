package br.edu.cesar.filafood.dominio.estoque.ficha;

import java.util.UUID;

/**
 * Identidade da ficha técnica: uma ficha por produto em cada unidade (RN 2).
 */
public record FichaTecnicaId(UUID valor) {

    public FichaTecnicaId {
        if (valor == null) {
            throw new IllegalArgumentException("o id da ficha técnica é obrigatório");
        }
    }

    public static FichaTecnicaId novo() {
        return new FichaTecnicaId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
