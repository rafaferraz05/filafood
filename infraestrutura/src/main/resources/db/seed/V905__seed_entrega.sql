-- Seed de dev da F2: zonas, entregadores, pedidos e entregas dos protótipos. Os pedidos usam ids
-- cujo sufixo forma o número mostrado na tela (#1341, #1343, #1338, #1339, #1344); o #1337 continua
-- sendo o pedido de preparo da F1.
insert into zona_entrega (id, unidade_id, nome, centro_latitude, centro_longitude, raio_km, valor_base,
    raio_gratuito_km, valor_por_km_excedente, teto, valor_minimo_pedido, estado)
values
    ('00000000-0000-0000-0000-000000000701', '00000000-0000-0000-0000-000000000001', 'Centro',
     -8.0533, -34.8813, 5, 5.00, 2, 1.50, 10.00, 30.00, 'ATIVA'),
    ('00000000-0000-0000-0000-000000000702', '00000000-0000-0000-0000-000000000001', 'Boa Viagem',
     -8.1200, -34.9000, 6, 6.00, 3, 1.20, 12.00, 40.00, 'ATIVA'),
    ('00000000-0000-0000-0000-000000000703', '00000000-0000-0000-0000-000000000001', 'Grande Recife',
     -8.0533, -34.8813, 20, 8.00, 5, 1.00, 25.00, 100.00, 'ATIVA'),
    ('00000000-0000-0000-0000-000000000704', '00000000-0000-0000-0000-000000000001', 'Orla',
     -8.1900, -34.8813, 10, 10.00, 2, 2.00, 15.00, 30.00, 'ATIVA')
on conflict do nothing;

insert into entregador (id, unidade_id, nome, carga_maxima, carga_em_andamento, estado, ultima_entrega_em)
values
    ('00000000-0000-0000-0000-000000000711', '00000000-0000-0000-0000-000000000001', 'Ana Paula Lima', 3, 1, 'ATIVO', now() - interval '2 hours'),
    ('00000000-0000-0000-0000-000000000712', '00000000-0000-0000-0000-000000000001', 'Bruno Souza', 3, 1, 'ATIVO', now() - interval '1 hour'),
    ('00000000-0000-0000-0000-000000000713', '00000000-0000-0000-0000-000000000001', 'Carla Dias', 2, 1, 'ATIVO', now() - interval '6 hours'),
    ('00000000-0000-0000-0000-000000000714', '00000000-0000-0000-0000-000000000001', 'Diego Melo', 4, 0, 'INATIVO', now() - interval '5 days')
on conflict do nothing;

insert into entregador_zona (entregador_id, zona)
values
    ('00000000-0000-0000-0000-000000000711', 'Centro'),
    ('00000000-0000-0000-0000-000000000711', 'Boa Viagem'),
    ('00000000-0000-0000-0000-000000000712', 'Centro'),
    ('00000000-0000-0000-0000-000000000712', 'Grande Recife'),
    ('00000000-0000-0000-0000-000000000713', 'Centro'),
    ('00000000-0000-0000-0000-000000000714', 'Grande Recife'),
    ('00000000-0000-0000-0000-000000000714', 'Orla')
on conflict do nothing;

insert into pedido (id, unidade_id, estado, valor, endereco_latitude, endereco_longitude, endereco)
values
    ('00000000-0000-0000-0000-000000001341', '00000000-0000-0000-0000-000000000001', 'CONCLUIDO', 25.00, -7.9500, -34.8813, 'Grande Recife, a 11,5 km'),
    ('00000000-0000-0000-0000-000000001343', '00000000-0000-0000-0000-000000000001', 'CONCLUIDO', 25.00, -7.9500, -34.8813, 'Grande Recife, a 11,5 km'),
    ('00000000-0000-0000-0000-000000001338', '00000000-0000-0000-0000-000000000001', 'CONCLUIDO', 25.00, -8.0175, -34.8813, 'Centro, a 4,0 km'),
    ('00000000-0000-0000-0000-000000001339', '00000000-0000-0000-0000-000000000001', 'CONCLUIDO', 25.00, -8.0398, -34.8813, 'Centro, a 1,5 km'),
    ('00000000-0000-0000-0000-000000001344', '00000000-0000-0000-0000-000000000001', 'CONCLUIDO', 25.00, -8.0398, -34.8813, 'Centro, a 1,5 km')
on conflict do nothing;

insert into entrega (id, unidade_id, pedido_id, zona_id, entregador_id, distancia_km, valor_pedido,
    tentativa, estado, taxa, criado_em)
values
    ('00000000-0000-0000-0000-000000000801', '00000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000001341', '00000000-0000-0000-0000-000000000703', null, 11.5, 25.00,
     1, 'AGUARDANDO', null, now() - interval '6 minutes'),
    ('00000000-0000-0000-0000-000000000802', '00000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000001343', '00000000-0000-0000-0000-000000000703', null, 11.5, 25.00,
     1, 'AGUARDANDO', null, now() - interval '2 minutes'),
    ('00000000-0000-0000-0000-000000000803', '00000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000001338', '00000000-0000-0000-0000-000000000701',
     '00000000-0000-0000-0000-000000000712', 4.0, 25.00, 1, 'EM_ROTA', 8.00, now() - interval '40 minutes'),
    ('00000000-0000-0000-0000-000000000804', '00000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000001339', '00000000-0000-0000-0000-000000000701',
     '00000000-0000-0000-0000-000000000713', 1.5, 25.00, 1, 'DESPACHADA', 5.00, now() - interval '30 minutes'),
    ('00000000-0000-0000-0000-000000000805', '00000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000001344', '00000000-0000-0000-0000-000000000701',
     '00000000-0000-0000-0000-000000000711', 1.5, 25.00, 1, 'DESPACHADA', 5.00, now() - interval '20 minutes')
on conflict do nothing;

insert into transicao_entrega (id, entrega_id, ordem, estado, data_hora)
values
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000801', 1, 'AGUARDANDO', now() - interval '6 minutes'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000802', 1, 'AGUARDANDO', now() - interval '2 minutes'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000803', 1, 'AGUARDANDO', now() - interval '40 minutes'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000803', 2, 'DESPACHADA', now() - interval '38 minutes'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000803', 3, 'EM_ROTA', now() - interval '30 minutes'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000804', 1, 'AGUARDANDO', now() - interval '30 minutes'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000804', 2, 'DESPACHADA', now() - interval '28 minutes'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000805', 1, 'AGUARDANDO', now() - interval '20 minutes'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000805', 2, 'DESPACHADA', now() - interval '18 minutes')
on conflict do nothing;

-- Mais duas entregas do Bruno para a carga dele ficar 3 de 3 e a fila de Grande Recife permanecer
-- visível com o motivo "todos na carga máxima" (o protótipo mostra essa situação).
insert into pedido (id, unidade_id, estado, valor, endereco_latitude, endereco_longitude, endereco)
values
    ('00000000-0000-0000-0000-000000001345', '00000000-0000-0000-0000-000000000001', 'CONCLUIDO', 25.00, -8.0175, -34.8813, 'Centro, a 4,0 km'),
    ('00000000-0000-0000-0000-000000001346', '00000000-0000-0000-0000-000000000001', 'CONCLUIDO', 25.00, -8.0175, -34.8813, 'Centro, a 4,0 km')
on conflict do nothing;

insert into entrega (id, unidade_id, pedido_id, zona_id, entregador_id, distancia_km, valor_pedido,
    tentativa, estado, taxa, criado_em)
values
    ('00000000-0000-0000-0000-000000000806', '00000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000001345', '00000000-0000-0000-0000-000000000701',
     '00000000-0000-0000-0000-000000000712', 4.0, 25.00, 1, 'EM_ROTA', 8.00, now() - interval '35 minutes'),
    ('00000000-0000-0000-0000-000000000807', '00000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000001346', '00000000-0000-0000-0000-000000000701',
     '00000000-0000-0000-0000-000000000712', 4.0, 25.00, 1, 'DESPACHADA', 8.00, now() - interval '25 minutes')
on conflict do nothing;

insert into transicao_entrega (id, entrega_id, ordem, estado, data_hora)
values
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000806', 1, 'AGUARDANDO', now() - interval '35 minutes'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000806', 2, 'DESPACHADA', now() - interval '33 minutes'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000806', 3, 'EM_ROTA', now() - interval '30 minutes'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000807', 1, 'AGUARDANDO', now() - interval '25 minutes'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000807', 2, 'DESPACHADA', now() - interval '23 minutes')
on conflict do nothing;
