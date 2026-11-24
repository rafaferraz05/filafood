package br.edu.cesar.filafood.aplicacao.compras;

import java.util.List;

/** Fornecedor pronto para a tela, com o catálogo restrito aos insumos da unidade consultada. */
public record FornecedorDetalhe(
        String id,
        String nome,
        String cnpj,
        String telefone,
        String email,
        boolean ativo,
        List<ItemDoCatalogoResumo> itens) {
}