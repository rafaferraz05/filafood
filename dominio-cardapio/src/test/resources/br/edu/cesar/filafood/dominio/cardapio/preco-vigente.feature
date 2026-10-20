# language: pt

@f6-rn4
Funcionalidade: Preço vigente do produto na unidade

  Cenário: Usar preço da unidade quando o período contém a data consultada
    Dado que o produto tem preço base de 20.00 reais
    E já existe preço de 25.00 reais de "2026-10-21" até "2026-10-31"
    Quando consulto o preço vigente em "2026-10-25"
    Então o preço vigente é 25.00 reais

  Cenário: Usar preço base quando não há período vigente
    Dado que o produto tem preço base de 20.00 reais
    Quando consulto o preço vigente em "2026-10-25"
    Então o preço vigente é 20.00 reais

  Cenário: Recusar sobreposição inclusive quando o período anterior não tem fim
    Dado que o produto tem preço base de 20.00 reais
    E já existe preço de 25.00 reais de "2026-10-21" até "sem fim"
    Quando o gestor define preço de 27.00 reais de "2026-11-01" até "2026-11-30"
    Então a definição é recusada por "períodos de vigência não podem se sobrepor"

  Cenário: Recusar início anterior à data atual
    Dado que o produto tem preço base de 20.00 reais
    Quando o gestor define preço de 27.00 reais de "2026-10-19" até "2026-11-30"
    Então a definição é recusada por "a data inicial não pode ser anterior à data atual"
