# Requisitos pendentes — F1 Estoque e F2 Entrega

**Base:** `docs/Spec.md` (rubrica), `docs/funcionalidades.md` (RNs), `docs/dominio.md` (vocabulário) e `docs/historia-do-usuario.md` (histórias ES/EN).

**Escopo:** F1 (estoque) e F2 (entrega), ambas de complexidade alta e sob responsabilidade de Gabriel Rocha. O alvo do Spec é uma aplicação **web com banco relacional** e funcionalidades **completas** — não basta a lógica de domínio em memória.

## 1. Situação atual

**Já existe**
- Descrição do domínio e linguagem onipresente em `docs/dominio.md`.
- Mapa da história do usuário e catálogo ES-01..ES-07 / EN-01..EN-09 em `docs/historia-do-usuario.md`.
- Regras de negócio de F1/F2 em `docs/funcionalidades.md`.
- Protótipos de alta fidelidade em HTML/CSS em `docs/prototipos/`.
- Cenários BDD e automação Cucumber de F1/F2, atualmente verdes (140 testes no reactor).
- Lógica de domínio de F1/F2 em memória nos módulos `dominio-estoque` e `dominio-entrega`, já com dinheiro em `BigDecimal`.
- Aplicação `app` com Spring Boot 4.1.2, Vaadin 25.3.4, JPA, Flyway e Actuator, subindo no perfil `dev`.
- Postgres 16 de desenvolvimento em `docker-compose.yml` e a migração `V001__insumo.sql` (`insumo` e `movimentacao_estoque`).
- Porta `Insumos` no domínio com adaptador JPA e mapper (`InsumoJpa`, `MovimentacaoEstoqueJpa`), validados pelo Hibernate contra a `V001` ao subir.
- Modelo tático de F1/F2 em `filafood.cml` e os ADRs 0001–0008.

**Parcial**
- DDD tático/operacional: entidades e regras existem; faltam camada de aplicação, persistência com IDs e integração real entre contextos.
- Modelo CML: tático de F1/F2 pronto; falta o ContextMap (nível estratégico), decidido no ADR 0008.
- Persistência: schema, Flyway, identidade do insumo, porta `Insumos` e adaptador JPA prontos; faltam teste de repositório, transações e os demais agregados.
- Arquitetura limpa: `app` tem os pacotes e quatro regras ArchUnit; faltam camada `application` e as regras de `*Jpa`.
- Camada web: tela inicial com os três perfis; faltam as telas de F1/F2.
- Tipos: dinheiro em `BigDecimal` no domínio de F1/F2; escala das quantidades e os módulos de F3–F7 seguem pendentes.

**Falta**
- Arquitetura limpa completa e 4+ padrões exigidos pelo Spec.
- Integração F1↔F4 (pedido/preparo), F2↔F4 (pedido concluído/falha), F1↔F6 (produto/ficha) e F2↔F3 (unidade/zonas).
- Transações, concorrência e histórico; ids e escopo por unidade já existem em F1.
- Testes de persistência, web, integração e concorrência.
- README de execução e documentação de arquitetura, ER e API.

## 2. Requisitos pendentes

### 2.1 Fundação Spring Boot e build

- [x] **G-01 — Criar o módulo da aplicação Spring Boot e o build agregado.**
  O POM raiz atual não agrega os módulos `dominio-*`. Definir se a aplicação será um novo módulo (`app`/`web`) ou o próprio raiz; alinhar versões e dependências.
  **Pronto quando:** um comando sobe a aplicação; `.\mvnw.cmd test` executa raiz + módulos; o protótipo da Parte 1 continua executável.

- [ ] **G-02 — Adicionar as dependências da stack.**
  Spring Boot (BOM/parent), `spring-boot-starter-web` ou Vaadin, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, driver do banco, Flyway ou Liquibase, `spring-boot-starter-test` e integração Cucumber/Spring.
  **Pronto quando:** build limpo resolve tudo; a aplicação inicia com o schema criado.
  **Feito:** Spring Boot 4.1.2, Vaadin 25.3.4, JPA, driver Postgres, Flyway e Actuator; a aplicação inicia e o Flyway cria o schema. **Falta:** `spring-boot-starter-validation`, `spring-boot-starter-test` e `cucumber-spring`.

- [ ] **G-03 — Configurar profiles e banco relacional.**
  `dev` (H2 ou Postgres local), `test` (H2 ou Testcontainers/Postgres) e `prod` (Postgres por variáveis de ambiente); configuração em `application.yml`; migrações versionadas.
  **Pronto quando:** a aplicação sobe em `dev` e `test`; a migração funciona em banco vazio; nenhuma credencial fica versionada.
  **Feito:** perfil `dev` com Postgres local, Flyway rodando em base vazia e credenciais fora do repositório (`.env`). **Falta:** perfis `test` (Testcontainers) e `prod`.

- [x] **G-04 — Decidir e registrar a camada de apresentação.**
  O Spec aceita Angular ou Vaadin. Registrar a decisão (ADR) e o contrato: se Angular, REST + OpenAPI + CORS; se Vaadin, views server-side.
  **Pronto quando:** a decisão está documentada e uma tela inicial carrega no navegador.

### 2.2 Persistência e modelo relacional

- [ ] **G-05 — Criar o schema de F1.**
  Tabelas para `unidade` (referência), `insumo`, `movimentacao_estoque`, `ficha_tecnica`, `item_ficha_tecnica` e `produto` (referência). Incluir FKs, `unidade_id`, ativo/inativo e histórico.
  **Pronto quando:** a migração cria o schema; o modelo cobre as RN 1–6.
  **Feito:** `insumo` e `movimentacao_estoque` na `V001`, com `unidade_id`, unique por unidade/nome e checks das RN 1–3. **Falta:** `unidade`, `ficha_tecnica`, `item_ficha_tecnica` e `produto`.

- [ ] **G-06 — Criar o schema de F2.**
  Tabelas para `entregador`, `entregador_zona`, `zona_entrega`, `entrega`, `transicao_entrega` e `pedido` (referência). Incluir tentativa, taxa, estado, motivo/observação e timestamps.
  **Pronto quando:** a migração cria o schema; o modelo cobre as RN 1–6.

- [ ] **G-07 — Implementar o mapeamento JPA sem contaminar o domínio.**
  Entidades JPA e repositórios na camada de adaptadores; portas de repositório no domínio/aplicação. Não colocar `@Entity`/`@Repository` nas classes de `dominio-*`.
  **Pronto quando:** o domínio compila sem Spring/JPA; os adaptadores implementam as portas; testes de repositório passam.
  **Feito:** porta `Insumos`, `InsumoJpa`/`MovimentacaoEstoqueJpa`, mapper e adaptador; o ArchUnit cobra domínio sem framework e o Hibernate valida o mapeamento contra a `V001` ao subir. **Falta:** teste de repositório com Testcontainers (D-13) e as demais portas.

- [x] **G-08 — Introduzir identidade e escopo por unidade.**
  IDs substitutos (UUID/Long) para insumo, ficha, entregador, zona e entrega; `unidade_id` em todos os dados de F1/F2. Restrições: insumo único por unidade/nome, ficha única por unidade/produto, item único por ficha/insumo, entrega única por pedido/tentativa.
  **Pronto quando:** duas unidades podem ter o mesmo nome de insumo sem colidir; consultas sempre filtram pela unidade.
  **Feito em F1:** `InsumoId` e `MovimentacaoId` (UUID), `unidadeId` no agregado e no estoque, teste de domínio provando que o mesmo nome em unidades diferentes não colide. **Falta:** ids de ficha e dos agregados de F2 (entram com as próximas fatias).

- [ ] **G-09 — Corrigir tipos e precisão.**
  Dinheiro com `BigDecimal` (`numeric(12,2)`), quantidades com escala definida, timestamps com fuso consistente e enums persistidos como texto.
  **Pronto quando:** não há `double` para dinheiro; testes cobrem arredondamento e comparação.
  **Feito:** dinheiro em `BigDecimal` no domínio de F1/F2 e `numeric(12,2)` na `V001`; enums como `varchar` com `check`. **Falta:** escala definida das quantidades.

- [ ] **G-10 — Garantir transações e atomicidade.**
  `@Transactional` na baixa de estoque (tudo ou nada), devolução, despacho e transições de entrega. Falha em qualquer item não grava movimentação parcial.
  **Pronto quando:** teste com falta de saldo em um insumo não altera nenhum saldo; falha no despacho não deixa entrega em estado inconsistente.

- [ ] **G-11 — Controlar concorrência.**
  `@Version` (optimistic locking) ou lock pessimista nos agregados críticos; impedir venda além do saldo, despacho duplo do mesmo pedido e devolução duplicada.
  **Pronto quando:** testes concorrentes não produzem oversell, duas entregas ativas para o mesmo pedido nem devolução em dobro.

- [ ] **G-12 — Preservar imutabilidade e auditoria.**
  `movimentacao_estoque` e `transicao_entrega` são append-only; não expor update/delete; toda transição grava data/hora e origem.
  **Pronto quando:** não existe endpoint/repositório que altere ou apague movimentação/transição; o histórico é consultável.

- [ ] **G-13 — Criar dados de demonstração canônicos.**
  Seed de `dev` com unidades, insumos, fichas, entregadores, zonas e pedidos dos protótipos (#1337, #1343, #1344 etc.), sem usar isso em produção.
  **Pronto quando:** banco novo sobe com o cenário dos protótipos e as telas mostram os mesmos objetos.

- [ ] **G-14 — Implementar as consultas de leitura.**
  Insumos a repor (saídas dos últimos 30 dias), fila de despacho por unidade com tempo de espera/motivo, carga e última entrega do entregador, histórico de movimentações e timeline da entrega.
  **Pronto quando:** as consultas têm testes e não carregam agregados inteiros sem necessidade.

### 2.3 Arquitetura, DDD e padrões

- [ ] **G-15 — Aplicar arquitetura limpa.**
  Separar `domain`, `application` (casos de uso/portas), `adapters` (JPA, web, integrações) e `config`. Dependências apontam para dentro.
  **Pronto quando:** teste ArchUnit impede `dominio-*` de importar Spring/JPA/web; controllers não usam entidades JPA diretamente.
  **Feito:** `app` com `adapter/in/web`, porta `Insumos` + adaptador JPA, quatro regras ArchUnit (três do `app` + pureza do domínio) e domínio sem framework. **Falta:** camada `application` e a regra de `*Jpa`.

- [ ] **G-16 — Criar casos de uso para todas as histórias.**
  ES-01..ES-07 e EN-01..EN-09, cada operação de escrita com caso de uso, DTO de entrada/saída, validação e limite transacional.
  **Pronto quando:** cada história tem um caso de uso identificável e um teste que a prova.

- [ ] **G-17 — Criar o modelo CML (DDD estratégico).**
  `filafood.cml` com os contextos Atendimento, Estoque, Entrega, Cardápio e Compras, e os relacionamentos (upstream/downstream, ACL, shared kernel quando fizer sentido).
  **Pronto quando:** o arquivo abre no Context Mapper sem erros e o README aponta para ele.
  **Feito:** `filafood.cml` tático de F1/F2, abrindo no Context Mapper, e README apontando. **Falta:** ContextMap e os contextos de F3–F11, conforme o ADR 0008.

- [ ] **G-18 — Documentar o nível operacional.**
  Mapear histórias/RNs → casos de uso → classes → endpoints/telas → testes. Explicitar o que é domínio, aplicação e adaptador.
  **Pronto quando:** a rastreabilidade fecha sem “órfãos” e cobre F1/F2.

- [ ] **G-19 — Implementar 4+ padrões do Spec.**
  Sugestão: **Strategy** (política de taxa e/ou escolha de entregador), **Observer** (eventos de pedido pronto, preparo cancelado e falha na entrega), **Template Method** (fluxo de transição da entrega/preparo) e **Decorator** (auditoria/autorização de casos de uso) ou **Proxy** (catálogo de produtos com cache). Cada padrão precisa de colaboração real, teste e justificativa.
  **Pronto quando:** 4 padrões estão implementados, testados e documentados em `docs/arquitetura.md`.

- [ ] **G-20 — Criar portas/adaptadores entre contextos.**
  F1 recebe itens/estado do pedido (F4); F2 recebe pedido pronto/endereço/valor (F4) e unidade/zonas (F3); F1 consulta produto/ficha (F6). Não acoplar os módulos de domínio diretamente.
  **Pronto quando:** integrações usam interfaces e adaptadores; testes usam fakes; nenhum `dominio-*` depende de outro `dominio-*`.

- [ ] **G-21 — Padronizar erros e validação.**
  Converter `RegraDeNegocioException` em 400/409 com `ProblemDetail`; erros de formulário por campo; não vazar stack trace.
  **Pronto quando:** API/view exibe a mensagem de negócio em português e o teste cobre cada família de erro.

- [ ] **G-22 — Definir o mínimo de perfis/segurança (decisão de escopo).**
  O Spec não exige autenticação, mas as telas separam gestor e entregador. Definir se haverá login real ou seleção de perfil para a demonstração; no mínimo, impedir que o entregador acesse cadastros do gestor.
  **Pronto quando:** a decisão está registrada e as rotas/telas respeitam o perfil.

### 2.4 F1 — Estoque (funcional e web)

- [ ] **G-23 — Insumos: listar, buscar, filtrar e CRUD.**
  Lista com saldo, mínimo, ponto de pedido e estado normal/a repor; busca por nome; filtro “a repor”; criar, editar, desativar e reativar com as validações da RN 1.
  **Pronto quando:** todas as ações dos protótipos de Insumos funcionam contra o banco.

- [ ] **G-24 — Entrada por compra e histórico.**
  Registrar entrada com quantidade/custo > 0, gravar movimentação imutável, somar saldo e listar o histórico do insumo.
  **Pronto quando:** a entrada aparece no histórico e altera o saldo; tentativa de alterar/excluir é recusada.

- [ ] **G-25 — Ficha técnica por unidade.**
  Manter ficha por produto/unidade; exigir insumo ativo, quantidade > 0 e sem duplicidade; produto sem ficha não pode ser vendido.
  **Pronto quando:** a ficha é consultável e editável; as recusas da RN 2 aparecem na tela.

- [ ] **G-26 — Baixa automática e devolução.**
  Ao iniciar o preparo, explodir a ficha, agregar por insumo, conferir todos os saldos e gravar tudo ou nada; ao cancelar em preparo, devolver exatamente o consumido.
  **Pronto quando:** o cenário de baixa/devolução passa contra o banco; cancelamento repetido não devolve duas vezes.

- [ ] **G-27 — Reposição.**
  Calcular consumo médio (30 dias), ponto de pedido e “a repor” apenas para insumo ativo; exibir na lista e no detalhe.
  **Pronto quando:** os valores batem com a RN 6 e o filtro “a repor” funciona.

- [ ] **G-28 — Telas de F1.**
  Implementar Insumos, Detalhe do insumo, Ficha técnica e Preparo do pedido com os estados dos protótipos (vazio, formulário, erros, confirmação e resultado).
  **Pronto quando:** é possível percorrer o Fluxo A dos protótipos na aplicação real, com dados persistidos.

### 2.5 F2 — Entrega (funcional e web)

- [ ] **G-29 — Entregadores: CRUD e estado.**
  Nome, zonas atendidas, carga máxima, ativo/inativo; inativar não cancela entregas em andamento; reativar.
  **Pronto quando:** cadastro/edição/inativação funcionam e a carga é consistente com as entregas ativas.

- [ ] **G-30 — Zonas e taxas: CRUD e estado.**
  Centro, raio, valor base, raio gratuito, valor por km excedente, teto e valor mínimo de pedido; desativar/reativar.
  **Pronto quando:** cadastro/edição/inativação funcionam e os exemplos de cálculo batem.

- [ ] **G-31 — Cálculo e gravação da taxa.**
  Escolher a zona de menor raio que contém o endereço; calcular base + excedente, limitar ao teto, zerar acima do mínimo; gravar na entrega no despacho e nunca recalcular.
  **Pronto quando:** a taxa persistida não muda quando a zona é editada depois.

- [ ] **G-32 — Fila de despacho e escolha do entregador.**
  Fila por unidade com zona, tempo de espera e motivo; escolher entregador ativo, que atende a zona e tem carga < máxima; menor carga; desempate pela última entrega mais antiga; sem elegível, permanece na fila.
  **Pronto quando:** a fila reflete o banco e o despacho automático respeita todas as regras.

- [ ] **G-33 — Estados da entrega.**
  `aguardando → despachada → em rota → entregue|falha`, sem pular etapa; despachada exige entregador, entregue exige recebedor, falha exige motivo e observação; cada transição grava data/hora.
  **Pronto quando:** transições inválidas são recusadas e a timeline é exibida.

- [ ] **G-34 — Falha, nova tentativa e cancelamento.**
  Falha libera a carga e devolve o pedido à fila com nova entrega em `aguardando`; apenas uma nova tentativa; segunda falha cancela o pedido; recusa do cliente cancela na primeira.
  **Pronto quando:** os dois desfechos (nova tentativa e cancelamento) estão persistidos e visíveis no despacho.

- [ ] **G-35 — Telas de F2.**
  Implementar Entregadores, Zonas e taxas, Despacho e entregas e Minhas entregas com os estados dos protótipos.
  **Pronto quando:** é possível percorrer os Fluxos B e C na aplicação real, incluindo falha e nova tentativa.

### 2.6 Integração entre contextos

- [ ] **G-36 — F1 ↔ F4 (pedido/preparo).**
  O início do preparo no contexto de atendimento dispara a baixa no estoque; o cancelamento durante o preparo dispara a devolução. A operação deve ser atômica ou compensável.
  **Pronto quando:** um pedido enviado e iniciado baixa os insumos; cancelado em preparo devolve; nenhum caminho grava baixa parcial.

- [ ] **G-37 — F2 ↔ F4 (pedido concluído/falha).**
  Pedido concluído entra na fila com entrega `aguardando`; falha devolve à fila ou cancela o pedido conforme a RN 6. Hoje `ConsequenciasDaFalha` só devolve um enum, sem atualizar o pedido.
  **Pronto quando:** o ciclo completo (concluído → despacho → falha → nova tentativa/cancelamento) atualiza o pedido e a entrega.

- [ ] **G-38 — F1 ↔ F6 e F2 ↔ F3.**
  F1 referencia produto/ficha do cardápio (F6); F2 referencia unidade e zonas da unidade (F3). Definir contratos e dados de teste quando esses contextos ainda não estiverem prontos.
  **Pronto quando:** não há nomes de produto/unidade hardcoded na produção; as consultas usam os contratos.

- [ ] **G-39 — Consistência, idempotência e eventos.**
  Definir se a integração é síncrona (modular monólito) ou por eventos/outbox. Garantir idempotência de cancelamento, devolução, despacho e falha; não perder evento se a transação falhar.
  **Pronto quando:** requisições repetidas não duplicam efeitos e há teste de falha no meio do fluxo.

### 2.7 Testes e qualidade

- [ ] **G-40 — Manter o BDD verde com Spring.**
  Adaptar a execução Cucumber para o contexto Spring (`cucumber-spring`), mantendo as features e os cenários existentes; usar banco de teste.
  **Pronto quando:** `.\mvnw.cmd -f ... test` executa BDD + testes Spring sem conflito de contexto.

- [ ] **G-41 — Testes de persistência e migração.**
  `@DataJpaTest`/Testcontainers para repositórios, constraints, mapeamentos e migração em banco vazio.
  **Pronto quando:** schema e consultas críticas têm testes; a migração roda do zero.

- [ ] **G-42 — Testes da camada web.**
  MockMvc/WebTestClient (ou Vaadin TestBench) para os fluxos principais, validações e mensagens de erro.
  **Pronto quando:** cada tela/endpoint crítico tem teste de sucesso e de recusa.

- [ ] **G-43 — Teste ponta a ponta F1+F2.**
  Um pedido completo: criar/enviar (F4) → baixar estoque (F1) → concluir → entrar na fila (F2) → despachar → retirar → entregar; e a variante com falha + nova tentativa/cancelamento.
  **Pronto quando:** o cenário roda contra o banco e a web, não só contra classes de domínio.

- [ ] **G-44 — Testes de concorrência.**
  Duas baixas simultâneas no mesmo insumo, dois despachos simultâneos para o mesmo pedido, cancelamento vs. início de preparo e devolução duplicada.
  **Pronto quando:** as invariantes se mantêm sob concorrência e o teste é reproduzível.

### 2.8 Documentação e entrega

- [ ] **G-45 — README de execução.**
  Como subir banco, rodar migrações, iniciar a aplicação, rodar testes, acessar a web e carregar os dados de demonstração.
  **Pronto quando:** uma pessoa clona o repositório e executa o passo a passo sem ajuda.

- [ ] **G-46 — Documentação de arquitetura.**
  Diagrama de camadas, mapa de contextos (CML), ER, decisões (ADR), padrões adotados e contrato da API (se REST).
  **Pronto quando:** os documentos explicam o código real e estão linkados no README.

- [ ] **G-47 — Atualizar a rastreabilidade.**
  Ligar ES-01..ES-07 e EN-01..EN-09 a casos de uso, telas/endpoints e testes; atualizar `docs/funcionalidades.md` e `docs/historia-do-usuario.md` com o que virou aplicação.
  **Pronto quando:** cada história tem implementação e prova localizáveis.

- [ ] **G-48 — Fechar a entrega do Spec.**
  Repositório público ou acessível ao `@profsauloaraujo`, evidências de execução, roteiro de demonstração e confirmação de que as funcionalidades não são só leitura.
  **Pronto quando:** o checklist da seção 4 está todo marcado.

## 3. Ordem sugerida

1. **G-01..G-04:** aplicação Spring Boot, banco, profiles e decisão web.
2. **G-05..G-14:** schema, JPA, IDs, transações, concorrência, seed e consultas.
3. **G-15..G-22:** camadas, casos de uso, CML, padrões, portas e erros.
4. **G-23..G-28:** F1 funcional + telas.
5. **G-29..G-35:** F2 funcional + telas.
6. **G-36..G-39:** integração real entre contextos.
7. **G-40..G-44:** testes de persistência, web, E2E e concorrência.
8. **G-45..G-48:** documentação e entrega.

## 4. Checklist do Spec.md

| Exigência | Situação | Requisitos |
| --- | --- | --- |
| Aplicação web + banco relacional | Parcial (app e banco de pé; faltam telas e schema completo) | G-01..G-14, G-28, G-35 |
| Funcionalidade completa (não só leitura) | Parcial (só domínio) | G-23..G-39 |
| Java + Spring Boot + JPA + Angular ou Vaadin | Parcial (Spring Boot, JPA e Vaadin; falta validação, Cucumber no app e teste de repositório) | G-01..G-07 |
| Descrição do domínio | Pronto | `docs/dominio.md` |
| Mapa da história do usuário | Pronto | `docs/historia-do-usuario.md` |
| Protótipos de alta fidelidade | Pronto | `docs/prototipos/` |
| Modelo CML | Parcial (tático de F1/F2; falta ContextMap) | G-17 |
| Cenários BDD + Cucumber | Parcial (só domínio) | G-40 |
| Níveis preliminar, estratégico, tático e operacional | Parcial (preliminar e tático; faltam estratégico e operacional) | G-17, G-18 |
| Arquitetura limpa | Parcial (pacotes, porta/adaptador e 4 regras ArchUnit; falta camada `application`) | G-15 |
| 4+ padrões do Spec | Falta | G-19 |
| Persistência com mapeamento objeto-relacional | Parcial (schema, entidades, mapper e adaptador; faltam teste de repositório e casos de uso) | G-05..G-14 |
| Camada de apresentação web | Parcial (tela inicial) | G-04, G-28, G-35 |

## 5. Riscos e cuidados

- **Refatoração para JPA:** manter o BDD existente verde; o domínio usa nome como chave e objetos aninhados, o que não mapeia diretamente para tabelas (o dinheiro já está em `BigDecimal`).
- **Integração entre contextos:** é o maior risco. F1 e F2 hoje recebem strings/objetos de teste; não há pedido real compartilhado nem atualização do pedido na falha da entrega.
- **Concorrência:** baixa de estoque e despacho precisam de transação e lock; os testes atuais são de uma thread só.
- **Build:** o POM raiz agrega tudo (ADR 0001); a Parte 1 saiu para `parte1/` e continua executável por `java -cp parte1/target/classes`.
- **Escopo de segurança:** autenticação não está no Spec; decidir cedo se a demonstração terá login ou seleção de perfil para não retrabalhar as telas.
