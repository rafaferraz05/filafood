package br.edu.cesar.filafood.aplicacao.compras;

public record AlteracaoDeFornecedor(
        String fornecedorId,
        String nome,
        String cnpj,
        String telefone,
        String email) {
}