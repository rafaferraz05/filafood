-- F7: fornecedor (RN 1) e catálogo do fornecedor (RN 2). O item do catálogo referencia o insumo do
-- estoque por id; a regra de "insumo ativo" é conferida pelo caso de uso, a de "um item por insumo"
-- também fica garantida aqui pela chave única.
create table fornecedor (
    id uuid primary key,
    nome varchar(120) not null,
    cnpj varchar(14) not null,
    telefone varchar(11) not null default '',
    email varchar(120) not null default '',
    estado varchar(20) not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    constraint uk_fornecedor_cnpj unique (cnpj),
    constraint ck_fornecedor_cnpj check (cnpj ~ '^[0-9]{14}$'),
    constraint ck_fornecedor_contato check (telefone <> '' or email <> ''),
    constraint ck_fornecedor_estado check (estado in ('ATIVO', 'INATIVO'))
);

create table item_catalogo_fornecedor (
    id uuid primary key,
    fornecedor_id uuid not null,
    insumo_id uuid not null,
    preco_unitario numeric(12,2) not null,
    quantidade_minima numeric(12,3) not null,
    prazo_de_entrega_em_dias integer not null,
    constraint fk_item_catalogo_fornecedor foreign key (fornecedor_id) references fornecedor (id) on delete cascade,
    constraint fk_item_catalogo_insumo foreign key (insumo_id) references insumo (id),
    constraint uk_item_catalogo_fornecedor_insumo unique (fornecedor_id, insumo_id),
    constraint ck_item_catalogo_preco check (preco_unitario > 0),
    constraint ck_item_catalogo_minimo check (quantidade_minima > 0),
    constraint ck_item_catalogo_prazo check (prazo_de_entrega_em_dias >= 1)
);

create index ix_item_catalogo_insumo on item_catalogo_fornecedor (insumo_id);
