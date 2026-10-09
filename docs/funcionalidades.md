# FilaFood: funcionalidades e regras de negócio

> Funcionalidades sob responsabilidade de Gabriel Rocha: 2 funcionalidades de complexidade alta.
> As funcionalidades dos demais integrantes entram neste mesmo arquivo conforme forem
> documentadas.
>
> O vocabulário segue `docs/dominio.md`. Este é o nível tático do DDD.

## Sumário

| # | Funcionalidade | Complexidade | Regras |
| --- | --- | --- | --- |
| 1 | [Controle de estoque de insumos com ficha técnica e reposição](#funcionalidade-1-alta) | alta | 6 |
| 2 | [Gerenciar entregadores, despacho e entregas](#funcionalidade-2-alta) | alta | 6 |

---

# Funcionalidade 1 (alta)

**Complexidade da funcionalidade: alta.** São 2 regras de complexidade alta (RN 4 e RN 6): a
explosão da ficha técnica, que agrega o consumo de todos os itens do pedido por insumo, e o ponto
de pedido, que projeta o consumo médio sobre o prazo de entrega.

| Título | Controle de estoque de insumos com ficha técnica e reposição |
| --- | --- |
| Descrição | O gestor mantém o cadastro de insumos e a ficha técnica dos produtos, registra entradas por compra e consulta a posição de estoque e o histórico de movimentações. A baixa por venda é automática: ao iniciar o preparo de um pedido, o sistema explode a ficha técnica dos produtos vendidos, dá baixa nos insumos e devolve tudo se o preparo for cancelado. A partir do consumo médio diário e do prazo de entrega do insumo, o sistema calcula o ponto de pedido e sinaliza os insumos a repor. |
| Entidades envolvidas | **Mantidas:** Insumo, FichaTecnicaItem, MovimentacaoEstoque. **Referenciadas:** Produto, Unidade e Pedido. |

**Definições usadas nas regras**

- **saldo**: soma das entradas menos a soma das saídas de um insumo.
- **necessidade**: quantidade de cada insumo exigida por um pedido, calculada a partir da ficha técnica.

## Regras de negócio

### RN 1: Cadastro de insumo

| Campo | Conteúdo |
| --- | --- |
| Descrição | Todo insumo tem nome não vazio, unidade de medida, estoque mínimo maior que zero e prazo de entrega em dias, e pode ser desativado sem perder o histórico. Insumo inativo não entra em ficha técnica nem aceita movimentação, e pode ser reativado a qualquer momento. |
| Consultas | nenhuma consulta adicional. |
| Complexidade | baixa |

### RN 2: Ficha técnica do produto

| Campo | Conteúdo |
| --- | --- |
| Descrição | A ficha técnica de um produto vale para uma unidade e tem um item da ficha técnica por insumo consumido, com a quantidade. Cada item exige insumo ativo e quantidade maior que zero, e o mesmo insumo não pode aparecer duas vezes no mesmo produto na mesma unidade. Produto sem ficha técnica na unidade não pode ser vendido. |
| Consultas | consultar o produto; consultar os insumos informados. |
| Complexidade | média |

### RN 3: Entrada por compra

| Campo | Conteúdo |
| --- | --- |
| Descrição | Toda entrada informa insumo ativo, quantidade maior que zero e custo unitário maior que zero. A entrada grava uma movimentação do tipo entrada, com o custo unitário, e soma a quantidade ao saldo. Entrada não pode ser alterada nem excluída. |
| Consultas | consultar o insumo pelo id e seu estado. |
| Complexidade | baixa |

### RN 4: Baixa automática por explosão da ficha técnica

| Campo | Conteúdo |
| --- | --- |
| Descrição | Ao iniciar o preparo de um pedido, o sistema calcula a necessidade de cada insumo: para cada item do pedido, multiplica a quantidade pedida pela quantidade do insumo na ficha técnica do produto, somando as contribuições dos itens repetidos. Se o saldo cobrir toda a necessidade, o sistema grava uma movimentação de saída por insumo e o pedido entra em preparo. Se faltar saldo de qualquer insumo, nenhuma movimentação é gravada e o pedido é recusado com motivo. |
| Consultas | consultar os itens do pedido; consultar a ficha técnica dos produtos envolvidos; consultar o saldo dos insumos envolvidos. |
| Complexidade | alta |

### RN 5: Devolução ao cancelar o preparo

| Campo | Conteúdo |
| --- | --- |
| Descrição | O cancelamento de um pedido que já iniciou o preparo devolve ao estoque as mesmas quantidades lançadas na RN 4, gravando uma movimentação de entrada do tipo devolução. A devolução é aceita somente para pedido em preparo e é recusada para pedido já concluído, recusado ou cancelado. |
| Consultas | consultar o pedido e seu estado; consultar a saída original do pedido. |
| Complexidade | média |

### RN 6: Ponto de pedido por consumo médio

| Campo | Conteúdo |
| --- | --- |
| Descrição | O consumo médio diário de um insumo é a soma das saídas dos últimos 30 dias dividida por 30, mesmo com dias sem movimento. O ponto de pedido é o consumo médio diário multiplicado pelo prazo de entrega em dias do insumo, mais o estoque mínimo. O insumo fica "a repor" quando o saldo é menor ou igual ao ponto de pedido. |
| Consultas | consultar as movimentações de saída do insumo dos últimos 30 dias; consultar o insumo. |
| Complexidade | alta |

## Protótipos da interface com o usuário

- Tela **Insumos**: lista com saldo, estoque mínimo, ponto de pedido e estado (normal ou a repor). Busca por nome e filtro por estado. Botões Novo insumo, Editar, Desativar, Reativar e Entrada.
- Tela **Detalhe do insumo**: movimentações e ficha técnica dos produtos que o consomem.
- Formulários exibem os erros das RN 1, 2 e 3 ao lado do campo.

Os protótipos visuais de alta fidelidade ainda deverão ser desenhados pela equipe.

---

# Funcionalidade 2 (alta)

**Complexidade da funcionalidade: alta.** São 2 regras de complexidade alta (RN 3 e RN 4): o
cálculo da taxa, com valor base, excedente por quilômetro, teto e isenção, e a escolha do
entregador, com múltiplos critérios de elegibilidade e critério de desempate.

| Título | Gerenciar entregadores, despacho e entregas |
| --- | --- |
| Descrição | O gestor mantém o cadastro de entregadores e de zonas de entrega. Quando o pedido fica pronto, ele entra na fila de despacho com uma entrega em `aguardando`. No despacho, o sistema grava a taxa de entrega calculada pela zona do endereço e escolhe o entregador elegível de menor carga. Durante o deslocamento, o entregador registra a retirada, a entrega ou a falha. O gestor acompanha a fila de despacho e as entregas em andamento, e consulta o histórico por entregador e por período. |
| Entidades envolvidas | **Mantidas:** Entregador, ZonaEntrega, Entrega. **Referenciadas:** Pedido, EnderecoEntrega e Unidade. |

**Definições usadas nas regras**

- **distância**: distância em linha reta, em quilômetros, entre duas coordenadas.
- **carga em andamento**: entregas de um entregador nos estados `despachada` ou `em rota`.

## Regras de negócio

### RN 1: Cadastro de entregador

| Campo | Conteúdo |
| --- | --- |
| Descrição | Todo entregador tem nome não vazio, zonas atendidas, carga máxima maior que zero e estado `ativo` ou `inativo`. Entregador inativo não recebe novas entregas, e as que estão em andamento não são canceladas. |
| Consultas | nenhuma consulta adicional. |
| Complexidade | baixa |

### RN 2: Cadastro de zona de entrega

| Campo | Conteúdo |
| --- | --- |
| Descrição | Toda zona tem centro, raio em quilômetros, valor base, raio gratuito, valor por quilômetro excedente, teto e valor mínimo de pedido. Zona desativada não recebe novas entregas e pode ser reativada. |
| Consultas | nenhuma consulta adicional. |
| Complexidade | baixa |

### RN 3: Taxa de entrega calculada pela zona

| Campo | Conteúdo |
| --- | --- |
| Descrição | O endereço do pedido pertence à zona de menor raio que o contém. A taxa é o valor base da zona mais o valor por quilômetro que excede o raio gratuito, limitada ao teto da zona. A taxa é zerada quando o valor do pedido passa do mínimo da zona. Ela é gravada na entrega no momento do despacho e não é recalculada depois. |
| Consultas | consultar as zonas que contêm as coordenadas do endereço; consultar o valor do pedido. |
| Complexidade | alta |

### RN 4: Despacho para o entregador elegível de menor carga

| Campo | Conteúdo |
| --- | --- |
| Descrição | É elegível o entregador ativo, que atende a zona do endereço e cuja carga em andamento é menor que a carga máxima. Entre os elegíveis, escolhe-se o de menor carga; em empate, o que está há mais tempo sem receber entrega. Sem elegível, o pedido permanece na fila de despacho. |
| Consultas | consultar os entregadores ativos que atendem a zona; consultar a carga em andamento de cada um; consultar a última entrega concluída de cada um; consultar a carga máxima. |
| Complexidade | alta |

### RN 5: Estados e desfecho da entrega

| Campo | Conteúdo |
| --- | --- |
| Descrição | Os estados da entrega são aguardando, despachada, em rota, entregue e falha. Não é permitido pular etapa nem alterar entrega já finalizada. Registrar "despachada" exige entregador atribuído, registrar "entregue" exige o nome do recebedor e registrar "falha" exige o motivo. Toda transição grava data e hora. |
| Consultas | consultar a entrega pelo id e seu estado. |
| Complexidade | média |

### RN 6: Nova tentativa após falha

| Campo | Conteúdo |
| --- | --- |
| Descrição | Registrar falha exige o motivo (ausência do cliente, endereço não encontrado, recusa do cliente ou problema no transporte) e uma observação. A falha encerra a entrega, libera a carga do entregador e devolve o pedido à fila de despacho, onde uma nova entrega é criada em aguardando. O pedido admite apenas uma nova tentativa; se a segunda entrega também falhar, o pedido é cancelado. Na recusa do cliente o pedido é cancelado já na primeira falha. O pedido permanece concluído enquanto as entregas correm, e o cancelamento é o único estado que ele assume depois de produzido. |
| Consultas | consultar as entregas anteriores do pedido; consultar o pedido. |
| Complexidade | média |

## Protótipos da interface com o usuário

- Tela **Entregadores**: lista com nome, zonas atendidas, carga atual e estado, com botões Novo, Editar, Desativar e Reativar.
- Tela **Zonas**: zonas com centro, raio, valor base, raio gratuito, valor por quilômetro, teto e valor mínimo de pedido, com botões Novo, Editar, Desativar e Reativar.
- Tela **Despacho**: fila de pedidos prontos sem entregador, com zona, tempo de espera e motivo de estar na fila, e as entregas em andamento. O despacho é automático.
- Tela **Minhas entregas**: entregas atribuídas ao entregador, com a ação Registrar, que grava a retirada, a entrega ou a falha.
- Tela **Histórico do entregador**: entregas por período, falhas e tempo médio.

Os protótipos visuais de alta fidelidade ainda deverão ser desenhados pela equipe.
