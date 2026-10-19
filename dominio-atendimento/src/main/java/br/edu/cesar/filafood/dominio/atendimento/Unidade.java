package br.edu.cesar.filafood.dominio.atendimento;

public class Unidade {
    private final String id;
    private String nome, regiao, telefone;
    private boolean ativa = true;
    private boolean recebendoPedidos = true;
    private String motivoPausa = "";

    public Unidade(String id, String nome, String regiao, String telefone) {
        this.id = Dados.obrigatorio(id, "informe o id da unidade");
        editar(nome, regiao, telefone);
    }

    public synchronized void editar(String nome, String regiao, String telefone) {
        String novoNome = Dados.obrigatorio(nome, "informe o nome da unidade");
        String novaRegiao = Dados.obrigatorio(regiao, "informe a região de atendimento");
        String novoTelefone = Dados.telefone(telefone);
        this.nome = novoNome;
        this.regiao = novaRegiao;
        this.telefone = novoTelefone;
    }

    public synchronized void desativar() { ativa = false; recebendoPedidos = false; motivoPausa = ""; }
    public synchronized void reativar() { ativa = true; recebendoPedidos = true; motivoPausa = ""; }

    public synchronized void pausar(String motivo) {
        if (!ativa) throw new RegraDeNegocioException("a unidade está inativa");
        String texto = Dados.obrigatorio(motivo, "informe o motivo de pausa");
        if (texto.length() > 200) throw new RegraDeNegocioException("o motivo de pausa deve ter até 200 caracteres");
        recebendoPedidos = false;
        motivoPausa = texto;
    }

    public synchronized void retomar() {
        if (!ativa) throw new RegraDeNegocioException("a unidade está inativa");
        recebendoPedidos = true;
        motivoPausa = "";
    }

    public synchronized boolean atende(String regiaoDeEntrega) {
        return ativa && recebendoPedidos && regiaoDeEntrega != null
                && regiao.equalsIgnoreCase(regiaoDeEntrega.strip());
    }

    /** A pausa e a confirmação de recebimento usam o mesmo monitor. */
    synchronized boolean receber(String regiaoDeEntrega, Runnable confirmar) {
        if (!atende(regiaoDeEntrega)) return false;
        confirmar.run();
        return true;
    }

    public String id() { return id; }
    public synchronized String nome() { return nome; }
    public synchronized String regiao() { return regiao; }
    public synchronized String telefone() { return telefone; }
    public synchronized boolean ativa() { return ativa; }
    public synchronized boolean disponivel() { return ativa && recebendoPedidos; }
    public synchronized String motivoPausa() { return motivoPausa; }
}
