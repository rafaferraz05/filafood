# language: pt

@f6-rn5
Funcionalidade: Disponibilidade calculada pelo estoque

  Cenário: Calcular porções pelo insumo limitante
    Dado a ficha técnica tem 2 "Pães" por porção, saldo 10 e está "ativo"
    E a ficha técnica tem 1 "Carne" por porção, saldo 3 e está "ativo"
    Quando calculo as porções possíveis
    Então as porções possíveis são 3
    E o produto fica "disponível"

  Cenário: Produto indisponível quando um insumo está sem saldo
    Dado a ficha técnica tem 1 "Pão" por porção, saldo 10 e está "ativo"
    E a ficha técnica tem 1 "Carne" por porção, saldo 0 e está "ativo"
    Quando calculo as porções possíveis
    Então as porções possíveis são 0
    E o produto fica "indisponível"

  Cenário: Produto indisponível quando um insumo da ficha está inativo
    Dado a ficha técnica tem 1 "Pão" por porção, saldo 10 e está "ativo"
    E a ficha técnica tem 1 "Carne" por porção, saldo 5 e está "inativo"
    Quando calculo as porções possíveis
    Então as porções possíveis são 0
    E o produto fica "indisponível"
