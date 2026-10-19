# FilaFood: funcionalidades e regras de negócio

> Gabriel Rocha: F1 e F2 (altas). Rafael Ferraz Silva: F3 (baixa), F4 (média) e F5 (baixa).
> Os números são globais neste repositório. No formulário individual de Rafael, F3/F4/F5
> correspondem, respectivamente, às funcionalidades 1/2/3 aprovadas com o professor.
>
> O vocabulário segue `docs/dominio.md`. Este é o nível tático do DDD.

## Sumário

| # | Funcionalidade | Complexidade | Regras |
| --- | --- | --- | --- |
| 1 | [Controle de estoque de insumos com ficha técnica e reposição](#funcionalidade-1-alta) | alta | 6 |
| 2 | [Gerenciar entregadores, despacho e entregas](#funcionalidade-2-alta) | alta | 6 |
| 3 | [Gerenciar unidades de atendimento](#funcionalidade-3-baixa) | baixa | 4 |
| 4 | [Pedido do cliente](#funcionalidade-4-média) | média | 6 |
| 5 | [Gerenciar funcionários](#funcionalidade-5-baixa) | baixa | 4 |

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
| Descrição | Ao iniciar o preparo de um pedido, o sistema calcula a necessidade de cada insumo: para cada item do pedido, multiplica a quantidade pedida pela quantidade do insumo na ficha técnica do produto e acumula por insumo, somando as contribuições dos produtos que usam o mesmo insumo. Se o saldo cobrir toda a necessidade, o sistema grava uma movimentação de saída por insumo e o pedido entra em preparo. Se faltar saldo de qualquer insumo, nenhuma movimentação é gravada e o pedido é recusado com motivo. |
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
| Descrição | O consumo médio diário de um insumo é a soma das saídas dos últimos 30 dias dividida por 30, mesmo com dias sem movimento. O ponto de pedido é o consumo médio diário multiplicado pelo prazo de entrega em dias do insumo, mais o estoque mínimo. O insumo fica "a repor" quando o saldo é menor ou igual ao ponto de pedido. Insumo inativo não é monitorado: não entra em ficha técnica e não aceita movimentação, então não há consumo a repor. |
| Consultas | consultar as movimentações de saída do insumo dos últimos 30 dias; consultar o insumo. |
| Complexidade | alta |

## Protótipos da interface com o usuário

- Tela **Insumos**: lista com saldo, estoque mínimo, ponto de pedido e estado (normal ou a repor). Busca por nome e filtro por estado. Botões Novo insumo, Editar, Desativar, Reativar e Entrada.
- Tela **Detalhe do insumo**: dados do cadastro, entrada por compra, movimentações e os produtos que consomem o insumo.
- Tela **Ficha técnica**: produtos do cardápio com a disponibilidade e os itens de cada ficha.
- Tela **Preparo do pedido**: itens do pedido, insumos a baixar e cancelamento do preparo.
- Formulários exibem os erros das RN 1, 2 e 3 ao lado do campo.

Os protótipos de alta fidelidade estão em [`docs/prototipos/`](prototipos/).

---

# Funcionalidade 2 (alta)

**Complexidade da funcionalidade: alta.** São 2 regras de complexidade alta (RN 3 e RN 4): o
cálculo da taxa, com valor base, excedente por quilômetro, teto e isenção, e a escolha do
entregador, com múltiplos critérios de elegibilidade e critério de desempate.

| Título | Gerenciar entregadores, despacho e entregas |
| --- | --- |
| Descrição | O gestor mantém o cadastro de entregadores e de zonas de entrega. Quando o pedido fica pronto, ele entra na fila de despacho com uma entrega em `aguardando`. No despacho, o sistema grava a taxa de entrega calculada pela zona do endereço e escolhe o entregador elegível de menor carga. Durante o deslocamento, o entregador registra a retirada, a entrega ou a falha. O gestor acompanha a fila de despacho e as entregas em andamento. |
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
- Tela **Zonas e taxas**: zonas com centro, raio, valor base, raio gratuito, valor por quilômetro, teto e valor mínimo de pedido, com botões Novo, Editar, Desativar e Reativar, e os exemplos de cálculo da taxa.
- Tela **Despacho e entregas**, do gestor da unidade: fila de pedidos prontos sem entregador, com zona, tempo de espera e motivo de estar na fila, e as entregas em andamento com a linha do tempo das transições. O despacho é automático.
- Tela **Minhas entregas**, do entregador: entregas atribuídas a ele, com o registro da retirada, da entrega ou da falha.

Os protótipos de alta fidelidade estão em [`docs/prototipos/`](prototipos/).

---

# Funcionalidade 3 (baixa)

**Responsável: Rafael Ferraz Silva.** Corresponde à Funcionalidade 1 do formulário individual.
A pausa faz parte desta funcionalidade, conforme orientação do professor.

| Título | Gerenciar unidades de atendimento |
| --- | --- |
| Descrição | O administrador da rede cadastra, consulta e edita unidades, desativa e reativa seu cadastro e pausa ou retoma o recebimento de pedidos. Alterações de disponibilidade afetam novos pedidos; os já aceitos continuam. |
| Entidades envolvidas | Unidade: id, nome, região de atendimento, telefone, ativa, recebendoPedidos e motivoPausa. |
| Complexidade | baixa: regras sobre os atributos da própria Unidade. A seleção de destino é tratada na F4. |

### RN 1: Nome da unidade

| Campo | Conteúdo |
| --- | --- |
| Descrição | O nome deve ser não vazio. A edição preserva o identificador da unidade e valida todos os campos antes de gravar qualquer alteração. |
| Consultas | nenhuma consulta adicional para a validação. |
| Complexidade | baixa |

### RN 2: Região de atendimento

| Campo | Conteúdo |
| --- | --- |
| Descrição | A região de atendimento é obrigatória e pode ser editada. Ela é comparada com a região de entrega do pedido; não é a zona usada para calcular taxa. |
| Consultas | nenhuma consulta adicional para a validação. |
| Complexidade | baixa |

### RN 3: Telefone opcional

| Campo | Conteúdo |
| --- | --- |
| Descrição | O telefone é opcional. Se informado, deve conter DDD e 10 ou 11 algarismos, sem DDI. Espaços, parênteses, ponto, sinal de mais e hífen são retirados antes da validação. Letras são recusadas. |
| Consultas | nenhuma consulta adicional. |
| Complexidade | baixa |

### RN 4: Cadastro ativo e recebimento

| Campo | Conteúdo |
| --- | --- |
| Descrição | A unidade nasce ativa e recebendo pedidos. Desativar preserva o cadastro e interrompe novos recebimentos. Reativar deixa a unidade disponível e limpa eventual motivo de pausa. Somente uma unidade ativa pode ser pausada ou retomada. Pausar exige motivo não vazio de até 200 caracteres. Retomar habilita o recebimento e limpa o motivo. Disponível significa ativa e recebendoPedidos. A pausa e a desativação não cancelam pedidos já aceitos. |
| Consultas | nenhuma consulta adicional para validar a própria Unidade. |
| Complexidade | baixa |

**Histórias:** UN-01 a UN-04. **BDD:** [unidade.feature](../dominio-atendimento/src/test/resources/br/edu/cesar/filafood/dominio/atendimento/unidade/unidade.feature).
**Protótipos:** [Unidades](prototipos/atendimento/unidades/unidades.html): lista vazia, cadastro e edição, erros, confirmação de desativação, inativa, reativada, pausa, erros do motivo, pausada e retomada.

# Funcionalidade 4 (média)

**Responsável: Rafael Ferraz Silva.** Corresponde à Funcionalidade 2 do formulário individual.
Classificação média conforme orientação do professor: consulta unidades e pedidos, com regras de estado.

| Título | Montar, enviar e acompanhar pedido do cliente |
| --- | --- |
| Descrição | O cliente monta um rascunho, altera os dados de entrega e os itens, envia, consulta seus pedidos e cancela enquanto o preparo não começou. Vê a unidade escolhida ou o motivo de recusa. |
| Entidades envolvidas | Pedido, ItemDoPedido e referência ao Cliente; consulta Unidade e recebe o resultado do contexto de estoque. |
| Limites | Sem pagamento ou rastreamento em tempo real. O preparo e a devolução de insumos são da F1; despacho e entrega são da F2. |

### RN 1: Dados para enviar

| Campo | Conteúdo |
| --- | --- |
| Descrição | O envio exige nome do cliente, endereço textual, região de entrega e pelo menos um item. Dado inválido mantém o rascunho e apresenta a mensagem junto ao campo. O clienteId é a referência do dono; a autenticação não é implementada por este protótipo. |
| Consultas | consultar o rascunho do cliente. |
| Complexidade | média |

### RN 2: Itens e quantidades

| Campo | Conteúdo |
| --- | --- |
| Descrição | Cada item tem produto não vazio e quantidade inteira maior que zero. Um produto ocupa uma linha: acrescentar o mesmo produto soma a quantidade; editar a quantidade a substitui. É possível remover itens. Quantidade inválida não altera a composição anterior. |
| Consultas | nenhuma consulta adicional para validar os itens do próprio Pedido. |
| Complexidade | baixa |

### RN 3: Edição e descarte de rascunho

| Campo | Conteúdo |
| --- | --- |
| Descrição | Dados de entrega e itens só podem ser alterados em rascunho. Descartar remove o rascunho; não cria um pedido cancelado nem um registro de envio no histórico. Após o envio não se edita nem se descarta. |
| Consultas | consultar o pedido do cliente e seu estado. |
| Complexidade | média |

### RN 4: Seleção de unidade

| Campo | Conteúdo |
| --- | --- |
| Descrição | O envio escolhe uma unidade disponível cuja região de atendimento corresponda à região de entrega, desconsiderando maiúsculas e espaços nas extremidades. Sem unidade elegível, registra pedido recusado com motivo. A disponibilidade é conferida na confirmação de recebimento, para uma pausa concorrente não deixar passar novo pedido. O envio não consome estoque. A validação de saldo ocorre ao iniciar o preparo. |
| Consultas | consultar unidades disponíveis e suas regiões. |
| Complexidade | média |

### RN 5: Histórico e estados

| Campo | Conteúdo |
| --- | --- |
| Descrição | O cliente consulta somente seus pedidos enviados, com id, itens, unidade quando atribuída, estado e motivo de recusa quando houver. Rascunhos ficam fora desse histórico. Os estados são rascunho, enviado, em preparo, concluído, recusado e cancelado. Concluído significa preparo terminado; não significa entregue. O retorno de falta de insumo da F1 é apresentado como recusa com motivo, sem consumo parcial. |
| Consultas | consultar pedidos do cliente e seus estados. |
| Complexidade | média |

### RN 6: Cancelamento pelo cliente

| Campo | Conteúdo |
| --- | --- |
| Descrição | Somente pedido enviado pode ser cancelado pelo cliente. Verificação e mudança de estado são indivisíveis frente ao início do preparo: vence uma operação. Se cancelado, nenhum consumo pode ser solicitado. O registro permanece no histórico e novo cancelamento é recusado. A unidade pode cancelar um preparo pela F1, com devolução; isso não é uma permissão do cliente. |
| Consultas | consultar pedido do cliente e estado atual. |
| Complexidade | média |

**Histórias:** PE-01 a PE-06. **BDD:** [pedido.feature](../dominio-atendimento/src/test/resources/br/edu/cesar/filafood/dominio/atendimento/pedido/pedido.feature).
**Protótipos:** [Meu pedido](prototipos/atendimento/pedidos/novo-pedido.html), [Meus pedidos](prototipos/atendimento/pedidos/meus-pedidos.html) e [Detalhe](prototipos/atendimento/pedidos/pedido-enviado.html), com vazio, edição, erros, descarte, envio, recusas, preparo, conclusão e cancelamento.

# Funcionalidade 5 (baixa)

**Responsável: Rafael Ferraz Silva.** Corresponde à Funcionalidade 3 do formulário individual.

| Título | Gerenciar funcionários da unidade |
| --- | --- |
| Descrição | O gestor mantém nome, função e telefone dos funcionários da própria unidade. Pode cadastrar, consultar, editar, desativar e reativar sem perder os dados. |
| Entidades envolvidas | Funcionário: id, unidadeId, nome, função, telefone e ativo. |
| Complexidade | baixa: regras nos atributos do Funcionário. |

### RN 1: Identificação do funcionário

| Campo | Conteúdo |
| --- | --- |
| Descrição | O nome é obrigatório e tem até 100 caracteres. Alterar dados preserva o id e o vínculo à unidade. Erro de validação não grava uma edição parcial. |
| Consultas | nenhuma consulta adicional para validar os atributos. |
| Complexidade | baixa |

### RN 2: Função na unidade

| Campo | Conteúdo |
| --- | --- |
| Descrição | A função deve ser gestor, atendente ou cozinheiro. Administrador da rede é papel de acesso global, separado deste cadastro. O formulário do gestor não concede esse acesso. Entregador tem o cadastro próprio da F2. |
| Consultas | nenhuma consulta adicional. |
| Complexidade | baixa |

### RN 3: Telefone do funcionário

| Campo | Conteúdo |
| --- | --- |
| Descrição | Opcional; se informado, usa a mesma regra de DDD e 10 ou 11 algarismos da F3 RN 3. |
| Consultas | nenhuma consulta adicional. |
| Complexidade | baixa |

### RN 4: Desativação e reativação

| Campo | Conteúdo |
| --- | --- |
| Descrição | Desativar conserva o cadastro consultável; reativar preserva id e unidade. Cadastro de pessoa não configura executor, quantidade de threads ou trabalhador Java. |
| Consultas | nenhuma consulta adicional para validar os atributos. |
| Complexidade | baixa |

**Histórias:** FU-01 a FU-04. **BDD:** [funcionario.feature](../dominio-atendimento/src/test/resources/br/edu/cesar/filafood/dominio/atendimento/funcionario/funcionario.feature).
**Protótipos:** [Funcionários](prototipos/atendimento/funcionarios/funcionarios.html), com lista vazia, cadastro, edição, dados inválidos, nome longo, confirmação, desativado e reativado.

## Limites de integração desta etapa

Os HTMLs são demonstrações estáticas por links, como os do Gabriel. Não salvam os dados digitados.
O módulo `dominio-atendimento` executa as regras em Java puro com Cucumber e coleções em memória.
A chamada de início de preparo recebe uma operação de consumo: o monitor do Pedido protege essa
operação contra o cancelamento do cliente. Os testes usam um substituto do consumo para verificar
o protocolo; não ligam automaticamente os módulos de estoque e entrega. Adaptadores, transação
em banco, autenticação e a interface real permanecem etapas futuras. Coordenadas e valor do pedido,
necessários à F2, serão fornecidos pela integração com endereço e produto; não há geocodificação
ou cobrança neste protótipo.
