# FilaFood: descrição do domínio e linguagem onipresente

> **Nível preliminar do DDD.** Este documento fixa o vocabulário do domínio antes da modelagem
> (Context Mapper) e antes do código. Ele é a fonte da verdade para nomes de classes, campos,
> telas, cenários BDD e mensagens do sistema.
>
> **Status:** rascunho produzido com apoio de IA, pendente de revisão do grupo.

## 1. O domínio

FilaFood é uma rede de unidades de restaurante que atende pedidos por região de entrega.

O cliente monta um pedido informando o endereço de entrega e os itens desejados. O sistema escolhe
uma unidade disponível que atenda aquela região e encaminha o pedido para a fila dela. A unidade prepara
o pedido consumindo os insumos do próprio estoque, segundo a ficha técnica dos produtos vendidos.
Quando o pedido fica pronto, ele entra na fila de despacho e um entregador o leva até o cliente.

O domínio se organiza em quatro preocupações:

1. **Operação da unidade**: quem atende, o que está aberto e quem trabalha ali.
2. **Pedido do cliente**: montar, aplicar cupom, enviar, acompanhar e cancelar.
3. **Produção e suprimento**: ordenar a fila de preparo e prometer prazo, transformar produtos vendidos em
   consumo de insumos e repor o que falta.
4. **Entrega**: despachar, entregar e registrar o desfecho de cada entrega.

## 2. Atores

| Ator | O que faz |
|---|---|
| **Cliente** | Monta o pedido, envia, acompanha o andamento e cancela enquanto permitido. |
| **Administrador da rede** | Cadastra, consulta, altera, desativa, reativa, pausa e retoma o recebimento das unidades, e mantém o cardápio da rede. |
| **Gestor da unidade** | Mantém os cadastros da própria unidade: funcionários, insumos, ficha técnica, zonas de entrega, entregadores, cupons, estações de preparo e a configuração de preparo dos produtos. Acompanha os atrasos da cozinha. |
| **Atendente ou cozinheiro** | Recebe o pedido, inicia o preparo, marca os itens prontos na sua estação, conclui, cancela ou recusa. |
| **Entregador** | Registra a retirada, a entrega ou a falha. |

## 3. Linguagem onipresente

Esta é a lista de termos aceitos. A coluna **"não usar"** registra os nomes alternativos do
conceito que foram descartados: eles não podem nomear o conceito em documento, tela ou código
**novos**. A palavra descartada continua livre em outros sentidos, e o termo aceito prevalece
quando ela faz parte dele.

### 3.1 Organização

| Termo | Definição | Não usar |
|---|---|---|
| **Unidade** | Ponto de atendimento da rede, com região de atendimento e estoque próprios. | loja, filial, franquia |
| **Ativa** | Estado cadastral da unidade. Unidade inativa não recebe pedidos e seu cadastro é preservado. | habilitada, ligada |
| **Disponível** | Unidade que está **ativa** e **recebendo pedidos**. Apenas unidade disponível pode receber pedido. | aberta, livre, on-line |
| **Pausa** | Interrupção temporária do recebimento de pedidos de uma unidade ativa. | suspensão, bloqueio, fechamento |
| **Motivo de pausa** | Justificativa obrigatória, de até 200 caracteres, registrada ao pausar uma unidade. | observação, comentário |
| **Região de atendimento** | Área geográfica declarada por uma unidade, na qual ela aceita pedidos. Atributo **da unidade**, distinto da **zona de entrega**. | área de cobertura, setor, bairro |
| **Funcionário** | Pessoa vinculada a uma unidade, com uma função. Permanece consultável mesmo quando inativo. | empregado, colaborador, usuário |
| **Função** | Papel do funcionário da unidade: `gestor`, `atendente` ou `cozinheiro`. Administrador da rede é um papel de acesso separado, não concedido pelo cadastro do gestor. | cargo, posição, perfil |

### 3.2 Cardápio e produção

| Termo | Definição | Não usar |
|---|---|---|
| **Produto** | Item do cardápio da rede, vendido ao cliente dentro de um pedido. | prato, mercadoria, item |
| **Item do pedido** | Linha de um pedido: um produto e sua quantidade. | item, linha, entrada |
| **Ficha técnica** | Composição que define quais insumos, e em que quantidade, um produto consome no preparo de uma porção em uma unidade. É o que liga **produto** a **insumo**. | receita, composição, BOM |
| **Item da ficha técnica** | Linha da ficha técnica: um insumo e a quantidade consumida por porção. | linha, ingrediente |
| **Unidade de medida** | Unidade em que um insumo é controlado (kg, L, un). | métrica, medida |
| **Estação de preparo** | Posto da cozinha da unidade que prepara produtos, como chapa, fritadeira e montagem. Tem nome único na unidade, capacidade e estado `ativa` ou `inativa`. | setor, praça, bancada |
| **Capacidade da estação** | Quantidade de unidades de produto que a estação prepara ao mesmo tempo. | vazão, limite |
| **Configuração de preparo** | Estação e tempo de preparo de um produto em uma unidade. Produto sem configuração não pode ser vendido, como o produto sem ficha técnica. | roteiro, receita |
| **Tempo de preparo** | Minutos que a estação leva para preparar uma unidade do produto. | duração, tempo de forno |
| **Item de preparo** | Item do pedido como a cozinha o enxerga: produto, quantidade, estação e tempo de preparo gravados na chegada do pedido. Fica `pendente` até a estação marcá-lo `pronto`. | tarefa, comanda |
| **Fila de preparo** | Pedidos enviados que aguardam o início do preparo, ordenados por atraso, folga, quantidade de itens e chegada. Cada estação tem a sua fila, na mesma ordem. | fila da cozinha, backlog |
| **Prazo prometido** | Data e hora em que a unidade promete o pedido pronto, estimadas na chegada pela carga das estações e gravadas no pedido. Não é recalculado. | previsão, ETA, prazo de entrega |
| **Atrasado** | Pedido enviado ou em preparo cuja hora atual passou do prazo prometido. | vencido, estourado |
| **Alerta de atraso** | Registro gravado para o gestor quando o atraso de um pedido passa de 15 minutos, uma única vez por pedido, com a estação de maior carga no momento. | aviso, notificação |

### 3.3 Pedido

| Termo | Definição | Não usar |
|---|---|---|
| **Cliente** | Pessoa que monta, envia e acompanha pedidos. | usuário, consumidor, sócio |
| **Pedido** | Solicitação de um cliente, com um endereço de entrega e um ou mais itens, que percorre estados até um desfecho. | ordem, solicitação, requisição |
| **Região de entrega** | Área do endereço de entrega do pedido, comparada com a região de atendimento para escolher a unidade que o atende. | endereço, bairro, área |
| **Rascunho** | Estado do pedido ainda editável, que não foi enviado a nenhuma unidade. | carrinho, pendente |
| **Enviado** | Pedido aceito por uma unidade e aguardando o início do preparo. É o único estado em que o cliente pode cancelar. | aceito, recebido |
| **Em preparo** | A unidade já consumiu os insumos e está produzindo o pedido. | preparando, em produção |
| **Concluído** | O pedido foi produzido e seguiu para a entrega, ou ficou disponível para retirada na unidade. Não é desfecho: o pedido fica concluído enquanto a entrega corre, e passa a cancelado se ela falhar sem nova tentativa. | finalizado, pronto, entregue |
| **Recusado** | Desfecho em que o pedido não chegou a ser preparado, por nenhuma unidade atender a região ou por falta de saldo de insumo, sempre com **motivo de recusa** registrado. | rejeitado, negado |
| **Cancelado** | Desfecho: o pedido foi interrompido pelo cliente, pela unidade ou pelo sistema. O sistema cancela quando a entrega falha sem nova tentativa. | abortado, estornado |
| **Motivo de recusa** | Justificativa obrigatória registrada quando um pedido é recusado. | erro, observação |
| **Modalidade** | Como o pedido chega ao cliente: `entrega` ou `retirada`. | tipo de pedido, forma de entrega |
| **Retirada na unidade** | Modalidade em que o próprio cliente busca o pedido concluído no balcão da unidade. Não tem endereço nem taxa de entrega. | balcão, take-away, coleta |
| **Motivo do cancelamento** | Justificativa obrigatória registrada quando um pedido é cancelado. | observação |

São **desfechos** do pedido apenas `recusado` e `cancelado`. `concluído` e os estados anteriores
descrevem a produção; a fase de entrega é acompanhada pelo **estado da entrega**, e o pedido
permanece `concluído` enquanto ela corre.

### 3.3.1 Cupom

| Termo | Definição | Não usar |
|---|---|---|
| **Cupom** | Código que o cliente informa no pedido para obter desconto, do tipo percentual ou valor fixo, com validade, valor mínimo de subtotal, limites de uso e as unidades onde vale. O código é único na rede. | voucher, promoção, código promocional |
| **Uso do cupom** | Pedido que aplicou o cupom e cujo uso não foi liberado. Conta nos limites do cupom. É liberado quando o pedido é recusado ou cancelado antes de concluído. | resgate, consumo do cupom |
| **Desconto máximo** | Teto opcional do desconto de um cupom. | limite de desconto |

Subtotal, desconto e valor do pedido são grandezas calculadas pela F10 e ficam definidos na própria
funcionalidade, como pede a regra 6 da seção 7.

### 3.4 Estoque

| Termo | Definição | Não usar |
|---|---|---|
| **Insumo** | Material consumido na produção de um produto, mantido por uma unidade. Tem unidade de medida, estoque mínimo e prazo de entrega em dias. | matéria-prima, ingrediente |
| **Estoque** | Conjunto dos insumos mantidos por uma unidade, com os saldos respectivos. | depósito, almoxarifado |
| **Saldo** | Soma das entradas menos a soma das saídas de um insumo. | quantidade em estoque |
| **Compra** | Aquisição de insumos pelo gestor, que gera uma **entrada** de estoque. Nunca se refere ao pedido do cliente. | aquisição, pedido de compra |
| **Movimentação de estoque** | Registro **imutável** de alteração de saldo: é de entrada, quando soma, ou de saída, quando subtrai. | transação, lançamento |
| **Entrada** | Movimentação que soma ao saldo. Motivos: compra e devolução. | recebimento |
| **Saída** | Movimentação que subtrai do saldo. Motivo: consumo na produção. | baixa |
| **Devolução** | Motivo de entrada que recompõe o saldo quando um preparo é cancelado. | estorno, retorno |
| **Consumo** | Saída gerada pelo preparo de um pedido, calculada pela ficha técnica. | baixa, gasto |
| **Reposição** | Conjunto das regras de ponto de pedido e sinalização de insumo a repor. | ressuprimento, reabastecimento |
| **Estoque mínimo** | Quantidade que a unidade mantém como reserva. | estoque de segurança |
| **Consumo médio diário** | Média das saídas de um insumo nos últimos 30 dias, dividida por 30. | média de consumo, giro |
| **Ponto de pedido** | Consumo médio diário × prazo de entrega em dias + estoque mínimo. | ponto de ressuprimento |
| **Custo unitário** | Valor pago por uma unidade de medida do insumo, informado na **entrada por compra** e gravado na movimentação. O custo médio de estoque está fora de escopo. | preço |
| **A repor** | Estado de um insumo **ativo** cujo **saldo** está menor ou igual ao ponto de pedido. Insumo inativo não é monitorado. | crítico, acabando, baixo |

### 3.5 Entrega

| Termo | Definição | Não usar |
|---|---|---|
| **Entregador** | Pessoa que transporta pedidos de uma unidade até o cliente. Tem zonas atendidas, carga máxima e estado `ativo` ou `inativo`. | motoboy, driver, moto |
| **Zona de entrega** | Área definida por centro e raio, com valor base, raio gratuito, valor por quilômetro, teto e valor mínimo de pedido. Delimita a **taxa de entrega** e é distinta da região de atendimento. | área, região, setor |
| **EnderecoEntrega** | Endereço de entrega de um pedido, com a **região de entrega** e as coordenadas. A região escolhe a unidade; as coordenadas determinam a **zona de entrega**. | destino, local |
| **Carga máxima** | Número máximo de entregas simultâneas de um entregador. | capacidade, limite |
| **Taxa de entrega** | Valor cobrado pelo deslocamento, calculado pela zona e gravado na entrega no momento do despacho. | frete, valor de entrega |
| **Despacho** | Ato de escolher um entregador para um pedido pronto. | atribuição, designação |
| **Fila de despacho** | Conjunto de pedidos prontos que ainda não têm entregador. | pendências, backlog |
| **Entrega** | O deslocamento de um entregador para levar um pedido ao cliente. É criada em `aguardando` quando o pedido pronto entra na fila de despacho, recebe a **taxa** no despacho e termina em um desfecho. | corrida, rota, viagem |
| **Falha** | Desfecho em que a entrega não foi concluída, com motivo e observação registrados. | ocorrência, incidente, problema |

### 3.6 Estados

Nomes de estado aceitos no sistema, reunidos aqui para que não exista sinônimo entre telas.

| Termo | Definição | Não usar |
|---|---|---|
| **Estado da unidade** | `ativa` ou `inativa`, no cadastro, e `disponível` ou `pausada`, no recebimento. | status, situação |
| **Estado do funcionário** | `ativo` ou `inativo`. | status, situação |
| **Estado do insumo** | `ativo` ou `inativo`, no cadastro, e `normal` ou `a repor`, na reposição. O estado de reposição só existe para insumo ativo. | status, situação |
| **Estado do entregador** | `ativo` ou `inativo`. | status, situação |
| **Estado da zona** | `ativa` ou `inativa`. | status, situação |
| **Estado do cupom** | `ativo` ou `desativado`, no cadastro. Na lista de cupons, o cupom ativo cuja validade já terminou aparece como `expirado`. | status, situação |
| **Estado da estação** | `ativa` ou `inativa`. | status, situação |
| **Estado do item de preparo** | `pendente` ou `pronto`. | status, situação |
| **Estado do pedido** | `rascunho`, `enviado`, `em preparo`, `concluído`, `recusado`, `cancelado`. | status, situação |
| **Estado da entrega** | `aguardando`, `despachada`, `em rota`, `entregue`, `falha`. | status, situação |

### 3.7 Correspondência com o protótipo da Parte 1

O protótipo de console em `src/main/java` foi escrito antes deste documento e usa nomes próprios.
Ele não será alterado, porque é a entrega daquela etapa. Esta tabela serve de tradução entre os
dois vocabulários.

| No protótipo (`src/main/java/br/edu/cesar/filafood/`) | Termo do domínio |
| --- | --- |
| `Restaurante` | Unidade |
| `Restaurante.regiao` | Região de atendimento |
| `Restaurante.aberto` | Disponível |
| `Restaurante.fila` | Fila de pedidos da unidade |
| `Restaurante.receber(Pedido)` | Recebimento do pedido pela unidade |
| `ServidorCentral.enviarPedido` | Seleção de unidade e encaminhamento do pedido |
| `Pedido.cliente` | Cliente |
| `Pedido.endereco` | Endereço de entrega do pedido, que carrega a região de entrega |
| `Pedido.itens` e `ItemPedido` | Itens do pedido |
| `ItemPedido.nome` | Produto |
| `Estoque` | Estoque da unidade |
| `Estoque.reservar(Pedido)` e `Estoque.devolver(Pedido)` | Saída e Devolução |
| `CenarioDeCarga` | Não é do domínio, é cenário de teste |

Três diferenças de modelagem entre o protótipo e este documento também não exigem alterar o
protótipo:

1. O protótipo controla **produtos** direto no `Estoque` (retira "Hambúrguer", "Pizza"). O projeto
   separa **produto** (o que se vende) de **insumo** (o que se consome) e liga os dois pela
   **ficha técnica**.
2. O protótipo não representa o **estado do pedido**: o `Pedido` é imutável e o desfecho só existe
   nos contadores de cada unidade.
3. O protótipo não tem os cadastros de unidade, funcionário, insumo, ficha técnica, entregador e
   zona de entrega, e as duas unidades são criadas direto no `Main`.

## 4. Termos que se confundem

Estes pares já causaram ambiguidade na documentação das funcionalidades. Ficam resolvidos aqui:

| Par | Como distinguir |
|---|---|
| **Pedido** × **Compra** | Pedido é do **cliente** e contém produtos. A **compra** é o motivo de uma entrada de insumos, feita pelo gestor. A palavra compra nunca se aplica ao pedido do cliente. |
| **Produto** × **Insumo** | Produto é o que se **vende**. Insumo é o que se **consome** para produzir. A ficha técnica é a única ponte entre os dois. |
| **Região de atendimento** × **Zona de entrega** | Região é da **unidade** e diz onde ela aceita pedidos. Zona é da **entrega** e diz quanto se cobra pelo deslocamento. Uma unidade pode atender uma região e ainda assim ter entregas em várias zonas. |
| **Fila de preparo** × **Fila de despacho** | A fila de preparo é da **cozinha** e reúne pedidos enviados que ainda não começaram a ser preparados. A fila de despacho é da **entrega** e reúne pedidos concluídos que ainda não têm entregador. O pedido passa da primeira para a segunda quando é concluído, se for de entrega. |
| **Prazo prometido** × **Prazo de entrega do insumo** | O prazo prometido é a hora em que o **pedido do cliente** fica pronto, em minutos a partir da chegada. O prazo de entrega é do **insumo**, em dias, e alimenta o ponto de pedido. |
| **Retirada na unidade** × **Retirada registrada pelo entregador** | A retirada na unidade é a **modalidade** do pedido: o cliente busca. A retirada que o entregador registra na F2 é o momento em que ele pega o pedido para levar; ela só existe na modalidade entrega. |
| **Desconto** × **Isenção da taxa** | O desconto vem do **cupom** e reduz o valor do pedido. A isenção vem da **zona** e zera a taxa quando o valor do pedido passa do mínimo. Como a zona compara o valor já com desconto, um cupom pode fazer o pedido perder a isenção. |
| **Funcionário** × **Entregador** | Entregador é uma pessoa que transporta pedidos. Quando também for funcionário da unidade, são dois cadastros ligados, não um cadastro com dois nomes. |

## 5. Fluxos principais

### 5.1 Do pedido à baixa de insumos

```mermaid
flowchart TD
    A[Cliente monta o pedido] --> B{Unidade disponível<br/>e que atende a região?}
    B -- não --> C[Pedido recusado com motivo]
    B -- sim --> D[Pedido enviado para a fila da unidade]
    D --> E[Unidade calcula a necessidade<br/>pela ficha técnica]
    E --> F{Saldo cobre<br/>a necessidade?}
    F -- não --> C
    F -- sim --> G[Saída: consumo dos insumos]
    G --> H[Pedido concluído]
    H --> I[Entra na fila de despacho<br/>com uma entrega em aguardando]
    I --> J{Entregador elegível?}
    J -- não --> I
    J -- sim --> K[Despacho: taxa gravada<br/>e entrega despachada]
    K --> L[Entrega em rota]
    L --> M{Entregue?}
    M -- sim --> N[Entrega concluída]
    M -- não --> O{Segunda tentativa<br/>ou recusa do cliente?}
    O -- não --> I
    O -- sim --> P[Pedido cancelado]
    E -. cancelamento durante o preparo .-> Q[Devolução dos insumos ao estoque]
```

### 5.2 Da venda à reposição

1. A venda gera **saídas** de estoque, por insumo, através da ficha técnica.
2. As saídas dos últimos 30 dias alimentam o **consumo médio diário**.
3. O consumo médio, o prazo de entrega do insumo e o estoque mínimo definem o **ponto de pedido**.
4. Quando o **saldo** de um insumo **ativo** cai até o ponto de pedido, ele fica **a repor** e aparece como tal na tela de insumos. Insumo inativo não é monitorado, então fica fora da lista de reposição.

### 5.3 Cadastros de apoio

Unidade, funcionário, produto, insumo, ficha técnica, zona de entrega e entregador são mantidos
pelos papéis de administração. São insumos de dados para os fluxos acima.

### 5.4 Fronteiras das funcionalidades de Rafael

- F3 mantém unidades, incluindo pausa e retomada. Unidade nasce disponível; reativar a torna
  disponível e limpa o motivo. Pedidos já aceitos não são cancelados por pausa ou desativação.
- F4 mantém rascunho, envio, histórico e cancelamento pelo cliente. Cancelamento do cliente
  só ocorre antes do preparo e não devolve estoque: ele ainda não foi consumido.
- F5 mantém pessoas da unidade. Não altera pools ou threads, nem cria administradores da rede.
- Cancelar preparo pela unidade continua sendo a F1 RN 5 e devolve insumos.
- Dados inválidos mantêm o rascunho; falta de unidade elegível ou falta de saldo gera recusa
  registrada. Só pedidos que passaram pela tentativa de envio aparecem no histórico do cliente.
- O endereço textual e sua região são informados no pedido. Coordenadas e valor do pedido
  são dados de integração com entrega; não são calculados pelos protótipos estáticos.

### 5.5 Do pedido com cupom à conclusão do preparo

```mermaid
flowchart TD
    A[Cliente monta o pedido e informa o cupom] --> B{Cupom vale?<br/>ativo, validade, unidade,<br/>mínimo, limites, 1º pedido}
    B -- não --> C[Cupom recusado com o motivo<br/>o cliente decide seguir sem desconto]
    C --> D
    B -- sim --> D[Desconto, valor do pedido<br/>e taxa estimada sobre o valor com desconto]
    D --> E{Estações envolvidas<br/>estão ativas?}
    E -- não --> F[Pedido não é criado<br/>motivo informado ao cliente]
    E -- sim --> G[Pedido enviado com prazo prometido<br/>e uso do cupom gravado]
    G --> H[Fila de preparo<br/>atrasados, folga, itens e chegada]
    H --> I{Baixa de insumos<br/>aceita? F1 RN 4}
    I -- não --> J[Pedido recusado<br/>uso do cupom liberado]
    I -- sim --> K[Em preparo: estações<br/>marcam os itens prontos]
    K --> L{Modalidade}
    L -- entrega --> M[Concluído: fila de despacho da F2]
    L -- retirada --> N[Concluído: disponível para retirada]
    H -. atraso acima de 15 min .-> O[Alerta ao gestor<br/>com a estação de maior carga]
```

### 5.6 Fronteiras das funcionalidades de Abner

- A F10 parte do pedido com unidade definida e grava os valores: preços do momento, desconto, valor do
  pedido e total. O rascunho, a região e a seleção de unidade continuam na F4.
- A F10 permite o mesmo produto em mais de um item; o rascunho da F4 soma a quantidade numa linha só.
  Os dois convivem porque a F10 recebe os itens já compostos.
- A F11 calcula o prazo prometido na chegada do pedido e dispara a baixa da F1 RN 4 ao iniciar o preparo.
  Recusa por falta de saldo libera o uso do cupom (F10 RN 6). O cancelamento do preparo pelo gestor
  continua devolvendo os insumos pela F1 RN 5.
- A habilitação de produto na unidade (F6 RN 3) já exige a configuração de preparo mantida pela F11 RN 2.
- O pedido de entrega concluído entra na fila de despacho da F2; a taxa estimada pela F10 vem da F2 RN 3,
  e o total final usa a taxa gravada no despacho.

## 6. Fora de escopo

Declarado explicitamente para evitar expectativa errada:

- Pagamento, faturamento e nota fiscal.
- Rastreamento do entregador em tempo real.
- Avaliação de pedidos e de entregadores.
- Aplicativo móvel nativo.
- Integração real com mapas ou com API de distância. O cálculo usa coordenadas e distância em linha reta.
- Lote, validade, vencimento e custo médio de estoque.
- Perda e ajuste de inventário.
- Agrupamento de pedidos em uma mesma entrega e turno por entregador. A retirada do pedido na unidade
  saiu desta lista e entrou no escopo com a F10 e a F11.

## 7. Regra de uso do vocabulário

1. Se o termo não está na seção 3, ele não é do domínio. Discuta antes de usar.
2. Se o termo está na coluna "não usar", ele não pode nomear aquele conceito em documento,
   tela ou código **novos**. Em outros sentidos, a palavra continua livre.
3. A linguagem onipresente vale para o **código novo** do projeto web: nomes de classes, métodos,
   campos, tabelas e colunas.
4. O protótipo da Parte 1 **não será renomeado**. Ele permanece com os nomes que já tem, e a
   correspondência está na seção 3.7.
5. Ao encontrar um termo novo, ele entra **aqui primeiro** e só depois no código.
6. Grandezas calculadas por uma regra, como a **necessidade** da funcionalidade 1, ficam definidas
   no bloco "Definições usadas nas regras" da própria funcionalidade e não entram na seção 3.
