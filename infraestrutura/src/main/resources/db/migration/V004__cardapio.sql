-- F6: Evolui a tabela produto com categoria e descrição (RN 1) e cria os períodos de preço (RN 4).
alter table produto
    add column categoria varchar(80) not null default 'Lanches',
    add column descricao varchar(255) not null default '';

create table periodo_preco_produto (
    id uuid primary key,
    produto_id uuid not null,
    preco numeric(12,2) not null,
    data_inicio date not null,
    data_fim date,
    criado_em timestamptz not null default now(),
    constraint fk_periodo_preco_produto foreign key (produto_id) references produto (id) on delete cascade,
    constraint ck_periodo_preco_positivo check (preco > 0),
    constraint ck_periodo_datas check (data_fim is null or data_fim >= data_inicio)
);

create index ix_periodo_preco_produto on periodo_preco_produto (produto_id, data_inicio);