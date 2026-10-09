# FilaFood: mapa da história do usuário

> Nível preliminar do DDD. Este arquivo cobre as duas funcionalidades sob responsabilidade de
> Gabriel Rocha. O backbone do grupo é maior, e as etapas dos demais integrantes estão marcadas
> como pendentes na seção 2, para que o mapa seja completado depois.
>
> O vocabulário segue `docs/dominio.md`, e cada história aponta para a regra de negócio que a
> implementa, em `docs/funcionalidades.md`.

## 1. Como ler o mapa

- **Backbone** (seção 2): a jornada do usuário, na horizontal, em ordem cronológica.
- **Mapa** (seções 4 e 6): as histórias sob cada etapa do backbone, na vertical, distribuídas por
  entrega. O que está mais acima entra primeiro.
- **Fatias**, ou entregas: dois incrementos por funcionalidade. A entrega 1 é o mínimo que já entrega valor ponta a ponta.
- **Catálogos** (seções 5 e 7): as histórias escritas por extenso, com ator e rastreabilidade.

Nenhuma funcionalidade é apenas leitura: as duas têm cadastro, consulta, alteração, desativação e
reativação. Algumas histórias isoladas são consulta, mas sempre dentro de uma funcionalidade que
escreve.

## 2. Backbone do produto

| # | Etapa da jornada | Responsável |
| --- | --- | --- |
| 1 | Cliente monta o pedido | pendente |
| 2 | Unidade recebe e prepara o pedido | pendente |
| 3 | Insumos são consumidos pela ficha técnica | Gabriel Rocha (Funcionalidade 1) |
| 4 | Estoque é reposto a partir do consumo | Gabriel Rocha (Funcionalidade 1) |
| 5 | Pedido pronto entra na fila de despacho | Gabriel Rocha (Funcionalidade 2) |
| 6 | Entregador é escolhido e a entrega é despachada | Gabriel Rocha (Funcionalidade 2) |
| 7 | Entrega é concluída ou falha | Gabriel Rocha (Funcionalidade 2) |

As funcionalidades dos demais integrantes ocupam as etapas 1 e 2 e também a administração de
cadastros que alimenta todas elas.

## 3. Atores

| Ator | Histórias |
| --- | --- |
| **Gestor da unidade** | Cadastros, movimentações de estoque e despacho |
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
| **Consultar** | | EN-10 Histórico |

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
| EN-10 | Como gestor da unidade, quero consultar o histórico de entregas por entregador e por período, para avaliar desempenho e falhas. | gestor | RN 5 | 2 |

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
| F2 RN 5 | EN-06, EN-07, EN-10 |
| F2 RN 6 | EN-08, EN-09 |

Toda regra de negócio tem pelo menos uma história, e toda história aponta para uma regra.
