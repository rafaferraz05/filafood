# language: pt

@f1-rn6
Funcionalidade: Ponto de pedido por consumo médio

  Cenário: Calcular o consumo médio diário dividindo as saídas dos últimos 30 dias por 30
    Dado o insumo "Farinha" com 60 "kg" de saídas nos últimos 30 dias
    Quando o sistema calcula o consumo médio diário do insumo "Farinha"
    Então o consumo médio diário do insumo "Farinha" é 2,0 "kg"

  Cenário: Contar os dias sem movimento na média
    Dado o insumo "Farinha" com 30 "kg" de saídas em 10 dias dos últimos 30 dias
    Quando o sistema calcula o consumo médio diário do insumo "Farinha"
    Então o consumo médio diário do insumo "Farinha" é 1,0 "kg"

  Cenário: Calcular o ponto de pedido com o prazo de entrega e o estoque mínimo
    Dado o insumo "Farinha" com consumo médio diário 2,0, prazo de entrega de 3 dias e estoque mínimo 5
    Quando o sistema calcula o ponto de pedido do insumo "Farinha"
    Então o ponto de pedido do insumo "Farinha" é 11

  Cenário: Sinalizar o insumo a repor quando o saldo alcança o ponto de pedido
    Dado o insumo "Farinha" com ponto de pedido 11 e saldo 11
    Então o insumo "Farinha" fica a repor

  Cenário: Manter o insumo normal quando o saldo está acima do ponto de pedido
    Dado o insumo "Farinha" com ponto de pedido 11 e saldo 12
    Então o insumo "Farinha" fica normal
