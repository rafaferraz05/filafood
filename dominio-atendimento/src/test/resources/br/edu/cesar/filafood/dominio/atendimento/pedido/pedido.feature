# language: pt
@pedidos @f4
Funcionalidade: Montar, enviar e acompanhar pedido do cliente

  @f4-rn1 @pe-rascunho
  Cenário: Rascunho não aparece no histórico de pedidos enviados
    Dado um rascunho válido do cliente "C01"
    Então o pedido fica no estado "rascunho"
    E o histórico de "C01" possui 0 pedidos

  @f4-rn1 @pe-dados-erros
  Esquema do Cenário: Manter rascunho quando faltam dados para o envio
    Dado um rascunho válido do cliente "C01"
    Quando altero os dados do pedido para nome "<nome>", endereço "<endereco>" e região "<regiao>"
    E envio o pedido
    Então a operação é recusada com "<mensagem>"
    E o pedido fica no estado "rascunho"
    Exemplos:
      | nome   | endereco                | regiao     | mensagem                       |
      |        | Rua dos Navegantes, 100 | Boa Viagem | informe o nome do cliente       |
      | Rafael |                         | Boa Viagem | informe o endereço de entrega   |
      | Rafael | Rua dos Navegantes, 100 |            | informe a região de entrega     |

  @f4-rn1 @pe-sem-itens
  Cenário: Recusar envio sem itens
    Dado um rascunho válido do cliente "C01"
    Quando removo "Clássico" do pedido
    E envio o pedido
    Então a operação é recusada com "adicione pelo menos um item"
    E o pedido fica no estado "rascunho"

  @f4-rn2 @pe-adicionar
  Cenário: Acrescentar produto já presente soma sua quantidade sem duplicar linha
    Dado um rascunho válido do cliente "C01"
    Quando adiciono 1 unidades do produto "Clássico" ao pedido
    Então o pedido possui 1 linhas e 3 unidades de "Clássico"

  @f4-rn2 @pe-quantidade-erro
  Esquema do Cenário: Recusar quantidade não positiva
    Dado um rascunho válido do cliente "C01"
    Quando altero a quantidade de "Clássico" para <quantidade>
    Então a operação é recusada com "a quantidade deve ser maior que zero"
    E o pedido possui 1 linhas e 2 unidades de "Clássico"
    Exemplos:
      | quantidade |
      | 0          |
      | -1         |

  @f4-rn3 @pe-edicao
  Cenário: Alterar itens antes de enviar
    Dado um rascunho válido do cliente "C01"
    Quando altero a quantidade de "Clássico" para 3
    Então o pedido possui 1 linhas e 3 unidades de "Clássico"

  @f4-rn2 @pe-varios-itens
  Cenário: Acrescentar outro produto mantém as linhas já existentes
    Dado um rascunho válido do cliente "C01"
    Quando adiciono 1 unidades do produto "Vegetariano" ao pedido
    Então o pedido possui 2 linhas e 2 unidades de "Clássico"
    E o pedido possui 2 linhas e 1 unidades de "Vegetariano"

  @f4-rn4 @pe-envio-varios
  Cenário: Enviar pedido com dois produtos preserva a composição
    Dado um rascunho válido do cliente "C01"
    Quando adiciono 1 unidades do produto "Vegetariano" ao pedido
    E envio o pedido
    Então o pedido fica no estado "enviado"
    E o pedido possui 2 linhas e 2 unidades de "Clássico"
    E o pedido possui 2 linhas e 1 unidades de "Vegetariano"

  @f4-rn4 @pe-envio-editado
  Cenário: Enviar pedido após alterar quantidade preserva a alteração
    Dado um rascunho válido do cliente "C01"
    Quando altero a quantidade de "Clássico" para 3
    E envio o pedido
    Então o pedido fica no estado "enviado"
    E o pedido possui 1 linhas e 3 unidades de "Clássico"

  @f4-rn3 @pe-descartar
  Cenário: Descartar rascunho sem criar pedido enviado
    Dado um rascunho válido do cliente "C01"
    Quando descarto o rascunho
    Então o rascunho deixa de existir
    E o histórico de "C01" possui 0 pedidos

  @f4-rn3 @pe-edicao-bloqueada
  Cenário: Bloquear edição depois do envio
    Dado um rascunho válido do cliente "C01"
    Quando envio o pedido
    E altero a quantidade de "Clássico" para 3
    Então a operação é recusada com "o pedido só pode ser editado em rascunho"
    E o pedido fica no estado "enviado"

  @f4-rn4 @pe-envio
  Cenário: Encaminhar a unidade disponível da região
    Dado um rascunho válido do cliente "C01"
    Quando envio o pedido
    Então o pedido fica no estado "enviado"
    E a unidade escolhida é "U01"
    E nenhuma baixa de estoque foi efetivada

  @f4-rn4 @pe-unidade-pausada
  Cenário: Ignorar unidade pausada e escolher outra da mesma região
    Dado um rascunho válido do cliente "C01"
    E outra unidade disponível em Boa Viagem
    Quando pauso a unidade com motivo "Manutenção da cozinha"
    E envio o pedido
    Então o pedido fica no estado "enviado"
    E a unidade escolhida é "U02"

  @f4-rn4 @pe-recusa-regiao
  Cenário: Registrar recusa quando nenhuma unidade atende
    Dado um rascunho válido do cliente "C01"
    Quando altero os dados do pedido para nome "Rafael", endereço "Rua X, 10" e região "Olinda"
    E envio o pedido
    Então o pedido fica no estado "recusado"
    E o motivo de recusa do pedido é "nenhuma unidade disponível atende a região de entrega"
    E o histórico de "C01" possui 1 pedidos

  @f4-rn4 @pe-unidade-inativa
  Cenário: Unidade inativa não recebe novos pedidos
    Dado um rascunho válido do cliente "C01"
    Quando desativo a unidade
    E envio o pedido
    Então o pedido fica no estado "recusado"

  @f4-rn4 @pe-pausa-apos-envio
  Cenário: Pausa não cancela pedido já aceito
    Dado um rascunho válido do cliente "C01"
    Quando envio o pedido
    E pauso a unidade com motivo "Manutenção da cozinha"
    E a unidade inicia o preparo
    Então o pedido fica no estado "em preparo"
    E a baixa de estoque foi efetivada uma vez

  @f4-rn5 @pe-historico
  Cenário: Consultar apenas pedidos do próprio cliente
    Dado um rascunho válido do cliente "C01"
    Quando envio o pedido
    E consulto o pedido pelo cliente "C02"
    Então a operação é recusada com "pedido não encontrado para este cliente"
    E o histórico de "C01" possui 1 pedidos
    E o histórico de "C02" possui 0 pedidos

  @f4-rn5 @pe-recusa-insumo
  Cenário: Apresentar recusa recebida do estoque
    Dado um rascunho válido do cliente "C01"
    Quando envio o pedido
    E o estoque recusa o início por falta de Carne
    Então o pedido fica no estado "recusado"
    E o motivo de recusa do pedido é "falta de saldo do insumo Carne"
    E o estoque recebeu 1 tentativas de baixa
    E nenhuma baixa de estoque foi efetivada

  @f4-rn5 @pe-concluido
  Cenário: Concluído representa preparo terminado
    Dado um rascunho válido do cliente "C01"
    Quando envio o pedido
    E a unidade inicia o preparo
    E a unidade conclui o preparo
    Então o pedido fica no estado "concluido"
    E o histórico de "C01" possui 1 pedidos

  @f4-rn6 @pe-cancelar
  Cenário: Cancelar pedido enviado preservando o histórico
    Dado um rascunho válido do cliente "C01"
    Quando envio o pedido
    E o cliente cancela o pedido
    Então o pedido fica no estado "cancelado"
    E o histórico de "C01" possui 1 pedidos
    E nenhuma baixa de estoque foi efetivada

  @f4-rn6 @pe-cancelamento-bloqueado
  Cenário: Bloquear cancelamento pelo cliente durante o preparo
    Dado um rascunho válido do cliente "C01"
    Quando envio o pedido
    E a unidade inicia o preparo
    E o cliente cancela o pedido
    Então a operação é recusada com "o cliente só pode cancelar pedido enviado antes do preparo"
    E o pedido fica no estado "em preparo"
    E a baixa de estoque foi efetivada uma vez

  @f4-rn6 @pe-preparo-cancelado
  Cenário: Trabalhador não pode consumir insumos de pedido cancelado
    Dado um rascunho válido do cliente "C01"
    Quando envio o pedido
    E o cliente cancela o pedido
    E a unidade inicia o preparo
    Então a operação é recusada com "o pedido não está enviado"
    E o estoque recebeu 0 tentativas de baixa
    E nenhuma baixa de estoque foi efetivada

  @f4-rn6 @pe-cancelamento-repetido
  Cenário: Recusar cancelamento repetido
    Dado um rascunho válido do cliente "C01"
    Quando envio o pedido
    E o cliente cancela o pedido
    E o cliente cancela o pedido
    Então a operação é recusada com "o cliente só pode cancelar pedido enviado antes do preparo"
    E o pedido fica no estado "cancelado"
