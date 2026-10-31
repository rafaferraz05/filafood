# language: pt
# Especificação de aceitação; ainda sem classes de passos ou execução Cucumber.
Funcionalidade: F9 Refeições recorrentes

  @f9-planos
  Cenário: Consultar planos do cliente
    Dado que Rafael possui RC01 ativa
    Quando consultar suas refeições recorrentes
    Então deve visualizar somente os próprios planos com estado e próxima ocorrência

  @f9-planos-vazio
  Cenário: Consultar lista vazia
    Dado que o cliente não possui planos
    Quando consultar refeições recorrentes
    Então deve visualizar orientação para criar a primeira recorrência

  @f9-plano-novo
  Cenário: Preencher recorrência
    Dado que hoje é 20/10/2026 e o cliente informa itens dias endereço e limite válidos
    Quando revisar o plano iniciado em 21/10/2026
    Então deve visualizar a primeira geração futura sem criar pedido ou consumir estoque

  @f9-plano-erros
  Cenário: Recusar agenda inválida
    Dado que o formulário contém nome vazio quantidade zero nenhum dia limite zero e início passado
    Quando tentar salvar a recorrência
    Então o plano não deve ser criado e cada erro deve ser apresentado junto ao campo

  @f9-plano-revisar
  Cenário: Autorizar geração recorrente
    Dado que o plano revisado contém 2 Clássico às segundas quartas e sextas com limite 45
    Quando o cliente confirmar
    Então RC01 deve ficar ativa com primeira geração em 21/10/2026 às 10h

  @f9-plano-ativo
  Cenário: Consultar próxima ocorrência
    Dado que RC01 está ativa antes de 21/10/2026 às 10h
    Quando consultar o plano
    Então deve mostrar as ocorrências de 21 23 e 26 de outubro com geração às 10h

  @f9-plano-pausar
  Cenário: Confirmar pausa
    Dado que RC01 está ativa
    Quando o cliente solicitar pausa
    Então a confirmação deve informar que pedidos já gerados não serão cancelados

  @f9-plano-pausado
  Cenário: Pausar sem cancelar pedidos
    Dado que RC01 tem uma ocorrência já convertida em pedido
    Quando o cliente pausar o plano
    Então nenhuma nova ocorrência deve gerar pedido enquanto pausado e o pedido anterior deve continuar

  @f9-plano-retomado
  Cenário: Retomar sem recuperar passado
    Dado que RC01 esteve pausada em 21/10
    Quando o cliente retomar em 22/10/2026 às 15h
    Então a próxima geração deve ser 23/10 às 10h sem criar pedido retroativo

  @f9-ocorrencia-pular
  Cenário: Confirmar pulo de ocorrência
    Dado que a ocorrência de 21/10 ainda não foi gerada
    Quando o cliente solicitar pular somente essa data
    Então deve visualizar que a próxima geração será 23/10 e o plano continuará ativo

  @f9-ocorrencia-pulada
  Cenário: Pular uma única ocorrência
    Dado que RC01 possui ocorrências em 21 e 23 de outubro
    Quando o cliente confirmar o pulo de 21/10 antes das 10h
    Então somente 21/10 deve ficar pulada e a geração de 23/10 deve permanecer agendada

  @f9-plano-cancelar
  Cenário: Confirmar cancelamento de plano ativo
    Dado que RC01 está ativa
    Quando o cliente solicitar cancelamento
    Então a desistência deve preservar o estado anterior e a confirmação deve encerrar apenas as ocorrências futuras

  @f9-plano-cancelar-pausado
  Cenário: Confirmar cancelamento de plano pausado
    Dado que RC01 está pausada
    Quando o cliente solicitar cancelamento
    Então a desistência deve preservar o estado anterior e a confirmação deve encerrar apenas as ocorrências futuras

  @f9-plano-cancelado
  Cenário: Cancelar sem apagar histórico
    Dado que RC01 possui histórico de ocorrências
    Quando o cliente confirmar o cancelamento
    Então o plano deve ficar cancelado sem novas gerações nem exclusão de pedidos anteriores

  @f9-pedido-gerado
  Cenário: Gerar pedido uma única vez
    Dado que RC01 está ativa e 2 Clássico custam 39 dentro do limite 45
    Quando a ocorrência de 21/10 às 10h for processada duas vezes
    Então deve existir somente o pedido 2601 para RC01 e essa ocorrência sem consumo antecipado de estoque

  @f9-pedido-cancelar
  Cenário: Confirmar cancelamento de pedido recorrente
    Dado que 2601 está enviado e RC01 está ativa
    Quando o cliente solicitar cancelamento do pedido
    Então deve visualizar que o cancelamento não encerra RC01

  @f9-pedido-cancelado
  Cenário: Cancelar pedido mantendo plano
    Dado que 2601 ainda está enviado
    Quando o cliente confirmar seu cancelamento
    Então o pedido deve ficar cancelado e a ocorrência seguinte deve permanecer agendada

  @f9-prazo-encerrado
  Cenário: Impedir pulo após geração
    Dado que a ocorrência já foi convertida no pedido 2601
    Quando o cliente tentar pulá-la
    Então a operação deve ser recusada e o pedido existente deve ser exibido

  @f9-limite-excedido
  Cenário: Bloquear acima do limite
    Dado que 2 Clássico custam 48 e RC01 autoriza até 45 em produtos
    Quando processar a ocorrência de 21/10
    Então não deve criar pedido e deve registrar ocorrência não gerada com motivo sem cancelar o plano

  @f9-sem-unidade
  Cenário: Registrar recusa sem unidade
    Dado que RC01 está ativa mas nenhuma unidade da região está disponível
    Quando enviar o pedido da ocorrência
    Então F4 deve registrar 2602 recusado com motivo e RC01 deve continuar ativa

  @f9-pedido-com-recompensa
  Cenário: Anexar recompensa à ocorrência recorrente
    Dado que RC01 está ativa e Resgate #001 está pendente com saldo de pontos 40
    Quando a ocorrência gerar um pedido elegível
    Então Resgate #001 deve ser vinculada uma única vez e não deve aumentar o limite de produtos nem a taxa de entrega
