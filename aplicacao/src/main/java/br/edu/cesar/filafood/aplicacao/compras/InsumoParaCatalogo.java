package br.edu.cesar.filafood.aplicacao.compras;

/** Insumo ativo do estoque da unidade que o gestor pode incluir no catálogo de um fornecedor. */
public record InsumoParaCatalogo(
        String insumoId,
        String nome,
        String unidadeDeMedida) {
}