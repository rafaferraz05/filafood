# language: pt

@f6-rn1
Funcionalidade: CRUD de produtos do cardápio

  Cenário: Cadastrar produto ativo com dados válidos
    Quando o gestor cadastra o produto "Hambúrguer Artesanal" na categoria "Lanches" com descrição "Pão brioche e blend 160g" e preço base de 32.90 reais
    Então o produto "Hambúrguer Artesanal" fica ativo com preço base de 32.90 reais

  Cenário: Editar produto preservando períodos de preço vigentes
    Dado que o produto "Clássico" está cadastrado na categoria "Lanches" com preço base de 20.00 reais
    E já existe preço de 25.00 reais de "2026-10-21" até "2026-10-31"
    Quando o gestor edita o produto para nome "Clássico Especial", categoria "Hambúrgueres", descrição "Receita da casa" e preço base de 22.00 reais
    Então o produto passa a se chamar "Clássico Especial" com preço base de 22.00 reais
    Quando consulto o preço vigente em "2026-10-25"
    Então o preço vigente é 25.00 reais

  Cenário: Desativar e reativar produto sem perder o cadastro
    Dado que o produto "Clássico" está cadastrado na categoria "Lanches" com preço base de 20.00 reais
    Quando o gestor desativa o produto
    Então o produto fica inativo
    Quando o gestor reativa o produto
    Então o produto fica ativo

  Cenário: Recusar cadastro de produto sem nome
    Quando o gestor cadastra o produto "" na categoria "Lanches" com descrição "" e preço base de 20.00 reais
    Então a operação do produto é recusada por "informe o nome do produto"

  Cenário: Recusar cadastro de produto com preço base não positivo
    Quando o gestor cadastra o produto "Clássico" na categoria "Lanches" com descrição "" e preço base de 0.00 reais
    Então a operação do produto é recusada por "o preço deve ser maior que zero"