# language: pt

@f1-rn2
Funcionalidade: Ficha técnica do produto

  Contexto:
    Dado que o produto "Hambúrguer" existe no cardápio da rede

  Cenário: Definir a ficha técnica de um produto
    Dado o insumo "Pão" ativo e o insumo "Carne" ativo na unidade
    Quando o gestor da unidade define a ficha técnica do produto "Hambúrguer" com 1 "Pão" e 1 "Carne"
    Então a ficha técnica do produto "Hambúrguer" na unidade fica com 2 itens

  Cenário: Recusar o mesmo insumo duas vezes no mesmo produto
    Dado o insumo "Pão" ativo na unidade
    Quando o gestor da unidade define a ficha técnica do produto "Hambúrguer" com 1 "Pão" e 2 "Pão"
    Então a ficha técnica é recusada com a mensagem "o insumo já está na ficha técnica"

  Cenário: Recusar item da ficha técnica com quantidade não positiva
    Dado o insumo "Pão" ativo na unidade
    Quando o gestor da unidade define a ficha técnica do produto "Hambúrguer" com 0 "Pão"
    Então a ficha técnica é recusada com a mensagem "a quantidade deve ser maior que zero"

  Cenário: Recusar item da ficha técnica com insumo inativo
    Dado o insumo "Pão" inativo na unidade
    Quando o gestor da unidade define a ficha técnica do produto "Hambúrguer" com 1 "Pão"
    Então a ficha técnica é recusada com a mensagem "o insumo está inativo"

  Cenário: Recusar a venda de produto sem ficha técnica na unidade
    Dado que o produto "Pizza" não tem ficha técnica na unidade
    Quando a unidade inicia o preparo de um pedido com 1 "Pizza"
    Então o pedido é recusado com o motivo "produto sem ficha técnica na unidade"
