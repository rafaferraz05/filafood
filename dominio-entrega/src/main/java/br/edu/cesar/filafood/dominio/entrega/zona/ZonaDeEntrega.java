package br.edu.cesar.filafood.dominio.entrega.zona;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.edu.cesar.filafood.dominio.entrega.Coordenada;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;

/**
 * Zona de entrega: área definida por centro e raio, que delimita a taxa de entrega.
 *
 * <p>A taxa é calculada por {@link PoliticaDeTaxa}; a zona guarda os parâmetros e a identidade.
 * Dinheiro é {@link BigDecimal} com escala 2; distância continua {@code double}.
 */
public class ZonaDeEntrega {

    private final ZonaId id;
    private final String unidadeId;
    private String nome;
    private Coordenada centro;
    private double raioKm;
    private BigDecimal valorBase;
    private double raioGratuitoKm;
    private BigDecimal valorPorKmExcedente;
    private BigDecimal teto;
    private BigDecimal valorMinimoDePedido;
    private boolean ativa = true;

    public ZonaDeEntrega(ZonaId id, String unidadeId, String nome, Coordenada centro, double raioKm,
            BigDecimal valorBase, double raioGratuitoKm, BigDecimal valorPorKmExcedente, BigDecimal teto,
            BigDecimal valorMinimoDePedido) {
        if (id == null) {
            throw new IllegalArgumentException("o id da zona é obrigatório");
        }
        if (unidadeId == null || unidadeId.isBlank()) {
            throw new RegraDeNegocioException("informe a unidade da zona");
        }
        this.id = id;
        this.unidadeId = unidadeId;
        alterarDados(nome, centro, raioKm, valorBase, raioGratuitoKm, valorPorKmExcedente, teto,
                valorMinimoDePedido);
    }

    /** Reconstitui a zona a partir da persistência, preservando o estado. */
    public static ZonaDeEntrega reconstituir(ZonaId id, String unidadeId, String nome, Coordenada centro,
            double raioKm, BigDecimal valorBase, double raioGratuitoKm, BigDecimal valorPorKmExcedente,
            BigDecimal teto, BigDecimal valorMinimoDePedido, boolean ativa) {
        ZonaDeEntrega zona = new ZonaDeEntrega(id, unidadeId, nome, centro, raioKm, valorBase, raioGratuitoKm,
                valorPorKmExcedente, teto, valorMinimoDePedido);
        zona.ativa = ativa;
        return zona;
    }

    public void alterarDados(String nome, Coordenada centro, double raioKm, BigDecimal valorBase,
            double raioGratuitoKm, BigDecimal valorPorKmExcedente, BigDecimal teto,
            BigDecimal valorMinimoDePedido) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("informe o nome da zona");
        }
        if (centro == null) {
            throw new RegraDeNegocioException("informe o centro da zona");
        }
        if (raioKm <= 0) {
            throw new RegraDeNegocioException("o raio da zona deve ser maior que zero");
        }
        if (raioGratuitoKm < 0) {
            throw new RegraDeNegocioException("o raio grátis não pode ser negativo");
        }
        if (valorBase == null || valorBase.signum() < 0) {
            throw new RegraDeNegocioException("o valor base não pode ser negativo");
        }
        if (valorPorKmExcedente == null || valorPorKmExcedente.signum() < 0) {
            throw new RegraDeNegocioException("o valor por quilômetro não pode ser negativo");
        }
        if (teto == null || teto.signum() < 0) {
            throw new RegraDeNegocioException("o teto não pode ser negativo");
        }
        if (valorMinimoDePedido == null || valorMinimoDePedido.signum() < 0) {
            throw new RegraDeNegocioException("o valor mínimo de pedido não pode ser negativo");
        }
        this.nome = nome;
        this.centro = centro;
        this.raioKm = raioKm;
        this.valorBase = valorBase;
        this.raioGratuitoKm = raioGratuitoKm;
        this.valorPorKmExcedente = valorPorKmExcedente;
        this.teto = teto;
        this.valorMinimoDePedido = valorMinimoDePedido;
    }

    public boolean contem(Coordenada endereco) {
        return centro.distanciaEmKm(endereco) <= raioKm;
    }

    public BigDecimal taxaPara(Coordenada endereco, BigDecimal valorDoPedido) {
        return new PoliticaDeTaxaPorZona().calcular(this, centro.distanciaEmKm(endereco), valorDoPedido);
    }

    public BigDecimal taxaParaDistancia(double distanciaKm, BigDecimal valorDoPedido) {
        return new PoliticaDeTaxaPorZona().calcular(this, distanciaKm, valorDoPedido);
    }

    public void desativar() {
        ativa = false;
    }

    /** Edita só o valor base, pelo cadastro da zona (RN 2). */
    public void alterarValorBase(BigDecimal valorBase) {
        if (valorBase == null || valorBase.signum() < 0) {
            throw new RegraDeNegocioException("o valor base não pode ser negativo");
        }
        this.valorBase = valorBase;
    }

    public void reativar() {
        ativa = true;
    }

    public ZonaId id() {
        return id;
    }

    public String unidadeId() {
        return unidadeId;
    }

    public String nome() {
        return nome;
    }

    public Coordenada centro() {
        return centro;
    }

    public double raioKm() {
        return raioKm;
    }

    public BigDecimal valorBase() {
        return valorBase;
    }

    public double raioGratuitoKm() {
        return raioGratuitoKm;
    }

    public BigDecimal valorPorKmExcedente() {
        return valorPorKmExcedente;
    }

    public BigDecimal teto() {
        return teto;
    }

    public BigDecimal valorMinimoDePedido() {
        return valorMinimoDePedido;
    }

    public boolean isAtiva() {
        return ativa;
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof ZonaDeEntrega zona && id.equals(zona.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Zonas mantidas pela unidade. A zona do endereço é a de menor raio que o contém, para que a
     * zona mais específica tenha prioridade quando houver sobreposição.
     */
    public static class ZonasDaUnidade {

        private final List<ZonaDeEntrega> zonas = new ArrayList<>();

        public ZonaDeEntrega cadastrar(ZonaDeEntrega zona) {
            zonas.add(zona);
            return zona;
        }

        public ZonaDeEntrega zonaDoEndereco(Coordenada endereco) {
            return zonas.stream()
                    .filter(ZonaDeEntrega::isAtiva)
                    .filter(zona -> zona.contem(endereco))
                    .min(Comparator.comparingDouble(ZonaDeEntrega::raioKm))
                    .orElseThrow(() -> new RegraDeNegocioException("nenhuma zona atende o endereço"));
        }

        public List<ZonaDeEntrega> zonas() {
            return List.copyOf(zonas);
        }
    }
}
