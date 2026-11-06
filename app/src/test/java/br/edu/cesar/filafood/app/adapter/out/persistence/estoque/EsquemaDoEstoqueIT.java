package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

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
}
