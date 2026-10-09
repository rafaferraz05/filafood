# language: pt

@f1-rn3
Funcionalidade: Entrada de insumos por compra

  Cenário: Registrar entrada e somar ao saldo
    Dado o insumo "Farinha" ativo com saldo 10 "kg"
    Quando o gestor da unidade registra uma entrada de 5 "kg" do insumo "Farinha" com custo unitário 4,00
    Então o saldo do insumo "Farinha" fica 15 "kg"
    E fica registrada uma movimentação de entrada de 5 "kg" com custo unitário 4,00

  Cenário: Recusar entrada com quantidade não positiva
    Dado o insumo "Farinha" ativo
    Quando o gestor da unidade registra uma entrada de 0 "kg" do insumo "Farinha" com custo unitário 4,00
    Então a entrada é recusada com a mensagem "a quantidade deve ser maior que zero"

  Cenário: Recusar entrada com custo unitário não positivo
    Dado o insumo "Farinha" ativo
    Quando o gestor da unidade registra uma entrada de 5 "kg" do insumo "Farinha" com custo unitário 0,00
    Então a entrada é recusada com a mensagem "o custo unitário deve ser maior que zero"

  Cenário: Recusar entrada em insumo inativo
    Dado o insumo "Farinha" inativo
    Quando o gestor da unidade registra uma entrada de 5 "kg" do insumo "Farinha" com custo unitário 4,00
    Então a entrada é recusada com a mensagem "o insumo está inativo"

  Cenário: Recusar alteração de uma entrada registrada
    Dado uma entrada de 5 "kg" registrada para o insumo "Farinha"
    Quando o gestor da unidade tenta alterar a quantidade da entrada para 8 "kg"
    Então a alteração é recusada com a mensagem "a movimentação de estoque não pode ser alterada"
