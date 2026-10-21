
> Gabriel Rocha: F1 e F2 (altas). Rafael Ferraz Silva: F3 (baixa), F4 (média) e F5 (baixa).
> F6 (Cardápio) e F7 (Compras) seguem o documento de requisitos `FilaFood_Funcionalidades_Cardapio_e_Compras.pdf`.
> Os números são globais neste repositório. No formulário individual de Rafael, F3/F4/F5
> correspondem, respectivamente, às funcionalidades 1/2/3 aprovadas com o professor.
> Abner: F10 e F11 (altas), do documento `FilaFood: funcionalidades e regras de negócio (3 e 4)`; no
> formulário individual, F10/F11 correspondem às funcionalidades 3/4, que já se referem a F1 e F2 por esses números.
> F8 (fidelidade) e F9 (refeições recorrentes), de Vitor Veloso, existem por enquanto só como protótipos em
> `docs/prototipos/fidelidade` e `docs/prototipos/recorrencia`, sem seção neste documento; por isso o sumário salta de F7 para F10.
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
| 6 | [Gerenciamento do cardápio, preço vigente e disponibilidade](#funcionalidade-6-alta) | alta | 6 |
| 7 | [Compra de insumos por fornecedor](#funcionalidade-7-alta) | alta | 6 |
| 10 | [Pedido com cupom de desconto e cálculo do total](#funcionalidade-10-alta) | alta | 6 |
| 11 | [Fila de preparo na cozinha e prazo prometido](#funcionalidade-11-alta) | alta | 6 |

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

---

# Funcionalidade 6 (alta)

| Título | Gerenciamento do cardápio, preço vigente e disponibilidade dos produtos |
| --- | --- |
| Descrição | O administrador da rede cadastra, pesquisa, altera, desativa e reativa produtos e categorias do cardápio. O gestor da unidade habilita os produtos que a unidade vende e define preços próprios com período de vigência. O sistema calcula o preço vigente de cada produto e a disponibilidade pelo saldo dos insumos da ficha técnica, e publica o cardápio da unidade para o cliente montar o pedido. |
| Entidades envolvidas | **Mantidas:** Produto, Categoria, ProdutoNaUnidade e PrecoDaUnidade. **Referenciadas:** Unidade, FichaTecnica, Insumo e ConfiguracaoPreparo. |
| Complexidade | alta: RN 4 resolve períodos de preço e sobreposições; RN 5 calcula as porções possíveis pelo insumo limitante da ficha técnica. |

**Definições usadas nas regras**

- **preço base:** preço do produto em toda a rede, definido no cadastro do produto.
- **preço da unidade:** preço próprio de um produto em uma unidade, com data inicial e data final de vigência opcional.
- **preço vigente:** preço que vale em um instante para um produto em uma unidade.
- **porções possíveis:** quantidade de unidades do produto que o saldo atual dos insumos permite preparar.

## Regras de negócio

### RN 1: Cadastro de produto

| Campo | Conteúdo |
| --- | --- |
| Descrição | Todo produto tem nome não vazio e único na rede (sem diferenciar maiúsculas de minúsculas), categoria ativa, descrição opcional de até 300 caracteres e preço base maior que zero, com duas casas decimais. O produto pode ser editado, desativado sem perder o histórico e reativado a qualquer momento. Produto inativo não aparece no cardápio e não pode ser habilitado em unidade. |
| Consultas | Consultar o produto pelo nome, para garantir a unicidade; consultar a categoria informada e seu estado. |
| Complexidade | média |

### RN 2: Categorias do cardápio

| Campo | Conteúdo |
| --- | --- |
| Descrição | Toda categoria tem nome não vazio e único na rede e ordem de exibição inteira maior ou igual a zero. A categoria pode ser editada, desativada e reativada. Categoria com produto ativo não pode ser desativada. Categoria inativa não aceita novos produtos. |
| Consultas | Consultar a categoria pelo nome, para garantir a unicidade; consultar os produtos ativos da categoria, para a desativação. |
| Complexidade | média |

### RN 3: Habilitação do produto na unidade

| Campo | Conteúdo |
| --- | --- |
| Descrição | O gestor da unidade habilita um produto ativo para venda na unidade. A habilitação exige ficha técnica do produto na unidade (RN 2 do controle de estoque) e configuração de preparo do produto na unidade. O gestor pode desabilitar o produto a qualquer momento. A desabilitação não altera pedidos já criados e o produto desabilitado deixa de aparecer no cardápio da unidade. |
| Consultas | Consultar o produto e seu estado; consultar a ficha técnica do produto na unidade; consultar a configuração de preparo do produto na unidade; consultar a habilitação existente do produto na unidade. |
| Complexidade | média |

### RN 4: Preço vigente do produto na unidade

| Campo | Conteúdo |
| --- | --- |
| Descrição | O gestor define o preço da unidade, maior que zero, com data inicial e data final opcional. Para o mesmo produto e a mesma unidade, os períodos de vigência não podem se sobrepor, e a ausência de data final conta como vigência sem fim. A data inicial não pode ser anterior à data atual. O preço vigente em um instante é o preço da unidade cujo período contém o instante; se não houver nenhum, vale o preço base. Preço de período já iniciado não pode ser alterado, apenas encerrado com data final. O preço vigente no momento da criação do pedido é o que o pedido grava no item. |
| Consultas | Consultar os preços da unidade do produto cujo período intersecta o período informado, para detectar sobreposição; consultar o preço da unidade vigente no instante; consultar o preço base do produto. |
| Complexidade | alta |

### RN 5: Disponibilidade calculada pelo estoque

| Campo | Conteúdo |
| --- | --- |
| Descrição | As porções possíveis do produto na unidade são o menor valor, entre os itens da ficha técnica, do resultado da divisão inteira do saldo do insumo pela quantidade do insumo por porção. O produto fica disponível quando as porções possíveis são maiores ou iguais a 1. Se algum insumo da ficha estiver inativo ou com saldo zero, as porções possíveis são zero e o produto fica indisponível. O cálculo apenas consulta o saldo: não reserva insumos, e a baixa continua ocorrendo ao iniciar o preparo (F1 RN 4). |
| Consultas | Consultar a ficha técnica do produto na unidade; consultar o saldo e o estado de cada insumo da ficha. |
| Complexidade | alta |

### RN 6: Cardápio publicado da unidade

| Campo | Conteúdo |
| --- | --- |
| Descrição | O cardápio da unidade lista os produtos ativos e habilitados na unidade, de categorias ativas, com o preço vigente e a disponibilidade. Os produtos são agrupados por categoria, na ordem de exibição das categorias, e ordenados por nome dentro de cada categoria. Produto indisponível aparece sinalizado e não pode ser adicionado ao pedido. Se a unidade não estiver disponível (inativa ou pausada), o cardápio informa que ela não recebe pedidos. |
| Consultas | Consultar os produtos habilitados da unidade; consultar as categorias ativas; consultar o preço vigente e a disponibilidade de cada produto; consultar o estado da unidade. |
| Complexidade | média |

## Protótipos da interface com o usuário

Protótipos estáticos em HTML, com `estilo.css`, navegação por links e um arquivo por estado, sem JavaScript. A identidade visual usa vermelho e amarelo; os dados de demonstração usam os nomes canônicos **Clássico**, **Vegetariano**, **Pão** e **Carne**.

- [Preços da unidade — estado principal](prototipos/cardapio/precos.html), [formulário](prototipos/cardapio/precos-formulario.html), [período sobreposto](prototipos/cardapio/precos-sobreposicao.html), [período iniciado](prototipos/cardapio/precos-periodo-iniciado.html) e [período encerrado](prototipos/cardapio/precos-encerrado.html).
- [Disponibilidade — estado principal](prototipos/cardapio/disponibilidade.html), [sem saldo](prototipos/cardapio/disponibilidade-sem-saldo.html) e [insumo inativo](prototipos/cardapio/disponibilidade-insumo-inativo.html).
- [Mapa geral dos protótipos](prototipos/index.html).

**Histórias:** CA-01 a CA-06. **BDD:** [preco-vigente.feature](../dominio-cardapio/src/test/resources/br/edu/cesar/filafood/dominio/cardapio/preco-vigente.feature) (`@f6-rn4`) e [disponibilidade.feature](../dominio-cardapio/src/test/resources/br/edu/cesar/filafood/dominio/cardapio/disponibilidade.feature) (`@f6-rn5`). O BDD desta etapa cobre as duas regras de maior complexidade; as demais regras seguem documentadas para as entregas seguintes.

---

# Funcionalidade 7 (alta)

| Título | Compra de insumos por fornecedor |
| --- | --- |
| Descrição | O gestor da unidade cadastra, pesquisa, altera, desativa e reativa fornecedores e mantém o catálogo de insumos que cada um vende, com preço, quantidade mínima e prazo. Para os insumos sinalizados “a repor”, o sistema sugere a quantidade a comprar e o fornecedor mais vantajoso. O gestor cria a ordem de compra, envia ao fornecedor, acompanha seus estados e registra o recebimento total ou parcial, que gera a entrada por compra no estoque da unidade. |
| Entidades envolvidas | **Mantidas:** Fornecedor, ItemDoCatalogo, OrdemDeCompra e ItemDaOrdem. **Referenciadas:** Insumo, Unidade e MovimentacaoEstoque. |
| Complexidade | alta: RN 3 projeta consumo, saldo e ordens abertas sobre a cobertura desejada; RN 4 compara fornecedores por custo total, prazo e desempate. |

**Definições usadas nas regras**

- **ordem de compra:** solicitação de insumos feita pela unidade a um fornecedor; não se confunde com o pedido do cliente.
- **cobertura:** número de dias de consumo que a compra deve cobrir além do ponto de pedido. É inteiro, maior ou igual a 1, e definido por unidade.
- **quantidade em ordens abertas:** soma, para um insumo, da quantidade ainda não recebida em ordens nos estados enviada e recebida parcialmente.
- **custo total:** preço unitário do catálogo multiplicado pela quantidade da compra.

## Regras de negócio

### RN 1: Cadastro de fornecedor

| Campo | Conteúdo |
| --- | --- |
| Descrição | Todo fornecedor tem nome não vazio, CNPJ com 14 algarismos e dígitos verificadores válidos, único na rede, e ao menos um contato (telefone com 10 ou 11 algarismos ou e-mail válido). O fornecedor pode ser editado, desativado sem perder o histórico e reativado. Fornecedor com ordem de compra aberta (enviada ou recebida parcialmente) não pode ser desativado, e fornecedor inativo não recebe novas ordens. |
| Consultas | Consultar o fornecedor pelo CNPJ, para garantir a unicidade; consultar as ordens abertas do fornecedor, para a desativação. |
| Complexidade | média |

### RN 2: Catálogo do fornecedor

| Campo | Conteúdo |
| --- | --- |
| Descrição | Cada item do catálogo liga o fornecedor a um insumo ativo e informa preço unitário maior que zero, quantidade mínima de compra maior que zero e prazo de entrega em dias, inteiro maior ou igual a 1. Um fornecedor tem no máximo um item por insumo. O item pode ser alterado ou removido do catálogo. A alteração de preço vale para as ordens criadas depois dela e não altera ordens já criadas. |
| Consultas | Consultar o insumo e seu estado; consultar o item do fornecedor para o insumo, para impedir duplicidade. |
| Complexidade | média |

### RN 3: Sugestão de quantidade a comprar

| Campo | Conteúdo |
| --- | --- |
| Descrição | Para cada insumo ativo no estado “a repor” (F1 RN 6), a quantidade bruta é o ponto de pedido mais o consumo médio diário multiplicado pelos dias de cobertura, menos o saldo, menos a quantidade em ordens abertas. Se a quantidade bruta for menor ou igual a zero, não há sugestão. Caso contrário, a quantidade sugerida é a bruta arredondada para cima ao múltiplo da quantidade mínima do fornecedor escolhido na RN 4. A sugestão apenas informa: nenhuma ordem é criada sem a confirmação do gestor. |
| Consultas | Consultar os insumos ativos a repor; consultar o saldo e o consumo médio diário de cada insumo; consultar a quantidade em ordens abertas de cada insumo; consultar a cobertura da unidade. |
| Complexidade | alta |

### RN 4: Escolha do fornecedor

| Campo | Conteúdo |
| --- | --- |
| Descrição | Entre os fornecedores ativos que têm o insumo no catálogo, escolhe-se o de menor custo total para a quantidade sugerida, já arredondada ao múltiplo da quantidade mínima de cada um. Só concorrem os que entregam dentro do prazo de entrega do insumo. Em caso de empate no custo total, vence o de menor prazo; persistindo o empate, o de nome anterior em ordem alfabética. Se nenhum fornecedor cumprir o prazo, escolhe-se o de menor prazo e a sugestão é sinalizada com o atraso previsto. Sem fornecedor no catálogo, informa-se que não há fornecedor para o insumo. |
| Consultas | Consultar os itens de catálogo do insumo com seus fornecedores e estados; consultar o prazo de entrega do insumo. |
| Complexidade | alta |

### RN 5: Ordem de compra e seus estados

| Campo | Conteúdo |
| --- | --- |
| Descrição | A ordem tem unidade, fornecedor ativo e ao menos um item. Cada item exige insumo ativo presente no catálogo do fornecedor e quantidade maior ou igual à mínima do catálogo, e o mesmo insumo não pode repetir na ordem. O preço unitário é gravado do catálogo no momento da criação e não muda depois. Os estados são rascunho, enviada, recebida parcialmente, recebida e cancelada. A ordem só pode ser alterada em rascunho. Não é permitido pular etapa nem alterar ordem recebida ou cancelada. O cancelamento é aceito em rascunho ou em ordem enviada sem recebimento. Toda transição grava data e hora. |
| Consultas | Consultar a ordem pelo id e seu estado; consultar o fornecedor e seu estado; consultar o item do catálogo de cada insumo da ordem. |
| Complexidade | média |

### RN 6: Recebimento e entrada no estoque

| Campo | Conteúdo |
| --- | --- |
| Descrição | O gestor registra o recebimento por item, com quantidade maior que zero e que não ultrapasse a quantidade ainda pendente do item. Cada recebimento grava uma entrada por compra (F1 RN 3) com o custo unitário gravado na ordem e soma a quantidade ao saldo. Se todos os itens ficarem completos, a ordem passa a recebida; se restar quantidade pendente, passa a recebida parcialmente e continua aberta. O recebimento de insumo inativo é recusado, e o recebimento registrado não pode ser alterado nem excluído. |
| Consultas | Consultar a ordem, seus itens e as quantidades já recebidas; consultar o insumo e seu estado. |
| Complexidade | média |

## Protótipos da interface com o usuário

Protótipos estáticos em HTML, com `estilo.css`, navegação por links e um arquivo por estado, sem JavaScript. A identidade visual usa vermelho e amarelo e os dados de demonstração usam **Pão** e **Carne**.

- [Fornecedores — estado principal](prototipos/compras/fornecedores.html), [fornecedor escolhido](prototipos/compras/fornecedor-escolhido.html), [empate](prototipos/compras/fornecedor-empate.html), [atraso previsto](prototipos/compras/fornecedor-atraso.html) e [sem fornecedor](prototipos/compras/sem-fornecedor.html).
- [Sugestões — estado principal](prototipos/compras/sugestoes.html), [sugestão calculada](prototipos/compras/sugestao-calculada.html) e [sem necessidade](prototipos/compras/sugestoes-sem-necessidade.html).
- [Mapa geral dos protótipos](prototipos/index.html).

**Histórias:** CP-01 a CP-06. **BDD:** [sugestao-quantidade.feature](../dominio-compras/src/test/resources/br/edu/cesar/filafood/dominio/compras/sugestao-quantidade.feature) (`@f7-rn3`) e [escolha-fornecedor.feature](../dominio-compras/src/test/resources/br/edu/cesar/filafood/dominio/compras/escolha-fornecedor.feature) (`@f7-rn4`). O BDD desta etapa cobre as duas regras de maior complexidade; as demais regras seguem documentadas para as entregas seguintes.

---

# Funcionalidade 10 (alta)

**Responsável: Abner.** Corresponde à Funcionalidade 3 do formulário individual. O formulário chama de
**recebido** o estado em que o pedido nasce; aqui ele é `enviado`, porque o glossário descarta "recebido"
como nome de estado (seção 3.3 de `docs/dominio.md`). O significado é o mesmo: pedido aceito pela unidade
e aguardando o início do preparo.

**Complexidade da funcionalidade: alta.** São 2 regras de complexidade alta (RN 3 e RN 4): a validação do
cupom, que cruza validade, unidade, valor mínimo, limites de uso e histórico do cliente, e o cálculo do
total, que combina desconto percentual ou fixo, teto de desconto e o valor do pedido usado na isenção da
taxa de entrega.

| Título | Pedido com cupom de desconto e cálculo do total |
| --- | --- |
| Descrição | O cliente monta o pedido com produtos da unidade, escolhe entrega ou retirada e pode informar um cupom. O sistema valida o cupom, calcula o subtotal, o desconto e o total, e registra o pedido com os preços do momento. O gestor mantém o cadastro de cupons e acompanha os pedidos por estado. O pedido percorre os estados enviado, em preparo, concluído, recusado e cancelado: a passagem para em preparo e a recusa por falta de saldo seguem a F1, e o cancelamento após a produção segue a F2. |
| Entidades envolvidas | **Mantidas:** Cupom, Pedido, ItemPedido, UsoCupom. **Referenciadas:** Produto, Unidade, Cliente, EnderecoEntrega e ZonaEntrega. |

**Definições usadas nas regras**

- **subtotal**: soma, para todos os itens do pedido, do preço unitário gravado multiplicado pela quantidade.
- **desconto**: valor abatido do subtotal pelo cupom aplicado.
- **valor do pedido**: subtotal menos o desconto. É o valor comparado ao mínimo da zona na F2 RN 3.
- **uso do cupom**: pedido que aplicou o cupom e cujo uso não foi liberado.

## Regras de negócio

### RN 1: Cadastro de cupom

| Campo | Conteúdo |
| --- | --- |
| Descrição | Todo cupom tem código único e não vazio, tipo (percentual ou valor fixo), valor maior que zero, validade com data inicial e final, valor mínimo de subtotal maior ou igual a zero, limite total de usos e limite de usos por cliente, ambos maiores que zero, e as unidades onde vale. O percentual fica entre 1 e 100. O cupom pode ter desconto máximo maior que zero e pode ser restrito ao primeiro pedido do cliente. Cupom desativado não é aceito em novos pedidos, mantém o histórico e pode ser reativado a qualquer momento. |
| Consultas | consultar o cupom pelo código, para garantir a unicidade. |
| Complexidade | baixa |

### RN 2: Montagem do pedido

| Campo | Conteúdo |
| --- | --- |
| Descrição | Todo pedido tem unidade, cliente, modalidade (entrega ou retirada) e ao menos um item. Cada item exige produto ativo e vendido na unidade, com ficha técnica na unidade, e quantidade inteira maior que zero. O mesmo produto pode aparecer em mais de um item. O preço unitário de cada item é gravado no momento da criação e não muda depois. Pedido para entrega exige endereço contido em alguma zona de entrega ativa. O pedido nasce no estado enviado. |
| Consultas | consultar o produto e seu preço; consultar a ficha técnica do produto na unidade; consultar as zonas que contêm as coordenadas do endereço. |
| Complexidade | média |

### RN 3: Validação e aplicação do cupom

| Campo | Conteúdo |
| --- | --- |
| Descrição | O pedido aceita no máximo um cupom. O cupom é aceito quando todos os critérios valem, avaliados nesta ordem: está ativo; a data atual está dentro da validade; a unidade do pedido está entre as unidades do cupom; o subtotal é maior ou igual ao valor mínimo; os usos totais do cupom são menores que o limite total; os usos do cliente com esse cupom são menores que o limite por cliente; e, se o cupom é restrito ao primeiro pedido, o cliente não tem pedido anterior fora dos estados recusado e cancelado. Se algum critério falha, o cupom é recusado com o motivo do primeiro critério violado e o pedido segue sem desconto, a critério do cliente. Se todos valem, grava-se um uso do cupom ligado ao pedido. |
| Consultas | consultar o cupom pelo código; consultar os usos do cupom; consultar os usos do cliente com o cupom; consultar os pedidos anteriores do cliente e seus estados; consultar o subtotal do pedido. |
| Complexidade | alta |

### RN 4: Cálculo do desconto e do total

| Campo | Conteúdo |
| --- | --- |
| Descrição | O desconto percentual é o subtotal multiplicado pelo percentual dividido por 100. O desconto de valor fixo é o próprio valor do cupom. Em ambos os tipos, o desconto é limitado ao desconto máximo do cupom, quando houver, e nunca passa do subtotal. O desconto é arredondado ao centavo, com metade para cima. O valor do pedido é o subtotal menos o desconto. Na retirada, o total é o valor do pedido. Na entrega, o total exibido na criação soma a taxa estimada pela F2 RN 3, calculada sobre o valor do pedido já com desconto; o total final usa a taxa gravada no despacho. Desconto, valor do pedido e total são gravados no pedido. |
| Consultas | consultar os itens do pedido; consultar o cupom aplicado; consultar a zona do endereço, para a taxa estimada. |
| Complexidade | alta |

### RN 5: Estados do pedido

| Campo | Conteúdo |
| --- | --- |
| Descrição | Os estados do pedido são enviado, em preparo, concluído, recusado e cancelado. A sequência normal é enviado, em preparo e concluído. A recusa ocorre na passagem para em preparo, quando falta saldo (F1 RN 4). Não é permitido pular etapa nem alterar pedido recusado ou cancelado. Toda transição grava data e hora. Pedido concluído só assume o estado cancelado, e apenas pelo desfecho de entrega da F2 RN 6. |
| Consultas | consultar o pedido pelo id e seu estado. |
| Complexidade | média |

### RN 6: Cancelamento do pedido e liberação do cupom

| Campo | Conteúdo |
| --- | --- |
| Descrição | Cancelar exige motivo. O cliente pode cancelar o pedido apenas no estado enviado. Pedido em preparo só é cancelado pelo gestor e devolve o estoque (F1 RN 5). Pedido recusado ou cancelado antes de concluído libera o uso do cupom, que deixa de contar nos limites da RN 3. Pedido cancelado depois de concluído mantém o uso do cupom consumido, porque o pedido já foi produzido. |
| Consultas | consultar o pedido e seu estado; consultar o uso do cupom ligado ao pedido. |
| Complexidade | média |

## Protótipos da interface com o usuário

Protótipos estáticos em HTML, com `estilo.css`, navegação por links e um arquivo por estado, sem JavaScript,
no mesmo padrão das telas de estoque e entrega. O pedido **#3107** da Ana atravessa as telas da F10 e a fila
de preparo da F11.

- Tela **Cupons**: lista com código, tipo, valor, validade, usos e limite, e estado (ativo, expirado ou desativado). Busca por código e filtro por estado. Botões Novo cupom, Editar, Desativar e Reativar. [Com dados](prototipos/cupom/cupons.html), sem cupons, formulário, erros (com código repetido), cadastrado, edição, confirmação, desativado e reativado.
- Tela **Novo pedido**: escolha de produtos e quantidades, modalidade, campo de cupom com botão Aplicar e indicadores de subtotal, desconto, valor do pedido, taxa estimada e total. O motivo da recusa do cupom aparece ao lado do campo. [Com itens](prototipos/cupom/novo-pedido.html), [cupom aplicado](prototipos/cupom/novo-pedido-cupom-aplicado.html), [cupom recusado](prototipos/cupom/novo-pedido-cupom-recusado.html), retirada, erros, estação inativa e os três envios.
- Tela **Pedidos**: lista com número, cliente, total e estado, com filtro por estado e por período e botão Cancelar. [Com dados](prototipos/cupom/pedidos.html), filtro, confirmação com motivo, sem motivo, cancelado e concluído que não cancela.
- Tela **Detalhe do pedido**: itens, cupom aplicado, valores e histórico de estados com data e hora. [Enviado](prototipos/cupom/pedido-detalhe.html), concluído, recusado, cancelado e total final.
- Formulários exibem os erros das RN 1, 2 e 3 ao lado do campo.

**Histórias:** CU-01 a CU-06. Nesta etapa a funcionalidade é entregue como protótipo navegável, documentado em
[CUPOM-E-PREPARO.md](prototipos/CUPOM-E-PREPARO.md).

---

# Funcionalidade 11 (alta)

**Responsável: Abner.** Corresponde à Funcionalidade 4 do formulário individual, com a mesma troca de
"recebido" por `enviado`.

**Complexidade da funcionalidade: alta.** São 2 regras de complexidade alta (RN 3 e RN 4): a estimativa
do prazo prometido, que combina carga, capacidade e paralelismo entre estações, e a ordem da fila de
preparo, com múltiplos critérios e desempate.

| Título | Fila de preparo na cozinha e prazo prometido |
| --- | --- |
| Descrição | O gestor mantém as estações de preparo da cozinha (por exemplo, chapa, fritadeira e montagem) e define, para cada produto, a estação e o tempo de preparo. Quando o pedido é recebido pela unidade, o sistema estima o prazo prometido ao cliente e coloca o pedido na fila de preparo. A cozinha vê a fila ordenada por prioridade, inicia o preparo, marca os itens como prontos e conclui o pedido. O gestor acompanha os pedidos atrasados e recebe alertas. O início do preparo aciona a baixa de estoque da F1, e o pedido concluído segue para a retirada ou para a fila de despacho da F2. |
| Entidades envolvidas | **Mantidas:** EstacaoPreparo, ConfiguracaoPreparo, ItemPreparo, AlertaAtraso. **Referenciadas:** Pedido, ItemPedido, Produto, Unidade e Insumo. |

**Definições usadas nas regras**

- **capacidade da estação**: quantidade de unidades de produto que a estação prepara ao mesmo tempo.
- **carga da estação**: soma, em minutos, do tempo de preparo dos itens da estação que ainda não estão prontos, em pedidos nos estados enviado ou em preparo. O tempo de preparo de um item é o tempo unitário multiplicado pela quantidade.
- **tempo do pedido na estação**: soma, para os itens do pedido da estação, do tempo de preparo unitário multiplicado pela quantidade, dividida pela capacidade e arredondada para cima, em minutos.
- **folga**: prazo prometido menos a hora atual, em minutos. É negativa quando o pedido está atrasado.
- **quantidade de itens**: número de itens do pedido, isto é, de linhas, como define a linguagem onipresente.

## Regras de negócio

### RN 1: Cadastro de estação de preparo

| Campo | Conteúdo |
| --- | --- |
| Descrição | Toda estação tem nome não vazio, único na unidade, e capacidade maior que zero. A estação pode ser desativada sem perder o histórico e reativada a qualquer momento. Estação inativa não recebe produtos novos, e estação com itens na fila não pode ser desativada. |
| Consultas | consultar os itens em fila da estação, para a desativação. |
| Complexidade | baixa |

### RN 2: Configuração de preparo do produto

| Campo | Conteúdo |
| --- | --- |
| Descrição | Em cada unidade, o produto tem uma única estação ativa e um tempo de preparo unitário em minutos, maior que zero. Produto sem configuração de preparo na unidade não pode ser vendido, da mesma forma que o produto sem ficha técnica (F1 RN 2). A mudança de estação ou de tempo vale para os pedidos criados depois dela e não altera prazos já gravados. |
| Consultas | consultar o produto; consultar a estação informada e seu estado. |
| Complexidade | média |

### RN 3: Estimativa do prazo prometido

| Campo | Conteúdo |
| --- | --- |
| Descrição | Ao receber o pedido, o sistema calcula, para cada estação envolvida, o fim estimado como a carga atual da estação mais o tempo do pedido na estação. As estações trabalham em paralelo, então o prazo prometido é o maior fim estimado entre as estações envolvidas, somado à hora de recebimento. O prazo é gravado no pedido e não é recalculado depois, nem quando a fila é reordenada pela RN 4. Se alguma estação envolvida estiver inativa, o pedido não é criado e o motivo é informado ao cliente. |
| Consultas | consultar os itens do pedido; consultar a configuração de preparo de cada produto; consultar a capacidade e a carga atual de cada estação envolvida. |
| Complexidade | alta |

### RN 4: Ordem da fila de preparo

| Campo | Conteúdo |
| --- | --- |
| Descrição | A fila reúne os pedidos no estado enviado e é ordenada pelos critérios abaixo, aplicados nesta sequência. Primeiro vêm os pedidos atrasados, do mais atrasado para o menos. Entre os demais, vem o de menor folga. Em empate de folga, vem o pedido com menos itens. Persistindo o empate, vem o pedido recebido há mais tempo. A ordem é recalculada a cada consulta, e a fila de cada estação segue a ordem dos pedidos que têm item nela. |
| Consultas | consultar os pedidos no estado enviado; consultar o prazo prometido e a hora de recebimento de cada um; consultar a quantidade de itens de cada pedido. |
| Complexidade | alta |

### RN 5: Início e conclusão do preparo

| Campo | Conteúdo |
| --- | --- |
| Descrição | Iniciar o preparo aciona a baixa automática da F1 RN 4. Se a baixa for recusada por falta de saldo, o preparo não inicia e o pedido é recusado com o motivo. Em preparo, cada item é marcado como pronto pela sua estação, e a hora é gravada. Quando todos os itens estão prontos, o pedido passa a concluído, com data e hora. O pedido de entrega concluído entra na fila de despacho da F2, e o de retirada fica disponível para o cliente. Não é permitido marcar item pronto em pedido que não esteja em preparo. |
| Consultas | consultar o pedido e seu estado; consultar os itens do pedido e as estações de cada um. |
| Complexidade | média |

### RN 6: Atraso e alerta ao gestor

| Campo | Conteúdo |
| --- | --- |
| Descrição | O pedido está atrasado quando está no estado enviado ou em preparo e a hora atual passou do prazo prometido. Quando o atraso passa de 15 minutos, o sistema grava um alerta para o gestor, uma única vez por pedido, com a estação de maior carga no momento. Pedido concluído ou cancelado deixa de gerar alerta. O sistema guarda a hora real de conclusão para comparar com o prazo prometido nos relatórios. |
| Consultas | consultar o pedido, seu estado e o prazo prometido; consultar os alertas já gravados do pedido; consultar a carga atual das estações. |
| Complexidade | média |

## Protótipos da interface com o usuário

Protótipos estáticos em HTML, no mesmo padrão da F10. Os números de prazo, folga, carga e alertas foram
calculados pelas regras desta funcionalidade, com os pedidos que chegam entre 18:40 e 19:38, e são os mesmos
em todas as telas.

- Tela **Estações**: lista com nome, capacidade, carga atual e estado, com botões Nova, Editar, Desativar e Reativar. [Com dados](prototipos/preparo/estacoes.html), sem estações, formulário, erros, cadastrada, edição, confirmação, desativada e desativação recusada.
- Tela **Preparo dos produtos**: produtos da unidade com estação e tempo de preparo, com botão Editar e aviso para produto sem configuração. [Com dados](prototipos/preparo/produtos.html), edição, tempo inválido, salvo, configurar e estação inativa.
- Tela **Fila de preparo**: pedidos na ordem da RN 4, com prazo prometido, folga e itens por estação, e as ações Iniciar preparo e Marcar item pronto. Pedidos atrasados aparecem com o selo atrasado. [Com dados](prototipos/preparo/fila.html), fila da estação, iniciado, baixa recusada, item pronto, concluído para entrega e para retirada, e vazia.
- Tela **Painel do gestor**: pedidos atrasados, alertas gravados e carga por estação. [Com dados](prototipos/preparo/painel.html), novo alerta e sem atrasos.
- Formulários exibem os erros das RN 1 e 2 ao lado do campo.

**Histórias:** CZ-01 a CZ-06. Nesta etapa a funcionalidade é entregue como protótipo navegável, documentado em
[CUPOM-E-PREPARO.md](prototipos/CUPOM-E-PREPARO.md).

## Limites de integração da F10 e da F11

A F10 e a F11 usam dados de outras funcionalidades: o cardápio e a ficha técnica (F6 e F1 RN 2), as zonas e a
taxa (F2 RN 3), a baixa e a devolução de estoque (F1 RN 4 e RN 5) e a fila de despacho (F2). Nos protótipos,
esses dados aparecem com os mesmos valores das telas dessas funcionalidades; a ligação real entre elas é etapa
futura.
