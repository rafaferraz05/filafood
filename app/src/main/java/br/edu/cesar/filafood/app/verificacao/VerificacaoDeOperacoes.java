package br.edu.cesar.filafood.app.verificacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.aplicacao.estoque.ficha.FichaDetalhe;
import br.edu.cesar.filafood.aplicacao.estoque.ficha.FichaServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.ficha.FichasDaUnidade;
import br.edu.cesar.filafood.aplicacao.estoque.ficha.ItemDaFichaResumo;
import br.edu.cesar.filafood.aplicacao.estoque.ficha.ProdutoComFicha;
import br.edu.cesar.filafood.aplicacao.estoque.insumo.AlteracaoDeInsumo;
import br.edu.cesar.filafood.aplicacao.estoque.insumo.InsumoDetalhe;
import br.edu.cesar.filafood.aplicacao.estoque.insumo.InsumoResumo;
import br.edu.cesar.filafood.aplicacao.estoque.insumo.InsumoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.insumo.NovoInsumo;
import br.edu.cesar.filafood.aplicacao.estoque.movimentacao.EntradaDeInsumo;
import br.edu.cesar.filafood.aplicacao.estoque.movimentacao.EntradaDeInsumoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.pedido.PedidoParaPreparo;
import br.edu.cesar.filafood.aplicacao.estoque.pedido.PedidoRepositorioAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.preparo.PreparoServicoAplicacao;
import br.edu.cesar.filafood.app.config.PedidoDeDemonstracao;
import br.edu.cesar.filafood.app.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;

/**
 * Verificação de fumaça das operações contra o banco de dev semeado (D-19): insumos, ficha, baixa e
 * cancelamento. Roda com o perfil {@code verificacao}; a baixa e o cancelamento acontecem uma vez.
 */
@Component
@Profile("verificacao")
public class VerificacaoDeOperacoes implements CommandLineRunner {

    private final InsumoServicoAplicacao insumos;
    private final EntradaDeInsumoServicoAplicacao entrada;
    private final FichaServicoAplicacao fichas;
    private final PreparoServicoAplicacao preparo;
    private final PedidoRepositorioAplicacao pedidos;

    public VerificacaoDeOperacoes(InsumoServicoAplicacao insumos, EntradaDeInsumoServicoAplicacao entrada,
            FichaServicoAplicacao fichas, PreparoServicoAplicacao preparo, PedidoRepositorioAplicacao pedidos) {
        this.insumos = insumos;
        this.entrada = entrada;
        this.fichas = fichas;
        this.preparo = preparo;
        this.pedidos = pedidos;
    }

    @Override
    public void run(String... args) {
        String unidade = UnidadeDeDemonstracao.ID;

        List<InsumoResumo> lista = insumos.pesquisarResumos(unidade);
        exigir(lista.size() >= 4, "esperava ao menos os 4 insumos do seed, veio " + lista.size());

        InsumoResumo pao = buscar(lista, "Pão");
        InsumoDetalhe detalhe = insumos.obterResumoExpandido(pao.id().toString());
        exigir(detalhe.movimentacoes().size() >= 2,
                "histórico do Pão deveria ter entrada e saída, veio " + detalhe.movimentacoes().size());
        exigir(detalhe.saldo().compareTo(BigDecimal.ZERO) > 0, "o saldo do Pão deveria ser positivo");

        String nome = "Insumo de verificação " + UUID.randomUUID().toString().substring(0, 8);
        InsumoId novo = insumos.cadastrar(new NovoInsumo(unidade, nome, "kg", new BigDecimal("5"), 2));
        entrada.registrar(new EntradaDeInsumo(novo.toString(), new BigDecimal("3"), new BigDecimal("7.00")));

        InsumoResumo criado = buscar(insumos.pesquisarResumos(unidade), nome);
        exigir(comparar(criado.saldo(), "3.000"), "saldo do insumo novo deveria ser 3, veio " + criado.saldo());
        exigir(criado.aRepor(), "insumo com saldo 3 e mínimo 5 deveria estar a repor");

        insumos.desativar(novo.toString());
        exigir(!buscar(insumos.pesquisarResumos(unidade), nome).ativo(), "insumo deveria estar inativo");
        insumos.reativar(novo.toString());
        exigir(buscar(insumos.pesquisarResumos(unidade), nome).ativo(), "insumo deveria estar ativo");

        insumos.alterar(new AlteracaoDeInsumo(novo.toString(), nome + " editado", "sc",
                new BigDecimal("9"), 7));
        InsumoResumo editado = buscar(insumos.pesquisarResumos(unidade), nome + " editado");
        exigir(comparar(editado.estoqueMinimo(), "9.000"),
                "estoque mínimo do insumo editado deveria ser 9, veio " + editado.estoqueMinimo());
        exigir(editado.prazoDeEntregaEmDias() == 7, "prazo do insumo editado deveria ser 7");
        try {
            insumos.alterar(new AlteracaoDeInsumo(novo.toString(), "Pão", "kg", new BigDecimal("9"), 7));
            throw new IllegalStateException("VERIFICACAO FALHOU: nome repetido na edição deveria ser recusado");
        } catch (RegraDeNegocioException esperada) {
            exigir("já existe um insumo com esse nome na unidade".equals(esperada.getMessage()),
                    "mensagem inesperada da edição repetida: " + esperada.getMessage());
        }

        System.out.println("VERIFICACAO OK: " + lista.size()
                + " insumos no seed; cadastro, entrada, saldo, a-repor, desativar, reativar e editar conferidos.");
        verificarFichas(unidade, pao);
        verificarPreparo(unidade);
    }

    private void verificarFichas(String unidade, InsumoResumo pao) {
        FichasDaUnidade resumos = fichas.pesquisarResumos(unidade, null);
        exigir(resumos.produtos().size() == 3, "esperava os 3 produtos do seed, veio " + resumos.produtos().size());

        ProdutoComFicha classico = produto(resumos, "Hambúrguer Clássico");
        FichaDetalhe fichaDoClassico = fichas.pesquisarResumos(unidade, classico.id()).ficha().orElseThrow();
        exigir(fichaDoClassico.temFicha() && fichaDoClassico.itens().size() == 4,
                "ficha do Clássico deveria ter 4 itens, veio " + fichaDoClassico.itens().size());
        int menorSaldo = fichaDoClassico.itens().stream().mapToInt(ItemDaFichaResumo::porcoesCobertas).min().orElse(0);
        exigir(fichaDoClassico.porcoesCobertas() == menorSaldo,
                "Clássico deveria cobrir " + menorSaldo + " porções, veio " + fichaDoClassico.porcoesCobertas());

        ProdutoComFicha batata = produto(resumos, "Batata Rústica");
        exigir(!batata.temFicha(), "Batata Rústica deveria estar sem ficha");

        try {
            fichas.adicionarItem(unidade, classico.id(), pao.id().toString(), BigDecimal.ONE);
            throw new IllegalStateException("VERIFICACAO FALHOU: item repetido na ficha deveria ser recusado");
        } catch (RegraDeNegocioException esperada) {
            exigir("o insumo já está na ficha técnica".equals(esperada.getMessage()),
                    "mensagem inesperada do item repetido: " + esperada.getMessage());
        }

        fichas.adicionarItem(unidade, batata.id(), pao.id().toString(), new BigDecimal("0.010"));
        FichaDetalhe fichaDaBatata = fichas.pesquisarResumos(unidade, batata.id()).ficha().orElseThrow();
        exigir(fichaDaBatata.temFicha() && fichaDaBatata.itens().size() == 1,
                "Batata deveria ter 1 item depois de adicionar, veio " + fichaDaBatata.itens().size());

        fichas.removerItem(unidade, batata.id(), pao.id().toString());
        exigir(!fichas.pesquisarResumos(unidade, batata.id()).ficha().orElseThrow().temFicha(),
                "Batata deveria voltar ao estado sem ficha");

        System.out.println("VERIFICACAO OK: ficha técnica com 3 produtos, Clássico com 4 itens e "
                + fichaDoClassico.porcoesCobertas() + " porções, Batata sem ficha; adicionar, recusar "
                + "repetido e remover conferidos.");
    }

    private void verificarPreparo(String unidade) {
        String pedidoId = PedidoDeDemonstracao.ID;
        PedidoParaPreparo pedido = pedidos.porId(pedidoId);
        exigir(pedido.itens().size() == 2, "pedido do seed deveria ter 2 itens, veio " + pedido.itens().size());

        if (pedido.estado() == EstadoDoPedido.ENVIADO) {
            List<InsumoResumo> antes = insumos.pesquisarResumos(unidade);
            preparo.iniciar(pedidoId);
            List<InsumoResumo> depois = insumos.pesquisarResumos(unidade);

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

        List<InsumoResumo> antesDoCancelamento = insumos.pesquisarResumos(unidade);
        preparo.cancelar(pedidoId);
        List<InsumoResumo> depoisDoCancelamento = insumos.pesquisarResumos(unidade);

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
            preparo.cancelar(pedidoId);
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
