# language: pt
@funcionarios @f5
Funcionalidade: Gerenciar funcionários da unidade

  @f5-rn1 @f5-rn2 @fu-cadastro
  Esquema do Cenário: Cadastrar funcionário com função permitida
    Quando cadastro funcionário com nome "Ana", função "<funcao>" e telefone ""
    Então o funcionário fica ativo na mesma unidade com função "<funcao>"
    Exemplos:
      | funcao    |
      | gestor    |
      | atendente |
      | cozinheiro |

  @f5-rn1 @fu-edicao
  Cenário: Editar funcionário preservando o identificador
    Dado um funcionário cadastrado
    Quando edito o funcionário para nome "Ana Silva", função "cozinheiro" e telefone ""
    Então a operação é aceita
    E o funcionário conserva o id e tem nome "Ana Silva"

  @f5-rn1 @f5-rn4 @fu-edicao-inativo
  Cenário: Editar funcionário não reativa o cadastro
    Dado um funcionário cadastrado
    Quando desativo o funcionário
    E edito o funcionário para nome "Ana Silva", função "cozinheiro" e telefone ""
    Então o funcionário conserva o id e tem nome "Ana Silva"
    E o funcionário permanece inativo após a edição

  @f5-rn1 @fu-erros
  Cenário: Recusar funcionário sem nome
    Quando cadastro funcionário com nome "", função "atendente" e telefone ""
    Então a operação é recusada com "informe o nome do funcionário"

  @f5-rn1 @fu-nome-longo
  Cenário: Recusar nome maior que 100 caracteres
    Quando tento cadastrar funcionário com nome de 101 caracteres
    Então a operação é recusada com "o nome do funcionário deve ter até 100 caracteres"

  @f5-rn2 @fu-funcao
  Esquema do Cenário: Recusar função fora do cadastro da unidade
    Quando cadastro funcionário com nome "Ana", função "<funcao>" e telefone ""
    Então a operação é recusada com "escolha gestor, atendente ou cozinheiro"
    Exemplos:
      | funcao        |
      |               |
      | administrador |
      | entregador    |

  @f5-rn3 @fu-telefone
  Cenário: Normalizar telefone do funcionário
    Quando cadastro funcionário com nome "Ana", função "atendente" e telefone "(81) 3333-1234"
    Então o telefone do funcionário é "8133331234"

  @f5-rn3 @fu-telefone-invalido
  Cenário: Recusar telefone inválido
    Quando cadastro funcionário com nome "Ana", função "atendente" e telefone "123"
    Então a operação é recusada com "informe telefone com DDD e 10 ou 11 algarismos"

  @f5-rn4 @fu-desativar
  Cenário: Desativar sem apagar o funcionário
    Dado um funcionário cadastrado
    Quando desativo o funcionário
    Então o funcionário permanece consultável e inativo

  @f5-rn4 @fu-reativar
  Cenário: Reativar o funcionário na mesma unidade
    Dado um funcionário cadastrado
    Quando desativo o funcionário
    E reativo o funcionário
    Então o funcionário fica ativo na mesma unidade com função "atendente"
