# language: pt
@unidades @f3
Funcionalidade: Gerenciar unidades de atendimento
  # F3 no repositório corresponde à Funcionalidade 1 do formulário individual de Rafael.

  @f3-rn1 @un-cadastro
  Cenário: Cadastrar unidade disponível
    Quando cadastro uma unidade com nome "Boa Viagem", região "Boa Viagem" e telefone ""
    Então a unidade está disponível
    E a unidade conserva o id e tem nome "Boa Viagem"

  @f3-rn1 @un-edicao
  Cenário: Editar unidade preservando identificador
    Dado uma unidade disponível em Boa Viagem
    Quando edito a unidade para nome "Boa Viagem Sul", região "Boa Viagem" e telefone ""
    Então a operação é aceita
    E a unidade conserva o id e tem nome "Boa Viagem Sul"

  @f3-rn1 @f3-rn2 @f3-rn3 @un-erros
  Esquema do Cenário: Recusar dados inválidos de unidade
    Quando cadastro uma unidade com nome "<nome>", região "<regiao>" e telefone "<telefone>"
    Então a operação é recusada com "<mensagem>"
    Exemplos:
      | nome       | regiao     | telefone    | mensagem                                               |
      |            | Boa Viagem |             | informe o nome da unidade                              |
      | Boa Viagem |            |             | informe a região de atendimento                        |
      | Boa Viagem | Boa Viagem | 123         | informe telefone com DDD e 10 ou 11 algarismos          |
      | Boa Viagem | Boa Viagem | 819ABCD5678 | informe telefone com DDD e 10 ou 11 algarismos          |

  @f3-rn1 @f3-rn4 @un-edicao-pausada
  Cenário: Editar unidade não retoma o recebimento
    Dado uma unidade disponível em Boa Viagem
    Quando pauso a unidade com motivo "Manutenção da cozinha"
    E edito a unidade para nome "Boa Viagem Sul", região "Boa Viagem" e telefone ""
    Então a unidade conserva o id e tem nome "Boa Viagem Sul"
    E a unidade fica ativa e pausada com motivo "Manutenção da cozinha"

  @f3-rn1 @f3-rn4 @un-edicao-inativa
  Cenário: Editar unidade não reativa o cadastro
    Dado uma unidade disponível em Boa Viagem
    Quando desativo a unidade
    E edito a unidade para nome "Boa Viagem Sul", região "Boa Viagem" e telefone ""
    Então a unidade conserva o id e tem nome "Boa Viagem Sul"
    E a unidade permanece inativa após a edição

  @f3-rn3 @un-telefone
  Cenário: Normalizar pontuação do telefone
    Quando cadastro uma unidade com nome "Boa Viagem", região "Boa Viagem" e telefone "(81) 99999-1234"
    Então o telefone da unidade é "81999991234"

  @f3-rn4 @un-desativar
  Cenário: Desativar sem excluir o cadastro
    Dado uma unidade disponível em Boa Viagem
    Quando desativo a unidade
    Então o cadastro da unidade continua consultável e inativo

  @f3-rn4 @un-reativar
  Cenário: Reativar unidade para receber pedidos
    Dado uma unidade disponível em Boa Viagem
    Quando desativo a unidade
    E reativo a unidade
    Então a unidade está disponível

  @f3-rn4 @un-pausar
  Cenário: Pausar unidade com motivo
    Dado uma unidade disponível em Boa Viagem
    Quando pauso a unidade com motivo "Manutenção da cozinha"
    Então a unidade fica ativa e pausada com motivo "Manutenção da cozinha"

  @f3-rn4 @un-retomar
  Cenário: Retomar e limpar o motivo
    Dado uma unidade disponível em Boa Viagem
    Quando pauso a unidade com motivo "Manutenção da cozinha"
    E retomo o recebimento da unidade
    Então a unidade está disponível
    E o motivo de pausa fica vazio

  @f3-rn4 @un-pausa-erros
  Cenário: Recusar pausa sem motivo
    Dado uma unidade disponível em Boa Viagem
    Quando pauso a unidade com motivo ""
    Então a operação é recusada com "informe o motivo de pausa"
    E a unidade está disponível

  @f3-rn4 @un-limite-motivo
  Cenário: Aceitar motivo com 200 caracteres
    Dado uma unidade disponível em Boa Viagem
    Quando pauso a unidade com motivo de 200 caracteres
    Então a unidade fica pausada

  @f3-rn4 @un-motivo-longo
  Cenário: Recusar motivo com mais de 200 caracteres
    Dado uma unidade disponível em Boa Viagem
    Quando pauso a unidade com motivo de 201 caracteres
    Então a operação é recusada com "o motivo de pausa deve ter até 200 caracteres"
    E a unidade está disponível

  @f3-rn4 @un-inativa
  Cenário: Recusar retomada de unidade inativa
    Dado uma unidade disponível em Boa Viagem
    Quando desativo a unidade
    E retomo o recebimento da unidade
    Então a operação é recusada com "a unidade está inativa"
    E o cadastro da unidade continua consultável e inativo
