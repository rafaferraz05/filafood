package br.edu.cesar.filafood.dominio.compras;

import java.util.UUID;

/** Identidade do fornecedor: substituta e independente do CNPJ, que pode ser corrigido na edição. */
public record FornecedorId(UUID valor) {

    public FornecedorId {
        if (valor == null) {
            throw new IllegalArgumentException("o id do fornecedor é obrigatório");
        }
    }

    public static FornecedorId novo() {
        return new FornecedorId(UUID.randomUUID());
    }

    public static FornecedorId de(String texto) {
        return new FornecedorId(UUID.fromString(texto));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
