# language: pt

@f1-rn1
Funcionalidade: Cadastro de insumo

  Cenário: Cadastrar insumo com os dados obrigatórios
    Quando o gestor da unidade cadastra o insumo "Farinha" com unidade de medida "kg", estoque mínimo 5 e prazo de entrega de 3 dias
    Então o insumo "Farinha" fica ativo
    E o saldo do insumo "Farinha" fica zero

  Cenário: Recusar cadastro de insumo sem nome
    Quando o gestor da unidade cadastra um insumo com nome vazio, unidade de medida "kg", estoque mínimo 5 e prazo de entrega de 3 dias
    Então o cadastro é recusado com a mensagem "informe o nome do insumo"
    E nenhum insumo é cadastrado

  Cenário: Recusar cadastro de insumo com estoque mínimo não positivo
    Quando o gestor da unidade cadastra o insumo "Farinha" com unidade de medida "kg", estoque mínimo 0 e prazo de entrega de 3 dias
    Então o cadastro é recusado com a mensagem "o estoque mínimo deve ser maior que zero"

  Cenário: Desativar insumo sem perder o histórico
    Dado o insumo "Farinha" cadastrado e com movimentações de estoque registradas
    Quando o gestor da unidade desativa o insumo "Farinha"
    Então o insumo "Farinha" fica inativo
    E o histórico de movimentações do insumo "Farinha" continua consultável

  Cenário: Reativar insumo desativado
    Dado o insumo "Farinha" inativo
    Quando o gestor da unidade reativa o insumo "Farinha"
    Então o insumo "Farinha" fica ativo
