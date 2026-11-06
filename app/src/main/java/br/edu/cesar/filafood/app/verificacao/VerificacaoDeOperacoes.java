package br.edu.cesar.filafood.app.verificacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.app.application.estoque.AlterarEstadoDoInsumo;
import br.edu.cesar.filafood.app.application.estoque.CadastrarInsumo;
import br.edu.cesar.filafood.app.application.estoque.DetalharInsumo;
import br.edu.cesar.filafood.app.application.estoque.EntradaDeInsumo;
import br.edu.cesar.filafood.app.application.estoque.InsumoDetalhe;
import br.edu.cesar.filafood.app.application.estoque.InsumoResumo;
import br.edu.cesar.filafood.app.application.estoque.ListarInsumos;
import br.edu.cesar.filafood.app.application.estoque.NovoInsumo;
import br.edu.cesar.filafood.app.application.estoque.RegistrarEntradaDeInsumo;
import br.edu.cesar.filafood.app.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;

/**
 * Verificação de fumaça das operações contra o banco de dev semeado (D-19): lista, detalha,
 * cadastra, dá entrada, desativa e reativa, conferindo saldo e "a repor". Roda com
 * {@code --spring.profiles.active=dev,verificacao --spring.main.web-application-type=none}.
 */
@Component
@Profile("verificacao")
public class VerificacaoDeOperacoes implements CommandLineRunner {

    private final ListarInsumos listar;
    private final DetalharInsumo detalhar;
    private final RegistrarEntradaDeInsumo registrarEntrada;
    private final AlterarEstadoDoInsumo alterarEstado;
    private final CadastrarInsumo cadastrar;

    public VerificacaoDeOperacoes(ListarInsumos listar, DetalharInsumo detalhar,
            RegistrarEntradaDeInsumo registrarEntrada, AlterarEstadoDoInsumo alterarEstado,
            CadastrarInsumo cadastrar) {
        this.listar = listar;
        this.detalhar = detalhar;
        this.registrarEntrada = registrarEntrada;
        this.alterarEstado = alterarEstado;
        this.cadastrar = cadastrar;
    }

    @Override
    public void run(String... args) {
        String unidade = UnidadeDeDemonstracao.ID;

        List<InsumoResumo> insumos = listar.executar(unidade);
        exigir(insumos.size() >= 4, "esperava ao menos os 4 insumos do seed, veio " + insumos.size());

        InsumoResumo pao = buscar(insumos, "Pão");
        exigir(comparar(pao.saldo(), "42.000"), "saldo do Pão deveria ser 42, veio " + pao.saldo());

        InsumoDetalhe detalhe = detalhar.executar(pao.id().toString());
        exigir(detalhe.movimentacoes().size() >= 2,
                "histórico do Pão deveria ter entrada e saída, veio " + detalhe.movimentacoes().size());

        String nome = "Insumo de verificação " + UUID.randomUUID().toString().substring(0, 8);
        InsumoId novo = cadastrar.executar(new NovoInsumo(unidade, nome, "kg", 5, 2));
        registrarEntrada.executar(new EntradaDeInsumo(novo.toString(), 3, new BigDecimal("7.00")));

        InsumoResumo criado = buscar(listar.executar(unidade), nome);
        exigir(comparar(criado.saldo(), "3.000"), "saldo do insumo novo deveria ser 3, veio " + criado.saldo());
        exigir(criado.aRepor(), "insumo com saldo 3 e mínimo 5 deveria estar a repor");

        alterarEstado.desativar(novo.toString());
        exigir(!buscar(listar.executar(unidade), nome).ativo(), "insumo deveria estar inativo");
        alterarEstado.reativar(novo.toString());
        exigir(buscar(listar.executar(unidade), nome).ativo(), "insumo deveria estar ativo");

        System.out.println("VERIFICACAO OK: " + insumos.size()
                + " insumos no seed; cadastro, entrada, saldo, a-repor, desativar e reativar conferidos.");
    }

    private static InsumoResumo buscar(List<InsumoResumo> insumos, String nome) {
        return insumos.stream()
                .filter(insumo -> insumo.nome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("insumo não encontrado: " + nome));
    }

    private static boolean comparar(BigDecimal valor, String esperado) {
        return valor.compareTo(new BigDecimal(esperado)) == 0;
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new IllegalStateException("VERIFICACAO FALHOU: " + mensagem);
        }
    }
}
