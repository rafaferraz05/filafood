package br.edu.cesar.filafood.app.verificacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.app.application.estoque.AdicionarItemNaFicha;
import br.edu.cesar.filafood.app.application.estoque.AlterarEstadoDoInsumo;
import br.edu.cesar.filafood.app.application.estoque.CadastrarInsumo;
import br.edu.cesar.filafood.app.application.estoque.DetalharInsumo;
import br.edu.cesar.filafood.app.application.estoque.EntradaDeInsumo;
import br.edu.cesar.filafood.app.application.estoque.FichaDetalhe;
import br.edu.cesar.filafood.app.application.estoque.FichasDaUnidade;
import br.edu.cesar.filafood.app.application.estoque.InsumoDetalhe;
import br.edu.cesar.filafood.app.application.estoque.InsumoResumo;
import br.edu.cesar.filafood.app.application.estoque.ListarFichasDaUnidade;
import br.edu.cesar.filafood.app.application.estoque.ListarInsumos;
import br.edu.cesar.filafood.app.application.estoque.NovoInsumo;
import br.edu.cesar.filafood.app.application.estoque.ProdutoComFicha;
import br.edu.cesar.filafood.app.application.estoque.RegistrarEntradaDeInsumo;
import br.edu.cesar.filafood.app.application.estoque.RemoverItemDaFicha;
import br.edu.cesar.filafood.app.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
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
    private final ListarFichasDaUnidade listarFichas;
    private final AdicionarItemNaFicha adicionarItem;
    private final RemoverItemDaFicha removerItem;

    public VerificacaoDeOperacoes(ListarInsumos listar, DetalharInsumo detalhar,
            RegistrarEntradaDeInsumo registrarEntrada, AlterarEstadoDoInsumo alterarEstado,
            CadastrarInsumo cadastrar, ListarFichasDaUnidade listarFichas,
            AdicionarItemNaFicha adicionarItem, RemoverItemDaFicha removerItem) {
        this.listar = listar;
        this.detalhar = detalhar;
        this.registrarEntrada = registrarEntrada;
        this.alterarEstado = alterarEstado;
        this.cadastrar = cadastrar;
        this.listarFichas = listarFichas;
        this.adicionarItem = adicionarItem;
        this.removerItem = removerItem;
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
        InsumoId novo = cadastrar.executar(new NovoInsumo(unidade, nome, "kg", new BigDecimal("5"), 2));
        registrarEntrada.executar(new EntradaDeInsumo(novo.toString(), new BigDecimal("3"), new BigDecimal("7.00")));

        InsumoResumo criado = buscar(listar.executar(unidade), nome);
        exigir(comparar(criado.saldo(), "3.000"), "saldo do insumo novo deveria ser 3, veio " + criado.saldo());
        exigir(criado.aRepor(), "insumo com saldo 3 e mínimo 5 deveria estar a repor");

        alterarEstado.desativar(novo.toString());
        exigir(!buscar(listar.executar(unidade), nome).ativo(), "insumo deveria estar inativo");
        alterarEstado.reativar(novo.toString());
        exigir(buscar(listar.executar(unidade), nome).ativo(), "insumo deveria estar ativo");

        System.out.println("VERIFICACAO OK: " + insumos.size()
                + " insumos no seed; cadastro, entrada, saldo, a-repor, desativar e reativar conferidos.");
        verificarFichas(unidade, pao);
    }

    /** Ficha técnica do seed: produtos, itens, porções cobertas e as recusas da RN 2. */
    private void verificarFichas(String unidade, InsumoResumo pao) {
        FichasDaUnidade fichas = listarFichas.executar(unidade, null);
        exigir(fichas.produtos().size() == 3, "esperava os 3 produtos do seed, veio " + fichas.produtos().size());

        ProdutoComFicha classico = produto(fichas, "Hambúrguer Clássico");
        FichaDetalhe fichaDoClassico = listarFichas.executar(unidade, classico.id()).ficha().orElseThrow();
        exigir(fichaDoClassico.temFicha() && fichaDoClassico.itens().size() == 4,
                "ficha do Clássico deveria ter 4 itens, veio " + fichaDoClassico.itens().size());
        exigir(fichaDoClassico.porcoesCobertas() == 42,
                "Clássico deveria cobrir 42 porções, veio " + fichaDoClassico.porcoesCobertas());

        ProdutoComFicha batata = produto(fichas, "Batata Rústica");
        exigir(!batata.temFicha(), "Batata Rústica deveria estar sem ficha");

        try {
            adicionarItem.executar(unidade, classico.id(), pao.id().toString(), BigDecimal.ONE);
            throw new IllegalStateException("VERIFICACAO FALHOU: item repetido na ficha deveria ser recusado");
        } catch (RegraDeNegocioException esperada) {
            exigir("o insumo já está na ficha técnica".equals(esperada.getMessage()),
                    "mensagem inesperada do item repetido: " + esperada.getMessage());
        }

        adicionarItem.executar(unidade, batata.id(), pao.id().toString(), new BigDecimal("0.010"));
        FichaDetalhe fichaDaBatata = listarFichas.executar(unidade, batata.id()).ficha().orElseThrow();
        exigir(fichaDaBatata.temFicha() && fichaDaBatata.itens().size() == 1,
                "Batata deveria ter 1 item depois de adicionar, veio " + fichaDaBatata.itens().size());

        removerItem.executar(unidade, batata.id(), pao.id().toString());
        exigir(!listarFichas.executar(unidade, batata.id()).ficha().orElseThrow().temFicha(),
                "Batata deveria voltar ao estado sem ficha");

        System.out.println("VERIFICACAO OK: ficha técnica com 3 produtos, Clássico com 4 itens e 42 porções, "
                + "Batata sem ficha; adicionar, recusar repetido e remover conferidos.");
    }

    private static ProdutoComFicha produto(FichasDaUnidade fichas, String nome) {
        return fichas.produtos().stream()
                .filter(produto -> produto.nome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("produto não encontrado: " + nome));
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
