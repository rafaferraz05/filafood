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

    private final String nome;
    private final Set<String> zonasAtendidas;
    private final int cargaMaxima;
    private boolean ativo = true;
    private int cargaEmAndamento;
    private LocalDateTime ultimaEntregaEm;

    public Entregador(String nome, Set<String> zonasAtendidas, int cargaMaxima) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("informe o nome do entregador");
        }
        if (cargaMaxima <= 0) {
            throw new RegraDeNegocioException("a carga máxima deve ser maior que zero");
        }
        this.nome = nome;
        this.zonasAtendidas = new LinkedHashSet<>(zonasAtendidas);
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

    public String nome() {
        return nome;
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
}
