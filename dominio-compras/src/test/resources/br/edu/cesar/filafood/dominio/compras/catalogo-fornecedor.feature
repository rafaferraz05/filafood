# language: pt

@f7-rn2
Funcionalidade: Catálogo do fornecedor

  Contexto:
    Dado que o fornecedor "Mercado Central" está cadastrado
    E o insumo "Pão" está "ativo" no estoque

  Cenário: Incluir no catálogo um insumo ativo com preço, mínimo e prazo
    Quando o gestor inclui "Pão" no catálogo com preço 0.45, mínimo 20 e prazo 4 dias
    Então o catálogo do fornecedor tem 1 item
    E o item de "Pão" tem preço 0.45, mínimo 20 e prazo 4 dias

  Cenário: Recusar insumo inativo no catálogo
    Dado o insumo "Tomate" está "inativo" no estoque
    Quando o gestor inclui "Tomate" no catálogo com preço 6.00, mínimo 5 e prazo 2 dias
    Então a operação de compras é recusada por "o insumo está inativo"
    E o catálogo do fornecedor tem 0 itens

  Cenário: Recusar um segundo item para o mesmo insumo
    Dado o gestor inclui "Pão" no catálogo com preço 0.45, mínimo 20 e prazo 4 dias
    Quando o gestor inclui "Pão" no catálogo com preço 0.40, mínimo 10 e prazo 3 dias
    Então a operação de compras é recusada por "o fornecedor já tem item para esse insumo"
    E o catálogo do fornecedor tem 1 item

  Esquema do Cenário: Recusar condições de compra inválidas
    Quando o gestor inclui "Pão" no catálogo com preço <preco>, mínimo <minimo> e prazo <prazo> dias
    Então a operação de compras é recusada por "<mensagem>"

    Exemplos:
      | preco | minimo | prazo | mensagem                                       |
      | 0.00  | 20     | 4     | o preço unitário deve ser maior que zero       |
      | 0.45  | 0      | 4     | a quantidade mínima deve ser maior que zero    |
      | 0.45  | 20     | 0     | o prazo de entrega deve ser de ao menos 1 dia  |

  Cenário: Alterar as condições do item sem trocar o insumo
    Dado o gestor inclui "Pão" no catálogo com preço 0.45, mínimo 20 e prazo 4 dias
    Quando o gestor altera o item de "Pão" para preço 0.50, mínimo 10 e prazo 3 dias
    Então o item de "Pão" tem preço 0.50, mínimo 10 e prazo 3 dias
    E o catálogo do fornecedor tem 1 item

  Cenário: Remover o item do catálogo
    Dado o gestor inclui "Pão" no catálogo com preço 0.45, mínimo 20 e prazo 4 dias
    Quando o gestor remove "Pão" do catálogo
    Então o catálogo do fornecedor tem 0 itens
