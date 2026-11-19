package br.edu.cesar.filafood.app.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.app.adapter.in.web.layout.Acao;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Badge;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Busca;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Cabecalho;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Campo;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Entradas;
import br.edu.cesar.filafood.app.adapter.in.web.layout.MainLayout;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Tabela;
import br.edu.cesar.filafood.app.application.cardapio.AlteracaoDeProduto;
import br.edu.cesar.filafood.app.application.cardapio.AlterarEstadoDoProduto;
import br.edu.cesar.filafood.app.application.cardapio.CadastrarProduto;
import br.edu.cesar.filafood.app.application.cardapio.DefinirPrecoDoProduto;
import br.edu.cesar.filafood.app.application.cardapio.EditarProduto;
import br.edu.cesar.filafood.app.application.cardapio.EncerrarPeriodoDePreco;
import br.edu.cesar.filafood.app.application.cardapio.ExcluirProduto;
import br.edu.cesar.filafood.app.application.cardapio.ListarCardapioDaUnidade;
import br.edu.cesar.filafood.app.application.cardapio.NovoPeriodoDePreco;
import br.edu.cesar.filafood.app.application.cardapio.NovoProduto;
import br.edu.cesar.filafood.app.application.cardapio.PeriodoPrecoResumo;
import br.edu.cesar.filafood.app.application.cardapio.ProdutoCardapioDetalhe;
import br.edu.cesar.filafood.app.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.dominio.cardapio.RegraDeNegocioException;

/** Tela do Cardápio (F6): CRUD de produtos + Preço Vigente (RN 4) + Disponibilidade (RN 5). */
@Route(value = "cardapio", layout = MainLayout.class)
@PageTitle("FilaFood · Cardápio")
public class CardapioView extends Div {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String RODAPE = "O preço gravado no item do pedido é o preço vigente no instante em que "
            + "o pedido é criado. Sem período vigente, vale o preço base do cadastro. As porções possíveis são "
            + "calculadas pelo insumo limitante da ficha técnica, sem reservar estoque.";

    private final ListarCardapioDaUnidade listar;
    private final CadastrarProduto cadastrar;
    private final EditarProduto editar;
    private final AlterarEstadoDoProduto alterarEstado;
    private final ExcluirProduto excluir;
    private final DefinirPrecoDoProduto definirPreco;
    private final EncerrarPeriodoDePreco encerrarPeriodo;

    private final Cabecalho cabecalho = new Cabecalho("Cardápio e preços",
            "Catálogo de produtos da unidade " + UnidadeDeDemonstracao.NOME
                    + ", períodos de preço vigente e disponibilidade por estoque.");
    private final Busca busca = new Busca("Buscar por nome ou categoria");
    private final Select<String> filtro = new Select<>();
    private final Paragraph aviso = new Paragraph();
    private final Div conteudoDinamico = new Div();

    private List<ProdutoCardapioDetalhe> produtos = List.of();
    private String produtoSelecionadoId;

    public CardapioView(ListarCardapioDaUnidade listar, CadastrarProduto cadastrar, EditarProduto editar,
            AlterarEstadoDoProduto alterarEstado, ExcluirProduto excluir, DefinirPrecoDoProduto definirPreco,
            EncerrarPeriodoDePreco encerrarPeriodo) {
        this.listar = listar;
        this.cadastrar = cadastrar;
        this.editar = editar;
        this.alterarEstado = alterarEstado;
        this.excluir = excluir;
        this.definirPreco = definirPreco;
        this.encerrarPeriodo = encerrarPeriodo;

        busca.campo().addValueChangeListener(e -> render());
        filtro.setItems("Estado: todos", "Estado: disponíveis", "Estado: indisponíveis", "Estado: inativos");
        filtro.setValue("Estado: todos");
        filtro.addClassName("filtro");
        filtro.setWidth("210px");
        filtro.addValueChangeListener(e -> render());

        aviso.setVisible(false);

        Div barra = new Div();
        barra.addClassName("barra");
        barra.add(busca, filtro, Acao.primario("Novo produto", this::abrirModalCadastro));

        Paragraph rodape = new Paragraph(RODAPE);
        rodape.addClassName("rodape");

        add(cabecalho, aviso, barra, conteudoDinamico, rodape);
        recarregar();
    }

    private void recarregar() {
        produtos = listar.executar(UnidadeDeDemonstracao.ID);
        if (produtoSelecionadoId == null && !produtos.isEmpty()) {
            produtoSelecionadoId = produtos.get(0).id();
        } else if (produtoSelecionadoId != null
                && produtos.stream().noneMatch(p -> p.id().equals(produtoSelecionadoId))) {
            produtoSelecionadoId = produtos.isEmpty() ? null : produtos.get(0).id();
        }
        render();
    }

    private void render() {
        long ativos = produtos.stream().filter(ProdutoCardapioDetalhe::ativo).count();
        long disponiveis = produtos.stream().filter(ProdutoCardapioDetalhe::disponivelParaVenda).count();
        cabecalho.badges(
                Badge.neutro(ativos + " produtos ativos"),
                disponiveis > 0 ? Badge.ok(disponiveis + " disponíveis") : Badge.repor("0 disponíveis"));

        conteudoDinamico.removeAll();
        conteudoDinamico.add(cardKpis(), cardTabelaProdutos());

        ProdutoCardapioDetalhe selecionado = produtos.stream()
                .filter(p -> p.id().equals(produtoSelecionadoId))
                .findFirst()
                .orElse(null);

        if (selecionado != null) {
            Div grid = new Div();
            grid.addClassName("grid-2");
            grid.add(cardPeriodosDePreco(selecionado), cardNovoPeriodo(selecionado));
            conteudoDinamico.add(grid);
        }
    }

    private Div cardKpis() {
        long ativos = produtos.stream().filter(ProdutoCardapioDetalhe::ativo).count();
        long disponiveis = produtos.stream().filter(ProdutoCardapioDetalhe::disponivelParaVenda).count();
        long comPrecoDaUnidade = produtos.stream().filter(p -> !p.usandoPrecoBase()).count();
        int menorSaldo = produtos.stream()
                .filter(ProdutoCardapioDetalhe::disponivelParaVenda)
                .mapToInt(ProdutoCardapioDetalhe::porcoesPossiveis)
                .min()
                .orElse(0);

        Div kpis = new Div();
        kpis.addClassName("kpis");
        kpis.add(
                kpi("Produtos ativos", String.valueOf(ativos), "no catálogo da unidade"),
                kpi("Disponíveis hoje", String.valueOf(disponiveis), "ativos e com saldo na ficha"),
                kpi("Preço de vigência", String.valueOf(comPrecoDaUnidade), "usando período próprio"),
                kpi("Menor disponibilidade", String.valueOf(menorSaldo), "porções cobertas"),
                kpi("Unidade", UnidadeDeDemonstracao.NOME, "gestão do cardápio"));

        Div card = new Div();
        card.addClassName("card");
        card.add(kpis);
        return card;
    }

    private Div kpi(String rotulo, String valor, String nota) {
        Div kpi = new Div();
        kpi.addClassName("kpi");
        Div r = new Div();
        r.addClassName("kpi-rotulo");
        r.setText(rotulo);
        Div v = new Div();
        v.addClassName("kpi-valor");
        v.setText(valor);
        Div n = new Div();
        n.addClassName("kpi-nota");
        n.setText(nota);
        kpi.add(r, v, n);
        return kpi;
    }

    private Div cardTabelaProdutos() {
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Produtos do cardápio (CRUD · F6 RN 1, RN 4 e RN 5)",
                "clique no nome para gerenciar vigências de preço"));

        List<ProdutoCardapioDetalhe> visiveis = filtrar();
        if (visiveis.isEmpty()) {
            Paragraph vazio = new Paragraph("Nenhum produto encontrado para este filtro.");
            vazio.addClassName("vazio");
            card.add(vazio);
            return card;
        }

        Tabela tabela = new Tabela("Produto", "Categoria", "Preço base", "Preço vigente", "Porções", "Estado", "Ações");
        for (ProdutoCardapioDetalhe produto : visiveis) {
            tabela.linha(
                    celulaNome(produto),
                    new Span(produto.categoria()),
                    new Span(moeda(produto.precoBase())),
                    celulaPrecoVigente(produto),
                    new Span(produto.temFichaTecnica() ? produto.porcoesPossiveis() + " porções" : "sem ficha"),
                    celulaEstado(produto),
                    celulaAcoes(produto));
        }
        card.add(tabela);
        return card;
    }

    private Component celulaNome(ProdutoCardapioDetalhe produto) {
        Div container = new Div();
        if (produto.id().equals(produtoSelecionadoId)) {
            Span destaque = new Span(produto.nome());
            destaque.addClassName("destaque");
            container.add(destaque);
        } else {
            container.add(Acao.link(produto.nome(), () -> {
                produtoSelecionadoId = produto.id();
                render();
            }));
        }
        if (produto.descricao() != null && !produto.descricao().isBlank()) {
            Span desc = new Span(produto.descricao());
            desc.addClassName("unidade");
            container.add(desc);
        }
        return container;
    }

    private Component celulaPrecoVigente(ProdutoCardapioDetalhe produto) {
        if (produto.usandoPrecoBase()) {
            return Badge.neutro(moeda(produto.precoVigente()) + " · base");
        }
        return Badge.ok(moeda(produto.precoVigente()) + " · vigência");
    }

    private Component celulaEstado(ProdutoCardapioDetalhe produto) {
        if (!produto.ativo()) {
            return Badge.neutro("inativo");
        }
        if (!produto.temFichaTecnica()) {
            return Badge.alerta("sem ficha");
        }
        return produto.disponivelParaVenda() ? Badge.ok("disponível") : Badge.repor("indisponível");
    }

    private Component celulaAcoes(ProdutoCardapioDetalhe produto) {
        Div acoes = new Div();
        acoes.addClassName("acoes");
        acoes.add(Acao.link("Preços", () -> {
            produtoSelecionadoId = produto.id();
            render();
        }));
        acoes.add(Acao.link("Editar", () -> abrirModalEdicao(produto)));
        acoes.add(produto.ativo()
                ? Acao.apagada("Desativar", () -> alterarEstado(produto, false))
                : Acao.apagada("Reativar", () -> alterarEstado(produto, true)));
        acoes.add(Acao.apagada("Excluir", () -> confirmarExclusao(produto)));
        return acoes;
    }

    private Div cardPeriodosDePreco(ProdutoCardapioDetalhe produto) {
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Períodos de preço · " + produto.nome(),
                "preço base: " + moeda(produto.precoBase())));

        if (produto.periodos().isEmpty()) {
            Div corpo = new Div();
            corpo.addClassName("card-corpo");
            corpo.add(new Paragraph("Nenhum período específico cadastrado. O produto usa o preço base de "
                    + moeda(produto.precoBase()) + "."));
            card.add(corpo);
            return card;
        }

        Tabela tabela = new Tabela("Preço", "Início", "Fim", "Situação", "Ações");
        for (PeriodoPrecoResumo periodo : produto.periodos()) {
            tabela.linha(
                    new Span(moeda(periodo.preco())),
                    new Span(DATA_BR.format(periodo.inicio())),
                    new Span(periodo.fim() == null ? "sem fim" : DATA_BR.format(periodo.fim())),
                    periodo.vigenteHoje() ? Badge.ok("vigente") : Badge.neutro("programado/histórico"),
                    Acao.link("Encerrar", () -> abrirModalEncerrarPeriodo(produto, periodo)));
        }
        card.add(tabela);
        return card;
    }

    private Div cardNovoPeriodo(ProdutoCardapioDetalhe produto) {
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Definir período de preço · " + produto.nome(), "F6 RN 4"));

        Input preco = Entradas.texto();
        preco.setPlaceholder("Ex.: 27,90");
        Input inicio = Entradas.texto();
        inicio.setValue(LocalDate.now().toString());
        inicio.setPlaceholder("AAAA-MM-DD ou DD/MM/AAAA");
        Input fim = Entradas.texto();
        fim.setPlaceholder("Opcional (vazio = sem fim)");

        Div campos = new Div();
        campos.addClassName("linha-campos");
        campos.add(
                new Campo("Preço da unidade (R$)", preco),
                new Campo("Data inicial", inicio),
                new Campo("Data final (opcional)", fim));

        Div acoes = new Div();
        acoes.addClassName("acoes-form");
        acoes.add(Acao.primario("Salvar período", () -> salvarNovoPeriodo(produto.id(), preco, inicio, fim)));

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(campos, acoes);
        card.add(corpo);
        return card;
    }

    private void salvarNovoPeriodo(String produtoId, Input preco, Input inicio, Input fim) {
        try {
            LocalDate dataInicio = lerDataObrigatoria(inicio.getValue(), "a data inicial");
            LocalDate dataFim = (fim.getValue() == null || fim.getValue().isBlank())
                    ? null
                    : lerDataObrigatoria(fim.getValue(), "a data final");
            definirPreco.executar(new NovoPeriodoDePreco(produtoId, numero(preco.getValue()), dataInicio, dataFim));
            recarregar();
            mostrarAviso("Período de preço registrado.", true);
        } catch (RegraDeNegocioException | NumberFormatException excecao) {
            mostrarAviso(excecao.getMessage(), false);
        }
    }

    private void abrirModalEncerrarPeriodo(ProdutoCardapioDetalhe produto, PeriodoPrecoResumo periodo) {
        Dialog dialogo = new Dialog();
        dialogo.setWidth("420px");

        Div caixa = new Div();
        caixa.addClassName("confirmacao");

        Div titulo = new Div();
        titulo.addClassName("confirmacao-titulo");
        titulo.setText("Encerrar período · " + produto.nome());

        Input dataFim = Entradas.texto();
        dataFim.setValue(periodo.fim() != null ? periodo.fim().toString() : LocalDate.now().toString());

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(new Campo("Data final de encerramento", dataFim));

        Div acoes = new Div();
        acoes.addClassName("acoes-form");
        acoes.add(
                Acao.primario("Confirmar encerramento", () -> {
                    try {
                        encerrarPeriodo.executar(produto.id(), periodo.id(),
                                lerDataObrigatoria(dataFim.getValue(), "a data final"));
                        dialogo.close();
                        recarregar();
                        mostrarAviso("Período encerrado mantendo o histórico.", true);
                    } catch (RegraDeNegocioException excecao) {
                        mostrarAviso(excecao.getMessage(), false);
                    }
                }),
                Acao.claro("Cancelar", dialogo::close));

        caixa.add(titulo, corpo, acoes);
        dialogo.add(caixa);
        dialogo.open();
    }

    private void abrirModalCadastro() {
        Input nome = Entradas.texto();
        Input categoria = Entradas.texto();
        categoria.setValue("Hambúrgueres");
        Input descricao = Entradas.texto();
        Input precoBase = Entradas.texto();

        abrirFormularioProduto("Novo produto do cardápio", nome, categoria, descricao, precoBase, dialogo -> {
            try {
                var id = cadastrar.executar(new NovoProduto(
                        UnidadeDeDemonstracao.ID,
                        nome.getValue(),
                        categoria.getValue(),
                        descricao.getValue(),
                        numero(precoBase.getValue())));
                produtoSelecionadoId = id.toString();
                dialogo.close();
                recarregar();
                mostrarAviso("Produto cadastrado no cardápio.", true);
            } catch (RegraDeNegocioException | NumberFormatException excecao) {
                mostrarAviso(excecao.getMessage(), false);
            }
        });
    }

    private void abrirModalEdicao(ProdutoCardapioDetalhe produto) {
        Input nome = Entradas.texto();
        nome.setValue(produto.nome());
        Input categoria = Entradas.texto();
        categoria.setValue(produto.categoria());
        Input descricao = Entradas.texto();
        descricao.setValue(produto.descricao());
        Input precoBase = Entradas.texto();
        precoBase.setValue(produto.precoBase().toPlainString());

        abrirFormularioProduto("Editar produto · " + produto.nome(), nome, categoria, descricao, precoBase, dialogo -> {
            try {
                editar.executar(new AlteracaoDeProduto(
                        produto.id(),
                        nome.getValue(),
                        categoria.getValue(),
                        descricao.getValue(),
                        numero(precoBase.getValue())));
                dialogo.close();
                recarregar();
                mostrarAviso("Dados do produto atualizados.", true);
            } catch (RegraDeNegocioException | NumberFormatException excecao) {
                mostrarAviso(excecao.getMessage(), false);
            }
        });
    }

    private void abrirFormularioProduto(String tituloModal, Input nome, Input categoria, Input descricao,
            Input precoBase, java.util.function.Consumer<Dialog> aoSalvar) {
        Dialog dialogo = new Dialog();
        dialogo.setWidth("440px");

        Div caixa = new Div();
        caixa.addClassName("confirmacao");

        Div titulo = new Div();
        titulo.addClassName("confirmacao-titulo");
        titulo.setText(tituloModal);

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(
                new Campo("Nome do produto", nome),
                new Campo("Categoria", categoria),
                new Campo("Descrição (opcional)", descricao),
                new Campo("Preço base (R$)", precoBase));

        Div acoes = new Div();
        acoes.addClassName("acoes-form");
        acoes.add(
                Acao.primario("Salvar", () -> aoSalvar.accept(dialogo)),
                Acao.claro("Cancelar", dialogo::close));

        caixa.add(titulo, corpo, acoes);
        dialogo.add(caixa);
        dialogo.open();
    }

    private void alterarEstado(ProdutoCardapioDetalhe produto, boolean ativar) {
        try {
            if (ativar) {
                alterarEstado.reativar(produto.id());
            } else {
                alterarEstado.desativar(produto.id());
            }
            recarregar();
            mostrarAviso(ativar ? "Produto reativado." : "Produto desativado.", true);
        } catch (RegraDeNegocioException excecao) {
            mostrarAviso(excecao.getMessage(), false);
        }
    }

    private void confirmarExclusao(ProdutoCardapioDetalhe produto) {
        Dialog dialogo = new Dialog();
        dialogo.setWidth("420px");

        Div caixa = new Div();
        caixa.addClassName("confirmacao");

        Div titulo = new Div();
        titulo.addClassName("confirmacao-titulo");
        titulo.setText("Excluir " + produto.nome() + "?");

        Paragraph texto = new Paragraph("A exclusão remove o produto caso ele não possua ficha técnica nem pedidos. "
                + "Para preservar histórico operacional, use Desativar.");
        texto.addClassName("confirmacao-texto");

        Div acoes = new Div();
        acoes.addClassName("acoes-form");
        acoes.add(
                Acao.perigo("Confirmar exclusão", () -> {
                    try {
                        excluir.executar(produto.id());
                        dialogo.close();
                        recarregar();
                        mostrarAviso("Produto excluído do catálogo.", true);
                    } catch (RegraDeNegocioException excecao) {
                        dialogo.close();
                        mostrarAviso(excecao.getMessage(), false);
                    }
                }),
                Acao.claro("Cancelar", dialogo::close));

        caixa.add(titulo, texto, acoes);
        dialogo.add(caixa);
        dialogo.open();
    }

    private List<ProdutoCardapioDetalhe> filtrar() {
        String termo = busca.campo().getValue() == null ? ""
                : busca.campo().getValue().strip().toLowerCase(Locale.ROOT);
        String estado = filtro.getValue() == null ? "Estado: todos" : filtro.getValue();
        return produtos.stream()
                .filter(p -> p.nome().toLowerCase(Locale.ROOT).contains(termo)
                        || p.categoria().toLowerCase(Locale.ROOT).contains(termo))
                .filter(p -> switch (estado) {
                    case "Estado: disponíveis" -> p.disponivelParaVenda();
                    case "Estado: indisponíveis" -> p.ativo() && !p.disponivelParaVenda();
                    case "Estado: inativos" -> !p.ativo();
                    default -> true;
                })
                .toList();
    }

    private Div tituloDoCard(String texto, String direita) {
        Div titulo = new Div();
        titulo.addClassName("card-titulo");
        titulo.add(new Span(texto));
        Span dir = new Span(direita);
        dir.addClassName("dir");
        titulo.add(dir);
        return titulo;
    }

    private void mostrarAviso(String texto, boolean sucesso) {
        aviso.setText(texto);
        aviso.removeClassNames("aviso-ok", "aviso-erro");
        aviso.addClassName(sucesso ? "aviso-ok" : "aviso-erro");
        aviso.setVisible(true);
    }

    private static String moeda(BigDecimal valor) {
        return "R$ " + String.format(PT_BR, "%.2f", valor);
    }

    private static BigDecimal numero(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new NumberFormatException("informe o preço");
        }
        return new BigDecimal(valor.replace(',', '.').strip());
    }

    private static LocalDate lerDataObrigatoria(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new RegraDeNegocioException("informe " + campo);
        }
        String limpo = texto.strip();
        try {
            if (limpo.contains("/")) {
                return LocalDate.parse(limpo, DATA_BR);
            }
            return LocalDate.parse(limpo);
        } catch (DateTimeParseException e) {
            throw new RegraDeNegocioException("formato de data inválido; use DD/MM/AAAA ou AAAA-MM-DD");
        }
    }
}