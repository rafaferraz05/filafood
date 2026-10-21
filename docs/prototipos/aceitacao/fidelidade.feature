# language: pt
# Especificação de aceitação; ainda sem classes de passos ou execução Cucumber.
Funcionalidade: F8 Programa de fidelidade e recompensas

  @f8-pontos
  Cenário: Saldo de pontos após entregas
    Dado que Rafael possui dois créditos de 81 e 39 pontos por pedidos entregues
    Quando consultar sua conta de fidelidade
    Então deve visualizar 120 pontos e somente o próprio extrato

  @f8-pontos-vazio
  Cenário: Conta sem movimentações
    Dado que o cliente não possui pedidos entregues
    Quando consultar a fidelidade
    Então deve visualizar zero pontos e orientação para montar um pedido

  @f8-resgate-confirmar
  Cenário: Confirmar resgate com saldo suficiente
    Dado que Rafael tem 120 pontos e a recompensa ativa custa 80
    Quando confirmar o resgate
    Então devem ser debitados 80 pontos uma única vez e criado o resgate #001 pendente com saldo 40

  @f8-resgate-sem-pontos
  Cenário: Recusar resgate sem saldo
    Dado que o cliente tem 40 pontos e a recompensa custa 80
    Quando tentar um novo resgate
    Então o saldo deve permanecer 40 sem criar outro resgate

  @f8-resgate-pendente
  Cenário: Aguardar próximo pedido
    Dado que o resgate #001 está pendente e o saldo é 40
    Quando consultar a recompensa
    Então deve aparecer que será anexada ao próximo pedido aceito sem criar entrega isolada

  @f8-resgate-cancelar
  Cenário: Confirmar cancelamento pendente
    Dado que Resgate #001 está pendente e o saldo é 40
    Quando solicitar cancelamento
    Então deve ser apresentada confirmação com devolução de 80 pontos

  @f8-resgate-cancelado
  Cenário: Devolver pontos uma única vez
    Dado que Resgate #001 está pendente com débito de 80 e saldo 40
    Quando confirmar duas vezes seu cancelamento
    Então o resgate deve ficar cancelado e o saldo deve ser 120 sem devolução duplicada

  @f8-pedido-resumo
  Cenário: Conferir recompensa no rascunho
    Dado que Resgate #001 está pendente e o rascunho contém 2 Clássico de R$19,50
    Quando conferir o resumo
    Então deve visualizar 2 produtos pagos e 1 recompensa sem consumir estoque ou vincular definitivamente o resgate

  @f8-pedido-enviado
  Cenário: Vincular sem duplicar
    Dado que Resgate #001 está pendente e dois pedidos do cliente disputam seu vínculo
    Quando o primeiro pedido elegível for aceito
    Então somente esse pedido deve receber Resgate #001 e o outro deve seguir sem a mesma recompensa

  @f8-recompensa-indisponivel
  Cenário: Manter pendente quando indisponível
    Dado que Resgate #001 está pendente e o Clássico não está disponível na unidade mas o Vegetariano está
    Quando um pedido de Vegetariano for aceito
    Então o pedido deve seguir sem a recompensa e Resgate #001 deve permanecer pendente sem novo débito

  @f8-pedido-cancelar
  Cenário: Confirmar cancelamento do pedido
    Dado que o pedido 2501 está enviado com Resgate #001 vinculada
    Quando o cliente pedir cancelamento
    Então a confirmação deve explicar o retorno da recompensa ao estado pendente

  @f8-pedido-cancelado
  Cenário: Liberar resgate após cancelamento
    Dado que o pedido 2501 está enviado com Resgate #001 vinculada e saldo 40
    Quando o pedido for cancelado antes do preparo
    Então Resgate #001 deve voltar a pendente sem devolver os 80 pontos e sem consumir insumos

  @f8-preparo
  Cenário: Incluir recompensa na ficha técnica
    Dado que 2501 contém 2 Clássico pagos e 1 de recompensa
    E existem insumos suficientes para 3 Clássico
    Quando a unidade iniciar o preparo
    Então deve consumir atomicamente os insumos das 3 porções e manter o Resgate #001 vinculado

  @f8-entrega
  Cenário: Creditar apenas após entrega e uma única vez
    Dado que 2501 possui R$39 em produtos pagos e saldo de pontos 40
    Quando a confirmação de entrega for recebida duas vezes
    Então Resgate #001 deve ficar entregue e o saldo deve ser 79 sem crédito duplicado

  @f8-falha-definitiva
  Cenário: Liberar após falha definitiva
    Dado que a primeira falha manteve Resgate #001 vinculada para a segunda tentativa
    Quando a segunda entrega falhar e F2 cancelar o pedido
    Então Resgate #001 deve voltar a pendente sem crédito de pontos nem devolução automática de alimento produzido

  @f8-catalogo
  Cenário: Consultar catálogo
    Dado que existe uma recompensa ativa de 80 pontos
    Quando o administrador consultar o catálogo
    Então deve visualizar produto quantidade custo e estado

  @f8-recompensa-nova
  Cenário: Cadastrar recompensa válida
    Dado que o produto Clássico está ativo
    Quando o administrador cadastrar uma recompensa de 1 unidade por 80 pontos
    Então a recompensa deve nascer ativa sem reservar insumos

  @f8-recompensa-erros
  Cenário: Recusar cadastro inválido
    Dado que o formulário tem nome vazio quantidade zero e custo negativo
    Quando o administrador tentar salvar
    Então nenhuma recompensa deve ser criada e os campos inválidos devem ser indicados

  @f8-recompensa-criada
  Cenário: Preservar dados cadastrados
    Dado que a recompensa foi cadastrada com 1 Clássico por 80 pontos
    Quando o administrador consultar o resultado
    Então deve visualizar os mesmos dados e estado ativo

  @f8-recompensa-desativar
  Cenário: Confirmar desativação
    Dado que uma recompensa ativa já possui Resgate #001 confirmado
    Quando o administrador solicitar desativação
    Então deve ser informado que os resgates anteriores serão preservados

  @f8-recompensa-inativa
  Cenário: Preservar resgate após desativação
    Dado que Resgate #001 foi confirmado antes da desativação da recompensa
    Quando a recompensa for desativada
    Então novos resgates devem ser recusados e Resgate #001 deve manter produto quantidade e custo originais

  @f8-editar-ativa
  Cenário: Editar recompensa ativa
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "editar · ativa"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-editar-erros-ativa
  Cenário: Rejeitar edição inválida sem alterações parciais
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "erro · ativa"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-excluir-ativa
  Cenário: Excluir oferta preservando resgates e histórico
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "excluir · ativa"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-editar-inativa
  Cenário: Editar recompensa inativa
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "editar · inativa"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-editar-erros-inativa
  Cenário: Rejeitar edição inválida sem alterações parciais
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "erro · inativa"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-excluir-inativa
  Cenário: Excluir oferta preservando resgates e histórico
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "excluir · inativa"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-recompensa-editada
  Cenário: Salvar edição preservando estado editada
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "editada"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-editar-editada
  Cenário: Editar recompensa editada
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "editar · editada"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-editar-erros-editada
  Cenário: Rejeitar edição inválida sem alterações parciais
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "erro · editada"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-excluir-editada
  Cenário: Excluir oferta preservando resgates e histórico
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "excluir · editada"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-recompensa-editada-inativa
  Cenário: Salvar edição preservando estado editada-inativa
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "editada-inativa"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-editar-editada-inativa
  Cenário: Editar recompensa editada-inativa
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "editar · editada-inativa"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-editar-erros-editada-inativa
  Cenário: Rejeitar edição inválida sem alterações parciais
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "erro · editada-inativa"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-excluir-editada-inativa
  Cenário: Excluir oferta preservando resgates e histórico
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "excluir · editada-inativa"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-recompensa-excluida
  Cenário: Não exibir oferta excluída no catálogo
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "excluída"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-desativar-editada
  Cenário: Desativar preservando dados editados
    Dado que o administrador consulta a recompensa identificada por REC01
    Quando acessar o estado "desativar editada"
    Então os dados e o estado cadastral devem ser preservados ao desistir
    E os resgates confirmados devem manter sua composição e custo originais

  @f8-edicao-preserva-resgate
  Cenário: Editar oferta não altera resgate confirmado
    Dado que REC01 oferece 1 Clássico por 80 pontos e o Resgate #001 já foi confirmado
    Quando o administrador salvar Clássico especial por 90 pontos
    Então REC01 deve manter identificador e estado cadastral
    E novos resgates devem custar 90 pontos
    E o Resgate #001 deve continuar com 1 Clássico por 80 pontos

  @f8-edicao-invalida
  Cenário: Edição inválida não grava parcialmente
    Dado que REC01 está inativa com nome Clássico de presente e custo 80
    Quando o administrador tentar salvar nome vazio e custo zero
    Então deve visualizar erros nos dois campos
    E nome custo e estado anteriores devem permanecer intactos

  @f8-exclusao-historico
  Cenário: Exclusão preserva obrigações de resgates existentes
    Dado que REC01 possui o Resgate #001 pendente
    Quando o administrador confirmar a exclusão de REC01
    Então REC01 não deve aparecer no catálogo nem aceitar novos resgates
    E o Resgate #001 deve permanecer pendente e elegível para o próximo pedido
    E nenhum ponto deve ser devolvido ou debitado pela exclusão

  @f8-catalogo-vazio
  Cenário: Exibir catálogo sem recompensas cadastradas
    Dado que não existe recompensa cadastrada
    Quando o administrador consultar o catálogo
    Então deve visualizar a mensagem de catálogo vazio e o botão Nova recompensa
    E não deve visualizar ações Editar Desativar ou Excluir

  @f8-preparo-sem-recompensa
  Cenário: O pedido pago segue em preparo
    Dado que #2501 possui 2 Clássicos pagos e 1 gratuito e há saldo apenas para 2 porções
    Quando a unidade iniciar o preparo
    Então o pedido deve ficar em preparo com somente as 2 porções pagas e o Resgate #001 deve voltar a pendente com aviso ao cliente sem novo débito

  @f8-preparo-recusado
  Cenário: Faltam insumos para os produtos pagos
    Dado que #2501 exige 2 porções pagas mas o estoque atende somente 1
    Quando a unidade iniciar o preparo
    Então o pedido deve ser recusado sem consumir insumos e o Resgate #001 deve voltar a pendente

  @f8-preparo-concluido
  Cenário: Preparo concluído com recompensa
    Dado que #2501 está em preparo com 2 porções pagas e 1 de recompensa
    Quando a unidade concluir o preparo
    Então o pedido deve ficar concluído e seguir ao despacho com o resgate vinculado sem crédito antecipado de pontos
