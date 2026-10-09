# language: pt

@f2-rn6
Funcionalidade: Nova tentativa após falha na entrega

  Cenário: Devolver o pedido à fila e criar uma nova entrega
    Dado um pedido concluído com uma entrega em rota
    Quando o entregador registra uma falha com o motivo "endereço não encontrado"
    Então o pedido volta para a fila de despacho
    E uma nova entrega do pedido fica em aguardando
    E o pedido continua concluído

  Cenário: Cancelar o pedido quando a segunda entrega também falha
    Dado um pedido concluído com a primeira entrega falha e a nova tentativa em rota
    Quando o entregador registra uma falha na nova tentativa com o motivo "ausência do cliente"
    Então o pedido fica cancelado
    E nenhuma outra entrega é criada para o pedido

  Cenário: Cancelar o pedido na primeira falha quando o cliente recusa
    Dado um pedido concluído com uma entrega em rota
    Quando o entregador registra uma falha com o motivo "recusa do cliente"
    Então o pedido fica cancelado
    E nenhuma outra entrega é criada para o pedido

  Cenário: Exigir observação no registro da falha
    Dado uma entrega em rota
    Quando o entregador registra uma falha com o motivo "problema no transporte" e sem observação
    Então o registro é recusado com a mensagem "informe a observação da falha"

  Cenário: Liberar a carga do entregador na falha
    Dado o entregador "Ana" com carga em andamento 2 e uma entrega em rota
    Quando o entregador "Ana" registra uma falha na entrega com o motivo "problema no transporte"
    Então a carga em andamento do entregador "Ana" fica 1
