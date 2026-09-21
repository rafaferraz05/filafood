# Diário de uso da IA

Este documento registra como a IA foi usada como copiloto no Projeto - Parte 1. A equipe continua responsável por executar, estudar e explicar o código.

## 1. Organização da entrega

**Pedido feito à IA:** analisar a atividade e a rubrica e auxiliar na definição do que precisava ser construído.

**Sugestão recebida:** começar com um protótipo single-node em Java, depois acrescentar concorrência local e somente depois executar a comunicação gRPC.

**Decisão:** aceita.

**Justificativa:** mantém o primeiro protótipo pequeno, permite testar cada etapa e segue a evolução proposta na disciplina.

## 2. Escolha das tecnologias

**Pedido feito à IA:** verificar se Spring Boot era obrigatório e comparar o projeto com os materiais das duas disciplinas.

**Sugestão recebida:** usar Java 21 e Maven agora; deixar Spring Boot, banco de dados e interface web para a disciplina de Requisitos de Software.

**Decisão:** aceita.

**Justificativa:** Spring Boot não é necessário para demonstrar threads, filas e sincronização em um único computador. Maven será útil posteriormente para adicionar gRPC.

## 3. Configuração do ambiente

**Pedido feito à IA:** ajudar e corrigir os erros de configuração.

**Problemas encontrados:** projeto inicialmente configurado como Java 8, ausência de codificação explícita e erro do Eclipse ao localizar o esquema XML do Maven.

**Correções aceitas:** configurar Java 21, UTF-8 e usar o esquema Maven já disponível no catálogo local do Eclipse.

## 4. Divisão das responsabilidades

**Maior autonomia da equipe:** definição do problema, regras do FilaFood, fluxo desejado entre servidor central e unidades, fornecimento dos materiais e restrições, decisões finais, execução, validação, estudo e apresentação.

**Construção compartilhada:** responsabilidades das classes, identificação dos pontos de concorrência e do estoque compartilhado, escolha de `ExecutorService`, `LinkedBlockingQueue` e `synchronized`, organização do contrato `.proto` e documentação das decisões.

**Maior apoio da IA:** elaboração da primeira versão de parte das classes e da documentação; configuração do Java, Maven e Eclipse; detalhes de `Future`, `awaitTermination`, `volatile` e interrupção; revisão independente; tratamento de situações de encerramento.


## 5. Decisões rejeitadas ou adiadas

- **Spring Boot:** adiado porque não é necessário para demonstrar concorrência local e será mais adequado às etapas web da outra disciplina.
- **Banco de dados e interface web:** adiados porque a Parte 1 pede um protótipo single-node e o uso agora aumentaria a complexidade sem melhorar a demonstração de threads.
- **Execução completa do gRPC:** adiada; nesta etapa foi mantido apenas o contrato `.proto` já elaborado.
- **Semáforos e locks explícitos:** não usados nesta versão porque `BlockingQueue` e o monitor criado por `synchronized` resolvem os riscos identificados com menos complexidade.

## 6. Verificações realizadas

- compilação das classes com Java 21;
- execução de pedidos enviados por quatro threads de clientes;
- processamento por duas threads em cada restaurante;
- recusa de endereço não atendido;
- recusa quando o estoque é insuficiente;
- confirmação de que o estoque final não fica negativo;
- revisão independente solicitada com base nos slides e na rubrica.



## 7. Revisão independente

**Pedido feito ao revisor:** analisar o projeto sem presumir que o código estava correto, usando a rubrica e os slides como fontes principais e sem editar os arquivos.

**Problemas apontados:** erros de tarefas poderiam ficar escondidos nos `Future`; o recebimento poderia concorrer com o encerramento; uma interrupção poderia retirar estoque sem concluir o pedido; e faltavam riscos e detalhes do protocolo no README.

**Decisões aceitas:** guardar e verificar os `Future`, sincronizar recebimento e encerramento, devolver itens em caso de interrupção e ampliar a documentação.

**Decisão mantida:** continuar sem Spring Boot e sem executar gRPC nesta etapa, pois isso preserva o foco no protótipo single-node com concorrência local.
