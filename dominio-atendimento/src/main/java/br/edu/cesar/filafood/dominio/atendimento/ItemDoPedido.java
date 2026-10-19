package br.edu.cesar.filafood.dominio.atendimento;

public record ItemDoPedido(String produto, int quantidade) {
    public ItemDoPedido {
        produto = Dados.obrigatorio(produto, "informe o produto");
        if (quantidade <= 0) throw new RegraDeNegocioException("a quantidade deve ser maior que zero");
    }
}
