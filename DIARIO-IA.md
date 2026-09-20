# Diário de uso da IA

Este documento registra como a IA foi usada como copiloto no Projeto - Parte 1. A equipe continua responsável por executar, estudar e explicar o código.

## 1. Organização da entrega

**Pedido feito à IA:** analisar a atividade, os slides e a rubrica e indicar o que precisava ser construído.

**Sugestão recebida:** começar com um protótipo single-node em Java, depois acrescentar concorrência local e somente depois executar a comunicação gRPC.

**Decisão:** aceita.

**Justificativa:** mantém o primeiro protótipo pequeno, permite testar cada etapa e segue a evolução proposta na disciplina.

## 2. Escolha das tecnologias

**Pedido feito à IA:** verificar se Spring Boot era obrigatório e comparar o projeto com os materiais das duas disciplinas.

**Sugestão recebida:** usar Java 21 e Maven agora; deixar Spring Boot, banco de dados e interface web para a disciplina de Requisitos de Software.

**Decisão:** aceita.

**Justificativa:** Spring Boot não é necessário para demonstrar threads, filas e sincronização em um único computador. Maven será útil posteriormente para adicionar gRPC.

## 3. Configuração do ambiente

**Pedido feito à IA:** ajudar desde GitHub, Git e Eclipse e corrigir os erros de configuração.

**Problemas encontrados:** projeto inicialmente configurado como Java 8, ausência de codificação explícita e erro do Eclipse ao localizar o esquema XML do Maven.

**Correções aceitas:** configurar Java 21, UTF-8 e usar o esquema Maven já disponível no catálogo local do Eclipse.

## 4. Protótipo single-node

**Pedido feito à IA:** criar os próximos passos com código simples, funcional, explicável e próximo dos conteúdos dos slides.

**Sugestões aceitas:**

- `ExecutorService` para simular clientes e trabalhadores concorrentes;
- `LinkedBlockingQueue` para a fila segura de cada restaurante;
- `synchronized` na reserva do estoque;
- chamadas de métodos Java na etapa local;
- manter o contrato `pedido.proto` preparado para a próxima evolução.

**Sugestões rejeitadas ou adiadas:**

- Spring Boot: adiado porque não é necessário nesta entrega;
- banco de dados: adiado para a disciplina de Requisitos de Software;
- gRPC executável: adiado até a validação do protótipo single-node;
- semáforos e locks explícitos: não usados porque `BlockingQueue` e `synchronized` resolvem os riscos atuais com menos complexidade.

## 5. Verificações realizadas

- compilação das classes com Java 21;
- execução de pedidos enviados por quatro threads de clientes;
- processamento por duas threads em cada restaurante;
- recusa de endereço não atendido;
- recusa quando o estoque é insuficiente;
- confirmação de que o estoque final não fica negativo;
- revisão independente solicitada com base nos slides e na rubrica.

## 6. Pontos que a equipe deve saber explicar

1. Por que uma `BlockingQueue` pode ser acessada por várias threads.
2. Qual é a seção crítica do estoque.
3. O que poderia acontecer sem o `synchronized`.
4. Qual é a diferença entre as threads dos clientes e as threads dos restaurantes.
5. Por que o método gRPC planejado é unário.
6. Por que Spring Boot ainda não foi usado.
7. Como o servidor central escolhe uma unidade.

## 7. Evidências a anexar na entrega

- link ou exportação das conversas relevantes com a IA;
- este diário atualizado quando novas decisões forem tomadas;
- captura da execução no console;
- divisão do que cada integrante apresentará.

## 8. Revisão independente

**Pedido feito ao revisor:** analisar o projeto sem presumir que o código estava correto, usando a rubrica e os slides como fontes principais e sem editar os arquivos.

**Problemas apontados:** erros de tarefas poderiam ficar escondidos nos `Future`; o recebimento poderia concorrer com o encerramento; uma interrupção poderia retirar estoque sem concluir o pedido; e faltavam riscos e detalhes do protocolo no README.

**Decisões aceitas:** guardar e verificar os `Future`, sincronizar recebimento e encerramento, devolver itens em caso de interrupção e ampliar a documentação.

**Decisão mantida:** continuar sem Spring Boot e sem executar gRPC nesta etapa, pois isso preserva o foco no protótipo single-node com concorrência local.
