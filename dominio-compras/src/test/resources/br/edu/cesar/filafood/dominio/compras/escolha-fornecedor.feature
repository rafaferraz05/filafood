# language: pt

@f7-rn4
Funcionalidade: Escolha do fornecedor

  Cenário: Escolher menor custo total após arredondar pelo mínimo de cada fornecedor
    Dado a quantidade sugerida inicial é 11 e o prazo limite é 5 dias
    E o fornecedor "Alfa" tem preço 2.00, mínimo 10, prazo 3 dias e está "ativo"
    E o fornecedor "Beta" tem preço 1.50, mínimo 5, prazo 4 dias e está "ativo"
    Quando o sistema escolhe o fornecedor
    Então o fornecedor escolhido é "Beta"
    E a quantidade final é 15

  Cenário: Desempatar custo pelo menor prazo
    Dado a quantidade sugerida inicial é 10 e o prazo limite é 5 dias
    E o fornecedor "Alfa" tem preço 2.00, mínimo 5, prazo 4 dias e está "ativo"
    E o fornecedor "Beta" tem preço 2.00, mínimo 5, prazo 2 dias e está "ativo"
    Quando o sistema escolhe o fornecedor
    Então o fornecedor escolhido é "Beta"

  Cenário: Se ninguém cumpre o prazo, escolher o menor prazo e sinalizar atraso
    Dado a quantidade sugerida inicial é 10 e o prazo limite é 2 dias
    E o fornecedor "Alfa" tem preço 1.00, mínimo 5, prazo 5 dias e está "ativo"
    E o fornecedor "Beta" tem preço 3.00, mínimo 5, prazo 3 dias e está "ativo"
    Quando o sistema escolhe o fornecedor
    Então o fornecedor escolhido é "Beta"
    E a sugestão sinaliza atraso
    E a sugestão sinaliza atraso previsto de 1 dia

  Cenário: Desempatar custo e prazo pelo nome em ordem alfabética
    Dado a quantidade sugerida inicial é 10 e o prazo limite é 5 dias
    E o fornecedor "Mercado Central" tem preço 2.00, mínimo 5, prazo 3 dias e está "ativo"
    E o fornecedor "Alimentos Recife" tem preço 2.00, mínimo 5, prazo 3 dias e está "ativo"
    Quando o sistema escolhe o fornecedor
    Então o fornecedor escolhido é "Alimentos Recife"

  Cenário: Fornecedor inativo não concorre, mesmo sendo mais barato
    Dado a quantidade sugerida inicial é 10 e o prazo limite é 5 dias
    E o fornecedor "Alfa" tem preço 1.00, mínimo 5, prazo 2 dias e está "inativo"
    E o fornecedor "Beta" tem preço 3.00, mínimo 5, prazo 2 dias e está "ativo"
    Quando o sistema escolhe o fornecedor
    Então o fornecedor escolhido é "Beta"
    E a quantidade final é 10

  Cenário: Informar quando não existe fornecedor ativo no catálogo
    Dado a quantidade sugerida inicial é 10 e o prazo limite é 2 dias
    Quando o sistema escolhe o fornecedor
    Então o sistema informa "não há fornecedor para o insumo"

  Cenário: Informar quando só há fornecedor inativo no catálogo
    Dado a quantidade sugerida inicial é 10 e o prazo limite é 2 dias
    E o fornecedor "Alfa" tem preço 1.00, mínimo 5, prazo 1 dia e está "inativo"
    Quando o sistema escolhe o fornecedor
    Então o sistema informa "não há fornecedor para o insumo"
