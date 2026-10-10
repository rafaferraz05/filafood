package br.edu.cesar.filafood.dominio.entrega.zona;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.cesar.filafood.dominio.entrega.Coordenada;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.entrega.suporte.MundoDaEntrega;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos do cadastro de zona de entrega (F2 RN 2). */
public class ZonaSteps {

    private final MundoDaEntrega mundo;

    public ZonaSteps(MundoDaEntrega mundo) {
        this.mundo = mundo;
    }

    /**
     * As coordenadas chegam sem aspas, com ponto decimal, então são lidas como texto e convertidas
     * aqui. O {@code {double}} do Cucumber usaria a vírgula do idioma e leria o ponto como separador
     * de milhar.
     */
    @Quando("o gestor da unidade cadastra a zona {string} com centro em {}, {}, raio de {int} km, valor base {double}, raio gratuito {int} km, valor por quilômetro excedente {double}, teto {double} e valor mínimo de pedido {double}")
    public void cadastrarZona(String nome, String latitude, String longitude, int raioKm, double valorBase,
            int raioGratuitoKm, double valorPorKmExcedente, double teto, double valorMinimoDePedido) {
        mundo.tentar(() -> mundo.zonas.cadastrar(novaZona(nome, latitude, longitude, raioKm, valorBase,
                raioGratuitoKm, valorPorKmExcedente, teto, valorMinimoDePedido)));
    }

    /** Mesma zona, na forma curta que o contexto dos cenários de taxa usa. */
    @Dado("a zona {string} com centro em {}, {}, raio de {int} km, valor base {double}, raio gratuito {int} km, valor por quilômetro excedente {double}, teto {double} e valor mínimo de pedido {double}")
    public void zonaComCentro(String nome, String latitude, String longitude, int raioKm, double valorBase,
            int raioGratuitoKm, double valorPorKmExcedente, double teto, double valorMinimoDePedido) {
        mundo.tentar(() -> mundo.zonas.cadastrar(novaZona(nome, latitude, longitude, raioKm, valorBase,
                raioGratuitoKm, valorPorKmExcedente, teto, valorMinimoDePedido)));
    }

    private ZonaDeEntrega novaZona(String nome, String latitude, String longitude, int raioKm, double valorBase,
            int raioGratuitoKm, double valorPorKmExcedente, double teto, double valorMinimoDePedido) {
        return new ZonaDeEntrega(nome,
                new Coordenada(Double.parseDouble(latitude), Double.parseDouble(longitude)),
                raioKm, valorBase, raioGratuitoKm, valorPorKmExcedente, teto, valorMinimoDePedido);
    }

    @Dado("a zona {string} ativa e sem entregas em andamento")
    public void ativaSemEntregas(String nome) {
        mundo.garantirZona(nome, 5, 5.00, 2, 1.50, 10.00, 30.00);
        assertTrue(mundo.zona(nome).isAtiva(), "esperava a zona ativa");
        assertTrue(mundo.fila.entregas().isEmpty(), "esperava nenhuma entrega em andamento");
    }

    @Dado("a zona {string} inativa")
    public void zonaInativa(String nome) {
        if (mundo.zonas.zonas().stream().noneMatch(zona -> zona.nome().equals(nome))) {
            mundo.garantirZona(nome, 5, 5.00, 2, 1.50, 10.00, 30.00);
        }
        mundo.zona(nome).desativar();
    }

    @Quando("o gestor da unidade desativa a zona {string}")
    public void desativar(String nome) {
        mundo.tentar(() -> mundo.zona(nome).desativar());
    }

    @Quando("o gestor da unidade reativa a zona {string}")
    public void reativar(String nome) {
        mundo.tentar(() -> mundo.zona(nome).reativar());
    }

    @Então("a zona {string} fica ativa")
    public void ficaAtiva(String nome) {
        mundo.semRecusa();
        assertTrue(mundo.zona(nome).isAtiva(), "esperava a zona ativa");
    }

    @Então("a zona {string} fica inativa")
    public void ficaInativa(String nome) {
        mundo.semRecusa();
        assertFalse(mundo.zona(nome).isAtiva(), "esperava a zona inativa");
    }

    /** Zona inativa sai do ar: um endereço dentro dela deixa de ser atendido. */
    @Então("nenhuma nova entrega é atribuída à zona {string}")
    public void nenhumaNovaEntrega(String nome) {
        Coordenada dentroDaZona = mundo.zona(nome).centro();
        assertThrows(RegraDeNegocioException.class,
                () -> mundo.zonas.zonaDoEndereco(dentroDaZona),
                "esperava a zona fora do ar");
    }
}
