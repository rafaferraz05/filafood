# language: pt

@f2-rn4
Funcionalidade: Despacho para o entregador elegível de menor carga

  Contexto:
    Dado um pedido pronto com endereço na zona "Centro"

  Cenário: Escolher o entregador elegível de menor carga
    Dado o entregador "Ana" ativo, que atende a zona "Centro", com carga máxima 3 e carga em andamento 1
    E o entregador "Bruno" ativo, que atende a zona "Centro", com carga máxima 3 e carga em andamento 2
    Quando o sistema despacha o pedido
    Então o pedido é despachado para o entregador "Ana"

  Cenário: Desempatar pelo entregador que está há mais tempo sem receber entrega
    Dado o entregador "Ana" ativo, que atende a zona "Centro", com carga máxima 3 e carga em andamento 1
    E o entregador "Bruno" ativo, que atende a zona "Centro", com carga máxima 3 e carga em andamento 1
    E a última entrega do entregador "Bruno" é mais antiga que a do entregador "Ana"
    Quando o sistema despacha o pedido
    Então o pedido é despachado para o entregador "Bruno"

  Cenário: Não considerar entregador inativo
    Dado o entregador "Ana" inativo, que atende a zona "Centro", com carga máxima 3 e carga em andamento 0
    E o entregador "Bruno" ativo, que atende a zona "Centro", com carga máxima 3 e carga em andamento 2
    Quando o sistema despacha o pedido
    Então o pedido é despachado para o entregador "Bruno"

  Cenário: Não considerar entregador que não atende a zona do endereço
    Dado o entregador "Ana" ativo, que não atende a zona "Centro", com carga máxima 3 e carga em andamento 0
    E o entregador "Bruno" ativo, que atende a zona "Centro", com carga máxima 3 e carga em andamento 2
    Quando o sistema despacha o pedido
    Então o pedido é despachado para o entregador "Bruno"

  Cenário: Não considerar entregador com a carga cheia
    Dado o entregador "Ana" ativo, que atende a zona "Centro", com carga máxima 2 e carga em andamento 2
    Quando o sistema despacha o pedido
    Então o pedido permanece na fila de despacho

  Cenário: Manter o pedido na fila quando não há entregador elegível
    Dado que não há entregador ativo que atende a zona "Centro"
    Quando o sistema tenta despachar o pedido
    Então o pedido permanece na fila de despacho
