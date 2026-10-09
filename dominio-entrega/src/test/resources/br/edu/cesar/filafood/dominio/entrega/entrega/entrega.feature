# language: pt

@f2-rn5
Funcionalidade: Estados e desfecho da entrega

  Cenário: Percorrer os estados da entrega na ordem
    Dado uma entrega em aguardando
    Quando o sistema despacha a entrega para o entregador "Ana"
    Então a entrega fica despachada
    E a transição grava data e hora
    Quando o entregador "Ana" registra a retirada
    Então a entrega fica em rota
    Quando o entregador "Ana" registra a entrega com o recebedor "Maria"
    Então a entrega fica entregue

  Cenário: Registrar o desfecho de falha com o motivo
    Dado uma entrega em rota com o entregador "Ana"
    Quando o entregador "Ana" registra uma falha com o motivo "ausência do cliente"
    Então a entrega fica falha
    E a falha fica registrada com o motivo

  Cenário: Recusar o registro de despachada sem entregador atribuído
    Dado uma entrega em aguardando
    Quando o sistema marca a entrega como despachada sem entregador
    Então a transição é recusada com a mensagem "a entrega despachada exige um entregador"

  Cenário: Recusar o registro de entregue sem o nome do recebedor
    Dado uma entrega em rota
    Quando o entregador registra a entrega sem informar o recebedor
    Então o registro é recusado com a mensagem "informe o nome do recebedor"

  Cenário: Recusar o registro de falha sem o motivo
    Dado uma entrega em rota
    Quando o entregador registra uma falha sem informar o motivo
    Então o registro é recusado com a mensagem "informe o motivo da falha"

  Cenário: Recusar pular etapa
    Dado uma entrega em aguardando
    Quando o entregador registra a retirada
    Então a transição é recusada com a mensagem "a entrega precisa estar despachada"

  Cenário: Recusar alteração de entrega já finalizada
    Dado uma entrega entregue
    Quando o entregador registra uma falha com o motivo "ausência do cliente"
    Então a transição é recusada com a mensagem "a entrega já foi finalizada"
