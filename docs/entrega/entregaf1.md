# Como avaliar a entrega

A F1 (estoque) está completa e é demonstrável **sem instalar nada** na máquina de quem avalia. Este
documento tem três partes: o que está pronto, como validar em cada nível e um roteiro de 5 minutos
para a demonstração.

## 1. O que está pronto

| Regra | O que funciona na aplicação |
| --- | --- |
| F1 RN 1 | cadastrar, **editar**, desativar e reativar insumo; nome único por unidade |
| F1 RN 2 | ficha técnica por unidade; insumo ativo, quantidade > 0, item único; produto sem ficha |
| F1 RN 3 | entrada por compra com custo; movimentação imutável; saldo somado e histórico |
| F1 RN 4 | baixa por explosão da ficha, agregando por insumo, em transação única (tudo ou nada) |
| F1 RN 5 | cancelamento devolve exatamente o consumido e não devolve duas vezes |
| F1 RN 6 | ponto de pedido por consumo de 30 dias; filtro "a repor" |

Telas: **Início**, **Insumos**, **Detalhe do insumo**, **Ficha técnica** e **Preparo do pedido**, no
layout e no CSS dos protótipos de `docs/prototipos/`.

## 2. Validar sem instalar nada

### Nível 1 — navegando no repositório

- [`docs/dominio.md`](../dominio.md): linguagem onipresente.
- [`docs/funcionalidades.md`](../funcionalidades.md): regras F1–F7.
- [`docs/arquitetura.md`](../arquitetura.md): camadas, DDD, padrões e modelo de dados.
- [`docs/prototipos/`](../prototipos/): protótipos de alta fidelidade (HTML/CSS).
- [`docs/requisitos-pendentes-f1-f2.md`](../requisitos-pendentes-f1-f2.md): rastreabilidade do que a
  F1 entrega e do que falta.
- Testes legíveis: `dominio-estoque/src/test/resources/` (Gherkin em pt-BR) e
  `app/src/test/java/` (casos de uso e arquitetura).

### Nível 2 — a suíte roda no GitHub Actions

Cada push em `main` dispara o workflow **verificação** (`.github/workflows/ci.yml`), com dois jobs:

1. **Build e BDD (sem Docker)** — `./mvnw -B -ntp test` na raiz: Parte 1, todos os `dominio-*` e o
   `app`. É a mesma suíte que roda na máquina do autor.
2. **Persistência com Testcontainers** — `./mvnw -pl app -am -Pintegracao test`, que sobe um Postgres
   real no runner e valida migração, mapeamento e constraints.

Abra a aba **Actions**: verde significa que o projeto compila e os cenários passam, sem que o avaliador
instale Java, Maven ou Docker. Os relatórios Cucumber ficam como artefato do primeiro job.

### Nível 3 — rodar no navegador com Codespaces (sem instalar nada)

1. Abra `https://codespaces.new/rafaferraz05/filafood` (é preciso conta GitHub; o repositório é público).
2. Aguarde o container subir. O `.devcontainer/devcontainer.json` já instala Java 21 e Docker, cria o
   `.env` e sobe o Postgres do `docker-compose.yml`.
3. No terminal integrado, rode a task **FilaFood: subir a aplicação** (`Terminal → Run Task`) ou o
   comando:

   ```bash
   ./mvnw -pl app -am spring-boot:run "-Dspring-boot.run.arguments=--server.port=8090"
   ```

4. A porta 8090 é encaminhada e aberta no navegador automaticamente. Navegue pelo menu da topbar.

### Nível 4 — rodar na própria máquina (opcional)

```powershell
cd C:\Users\pc\FilaFood
$env:DB_PASSWORD = ((Get-Content .env) | Where-Object { $_ -like 'DB_PASSWORD=*' }) -replace '^DB_PASSWORD=',''
.\mvnw.cmd -pl app -am spring-boot:run "-Dspring-boot.run.arguments=--server.port=8090"
```

Abre em `http://localhost:8090`. O banco é o Postgres do `docker-compose.yml`; o Flyway aplica as
migrações e o seed de demonstração no primeiro start.

## 3. Roteiro de demonstração (5 minutos)

O banco de dev já nasce com o cenário dos protótipos: Pão 42 un, Carne 8 kg, Queijo 15 kg, Tomate 6 kg,
três produtos no cardápio e o pedido **#1337** (2 Hambúrguer Clássico + 1 Duplo) em `ENVIADO`.

1. **Início → Insumos.** Dois badges no topo (insumos ativos e "a repor"), busca por nome e filtro por
   estado. "Carne" e "Tomate" aparecem como **a repor**.
2. **Editar.** Clique em "Editar" em qualquer insumo: o diálogo abre preenchido. Salve sem mudar o nome
   (RN 1). Tentar um nome já usado na unidade é recusado com mensagem.
3. **Entrada.** Em "Entrada" o detalhe mostra saldo, consumo médio, ponto de pedido e o histórico.
   Registre uma entrada, por exemplo `5` a `R$ 18,00`, e veja o saldo somar (RN 3).
4. **Ficha técnica.** Três produtos: Clássico com 4 itens e 42 porções cobertas, Duplo com 4, Batata
   **sem ficha**. Adicione um insumo que já está na ficha: recusa da RN 2. Remova o último item de um
   produto e ele volta ao estado "sem ficha".
5. **Preparo do pedido.** O pedido #1337 mostra 3 porções e os insumos a baixar: Pão 3, Carne 0,6,
   Queijo 0,12, Tomate 0,08 (quantidade pedida × quantidade da ficha). Clique em **Iniciar preparo e dar
   baixa** (RN 4): o pedido fica em preparo e os saldos caem exatamente isso.
6. **Cancelar o preparo** (RN 5): os mesmos valores voltam ao estoque e o pedido fica cancelado; tentar
   de novo é recusado. Se preferir, use **Concluir preparo**.
7. **Volte em Insumos.** Os saldos e o "a repor" refletem a movimentação (RN 6).

Para repetir do zero: `docker compose down -v` e subir a aplicação de novo — o Flyway reaplica as seis
migrações.

## 4. Evidências de execução

Saída do runner de fumaça (perfil `verificacao`) contra o Postgres de dev, em 12/11/2026:

```text
VERIFICACAO OK: 4 insumos no seed; cadastro, entrada, saldo, a-repor, desativar, reativar e editar conferidos.
VERIFICACAO OK: ficha técnica com 3 produtos, Clássico com 4 itens e 42 porções, Batata sem ficha;
                adicionar, recusar repetido e remover conferidos.
VERIFICACAO OK: preparo do pedido #1337 baixou Pão 3, Carne 0,6, Queijo 0,12 e Tomate 0,08.
VERIFICACAO OK: cancelamento do pedido #1337 devolveu Pão 3, Carne 0,6, Queijo 0,12 e Tomate 0,08
                e recusou o segundo cancelamento.
```

Suíte local: 35 testes no `dominio-estoque` (29 de BDD + 6 da política de reposição) e 28 no `app`.

## 5. Limitações conhecidas

- **F2 (entrega) ainda não existe**: entregadores, zonas, taxa e despacho estão mapeados no plano, não
  implementados.
- **Integração entre contextos** usa adaptadores de referência (ADR 0011): o pedido e o cardápio vêm de
  tabelas de demonstração até F4 e F6 chegarem.
- **Concorrência** (G-11) não está coberta: falta lock otimista e teste concorrente.
- **Build de produção do Vaadin** não está configurado; a aplicação roda em modo de desenvolvimento.
- O **CI** foi escrito e ainda não teve a primeira execução neste repositório; se o job falhar, o log
  aponta o ponto exato.