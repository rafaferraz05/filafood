# language: pt

@f1-rn4
Funcionalidade: Baixa automática por explosão da ficha técnica

  Contexto:
    Dado que o produto "Hambúrguer" tem ficha técnica com 1 "Pão" e 1 "Carne" na unidade
    E o pedido tem 3 "Hambúrguer"

  Cenário: Dar baixa somando as contribuições dos itens repetidos
    Dado o saldo do insumo "Pão" igual a 10 e o saldo do insumo "Carne" igual a 10
    Quando a unidade inicia o preparo do pedido
    Então o pedido fica em preparo
    E o saldo do insumo "Pão" fica 7
    E o saldo do insumo "Carne" fica 7

  Cenário: Dar baixa quando o saldo é exatamente a necessidade
    Dado o saldo do insumo "Pão" igual a 3 e o saldo do insumo "Carne" igual a 3
    Quando a unidade inicia o preparo do pedido
    Então o pedido fica em preparo
    E o saldo do insumo "Pão" fica zero
    E o saldo do insumo "Carne" fica zero

  Cenário: Não gravar nenhuma movimentação quando falta saldo de um insumo
    Dado o saldo do insumo "Pão" igual a 10 e o saldo do insumo "Carne" igual a 1
    Quando a unidade inicia o preparo do pedido
    Então o pedido é recusado com o motivo "falta de saldo do insumo Carne"
    E o saldo do insumo "Pão" continua 10
    E o saldo do insumo "Carne" continua 1
