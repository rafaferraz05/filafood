# language: pt

@f2-rn1
Funcionalidade: Cadastro de entregador

  Cenário: Cadastrar entregador com os dados obrigatórios
    Quando o gestor da unidade cadastra o entregador "Ana" com as zonas "Centro" e "Boa Viagem" e carga máxima 3
    Então o entregador "Ana" fica ativo

  Cenário: Recusar cadastro de entregador sem nome
    Quando o gestor da unidade cadastra um entregador com nome vazio, a zona "Centro" e carga máxima 3
    Então o cadastro é recusado com a mensagem "informe o nome do entregador"

  Cenário: Recusar cadastro de entregador com carga máxima não positiva
    Quando o gestor da unidade cadastra o entregador "Ana" com a zona "Centro" e carga máxima 0
    Então o cadastro é recusado com a mensagem "a carga máxima deve ser maior que zero"

  Cenário: Inativar entregador sem cancelar as entregas em andamento
    Dado o entregador "Ana" ativo com uma entrega em rota
    Quando o gestor da unidade inativa o entregador "Ana"
    Então o entregador "Ana" fica inativo
    E a entrega continua em rota

  Cenário: Reativar entregador inativo
    Dado o entregador "Ana" inativo
    Quando o gestor da unidade reativa o entregador "Ana"
    Então o entregador "Ana" fica ativo
