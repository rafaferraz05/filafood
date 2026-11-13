package br.edu.cesar.filafood.app.verificacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.app.application.estoque.AdicionarItemNaFicha;
import br.edu.cesar.filafood.app.application.estoque.AlteracaoDeInsumo;
import br.edu.cesar.filafood.app.application.estoque.AlterarEstadoDoInsumo;
import br.edu.cesar.filafood.app.application.estoque.BaixaDePreparo;
import br.edu.cesar.filafood.app.application.estoque.BaixaDoPreparo;
import br.edu.cesar.filafood.app.application.estoque.CadastrarInsumo;
import br.edu.cesar.filafood.app.application.estoque.CancelamentoDoPreparo;
import br.edu.cesar.filafood.app.application.estoque.DetalharInsumo;
import br.edu.cesar.filafood.app.application.estoque.DevolucaoDoCancelamento;
import br.edu.cesar.filafood.app.application.estoque.EditarInsumo;
import br.edu.cesar.filafood.app.application.estoque.EntradaDeInsumo;
import br.edu.cesar.filafood.app.application.estoque.FichaDetalhe;
import br.edu.cesar.filafood.app.application.estoque.FichasDaUnidade;
import br.edu.cesar.filafood.app.application.estoque.InsumoDetalhe;
import br.edu.cesar.filafood.app.application.estoque.InsumoResumo;
import br.edu.cesar.filafood.app.application.estoque.ItemDaFichaResumo;
import br.edu.cesar.filafood.app.application.estoque.ListarFichasDaUnidade;
import br.edu.cesar.filafood.app.application.estoque.ListarInsumos;
import br.edu.cesar.filafood.app.application.estoque.NovoInsumo;
import br.edu.cesar.filafood.app.application.estoque.PedidoDoPreparo;
import br.edu.cesar.filafood.app.application.estoque.PedidoParaPreparo;
import br.edu.cesar.filafood.app.application.estoque.ProdutoComFicha;
import br.edu.cesar.filafood.app.application.estoque.RegistrarEntradaDeInsumo;
import br.edu.cesar.filafood.app.application.estoque.RemoverItemDaFicha;
import br.edu.cesar.filafood.app.config.PedidoDeDemonstracao;
import br.edu.cesar.filafood.app.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;

/**
 * Verificação de fumaça das operações contra o banco de dev semeado (D-19): lista, detalha,
 * cadastra, dá entrada, desativa e reativa, confere a ficha, dá baixa no pedido do seed e cancela o
 * preparo devolvendo os insumos. Roda com o perfil {@code verificacao}.
 *
 * A baixa e o cancelamento acontecem uma vez: se o pedido já não está no estado esperado, a etapa é
 * apenas informada.
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
    private final PedidoDoPreparo pedidos;
    private final BaixaDoPreparo baixa;
    private final DevolucaoDoCancelamento devolucao;
    private final EditarInsumo editar;

    public VerificacaoDeOperacoes(ListarInsumos listar, DetalharInsumo detalhar,
            RegistrarEntradaDeInsumo registrarEntrada, AlterarEstadoDoInsumo alterarEstado,
            CadastrarInsumo cadastrar, ListarFichasDaUnidade listarFichas,
            AdicionarItemNaFicha adicionarItem, RemoverItemDaFicha removerItem, PedidoDoPreparo pedidos,
            BaixaDoPreparo baixa, DevolucaoDoCancelamento devolucao, EditarInsumo editar) {
        this.listar = listar;
        this.detalhar = detalhar;
        this.registrarEntrada = registrarEntrada;
        this.alterarEstado = alterarEstado;
        this.cadastrar = cadastrar;
        this.listarFichas = listarFichas;
        this.adicionarItem = adicionarItem;
        this.removerItem = removerItem;
        this.pedidos = pedidos;
        this.baixa = baixa;
        this.devolucao = devolucao;
        this.editar = editar;
    }

    @Override
    public void run(String... args) {
        String unidade = UnidadeDeDemonstracao.ID;

        List<InsumoResumo> insumos = listar.executar(unidade);
        exigir(insumos.size() >= 4, "esperava ao menos os 4 insumos do seed, veio " + insumos.size());

        InsumoResumo pao = buscar(insumos, "Pão");
        InsumoDetalhe detalhe = detalhar.executar(pao.id().toString());
        exigir(detalhe.movimentacoes().size() >= 2,
                "histórico do Pão deveria ter entrada e saída, veio " + detalhe.movimentacoes().size());
        exigir(detalhe.saldo().compareTo(BigDecimal.ZERO) > 0, "o saldo do Pão deveria ser positivo");

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

        editar.executar(new AlteracaoDeInsumo(novo.toString(), nome + " editado", "sc", new BigDecimal("9"), 7));
        InsumoResumo editado = buscar(listar.executar(unidade), nome + " editado");
        exigir(comparar(editado.estoqueMinimo(), "9.000"),
                "estoque mínimo do insumo editado deveria ser 9, veio " + editado.estoqueMinimo());
        exigir(editado.prazoDeEntregaEmDias() == 7, "prazo do insumo editado deveria ser 7");
        try {
            editar.executar(new AlteracaoDeInsumo(novo.toString(), "Pão", "kg", new BigDecimal("9"), 7));
            throw new IllegalStateException("VERIFICACAO FALHOU: nome repetido na edição deveria ser recusado");
        } catch (RegraDeNegocioException esperada) {
            exigir("já existe um insumo com esse nome na unidade".equals(esperada.getMessage()),
                    "mensagem inesperada da edição repetida: " + esperada.getMessage());
        }

        System.out.println("VERIFICACAO OK: " + insumos.size()
                + " insumos no seed; cadastro, entrada, saldo, a-repor, desativar, reativar e editar conferidos.");
        verificarFichas(unidade, pao);
        verificarPreparo(unidade);
    }

    /** Ficha técnica do seed: produtos, itens, porções cobertas e as recusas da RN 2. */
    private void verificarFichas(String unidade, InsumoResumo pao) {
        FichasDaUnidade fichas = listarFichas.executar(unidade, null);
        exigir(fichas.produtos().size() == 3, "esperava os 3 produtos do seed, veio " + fichas.produtos().size());

        ProdutoComFicha classico = produto(fichas, "Hambúrguer Clássico");
        FichaDetalhe fichaDoClassico = listarFichas.executar(unidade, classico.id()).ficha().orElseThrow();
        exigir(fichaDoClassico.temFicha() && fichaDoClassico.itens().size() == 4,
                "ficha do Clássico deveria ter 4 itens, veio " + fichaDoClassico.itens().size());
        int menorSaldo = fichaDoClassico.itens().stream().mapToInt(ItemDaFichaResumo::porcoesCobertas).min().orElse(0);
        exigir(fichaDoClassico.porcoesCobertas() == menorSaldo,
                "Clássico deveria cobrir " + menorSaldo + " porções, veio " + fichaDoClassico.porcoesCobertas());

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

        System.out.println("VERIFICACAO OK: ficha técnica com 3 produtos, Clássico com 4 itens e "
                + fichaDoClassico.porcoesCobertas() + " porções, Batata sem ficha; adicionar, recusar "
                + "repetido e remover conferidos.");
    }

    /** Ciclo do preparo (RN 4 e RN 5): baixa o pedido do seed e, em seguida, cancela e devolve. */
    private void verificarPreparo(String unidade) {
        String pedidoId = PedidoDeDemonstracao.ID;
        PedidoParaPreparo pedido = pedidos.porId(pedidoId);
        exigir(pedido.itens().size() == 2, "pedido do seed deveria ter 2 itens, veio " + pedido.itens().size());

        if (pedido.estado() == EstadoDoPedido.ENVIADO) {
            List<InsumoResumo> antes = listar.executar(unidade);
            baixa.executar(new BaixaDePreparo(pedidoId));
            List<InsumoResumo> depois = listar.executar(unidade);

            exigir(baixou(antes, depois, "Pão", "3"), "a baixa do Pão deveria ser 3");
            exigir(baixou(antes, depois, "Carne", "0.6"), "a baixa da Carne deveria ser 0,6");
            exigir(baixou(antes, depois, "Queijo", "0.12"), "a baixa do Queijo deveria ser 0,12");
            exigir(baixou(antes, depois, "Tomate", "0.08"), "a baixa do Tomate deveria ser 0,08");
            exigir(pedidos.porId(pedidoId).estado() == EstadoDoPedido.EM_PREPARO,
                    "o pedido deveria ter entrado em preparo");

            System.out.println("VERIFICACAO OK: preparo do pedido #" + PedidoDeDemonstracao.NUMERO
                    + " baixou Pão 3, Carne 0,6, Queijo 0,12 e Tomate 0,08.");
        } else {
            System.out.println("VERIFICACAO OK: pedido #" + PedidoDeDemonstracao.NUMERO + " já está em "
                    + pedido.estado() + "; a baixa não é repetida.");
        }

        if (pedidos.porId(pedidoId).estado() != EstadoDoPedido.EM_PREPARO) {
            return;
        }

        List<InsumoResumo> antesDoCancelamento = listar.executar(unidade);
        devolucao.executar(new CancelamentoDoPreparo(pedidoId));
        List<InsumoResumo> depoisDoCancelamento = listar.executar(unidade);

        exigir(devolveu(antesDoCancelamento, depoisDoCancelamento, "Pão", "3"), "a devolução do Pão deveria ser 3");
        exigir(devolveu(antesDoCancelamento, depoisDoCancelamento, "Carne", "0.6"),
                "a devolução da Carne deveria ser 0,6");
        exigir(devolveu(antesDoCancelamento, depoisDoCancelamento, "Queijo", "0.12"),
                "a devolução do Queijo deveria ser 0,12");
        exigir(devolveu(antesDoCancelamento, depoisDoCancelamento, "Tomate", "0.08"),
                "a devolução do Tomate deveria ser 0,08");
        exigir(pedidos.porId(pedidoId).estado() == EstadoDoPedido.CANCELADO,
                "o pedido deveria ter sido cancelado");

        try {
            devolucao.executar(new CancelamentoDoPreparo(pedidoId));
            throw new IllegalStateException("VERIFICACAO FALHOU: cancelar de novo deveria ser recusado");
        } catch (RegraDeNegocioException esperada) {
            exigir("a devolução só é aceita para pedido em preparo".equals(esperada.getMessage()),
                    "mensagem inesperada do segundo cancelamento: " + esperada.getMessage());
        }

        System.out.println("VERIFICACAO OK: cancelamento do pedido #" + PedidoDeDemonstracao.NUMERO
                + " devolveu Pão 3, Carne 0,6, Queijo 0,12 e Tomate 0,08 e recusou o segundo cancelamento.");
    }

    private static boolean baixou(List<InsumoResumo> antes, List<InsumoResumo> depois, String nome, String quantidade) {
        return variacao(antes, depois, nome).compareTo(new BigDecimal(quantidade).negate()) == 0;
    }

    private static boolean devolveu(List<InsumoResumo> antes, List<InsumoResumo> depois, String nome,
            String quantidade) {
        return variacao(antes, depois, nome).compareTo(new BigDecimal(quantidade)) == 0;
    }

    private static BigDecimal variacao(List<InsumoResumo> antes, List<InsumoResumo> depois, String nome) {
        return saldoDe(depois, nome).subtract(saldoDe(antes, nome));
    }

    private static BigDecimal saldoDe(List<InsumoResumo> insumos, String nome) {
        return insumos.stream()
                .filter(insumo -> insumo.nome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("insumo não encontrado: " + nome))
                .saldo();
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