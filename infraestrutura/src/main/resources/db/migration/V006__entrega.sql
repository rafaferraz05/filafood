-- F2: entregadores, zonas, entregas e transições. O pedido de referência da F1 ganha o valor e o
-- endereço de entrega que a taxa precisa (ADR 0011/0013).
alter table pedido
    add column valor numeric(12,2) not null default 0,
    add column endereco_latitude numeric(9,6),
    add column endereco_longitude numeric(9,6),
    add column endereco varchar(255);

create table entregador (
    id uuid primary key,
    unidade_id uuid not null,
    nome varchar(120) not null,
    carga_maxima integer not null,
    carga_em_andamento integer not null default 0,
    estado varchar(20) not null,
    ultima_entrega_em timestamptz,
    versao bigint not null default 0,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    constraint ck_entregador_carga_maxima check (carga_maxima > 0),
    constraint ck_entregador_carga check (carga_em_andamento >= 0),
    constraint ck_entregador_estado check (estado in ('ATIVO', 'INATIVO'))
);

create table entregador_zona (
    entregador_id uuid not null,
    zona varchar(120) not null,
    constraint pk_entregador_zona primary key (entregador_id, zona),
    constraint fk_entregador_zona foreign key (entregador_id) references entregador (id) on delete cascade
);

create table zona_entrega (
    id uuid primary key,
    unidade_id uuid not null,
    nome varchar(120) not null,
    centro_latitude numeric(9,6) not null,
    centro_longitude numeric(9,6) not null,
    raio_km numeric(8,3) not null,
    valor_base numeric(12,2) not null,
    raio_gratuito_km numeric(8,3) not null,
    valor_por_km_excedente numeric(12,2) not null,
    teto numeric(12,2) not null,
    valor_minimo_pedido numeric(12,2) not null,
    estado varchar(20) not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    constraint uk_zona_unidade_nome unique (unidade_id, nome),
    constraint ck_zona_raio check (raio_km > 0),
    constraint ck_zona_raio_gratis check (raio_gratuito_km >= 0),
    constraint ck_zona_base check (valor_base >= 0),
    constraint ck_zona_excedente check (valor_por_km_excedente >= 0),
    constraint ck_zona_teto check (teto >= 0),
    constraint ck_zona_minimo check (valor_minimo_pedido >= 0),
    constraint ck_zona_estado check (estado in ('ATIVA', 'INATIVA'))
);

create table entrega (
    id uuid primary key,
    unidade_id uuid not null,
    pedido_id uuid not null,
    zona_id uuid not null,
    entregador_id uuid,
    distancia_km numeric(8,3) not null,
    valor_pedido numeric(12,2) not null,
    tentativa integer not null,
    estado varchar(20) not null,
    taxa numeric(12,2),
    recebedor varchar(120),
    motivo_falha varchar(40),
    observacao_falha varchar(255),
    versao bigint not null default 0,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    constraint fk_entrega_pedido foreign key (pedido_id) references pedido (id),
    constraint fk_entrega_zona foreign key (zona_id) references zona_entrega (id),
    constraint fk_entrega_entregador foreign key (entregador_id) references entregador (id),
    constraint uk_entrega_pedido_tentativa unique (pedido_id, tentativa),
    constraint ck_entrega_tentativa check (tentativa between 1 and 2),
    constraint ck_entrega_estado check (estado in ('AGUARDANDO', 'DESPACHADA', 'EM_ROTA', 'ENTREGUE', 'FALHA')),
    constraint ck_entrega_taxa check (taxa is null or taxa >= 0),
    constraint ck_entrega_motivo check (motivo_falha is null or motivo_falha in
        ('AUSENCIA_DO_CLIENTE', 'ENDERECO_NAO_ENCONTRADO', 'RECUSA_DO_CLIENTE', 'PROBLEMA_NO_TRANSPORTE')),
    constraint ck_entrega_falha check (estado <> 'FALHA' or (motivo_falha is not null and observacao_falha is not null))
);

-- Uma entrega ativa por pedido: o despacho e a nova tentativa não podem duplicar.
create unique index uk_entrega_ativa_pedido on entrega (pedido_id)
    where estado in ('AGUARDANDO', 'DESPACHADA', 'EM_ROTA');
create index ix_entrega_unidade_estado on entrega (unidade_id, estado, criado_em);
create index ix_entrega_entregador on entrega (entregador_id, estado);

create table transicao_entrega (
    id uuid primary key,
    entrega_id uuid not null,
    ordem integer not null,
    estado varchar(20) not null,
    data_hora timestamptz not null,
    constraint fk_transicao_entrega foreign key (entrega_id) references entrega (id) on delete cascade,
    constraint uk_transicao_entrega_ordem unique (entrega_id, ordem),
    constraint ck_transicao_estado check (estado in ('AGUARDANDO', 'DESPACHADA', 'EM_ROTA', 'ENTREGUE', 'FALHA'))
);

create index ix_transicao_entrega on transicao_entrega (entrega_id, ordem);
