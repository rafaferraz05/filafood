# FilaFood

Protótipo acadêmico de um sistema de distribuição de pedidos entre unidades de uma rede de restaurantes.

## Objetivo da Parte 1

Executar em um único computador uma simulação na qual vários clientes enviam pedidos ao mesmo tempo. O servidor central escolhe uma unidade que atende o endereço e encaminha o pedido para sua fila.

## Arquitetura atual

```text
Clientes simultâneos (ExecutorService)
                |
                v
        ServidorCentral
                |
        escolhe pela região
                |
       +--------+--------+
       |                 |
 Restaurante Centro  Restaurante Boa Viagem
 BlockingQueue       BlockingQueue
       |                 |
 ExecutorService     ExecutorService
       |                 |
 Estoque synchronized   Estoque synchronized
```

- `ItemPedido`: nome e quantidade de um item.
- `Pedido`: identificador, cliente, endereço e itens.
- `ServidorCentral`: seleciona uma unidade disponível que atende o endereço.
- `Restaurante`: mantém uma fila segura e trabalhadores concorrentes.
- `Estoque`: verifica e retira itens dentro de métodos `synchronized`.
- `Main`: cria os dados e simula oito pedidos chegando simultaneamente.

## Concorrência e sincronização

Os pedidos dos clientes são enviados por um `ExecutorService` com quatro threads. Cada restaurante usa outro `ExecutorService` com duas threads para processar sua `BlockingQueue`.

O risco principal é duas threads consultarem e alterarem o mesmo estoque ao mesmo tempo. Sem sincronização, ambas poderiam enxergar a mesma quantidade e vender mais itens do que existem. O método `reservar` é `synchronized`, portanto apenas uma thread por vez executa a verificação e a retirada dos itens.

A `LinkedBlockingQueue` foi escolhida porque já é uma fila segura para uso por várias threads. Não foi criado um bloqueio manual para a fila.

Outros riscos e decisões:

- **Encerramento:** o recebimento e a mudança do estado `aberto` usam o mesmo monitor do restaurante. Assim, um pedido não pode ser aceito depois que os trabalhadores começam a encerrar.
- **Interrupção:** se um trabalhador for interrompido depois de reservar os itens, o pedido é cancelado e os itens são devolvidos ao estoque.
- **Erros escondidos:** o programa guarda os `Future` dos clientes e chama `get()`, permitindo que uma falha seja propagada para a thread principal.
- **Lista de restaurantes:** é preenchida antes do início dos clientes e não é alterada durante a simulação.
- **Deadlock:** o servidor central pode adquirir seu monitor e depois o monitor do restaurante ao encaminhar um pedido. Não existe o caminho inverso, do restaurante para o servidor central, portanto o fluxo atual não forma espera circular.
- **Fila sem limite:** a `LinkedBlockingQueue` atual é ilimitada. Isso é suficiente para os oito pedidos da demonstração, mas uma versão real deveria limitar a capacidade para aplicar backpressure.
- **Seleção da unidade:** nesta demonstração existe uma unidade por região. A menor fila já é considerada pelo servidor para permitir que outras unidades sejam adicionadas depois.

O servidor sincroniza somente a decisão rápida de roteamento. O preparo dos pedidos continua acontecendo paralelamente nos restaurantes.

## Comunicação e dados

Nesta etapa single-node, a comunicação entre o servidor central e os restaurantes acontece por chamadas de métodos Java. O arquivo `src/main/proto/pedido.proto` registra o contrato gRPC planejado para a evolução distribuída.

O método `EnviarPedido` é unário: recebe um `PedidoRequest` e devolve um `PedidoResponse`. Essa escolha é suficiente porque cada pedido gera uma única confirmação.

O `PedidoRequest` contém `pedido_id`, `cliente`, `endereco` e uma lista de itens com `nome` e `quantidade`. O `PedidoResponse` informa `recebido`, `restaurante_id` e `status`. Protobuf define os campos e tipos do contrato.

gRPC foi escolhido para a futura comunicação interna entre o servidor central e as unidades porque oferece um contrato explícito no arquivo `.proto`, geração de código e mensagens compactas. REST continuaria adequado para uma API pública acessada por navegador ou aplicativo, mas não é necessário no enlace interno desta etapa.

As dependências e o plugin do gRPC ainda não foram adicionados. Assim, esta primeira versão continua pequena e executável somente com Java.

## Como executar no Eclipse

1. Abra `Main.java`.
2. Clique com o botão direito no arquivo.
3. Escolha **Run As > Java Application**.
4. Observe pedidos sendo processados por threads diferentes e pedidos recusados por endereço ou falta de estoque.

## Decisões tomadas

- Aceita: Java 21 e Maven, porque Java é usado nas disciplinas e Maven permitirá adicionar gRPC depois.
- Aceita: `ExecutorService`, por controlar um conjunto pequeno de threads sem criar cada thread manualmente.
- Aceita: `LinkedBlockingQueue`, por ser uma fila pronta e segura para concorrência.
- Aceita: `synchronized` no estoque, por proteger a seção crítica com uma solução ensinada em aula.
- Rejeitada nesta etapa: Spring Boot, porque não é necessário para o protótipo local de concorrência.
- Adiada: execução real de gRPC, porque primeiro será validado o comportamento single-node.

## Uso de IA nesta etapa

A IA foi usada como copiloto para organizar a estrutura Maven, propor um protótipo mínimo e revisar riscos de concorrência. A equipe deve executar o programa, estudar cada classe e registrar dúvidas ou mudanças antes da apresentação.

Os prompts, decisões aceitas ou adiadas e suas justificativas estão registrados em `DIARIO-IA.md`.

## Evidências

- [Captura da execução](evidencias/execucao-filafood.png)
- [Diário de uso da IA](DIARIO-IA.md)

![Execução do protótipo FilaFood](evidencias/execucao-filafood.png)
