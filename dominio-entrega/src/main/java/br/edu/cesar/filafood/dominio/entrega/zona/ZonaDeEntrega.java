package br.edu.cesar.filafood.dominio.entrega.zona;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.edu.cesar.filafood.dominio.entrega.Coordenada;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;

/**
 * Zona de entrega: área definida por centro e raio, que delimita a taxa de entrega.
 *
 * <p>A taxa é o valor base mais o valor por quilômetro que excede o raio gratuito, limitada ao teto,
 * e é zerada quando o valor do pedido passa do valor mínimo de pedido da zona.
 */
public class ZonaDeEntrega {

    private final String nome;
    private final Coordenada centro;
    private final double raioKm;
    private double valorBase;
    private final double raioGratuitoKm;
    private final double valorPorKmExcedente;
    private final double teto;
    private final double valorMinimoDePedido;
    private boolean ativa = true;

    public ZonaDeEntrega(String nome, Coordenada centro, double raioKm, double valorBase, double raioGratuitoKm,
            double valorPorKmExcedente, double teto, double valorMinimoDePedido) {
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

    public double taxaPara(Coordenada endereco, double valorDoPedido) {
        return taxaParaDistancia(centro.distanciaEmKm(endereco), valorDoPedido);
    }

    public double taxaParaDistancia(double distanciaKm, double valorDoPedido) {
        if (valorDoPedido > valorMinimoDePedido) {
            return 0;
        }
        double quilometrosExcedentes = Math.max(0, distanciaKm - raioGratuitoKm);
        return Math.min(valorBase + quilometrosExcedentes * valorPorKmExcedente, teto);
    }

    public void desativar() {
        ativa = false;
    }

    /** Edita o valor base pelo cadastro da zona. */
    public void alterarValorBase(double valorBase) {
        this.valorBase = valorBase;
    }

    public void reativar() {
        ativa = true;
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

    public boolean isAtiva() {
        return ativa;
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
