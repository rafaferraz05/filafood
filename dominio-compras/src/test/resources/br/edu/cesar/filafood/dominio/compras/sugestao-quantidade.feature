# language: pt

@f7-rn3
Funcionalidade: Sugestão de quantidade a comprar

  Cenário: Projetar cobertura e descontar saldo e ordens abertas
    Dado o ponto de pedido é 10, o consumo médio diário é 4, a cobertura é 5, o saldo é 6 e há 3 em ordens abertas
    Quando calculo a sugestão de compra
    Então a quantidade bruta sugerida é 21
    E a quantidade sugerida é 21

  Cenário: Não sugerir quando a quantidade bruta é zero
    Dado o ponto de pedido é 10, o consumo médio diário é 2, a cobertura é 3, o saldo é 12 e há 4 em ordens abertas
    Quando calculo a sugestão de compra
    Então a quantidade bruta sugerida é 0
    E nenhuma compra é sugerida

  Cenário: Não sugerir quando saldo e ordens abertas cobrem a necessidade
    Dado o ponto de pedido é 10, o consumo médio diário é 2, a cobertura é 3, o saldo é 20 e há 4 em ordens abertas
    Quando calculo a sugestão de compra
    Então a quantidade bruta sugerida é -8
    E nenhuma compra é sugerida
