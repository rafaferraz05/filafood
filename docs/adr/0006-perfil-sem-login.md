# 0006 — Perfil de acesso por seleção, sem login

- **Status:** proposta, para ratificar na reunião do grupo.
- **Data:** 2026-11-01
- **Contexto:** o Spec não exige autenticação, mas as telas separam gestor, atendente/cozinheiro e
  entregador, e o protótipo já navega por perfil (D6).

- **Decisão:** a seleção de perfil fica na **sessão** e uma guarda na view impede abrir tela de
  outro perfil. Entregador não abre cadastro de gestor; gestor não registra entrega. Não há login,
  senha nem cadastro de usuário.

- **Consequências:**
  - Demonstração simples, sem tabela de usuários e sem fluxo de senha.
  - A guarda é de tela, não de segurança real: qualquer um pode trocar de perfil.
  - Trocar por login depois é localizado nas views e na sessão.

- **Alternativas rejeitadas:** Spring Security com login — custo alto para um requisito que o Spec
  não pede, e o protótipo não tem tela de autenticação.
