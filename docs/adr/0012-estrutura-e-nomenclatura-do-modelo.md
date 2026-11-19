# ADR 0012 — Estrutura e nomenclatura do modelo de referência

- **Status:** aceita
- **Data:** 2026-11-19
- **Contexto:** revisão do professor (`sgb-2025-01`); a arquitetura da F1 estava confusa sobre o que
  é repositório, interface e serviço, e o código não seguia o modelo publicado.

## Decisão

Adotar a estrutura em módulos e a nomenclatura do modelo de referência, com as adaptações do
FilaFood (só Vaadin, sem Angular nem MVC):

| Módulo | Papel |
| --- | --- |
| `pai` | pai do build: properties, BOMs (JUnit → Vaadin → Boot), `pluginManagement` |
| `dominio-*` | agregados, value objects, portas `*Repositorio` e serviços `*Servico` |
| `aplicacao` | serviços `*ServicoAplicacao`, DTOs `*Resumo`/`*ResumoExpandido`, portas `*RepositorioAplicacao` |
| `infraestrutura` | entidades `*Jpa`, `JpaMapeador`, `*JpaRepository`, implementações `*RepositorioJpa`, Flyway |
| `apresentacao-vaadin` | rotas `*Rota`, layout e `VaadinAplicacao` (composition root) |

Nomes: `X`/`XId` para agregado e identidade; `XRepositorio` para porta; `XServico` para serviço de
domínio; `XServicoAplicacao` para serviço de aplicação; `XRepositorioAplicacao` para porta de leitura;
`XJpa`/`JpaMapeador`/`XJpaRepository`/`XRepositorioJpa` na persistência; `XRota` na apresentação;
`package-info.java` em cada pacote.

A fiação é explícita em `VaadinAplicacao`: serviços de domínio e de aplicação são `@Bean` (sem
`@Service`), recebendo as portas; adaptadores e `JpaMapeador` continuam `@Component` escaneados.

## Desvios conscientes do modelo

- **Sem `dominio-compartilhado`**: ainda não há VO/evento comum entre contextos.
- **Sem `apresentacao-backend`/`apresentacao-frontend`**: a única apresentação é Vaadin.
- **Sem ModelMapper**: `JpaMapeador` faz o mapeamento explícito, sem reflexão.
- **Injeção por construtor** (o modelo usa campo com `@Autowired`): mantém as classes testáveis sem
  Spring.
- **`@Transactional` nos serviços de aplicação**: o modelo não tem movimentação multiagregado; a
  baixa/devolução precisa do limite transacional explícito.
- **`package-info.java`** documenta o pacote; não há anotação de pacote.

## Consequências

- Revisão fica navegável: domínio, aplicação, infraestrutura e rotas são pastas distintas; a fiação
  cabe em um arquivo.
- Comandos passam a usar `-pl apresentacao-vaadin -am` (aplicação) e `-pl infraestrutura -am
  -Pintegracao` (Testcontainers).
- O BDD de domínio continua sobre as fixtures em `src/test/.../infraestrutura/persistencia/memoria/`;
  migrar os cenários para implementações em memória das portas é evolução futura.
