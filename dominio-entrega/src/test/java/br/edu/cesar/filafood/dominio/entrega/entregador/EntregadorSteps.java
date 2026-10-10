package br.edu.cesar.filafood.dominio.entrega.entregador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import br.edu.cesar.filafood.dominio.entrega.suporte.MundoDaEntrega;
import br.edu.cesar.filafood.dominio.entrega.entrega.EstadoDaEntrega;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos do cadastro de entregador (F2 RN 1). */
public class EntregadorSteps {

    private final MundoDaEntrega mundo;

    public EntregadorSteps(MundoDaEntrega mundo) {
        this.mundo = mundo;
    }

    @Quando("o gestor da unidade cadastra o entregador {string} com as zonas {string} e {string} e carga máxima {int}")
    public void cadastrarComDuasZonas(String nome, String primeiraZona, String segundaZona, int cargaMaxima) {
        mundo.tentar(() -> mundo.cadastrarEntregador(nome,
                new LinkedHashSet<>(Set.of(primeiraZona, segundaZona)), cargaMaxima));
    }

    @Quando("o gestor da unidade cadastra o entregador {string} com a zona {string} e carga máxima {int}")
    public void cadastrarComUmaZona(String nome, String zona, int cargaMaxima) {
        mundo.tentar(() -> mundo.cadastrarEntregador(nome, zona, cargaMaxima));
    }

    @Quando("o gestor da unidade cadastra um entregador com nome vazio, a zona {string} e carga máxima {int}")
    public void cadastrarSemNome(String zona, int cargaMaxima) {
        mundo.tentar(() -> mundo.cadastrarEntregador("", zona, cargaMaxima));
    }

    @Dado("o entregador {string} ativo com uma entrega em rota")
    public void ativoComEntregaEmRota(String nome) {
        mundo.garantirZona("Centro", 5, 5.00, 2, 1.50, 10.00, 30.00);
        mundo.cadastrarEntregador(nome, "Centro", 3);
        mundo.pedidoPronto("P1", 25.00, MundoDaEntrega.aoNorte(MundoDaEntrega.CENTRO_PADRAO, 2));
        assertTrue(mundo.despacharPedido(), "esperava o pedido despachado");
        mundo.entrega.registrarRetirada();
    }

    @Dado("o entregador {string} inativo")
    public void inativo(String nome) {
        mundo.garantirEntregador(nome, "Centro", 3).desativar();
    }

    @Quando("o gestor da unidade inativa o entregador {string}")
    public void inativar(String nome) {
        mundo.tentar(() -> mundo.entregador(nome).desativar());
    }

    @Quando("o gestor da unidade reativa o entregador {string}")
    public void reativar(String nome) {
        mundo.tentar(() -> mundo.entregador(nome).reativar());
    }

    @Então("o entregador {string} fica ativo")
    public void ficaAtivo(String nome) {
        mundo.semRecusa();
        assertTrue(mundo.entregador(nome).isAtivo(), "esperava o entregador ativo");
    }

    @Então("o entregador {string} fica inativo")
    public void ficaInativo(String nome) {
        mundo.semRecusa();
        assertFalse(mundo.entregador(nome).isAtivo(), "esperava o entregador inativo");
    }

    @Então("a entrega continua em rota")
    public void entregaContinuaEmRota() {
        assertEquals(EstadoDaEntrega.EM_ROTA, mundo.entrega.estado());
    }

    @Então("o cadastro é recusado com a mensagem {string}")
    public void cadastroRecusado(String mensagem) {
        assertNotNull(mundo.recusa, "esperava uma recusa, mas o cadastro foi aceito");
        assertEquals(mensagem, mundo.recusa.getMessage());
    }
}
