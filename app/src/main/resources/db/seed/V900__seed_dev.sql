-- Seed de desenvolvimento (D-19). Só o perfil dev aponta o Flyway para classpath:db/seed, então
-- este arquivo nunca roda em produção. Idempotente: se o insumo já existe (mesmo nome na unidade),
-- não duplica; as movimentações só entram quando o insumo ainda não tem nenhuma.
insert into insumo (id, unidade_id, nome, unidade_de_medida, estoque_minimo, prazo_de_entrega_em_dias, estado)
values
  ('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000001', 'Pão', 'un', 20, 3, 'ATIVO'),
  ('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000001', 'Carne', 'kg', 10, 2, 'ATIVO'),
  ('00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000001', 'Queijo', 'kg', 5, 3, 'ATIVO'),
  ('00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000001', 'Tomate', 'kg', 8, 2, 'ATIVO')
on conflict (unidade_id, nome) do nothing;

insert into movimentacao_estoque (id, insumo_id, tipo, motivo, quantidade, custo_unitario, origem, data_hora)
select '00000000-0000-0000-0000-000000000201', i.id, 'ENTRADA', 'COMPRA', 60.000, 0.50, '',
       now() - interval '6 days'
from insumo i
where i.unidade_id = '00000000-0000-0000-0000-000000000001' and i.nome = 'Pão'
  and not exists (select 1 from movimentacao_estoque m where m.insumo_id = i.id);

insert into movimentacao_estoque (id, insumo_id, tipo, motivo, quantidade, custo_unitario, origem, data_hora)
select '00000000-0000-0000-0000-000000000202', i.id, 'SAIDA', 'CONSUMO', 18.000, 0.00,
       'pedido #1268', now() - interval '2 days'
from insumo i
where i.unidade_id = '00000000-0000-0000-0000-000000000001' and i.nome = 'Pão'
  and (select count(*) from movimentacao_estoque m where m.insumo_id = i.id) = 1;

insert into movimentacao_estoque (id, insumo_id, tipo, motivo, quantidade, custo_unitario, origem, data_hora)
select '00000000-0000-0000-0000-000000000203', i.id, 'ENTRADA', 'COMPRA', 30.000, 18.00, '',
       now() - interval '5 days'
from insumo i
where i.unidade_id = '00000000-0000-0000-0000-000000000001' and i.nome = 'Carne'
  and not exists (select 1 from movimentacao_estoque m where m.insumo_id = i.id);

insert into movimentacao_estoque (id, insumo_id, tipo, motivo, quantidade, custo_unitario, origem, data_hora)
select '00000000-0000-0000-0000-000000000204', i.id, 'SAIDA', 'CONSUMO', 22.000, 0.00,
       'pedido #1200', now() - interval '1 day'
from insumo i
where i.unidade_id = '00000000-0000-0000-0000-000000000001' and i.nome = 'Carne'
  and (select count(*) from movimentacao_estoque m where m.insumo_id = i.id) = 1;

insert into movimentacao_estoque (id, insumo_id, tipo, motivo, quantidade, custo_unitario, origem, data_hora)
select '00000000-0000-0000-0000-000000000205', i.id, 'ENTRADA', 'COMPRA', 20.000, 25.00, '',
       now() - interval '5 days'
from insumo i
where i.unidade_id = '00000000-0000-0000-0000-000000000001' and i.nome = 'Queijo'
  and not exists (select 1 from movimentacao_estoque m where m.insumo_id = i.id);

insert into movimentacao_estoque (id, insumo_id, tipo, motivo, quantidade, custo_unitario, origem, data_hora)
select '00000000-0000-0000-0000-000000000206', i.id, 'SAIDA', 'CONSUMO', 5.000, 0.00,
       'pedido #1176', now() - interval '1 day'
from insumo i
where i.unidade_id = '00000000-0000-0000-0000-000000000001' and i.nome = 'Queijo'
  and (select count(*) from movimentacao_estoque m where m.insumo_id = i.id) = 1;

insert into movimentacao_estoque (id, insumo_id, tipo, motivo, quantidade, custo_unitario, origem, data_hora)
select '00000000-0000-0000-0000-000000000207', i.id, 'ENTRADA', 'COMPRA', 20.000, 6.00, '',
       now() - interval '4 days'
from insumo i
where i.unidade_id = '00000000-0000-0000-0000-000000000001' and i.nome = 'Tomate'
  and not exists (select 1 from movimentacao_estoque m where m.insumo_id = i.id);

insert into movimentacao_estoque (id, insumo_id, tipo, motivo, quantidade, custo_unitario, origem, data_hora)
select '00000000-0000-0000-0000-000000000208', i.id, 'SAIDA', 'CONSUMO', 14.000, 0.00,
       'pedido #1148', now() - interval '1 day'
from insumo i
where i.unidade_id = '00000000-0000-0000-0000-000000000001' and i.nome = 'Tomate'
  and (select count(*) from movimentacao_estoque m where m.insumo_id = i.id) = 1;
