package br.edu.cesar.filafood.dominio.atendimento;

import java.util.Set;

public class Funcionario {
    private final String id, unidadeId;
    private String nome, funcao, telefone;
    private boolean ativo = true;

    public Funcionario(String id, String unidadeId, String nome, String funcao, String telefone) {
        this.id = Dados.obrigatorio(id, "informe o id do funcionário");
        this.unidadeId = Dados.obrigatorio(unidadeId, "informe a unidade do funcionário");
        editar(nome, funcao, telefone);
    }

    public void editar(String nome, String funcao, String telefone) {
        String novoNome = Dados.obrigatorio(nome, "informe o nome do funcionário");
        if (novoNome.length() > 100) throw new RegraDeNegocioException("o nome do funcionário deve ter até 100 caracteres");
        if (!Set.of("gestor", "atendente", "cozinheiro").contains(funcao == null ? "" : funcao))
            throw new RegraDeNegocioException("escolha gestor, atendente ou cozinheiro");
        String novoTelefone = Dados.telefone(telefone);
        this.nome = novoNome; this.funcao = funcao; this.telefone = novoTelefone;
    }

    public void desativar() { ativo = false; }
    public void reativar() { ativo = true; }
    public String id() { return id; }
    public String unidadeId() { return unidadeId; }
    public String nome() { return nome; }
    public String funcao() { return funcao; }
    public String telefone() { return telefone; }
    public boolean ativo() { return ativo; }
}
