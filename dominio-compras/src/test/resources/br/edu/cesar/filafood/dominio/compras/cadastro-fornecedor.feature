# language: pt

@f7-rn1
Funcionalidade: Cadastro de fornecedor

  Cenário: Cadastrar fornecedor com CNPJ e contato válidos
    Quando o gestor cadastra o fornecedor "Mercado Central" com CNPJ "11.222.333/0001-81", telefone "(81) 99999-0000" e e-mail ""
    Então o fornecedor "Mercado Central" fica ativo com CNPJ "11222333000181"

  Cenário: Cadastrar fornecedor só com e-mail
    Quando o gestor cadastra o fornecedor "Casa do Insumo" com CNPJ "11222333000181", telefone "" e e-mail "compras@casadoinsumo.com.br"
    Então o fornecedor "Casa do Insumo" fica ativo com CNPJ "11222333000181"

  Cenário: Desativar e reativar o fornecedor sem perder o catálogo
    Dado que o fornecedor "Mercado Central" está cadastrado
    E o insumo "Pão" está "ativo" no estoque
    E o gestor inclui "Pão" no catálogo com preço 0.45, mínimo 20 e prazo 4 dias
    Quando o gestor desativa o fornecedor
    Então o fornecedor fica inativo
    E o catálogo do fornecedor tem 1 item
    Quando o gestor reativa o fornecedor
    Então o fornecedor fica ativo

  Esquema do Cenário: Recusar cadastro com dados inválidos
    Quando o gestor cadastra o fornecedor "<nome>" com CNPJ "<cnpj>", telefone "<telefone>" e e-mail "<email>"
    Então a operação de compras é recusada por "<mensagem>"

    Exemplos:
      | nome            | cnpj               | telefone    | email          | mensagem                                         |
      |                 | 11222333000181     | 81999990000 |                | informe o nome do fornecedor                     |
      | Mercado Central | 1122233300018      | 81999990000 |                | o CNPJ deve ter 14 algarismos                    |
      | Mercado Central | 11.222.333/0001-82 | 81999990000 |                | CNPJ inválido                                    |
      | Mercado Central | 11111111111111     | 81999990000 |                | CNPJ inválido                                    |
      | Mercado Central | 11222333000181     |             |                | informe ao menos um contato: telefone ou e-mail  |
      | Mercado Central | 11222333000181     | 8199        |                | o telefone deve ter 10 ou 11 algarismos          |
      | Mercado Central | 11222333000181     |             | compras@arroba | e-mail inválido                                  |
