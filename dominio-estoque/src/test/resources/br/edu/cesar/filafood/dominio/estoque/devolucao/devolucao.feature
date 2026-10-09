# language: pt

@f1-rn5
Funcionalidade: Devolução ao cancelar o preparo

  Cenário: Devolver ao estoque as quantidades lançadas na baixa
    Dado um pedido em preparo com a baixa de 3 "Pão" e 3 "Carne" já lançada
    Quando a unidade cancela o preparo do pedido
    Então fica registrada uma entrada do tipo devolução de 3 "Pão"
    E fica registrada uma entrada do tipo devolução de 3 "Carne"
    E o saldo do insumo "Pão" volta ao valor anterior à baixa

  Cenário: Não devolver quando o pedido já está concluído
    Dado um pedido concluído
    Quando a unidade cancela o pedido
    Então nenhuma movimentação de devolução é registrada

  Cenário: Não devolver quando o pedido já foi recusado
    Dado um pedido recusado
    Quando a unidade tenta devolver os insumos do pedido
    Então nenhuma movimentação de devolução é registrada

  Cenário: Não devolver quando o pedido já foi cancelado
    Dado um pedido cancelado
    Quando a unidade tenta devolver os insumos do pedido
    Então nenhuma movimentação de devolução é registrada
