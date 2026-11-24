package br.edu.cesar.filafood.aplicacao.compras;

public record NovoFornecedor(
        String nome,
        String cnpj,
        String telefone,
        String email) {
}