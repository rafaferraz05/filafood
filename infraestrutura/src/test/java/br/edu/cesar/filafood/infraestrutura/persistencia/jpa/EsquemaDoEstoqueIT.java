package br.edu.cesar.filafood.infraestrutura.persistencia.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.PersistenceException;

import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.FornecedorId;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.infraestrutura.persistencia.jpa.compras.FornecedorJpa;
import br.edu.cesar.filafood.infraestrutura.persistencia.jpa.compras.FornecedorMapper;
import br.edu.cesar.filafood.infraestrutura.persistencia.jpa.compras.ItemCatalogoFornecedorJpa;

/**
 * Migração, mapeamento e constraints contra um Postgres real (D-13). Usa Flyway + Hibernate direto
 * porque o Spring Boot 4 exige JUnit 6 no suporte de testes, e o reactor fica no JUnit 5.11.4.
 * Roda só com {@code -Pintegracao} (Docker).
 */
@Testcontainers
class EsquemaDoEstoqueIT {

    private static final String UNIDADE = "00000000-0000-0000-0000-000000000001";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static SessionFactory fabrica;

    @BeforeAll
    static void migrarEValidarMapeamento() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();

        Configuration configuracao = new Configuration();
        configuracao.addAnnotatedClass(InsumoJpa.class);
        configuracao.addAnnotatedClass(MovimentacaoEstoqueJpa.class);
        configuracao.addAnnotatedClass(ProdutoJpa.class);
        configuracao.addAnnotatedClass(FichaTecnicaJpa.class);
        configuracao.addAnnotatedClass(ItemFichaTecnicaJpa.class);
        configuracao.addAnnotatedClass(FornecedorJpa.class);
        configuracao.addAnnotatedClass(ItemCatalogoFornecedorJpa.class);
        configuracao.setProperty("hibernate.connection.url", POSTGRES.getJdbcUrl());
        configuracao.setProperty("hibernate.connection.username", POSTGRES.getUsername());
        configuracao.setProperty("hibernate.connection.password", POSTGRES.getPassword());
        configuracao.setProperty("hibernate.hbm2ddl.auto", "validate");
        configuracao.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        fabrica = configuracao.buildSessionFactory();
    }

    @AfterAll
    static void fechar() {
        if (fabrica != null) {
            fabrica.close();
        }
    }

    @Test
    void persisteInsumoComMovimentacaoELeDeVolta() {
        UUID id = UUID.randomUUID();
        try (Session sessao = fabrica.openSession()) {
            sessao.beginTransaction();
            InsumoJpa insumo = new InsumoJpa(id, UUID.fromString(UNIDADE), "Farinha", "kg",
                    new BigDecimal("5.000"), 3, "ATIVO");
            insumo.adicionarMovimentacao(new MovimentacaoEstoqueJpa(UUID.randomUUID(), insumo, "ENTRADA",
                    "COMPRA", new BigDecimal("10.000"), new BigDecimal("4.00"), "", Instant.now()));
            sessao.persist(insumo);
            sessao.getTransaction().commit();
        }

        try (Session sessao = fabrica.openSession()) {
            InsumoJpa carregado = sessao.find(InsumoJpa.class, id);
            assertNotNull(carregado);
            assertEquals(1, carregado.movimentacoes().size());
            assertEquals(0, new BigDecimal("10.000").compareTo(carregado.movimentacoes().get(0).quantidade()));
            assertEquals(0, new BigDecimal("4.00").compareTo(carregado.movimentacoes().get(0).custoUnitario()));
        }
    }

    @Test
    void recusaMesmoNomeNaMesmaUnidade() throws SQLException {
        String sql = "insert into insumo (id, unidade_id, nome, unidade_de_medida, estoque_minimo, "
                + "prazo_de_entrega_em_dias, estado) values (?, ?, 'Pão', 'kg', 5, 3, 'ATIVO')";
        try (Connection conexao = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
                POSTGRES.getPassword())) {
            try (var comando = conexao.prepareStatement(sql)) {
                comando.setObject(1, UUID.randomUUID());
                comando.setObject(2, UUID.fromString(UNIDADE));
                comando.executeUpdate();
            }
            try (var comando = conexao.prepareStatement(sql)) {
                comando.setObject(1, UUID.randomUUID());
                comando.setObject(2, UUID.fromString(UNIDADE));
                assertThrows(SQLException.class, comando::executeUpdate);
            }
        }
    }

    @Test
    void persisteFichaTecnicaComItemELeDeVolta() {
        UUID produtoId = UUID.randomUUID();
        UUID insumoId = UUID.randomUUID();
        UUID fichaId = UUID.randomUUID();
        try (Session sessao = fabrica.openSession()) {
            sessao.beginTransaction();
            ProdutoJpa produto = new ProdutoJpa(produtoId, UUID.fromString(UNIDADE), "Hambúrguer Clássico",
                    new BigDecimal("29.90"), "ATIVO");
            InsumoJpa insumo = new InsumoJpa(insumoId, UUID.fromString(UNIDADE), "Pão de ficha", "un",
                    new BigDecimal("20.000"), 2, "ATIVO");
            FichaTecnicaJpa ficha = new FichaTecnicaJpa(fichaId, UUID.fromString(UNIDADE), produto);
            ficha.adicionarItem(new ItemFichaTecnicaJpa(UUID.randomUUID(), ficha, insumo,
                    new BigDecimal("1.000")));
            sessao.persist(produto);
            sessao.persist(insumo);
            sessao.persist(ficha);
            sessao.getTransaction().commit();
        }

        try (Session sessao = fabrica.openSession()) {
            FichaTecnicaJpa carregada = sessao.find(FichaTecnicaJpa.class, fichaId);
            assertNotNull(carregada);
            assertEquals(1, carregada.itens().size());
            assertEquals("Pão de ficha", carregada.itens().get(0).insumo().nome());
            assertEquals("Hambúrguer Clássico", carregada.produto().nome());
        }
    }

    @Test
    void recusaItemDeFichaComQuantidadeNaoPositiva() throws SQLException {
        UUID produtoId = UUID.randomUUID();
        UUID fichaId = UUID.randomUUID();
        UUID insumoId = UUID.randomUUID();
        try (Connection conexao = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
                POSTGRES.getPassword())) {
            try (var comando = conexao.prepareStatement("insert into produto (id, unidade_id, nome, preco, "
                    + "estado) values (?, ?, 'Batata Rústica', 18.90, 'ATIVO')")) {
                comando.setObject(1, produtoId);
                comando.setObject(2, UUID.fromString(UNIDADE));
                comando.executeUpdate();
            }
            try (var comando = conexao.prepareStatement("insert into ficha_tecnica (id, unidade_id, produto_id) "
                    + "values (?, ?, ?)")) {
                comando.setObject(1, fichaId);
                comando.setObject(2, UUID.fromString(UNIDADE));
                comando.setObject(3, produtoId);
                comando.executeUpdate();
            }
            try (var comando = conexao.prepareStatement("insert into insumo (id, unidade_id, nome, "
                    + "unidade_de_medida, estoque_minimo, prazo_de_entrega_em_dias, estado) values (?, ?, "
                    + "'Batata', 'kg', 5, 3, 'ATIVO')")) {
                comando.setObject(1, insumoId);
                comando.setObject(2, UUID.fromString(UNIDADE));
                comando.executeUpdate();
            }
            String sql = "insert into item_ficha_tecnica (id, ficha_id, insumo_id, quantidade) values (?, ?, ?, ?)";
            try (var comando = conexao.prepareStatement(sql)) {
                comando.setObject(1, UUID.randomUUID());
                comando.setObject(2, fichaId);
                comando.setObject(3, insumoId);
                comando.setBigDecimal(4, BigDecimal.ZERO);
                assertThrows(SQLException.class, comando::executeUpdate);
            }
        }
    }

    @Test
    void persisteFornecedorComCatalogoELeDeVolta() {
        UUID insumoId = UUID.randomUUID();
        Fornecedor dominio = new Fornecedor(FornecedorId.novo(), "Mercado Central", "11222333000181",
                "8133330002", "");
        dominio.incluirItem(insumoId, true, new BigDecimal("0.45"), new BigDecimal("20"), 4);
        try (Session sessao = fabrica.openSession()) {
            sessao.beginTransaction();
            sessao.persist(new InsumoJpa(insumoId, UUID.fromString(UNIDADE), "Pão de catálogo", "un",
                    new BigDecimal("20.000"), 3, "ATIVO"));
            sessao.persist(FornecedorMapper.paraJpa(dominio));
            sessao.getTransaction().commit();
        }

        try (Session sessao = fabrica.openSession()) {
            FornecedorJpa carregado = sessao.find(FornecedorJpa.class, dominio.id().valor());
            assertNotNull(carregado);
            Fornecedor volta = FornecedorMapper.paraDominio(carregado);
            assertEquals(dominio.catalogo(), volta.catalogo());
        }
    }

    @Test
    void recusaSegundoItemDoMesmoInsumoNoCatalogo() throws SQLException {
        UUID fornecedorId = UUID.randomUUID();
        UUID insumoId = UUID.randomUUID();
        try (Connection conexao = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
                POSTGRES.getPassword())) {
            try (var comando = conexao.prepareStatement("insert into insumo (id, unidade_id, nome, "
                    + "unidade_de_medida, estoque_minimo, prazo_de_entrega_em_dias, estado) values (?, ?, "
                    + "'Cebola', 'kg', 5, 3, 'ATIVO')")) {
                comando.setObject(1, insumoId);
                comando.setObject(2, UUID.fromString(UNIDADE));
                comando.executeUpdate();
            }
            try (var comando = conexao.prepareStatement("insert into fornecedor (id, nome, cnpj, telefone, estado) "
                    + "values (?, 'Atacado Norte', '56789012000100', '8133330005', 'ATIVO')")) {
                comando.setObject(1, fornecedorId);
                comando.executeUpdate();
            }
            String sql = "insert into item_catalogo_fornecedor (id, fornecedor_id, insumo_id, preco_unitario, "
                    + "quantidade_minima, prazo_de_entrega_em_dias) values (?, ?, ?, 20.00, 10, 7)";
            try (var comando = conexao.prepareStatement(sql)) {
                comando.setObject(1, UUID.randomUUID());
                comando.setObject(2, fornecedorId);
                comando.setObject(3, insumoId);
                comando.executeUpdate();
            }
            try (var comando = conexao.prepareStatement(sql)) {
                comando.setObject(1, UUID.randomUUID());
                comando.setObject(2, fornecedorId);
                comando.setObject(3, insumoId);
                assertThrows(SQLException.class, comando::executeUpdate);
            }
        }
    }

    @Test
    void mapeamentoNaoAceitaInsumoSemNome() {
        try (Session sessao = fabrica.openSession()) {
            sessao.beginTransaction();
            assertThrows(PersistenceException.class, () -> {
                InsumoJpa invalido = new InsumoJpa(UUID.randomUUID(), UUID.fromString(UNIDADE), null, "kg",
                        new BigDecimal("5.000"), 3, "ATIVO");
                sessao.persist(invalido);
                sessao.flush();
            });
        }
    }

    @Test
    void removerItemDaFichaPersisteSemViolarUnicidade() {
        UUID produtoId = UUID.randomUUID();
        UUID insumoPaoId = UUID.randomUUID();
        UUID insumoQueijoId = UUID.randomUUID();
        UUID fichaId = UUID.randomUUID();

        try (Session sessao = fabrica.openSession()) {
            sessao.beginTransaction();
            ProdutoJpa produto = new ProdutoJpa(produtoId, UUID.fromString(UNIDADE), "Produto remoção",
                    new BigDecimal("29.90"), "ATIVO");
            InsumoJpa pao = new InsumoJpa(insumoPaoId, UUID.fromString(UNIDADE), "Pão remoção", "un",
                    new BigDecimal("20.000"), 2, "ATIVO");
            InsumoJpa queijo = new InsumoJpa(insumoQueijoId, UUID.fromString(UNIDADE), "Queijo remoção", "kg",
                    new BigDecimal("10.000"), 2, "ATIVO");
            FichaTecnicaJpa ficha = new FichaTecnicaJpa(fichaId, UUID.fromString(UNIDADE), produto);
            ficha.adicionarItem(new ItemFichaTecnicaJpa(UUID.randomUUID(), ficha, pao, new BigDecimal("1.000")));
            ficha.adicionarItem(new ItemFichaTecnicaJpa(UUID.randomUUID(), ficha, queijo, new BigDecimal("0.100")));
            sessao.persist(produto);
            sessao.persist(pao);
            sessao.persist(queijo);
            sessao.persist(ficha);
            sessao.getTransaction().commit();
        }

        JpaMapeador mapper = new JpaMapeador();
        try (Session sessao = fabrica.openSession()) {
            sessao.beginTransaction();
            FichaTecnicaJpa ficha = sessao.find(FichaTecnicaJpa.class, fichaId);
            FichaTecnica dominio = mapper.paraDominio(ficha);
            Insumo removido = dominio.itens().stream()
                    .filter(item -> item.insumo().nome().equals("Queijo remoção"))
                    .findFirst()
                    .orElseThrow()
                    .insumo();
            dominio.removerItem(removido);
            mapper.atualizar(ficha, dominio, id -> sessao.find(InsumoJpa.class, id));
            sessao.getTransaction().commit();
        }

        try (Session sessao = fabrica.openSession()) {
            FichaTecnicaJpa ficha = sessao.find(FichaTecnicaJpa.class, fichaId);
            assertEquals(1, ficha.itens().size());
            assertEquals("Pão remoção", ficha.itens().get(0).insumo().nome());
        }
    }
}
