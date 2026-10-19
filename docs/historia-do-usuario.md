# FilaFood: mapa da história do usuário

> Nível preliminar do DDD. Gabriel Rocha responde por F1/F2; Rafael Ferraz Silva por F3/F4/F5.
>
> O vocabulário segue `docs/dominio.md`, e cada história aponta para a regra de negócio que a
> implementa, em `docs/funcionalidades.md`.

## 1. Como ler o mapa

- **Backbone** (seção 2): a jornada do usuário, na horizontal, em ordem cronológica.
- **Mapa** (seções 4 e 6): as histórias sob cada etapa do backbone, mais os cadastros que alimentam
  a etapa, na vertical, distribuídas por entrega. O que está mais acima entra primeiro.
- **Fatias**, ou entregas: dois incrementos por funcionalidade. A entrega 1 é o mínimo que já entrega valor ponta a ponta.
- **Catálogos** (seções 5 e 7): as histórias escritas por extenso, com ator e rastreabilidade.

Nenhuma funcionalidade é apenas leitura. Os cadastros permitem manutenção; o pedido permite
montagem, envio e cancelamento. Algumas histórias isoladas são consulta dentro desse ciclo.

## 2. Backbone do produto

| # | Etapa da jornada | Responsável |
| --- | --- | --- |
| 1 | Cliente monta, envia, acompanha e cancela enquanto permitido | Rafael Ferraz Silva (Funcionalidade 4) |
| 2 | Unidade recebe e prepara o pedido | pendente |
| 3 | Insumos são consumidos pela ficha técnica | Gabriel Rocha (Funcionalidade 1) |
| 4 | Estoque é reposto a partir do consumo | Gabriel Rocha (Funcionalidade 1) |
| 5 | Pedido pronto entra na fila de despacho | Gabriel Rocha (Funcionalidade 2) |
| 6 | Entregador é escolhido e a entrega é despachada | Gabriel Rocha (Funcionalidade 2) |
| 7 | Entrega é concluída ou falha | Gabriel Rocha (Funcionalidade 2) |

A etapa 2 continua com a operação da unidade, a ser integrada pelo grupo. Rafael também mantém
os cadastros de unidades (F3) e funcionários (F5), que alimentam essa jornada.

## 3. Atores

| Ator | Histórias |
| --- | --- |
| **Cliente** | Monta, envia, acompanha e cancela o próprio pedido antes do preparo |
| **Administrador da rede** | Mantém unidades, incluindo pausa e retomada |
| **Gestor da unidade** | Cadastros, movimentações de estoque e despacho |
| **Atendente ou cozinheiro** | Inicia o preparo, conclui, cancela ou recusa o pedido. As histórias de baixa automática e de devolução decorrem da ação dele |
| **Entregador** | Execução da entrega: retirada, entrega e falha |
| **Sistema** | Regras automáticas (consumo, taxa e escolha do entregador), expressas como história porque o gestor é quem se beneficia do resultado |

---

# 4. Mapa da Funcionalidade 1: Estoque

| Épico (backbone) | Entrega 1 | Entrega 2 |
| --- | --- | --- |
| **Cadastrar** | ES-01 Insumo · ES-02 Ficha técnica | |
| **Receber** | ES-03 Entrada por compra | |
| **Consumir** | ES-04 Baixa automática | ES-05 Devolução ao cancelar |
| **Repor** | | ES-06 Ponto de pedido · ES-07 Insumos a repor |

**Entrega 1** entrega o ciclo: cadastrar o insumo e a ficha técnica, receber por compra e consumir
automaticamente na venda.
**Entrega 2** cobre a correção e a reposição: devolver o que volta quando o preparo é cancelado e
saber quando comprar.

## 5. Catálogo da Funcionalidade 1

| ID | História | Ator | Regra | Entrega |
| --- | --- | --- | --- | --- |
| ES-01 | Como gestor da unidade, quero cadastrar, editar, desativar e reativar insumos com unidade de medida, estoque mínimo e prazo de entrega, para manter o catálogo correto. | gestor | RN 1 | 1 |
| ES-02 | Como gestor da unidade, quero manter a ficha técnica de cada produto da minha unidade, informando os insumos e as quantidades consumidas, para que a baixa por venda seja automática. | gestor | RN 2 | 1 |
| ES-03 | Como gestor da unidade, quero registrar a entrada de insumos por compra, com quantidade e custo unitário, para que o saldo fique atualizado. | gestor | RN 3 | 1 |
| ES-04 | Como gestor da unidade, quero que o consumo dos insumos seja lançado automaticamente quando o preparo de um pedido começar, para não vender o que não tenho. | sistema | RN 4 | 1 |
| ES-05 | Como gestor da unidade, quero que os insumos voltem ao estoque quando um preparo for cancelado, para não perder o que não foi consumido. | sistema | RN 5 | 2 |
| ES-06 | Como gestor da unidade, quero que o sistema calcule o ponto de pedido a partir do consumo médio e do prazo de entrega, para saber quando comprar. | sistema | RN 6 | 2 |
| ES-07 | Como gestor da unidade, quero ver a lista de insumos a repor com saldo e ponto de pedido, para agir antes de faltar. | gestor | RN 6 | 2 |

---

# 6. Mapa da Funcionalidade 2: Entregas

| Épico (backbone) | Entrega 1 | Entrega 2 |
| --- | --- | --- |
| **Cadastrar** | EN-01 Entregador · EN-02 Zona | |
| **Despachar** | EN-03 Taxa pela zona · EN-04 Entregador elegível · EN-05 Fila de despacho | |
| **Acompanhar** | | EN-06 Retirada e em rota · EN-07 Entrega confirmada · EN-08 Falha com motivo · EN-09 Nova tentativa |

**Entrega 1** já despacha de verdade: cadastra quem entrega, calcula a taxa e escolhe o entregador.
**Entrega 2** acompanha o deslocamento e fecha os desfechos.

## 7. Catálogo da Funcionalidade 2

| ID | História | Ator | Regra | Entrega |
| --- | --- | --- | --- | --- |
| EN-01 | Como gestor da unidade, quero cadastrar, editar, desativar e reativar entregadores com zonas atendidas e carga máxima, para controlar quem pode receber entregas. | gestor | RN 1 | 1 |
| EN-02 | Como gestor da unidade, quero manter as zonas de entrega com centro, raio, valor base, raio gratuito, valor por quilômetro, teto e valor mínimo de pedido, para que a taxa saia do cadastro e não de conta manual. | gestor | RN 2 | 1 |
| EN-03 | Como gestor da unidade, quero que a taxa de entrega seja calculada pela zona do endereço e gravada no despacho, para cobrar sempre o mesmo valor pelo mesmo destino. | sistema | RN 3 | 1 |
| EN-04 | Como gestor da unidade, quero que o pedido pronto seja despachado para o entregador elegível de menor carga, para não sobrecarregar um entregador enquanto outro está livre. | sistema | RN 4 | 1 |
| EN-05 | Como gestor da unidade, quero ver a fila de despacho com zona, tempo de espera e motivo de estar na fila, para saber o que está travado. | gestor | RN 4 | 1 |
| EN-06 | Como entregador, quero registrar a retirada e o início do deslocamento, para que a unidade e o cliente acompanhem. | entregador | RN 5 | 2 |
| EN-07 | Como entregador, quero registrar a entrega informando o nome do recebedor, para que a entrega fique comprovada. | entregador | RN 5 | 2 |
| EN-08 | Como entregador, quero registrar uma falha escolhendo o motivo, para explicar por que a entrega não foi feita. | entregador | RN 6 | 2 |
| EN-09 | Como gestor da unidade, quero que o pedido volte à fila quando houver falha, com apenas uma nova tentativa, para que não fique em laço. | sistema | RN 6 | 2 |

---

## 8. Rastreabilidade

| Regra | Histórias |
| --- | --- |
| F1 RN 1 | ES-01 |
| F1 RN 2 | ES-02 |
| F1 RN 3 | ES-03 |
| F1 RN 4 | ES-04 |
| F1 RN 5 | ES-05 |
| F1 RN 6 | ES-06, ES-07 |
| F2 RN 1 | EN-01 |
| F2 RN 2 | EN-02 |
| F2 RN 3 | EN-03 |
| F2 RN 4 | EN-04, EN-05 |
| F2 RN 5 | EN-06, EN-07 |
| F2 RN 6 | EN-08, EN-09 |
| F3 RN 1 | UN-01 |
| F3 RN 2 | UN-02 |
| F3 RN 3 | UN-03 |
| F3 RN 4 | UN-04 |
| F4 RN 1 | PE-01 |
| F4 RN 2 | PE-02 |
| F4 RN 3 | PE-03 |
| F4 RN 4 | PE-04 |
| F4 RN 5 | PE-05 |
| F4 RN 6 | PE-06 |
| F5 RN 1 | FU-01 |
| F5 RN 2 | FU-02 |
| F5 RN 3 | FU-03 |
| F5 RN 4 | FU-04 |

Toda regra de negócio tem pelo menos uma história, e toda história aponta para uma regra.

## 9. Mapas das funcionalidades de Rafael

| Atividade | Entrega 1 | Entrega 2 |
| --- | --- | --- |
| Preparar a operação | UN-01 Nome e manutenção; UN-02 Região; UN-03 Telefone; FU-01 Cadastro e edição; FU-02 Função; FU-03 Telefone | UN-04 Pausa e estados; FU-04 Desativação e reativação |
| Montar e enviar pedido | PE-01 Dados de entrega; PE-02 Itens; PE-03 Edição/descarte; PE-04 Seleção de unidade | |
| Acompanhar e cancelar | | PE-05 Histórico e estados; PE-06 Cancelamento |

Entrega 1 permite cadastrar a operação e enviar um pedido válido. Entrega 2 cobre disponibilidade,
manutenção dos estados e acompanhamento. Os cadastros de apoio não são etapas executadas pelo
cliente: os perfis permanecem separados.

| ID | História | Ator | Regra |
| --- | --- | --- | --- |
| UN-01 | Como administrador da rede, quero cadastrar, consultar e editar unidades, para manter seus dados corretos. | administrador | F3 RN 1 |
| UN-02 | Como administrador da rede, quero informar a região de atendimento da unidade, para encaminhar pedidos ao destino correto. | administrador | F3 RN 2 |
| UN-03 | Como administrador da rede, quero manter um telefone opcional válido, para contato com a unidade. | administrador | F3 RN 3 |
| UN-04 | Como administrador da rede, quero desativar, reativar, pausar e retomar unidades, para controlar novos recebimentos sem perder os cadastros. | administrador | F3 RN 4 |
| PE-01 | Como cliente, quero informar meus dados e endereço, para enviar um pedido que possa ser atendido. | cliente | F4 RN 1 |
| PE-02 | Como cliente, quero adicionar, alterar e remover itens, para montar o que desejo pedir. | cliente | F4 RN 2 |
| PE-03 | Como cliente, quero editar ou descartar um rascunho antes de enviar, para corrigir ou desistir da montagem. | cliente | F4 RN 3 |
| PE-04 | Como cliente, quero que uma unidade disponível da minha região receba o pedido, ou que o sistema explique a recusa. | cliente | F4 RN 4 |
| PE-05 | Como cliente, quero consultar somente meus pedidos e seus estados, para acompanhar o andamento. | cliente | F4 RN 5 |
| PE-06 | Como cliente, quero cancelar enquanto o preparo não começou, para desistir sem consumo de insumos. | cliente | F4 RN 6 |
| FU-01 | Como gestor, quero cadastrar, consultar e editar funcionários da minha unidade, para manter os dados da equipe. | gestor | F5 RN 1 |
| FU-02 | Como gestor, quero definir a função na unidade, para distinguir suas responsabilidades. | gestor | F5 RN 2 |
| FU-03 | Como gestor, quero manter um telefone opcional válido do funcionário, para contato. | gestor | F5 RN 3 |
| FU-04 | Como gestor, quero desativar e reativar funcionários sem apagar dados, para manter o cadastro ao longo do tempo. | gestor | F5 RN 4 |

Os protótipos e seus cenários estão mapeados em [ATENDIMENTO.md](prototipos/ATENDIMENTO.md).
