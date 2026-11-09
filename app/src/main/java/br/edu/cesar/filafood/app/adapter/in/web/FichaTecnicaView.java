package br.edu.cesar.filafood.app.adapter.in.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.app.adapter.in.web.layout.Acao;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Badge;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Cabecalho;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Campo;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Entradas;
import br.edu.cesar.filafood.app.adapter.in.web.layout.MainLayout;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Tabela;
import br.edu.cesar.filafood.app.application.estoque.AdicionarItemNaFicha;
import br.edu.cesar.filafood.app.application.estoque.FichaDetalhe;
import br.edu.cesar.filafood.app.application.estoque.FichasDaUnidade;
import br.edu.cesar.filafood.app.application.estoque.InsumoResumo;
import br.edu.cesar.filafood.app.application.estoque.ItemDaFichaResumo;
import br.edu.cesar.filafood.app.application.estoque.ListarFichasDaUnidade;
import br.edu.cesar.filafood.app.application.estoque.ListarInsumos;
import br.edu.cesar.filafood.app.application.estoque.ProdutoComFicha;
import br.edu.cesar.filafood.app.application.estoque.RemoverItemDaFicha;
import br.edu.cesar.filafood.app.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;

/** Ficha técnica no layout e no CSS do protótipo (F1 RN 2, D-15). */
@Route(value = "ficha-tecnica", layout = MainLayout.class)
@PageTitle("FilaFood · Ficha técnica")
public class FichaTecnicaView extends Div {

    private static final String RODAPE = "Cada item exige insumo ativo e quantidade maior que zero, e o "
            + "mesmo insumo não pode aparecer duas vezes no produto nesta unidade. A ficha vale por "
            + "unidade, e produto sem ficha não pode ser vendido.";

    private final ListarFichasDaUnidade listar;
    private final AdicionarItemNaFicha adicionar;
    private final RemoverItemDaFicha remover;
    private final ListarInsumos listarInsumos;
    private final Paragraph aviso = new Paragraph();

    private String produtoSelecionado;

    public FichaTecnicaView(ListarFichasDaUnidade listar, AdicionarItemNaFicha adicionar,
            RemoverItemDaFicha remover, ListarInsumos listarInsumos) {
        this.listar = listar;
        this.adicionar = adicionar;
        this.remover = remover;
        this.listarInsumos = listarInsumos;
        aviso.setVisible(false);
        montar();
    }

    private void montar() {
        FichasDaUnidade dados = listar.executar(UnidadeDeDemonstracao.ID, produtoSelecionado);
        FichaDetalhe ficha = dados.ficha().orElse(null);
        if (ficha != null) {
            produtoSelecionado = ficha.produtoId();
        }

        removeAll();
        aviso.setVisible(false);

        Div grid = new Div();
        grid.addClassName("grid-2");
        grid.add(cardProdutos(dados.produtos()), cardFicha(ficha));

        add(cabecalho(dados.produtos()), aviso, grid, cardAdicionar(ficha), rodape());
    }

    private Cabecalho cabecalho(List<ProdutoComFicha> produtos) {
        long semFicha = produtos.stream().filter(produto -> !produto.temFicha()).count();
        Cabecalho cabecalho = new Cabecalho("Ficha técnica",
                "Insumos consumidos por cada produto, na unidade " + UnidadeDeDemonstracao.NOME + ".");
        cabecalho.badges(
                Badge.neutro(produtos.size() + " produtos no cardápio"),
                semFicha == 0 ? Badge.ok("todas as fichas") : Badge.alerta(semFicha + " sem ficha"));
        return cabecalho;
    }

    private Div cardProdutos(List<ProdutoComFicha> produtos) {
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Produtos do cardápio", "a disponibilidade é o menor saldo"));

        if (produtos.isEmpty()) {
            Paragraph vazio = new Paragraph("Nenhum produto cadastrado nesta unidade.");
            vazio.addClassName("vazio");
            card.add(vazio);
            return card;
        }

        Tabela tabela = new Tabela("Produto", "Itens", "Porções cobertas", "Situação");
        produtos.forEach(produto -> tabela.linha(
                nomeDoProduto(produto),
                new Span(produto.temFicha() ? String.valueOf(produto.quantidadeDeItens()) : "—"),
                new Span(produto.temFicha() ? String.valueOf(produto.porcoesCobertas()) : "—"),
                situacao(produto)));
        card.add(tabela);
        return card;
    }

    private Component nomeDoProduto(ProdutoComFicha produto) {
        if (produto.id().equals(produtoSelecionado)) {
            Span selecionado = new Span(produto.nome());
            selecionado.addClassName("destaque");
            return selecionado;
        }
        return Acao.link(produto.nome(), () -> selecionar(produto.id()));
    }

    private Component situacao(ProdutoComFicha produto) {
        if (!produto.temFicha()) {
            return Badge.alerta("sem ficha");
        }
        return produto.porcoesCobertas() > 0 ? Badge.ok("disponível") : Badge.repor("sem saldo");
    }

    private Div cardFicha(FichaDetalhe ficha) {
        Div card = new Div();
        card.addClassName("card");

        if (ficha == null) {
            card.add(tituloDoCard("Ficha técnica", "nenhum produto"), new Paragraph("Selecione um produto."));
            return card;
        }

        card.add(tituloDoCard("Ficha técnica · " + ficha.produto(),
                ficha.temFicha() ? ficha.itens().size() + " itens" : "sem ficha"));

        if (!ficha.temFicha()) {
            Div corpo = new Div();
            corpo.addClassName("card-corpo");
            Paragraph texto = new Paragraph("Produto sem ficha técnica na unidade não pode ser vendido. "
                    + "Adicione o primeiro insumo para abrir a ficha.");
            corpo.add(texto, Badge.alerta("sem ficha"));
            card.add(corpo);
            return card;
        }

        Tabela tabela = new Tabela("Insumo", "Por porção", "Saldo", "Porções cobertas", "Ações");
        for (ItemDaFichaResumo item : ficha.itens()) {
            tabela.linha(
                    destaque(item.insumo()),
                    new Span(texto(item.porcao()) + " " + item.unidadeDeMedida()),
                    new Span(texto(item.saldo()) + " " + item.unidadeDeMedida()),
                    new Span(String.valueOf(item.porcoesCobertas())),
                    Acao.link("Remover", () -> removerItem(ficha.produtoId(), item.insumoId())));
        }
        card.add(tabela);

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(ficha.porcoesCobertas() > 0
                ? Badge.ok("disponível: " + ficha.porcoesCobertas() + " porções")
                : Badge.repor("sem saldo para uma porção"));
        card.add(corpo);
        return card;
    }

    private Div cardAdicionar(FichaDetalhe ficha) {
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Adicionar item à ficha · " + (ficha == null ? "produto" : ficha.produto()), "F1 RN 2"));
        if (ficha == null) {
            card.add(new Paragraph("Selecione um produto para editar a ficha."));
            return card;
        }

        Map<String, String> idPorRotulo = new LinkedHashMap<>();
        for (InsumoResumo insumo : listarInsumos.executar(UnidadeDeDemonstracao.ID)) {
            if (insumo.ativo()) {
                idPorRotulo.put(insumo.nome() + " · " + insumo.unidadeDeMedida(), insumo.id().toString());
            }
        }

        Select<String> insumos = new Select<>();
        insumos.setItems(idPorRotulo.keySet());
        insumos.setWidth("100%");
        if (!idPorRotulo.isEmpty()) {
            insumos.setValue(idPorRotulo.keySet().iterator().next());
        }

        Input quantidade = Entradas.texto();

        Div campos = new Div();
        campos.addClassName("linha-campos-2");
        campos.add(new Campo("Insumo", insumos), new Campo("Quantidade por porção", quantidade));

        Div acoes = new Div();
        acoes.addClassName("acoes-form");
        acoes.add(Acao.primario("Adicionar item", () -> adicionarItem(ficha.produtoId(), insumos.getValue(),
                idPorRotulo, quantidade)));

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(campos, acoes);

        card.add(corpo);
        return card;
    }

    private void selecionar(String produtoId) {
        produtoSelecionado = produtoId;
        montar();
    }

    private void adicionarItem(String produtoId, String rotulo, Map<String, String> idPorRotulo, Input quantidade) {
        try {
            String insumoId = idPorRotulo.get(rotulo);
            if (insumoId == null) {
                throw new RegraDeNegocioException("selecione o insumo");
            }
            adicionar.executar(UnidadeDeDemonstracao.ID, produtoId, insumoId,
                    BigDecimal.valueOf(numero(quantidade.getValue())).setScale(3, RoundingMode.HALF_UP));
            montar();
            aviso("Item adicionado à ficha.", true);
        } catch (RegraDeNegocioException | NumberFormatException excecao) {
            aviso(excecao.getMessage(), false);
        }
    }

    private void removerItem(String produtoId, String insumoId) {
        try {
            remover.executar(UnidadeDeDemonstracao.ID, produtoId, insumoId);
            montar();
            aviso("Item removido da ficha.", true);
        } catch (RegraDeNegocioException excecao) {
            aviso(excecao.getMessage(), false);
        }
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

    private Span destaque(String texto) {
        Span span = new Span(texto);
        span.addClassName("destaque");
        return span;
    }

    private Paragraph rodape() {
        Paragraph rodape = new Paragraph(RODAPE);
        rodape.addClassName("rodape");
        return rodape;
    }

    private void aviso(String texto, boolean sucesso) {
        aviso.setText(texto);
        aviso.removeClassNames("aviso-ok", "aviso-erro");
        aviso.addClassName(sucesso ? "aviso-ok" : "aviso-erro");
        aviso.setVisible(true);
    }

    private static String texto(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    private static double numero(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new NumberFormatException("informe um número");
        }
        return Double.parseDouble(valor.replace(',', '.').strip());
    }
}
