package br.edu.cesar.filafood.dominio.entrega.entregador;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;

/**
 * Entregador: pessoa que transporta pedidos de uma unidade até o cliente.
 *
 * <p>A carga em andamento são as entregas em despachada ou em rota. Ela é liberada quando a entrega
 * termina, e o despacho só considera quem tem carga menor que a carga máxima.
 */
public class Entregador {

    private final EntregadorId id;
    private final String unidadeId;
    private String nome;
    private Set<String> zonasAtendidas;
    private int cargaMaxima;
    private boolean ativo = true;
    private int cargaEmAndamento;
    private LocalDateTime ultimaEntregaEm;

    public Entregador(EntregadorId id, String unidadeId, String nome, Set<String> zonasAtendidas, int cargaMaxima) {
        if (id == null) {
            throw new IllegalArgumentException("o id do entregador é obrigatório");
        }
        if (unidadeId == null || unidadeId.isBlank()) {
            throw new RegraDeNegocioException("informe a unidade do entregador");
        }
        this.id = id;
        this.unidadeId = unidadeId;
        this.zonasAtendidas = new LinkedHashSet<>();
        alterarDados(nome, zonasAtendidas, cargaMaxima);
    }

    /** Reconstitui o entregador a partir da persistência, preservando carga, estado e histórico. */
    public static Entregador reconstituir(EntregadorId id, String unidadeId, String nome,
            Set<String> zonasAtendidas, int cargaMaxima, boolean ativo, int cargaEmAndamento,
            LocalDateTime ultimaEntregaEm) {
        Entregador entregador = new Entregador(id, unidadeId, nome, zonasAtendidas, cargaMaxima);
        entregador.ativo = ativo;
        entregador.cargaEmAndamento = cargaEmAndamento;
        entregador.ultimaEntregaEm = ultimaEntregaEm;
        return entregador;
    }

    public void alterarDados(String nome, Set<String> zonasAtendidas, int cargaMaxima) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("informe o nome do entregador");
        }
        if (cargaMaxima <= 0) {
            throw new RegraDeNegocioException("a carga máxima deve ser maior que zero");
        }
        this.nome = nome;
        this.zonasAtendidas = zonasAtendidas == null ? new LinkedHashSet<>() : new LinkedHashSet<>(zonasAtendidas);
        this.cargaMaxima = cargaMaxima;
    }

    public boolean atende(String zona) {
        return zonasAtendidas.contains(zona);
    }

    public boolean temCargaDisponivel() {
        return cargaEmAndamento < cargaMaxima;
    }

    /** O entregador assume uma entrega: a carga sobe e o momento fica registrado. */
    public void assumirEntrega(LocalDateTime momento) {
        cargaEmAndamento++;
        registrarUltimaEntregaEm(momento);
    }

    /** Libera uma entrega, quando ela é concluída ou falha. */
    public void liberarCarga() {
        if (cargaEmAndamento > 0) {
            cargaEmAndamento--;
        }
    }

    /**
     * Momento em que o entregador recebeu a última entrega. É o critério de desempate do despacho:
     * quem está há mais tempo sem receber tem prioridade.
     */
    public void registrarUltimaEntregaEm(LocalDateTime momento) {
        this.ultimaEntregaEm = momento;
    }

    public void desativar() {
        ativo = false;
    }

    public void reativar() {
        ativo = true;
    }

    public EntregadorId id() {
        return id;
    }

    public String unidadeId() {
        return unidadeId;
    }

    public String nome() {
        return nome;
    }

    public Set<String> zonasAtendidas() {
        return Set.copyOf(zonasAtendidas);
    }

    public int cargaMaxima() {
        return cargaMaxima;
    }

    public int cargaEmAndamento() {
        return cargaEmAndamento;
    }

    public LocalDateTime ultimaEntregaEm() {
        return ultimaEntregaEm;
    }

    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof Entregador entregador && id.equals(entregador.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
