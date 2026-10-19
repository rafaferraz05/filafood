package br.edu.cesar.filafood.dominio.atendimento;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Pedido {
    private final String id, clienteId;
    private String nomeCliente = "", endereco = "", regiao = "", unidadeId = "", motivoRecusa = "";
    private EstadoDoPedido estado = EstadoDoPedido.RASCUNHO;
    private final Map<String, ItemDoPedido> itens = new LinkedHashMap<>();

    public Pedido(String id, String clienteId) {
        this.id = Dados.obrigatorio(id, "informe o id do pedido");
        this.clienteId = Dados.obrigatorio(clienteId, "informe o cliente do pedido");
    }

    public synchronized void editarDados(String nome, String endereco, String regiao) {
        exigirRascunho();
        this.nomeCliente = nome == null ? "" : nome.strip();
        this.endereco = endereco == null ? "" : endereco.strip();
        this.regiao = regiao == null ? "" : regiao.strip();
    }

    /** Um produto ocupa uma linha; adicionar de novo soma à quantidade existente. */
    public synchronized void adicionar(String produto, int quantidade) {
        exigirRascunho();
        ItemDoPedido novo = new ItemDoPedido(produto, quantidade);
        ItemDoPedido anterior = itens.get(novo.produto());
        int total;
        try { total = Math.addExact(anterior == null ? 0 : anterior.quantidade(), quantidade); }
        catch (ArithmeticException e) { throw new RegraDeNegocioException("quantidade acima do limite"); }
        itens.put(novo.produto(), new ItemDoPedido(novo.produto(), total));
    }

    public synchronized void alterarQuantidade(String produto, int quantidade) {
        exigirRascunho();
        if (!itens.containsKey(produto)) throw new RegraDeNegocioException("item do pedido não encontrado");
        itens.put(produto, new ItemDoPedido(produto, quantidade));
    }

    public synchronized void remover(String produto) {
        exigirRascunho();
        if (itens.remove(produto) == null) throw new RegraDeNegocioException("item do pedido não encontrado");
    }

    public synchronized void enviar(List<Unidade> unidades) {
        exigirRascunho();
        Dados.obrigatorio(nomeCliente, "informe o nome do cliente");
        Dados.obrigatorio(endereco, "informe o endereço de entrega");
        Dados.obrigatorio(regiao, "informe a região de entrega");
        if (itens.isEmpty()) throw new RegraDeNegocioException("adicione pelo menos um item");
        for (Unidade unidade : unidades) {
            if (unidade.receber(regiao, () -> { unidadeId = unidade.id(); estado = EstadoDoPedido.ENVIADO; })) return;
        }
        estado = EstadoDoPedido.RECUSADO;
        motivoRecusa = "nenhuma unidade disponível atende a região de entrega";
    }

    public synchronized void cancelarPeloCliente() {
        if (estado != EstadoDoPedido.ENVIADO)
            throw new RegraDeNegocioException("o cliente só pode cancelar pedido enviado antes do preparo");
        estado = EstadoDoPedido.CANCELADO;
    }

    /** A baixa da F1 e a passagem de estado ficam dentro da mesma disputa com o cancelamento. */
    public synchronized void iniciarPreparo(Runnable consumirInsumos) {
        exigirEnviado();
        try {
            consumirInsumos.run();
        } catch (RegraDeNegocioException e) {
            motivoRecusa = e.getMessage();
            estado = EstadoDoPedido.RECUSADO;
            return;
        }
        estado = EstadoDoPedido.EM_PREPARO;
    }

    /** Retorno da F1 quando a baixa não pode ser feita; nenhum insumo foi consumido. */
    public synchronized void recusarNoPreparo(String motivo) {
        exigirEnviado();
        motivoRecusa = Dados.obrigatorio(motivo, "informe o motivo de recusa");
        estado = EstadoDoPedido.RECUSADO;
    }

    public synchronized void concluirPreparo() {
        if (estado != EstadoDoPedido.EM_PREPARO) throw new RegraDeNegocioException("o pedido não está em preparo");
        estado = EstadoDoPedido.CONCLUIDO;
    }

    synchronized void exigirRascunho() {
        if (estado != EstadoDoPedido.RASCUNHO) throw new RegraDeNegocioException("o pedido só pode ser editado em rascunho");
    }
    private void exigirEnviado() {
        if (estado != EstadoDoPedido.ENVIADO) throw new RegraDeNegocioException("o pedido não está enviado");
    }
    public String id() { return id; }
    public String clienteId() { return clienteId; }
    public synchronized String nomeCliente() { return nomeCliente; }
    public synchronized String endereco() { return endereco; }
    public synchronized String regiao() { return regiao; }
    public synchronized String unidadeId() { return unidadeId; }
    public synchronized String motivoRecusa() { return motivoRecusa; }
    public synchronized EstadoDoPedido estado() { return estado; }
    public synchronized List<ItemDoPedido> itens() { return List.copyOf(new ArrayList<>(itens.values())); }
}
