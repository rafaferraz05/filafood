# Entrega da F2 — despacho e entregas

## O que está pronto

- Cadastro de entregadores (nome, zonas, carga máxima, ativar/inativar) e de zonas (centro, raio,
  base, raio grátis, por km, teto, mínimo).
- Taxa calculada pela zona de menor raio que contém o endereço, gravada no despacho e nunca
  recalculada.
- Fila de despacho persistida e escolha do entregador ativo de menor carga, com desempate pela
  última entrega.
- Estados `aguardando → despachada → em rota → entregue|falha`, com data/hora em cada transição.
- Falha com motivo e observação: nova tentativa única ou cancelamento (recusa do cliente cancela na
  primeira); o pedido é atualizado pelo listener do Observer.
- Telas: Entregadores, Zonas e taxas, Despacho e entregas e Minhas entregas, no layout do protótipo.

## Como demonstrar

1. Suba o banco e o app:
   ```powershell
   docker compose up -d --wait db
   $env:DB_PASSWORD = ((Get-Content .env) | Where-Object { $_ -like 'DB_PASSWORD=*' }) -replace '^DB_PASSWORD=',''
   .\mvnw.cmd -pl apresentacao-vaadin -am spring-boot:run "-Dspring-boot.run.arguments=--server.port=8090"
   ```
2. Abra `/entregadores`, `/zonas-taxas`, `/despacho` e `/minhas-entregas`.
3. Em `/despacho`, a fila mostra os pedidos de Grande Recife (Bruno está com a carga cheia); use
   “Despachar agora” depois de concluir uma entrega do Bruno em “Minhas entregas”.
4. Em `/minhas-entregas`, registre a retirada/entrega ou a falha. A falha mostra o desfecho e a nova
   tentativa no painel do gestor.

## Verificação automática

Com o perfil `verificacao`, o runner da F2 roda depois do runner da F1:

```powershell
.\mvnw.cmd -pl apresentacao-vaadin -am spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev,verificacao --server.port=8092"
```

Saída esperada (além das quatro linhas da F1):

```text
VERIFICACAO OK: 4 zonas, 4 entregadores, 2 pedido(s) despachado(s) pela política de menor carga.
VERIFICACAO OK: entrega ... concluída e carga do entregador liberada.
VERIFICACAO OK: falha da entrega ... gerou a nova tentativa ...
```

O runner altera o banco de dev. Para voltar ao seed limpo:
`docker compose down -v` e `docker compose up -d --wait db`.

## Testes

- `.\mvnw.cmd test` — BDD de F1/F2, políticas de taxa/escolha, listener do Observer, testes de
  aplicação e de arquitetura.
- `.\mvnw.cmd -pl infraestrutura -am -Pintegracao test` — `EsquemaDoEstoqueIT` e
  `EsquemaDaEntregaIT` (Flyway + Testcontainers).
