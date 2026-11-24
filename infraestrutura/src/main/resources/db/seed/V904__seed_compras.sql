-- Seed de desenvolvimento para F7 (Compras): os fornecedores dos protótipos e os catálogos sobre os
-- insumos reais do V900 (Pão, Carne e Tomate). Queijo fica sem catálogo de propósito, para a tela
-- mostrar "não há fornecedor para o insumo" quando ele entrar em reposição. Idempotente.
insert into fornecedor (id, nome, cnpj, telefone, email, estado)
values
  ('00000000-0000-0000-0000-000000000701', 'Alimentos Recife', '12345678000195', '8133330001', 'compras@alimentosrecife.com.br', 'ATIVO'),
  ('00000000-0000-0000-0000-000000000702', 'Mercado Central',  '23456789000195', '8133330002', '', 'ATIVO'),
  ('00000000-0000-0000-0000-000000000703', 'Entrega Rápida',   '34567890000130', '81999990003', '', 'ATIVO'),
  ('00000000-0000-0000-0000-000000000704', 'Casa do Insumo',   '45678901000175', '', 'pedidos@casadoinsumo.com.br', 'ATIVO'),
  ('00000000-0000-0000-0000-000000000705', 'Atacado Norte',    '56789012000100', '8133330005', '', 'ATIVO')
on conflict (cnpj) do nothing;

-- Pão (prazo do insumo: 3 dias): Entrega Rápida é a mais barata, mas excede o prazo.
-- Carne (prazo do insumo: 2 dias): Atacado Norte é mais barato, mas excede o prazo.
-- Tomate (prazo do insumo: 2 dias): Alimentos Recife vence pelo custo total depois do arredondamento.
insert into item_catalogo_fornecedor (id, fornecedor_id, insumo_id, preco_unitario, quantidade_minima, prazo_de_entrega_em_dias)
select v.id::uuid, v.fornecedor_id::uuid, i.id, v.preco, v.minimo, v.prazo
from (values
  ('00000000-0000-0000-0000-000000000711', '00000000-0000-0000-0000-000000000701', 'Pão',    0.50, 10, 3),
  ('00000000-0000-0000-0000-000000000712', '00000000-0000-0000-0000-000000000702', 'Pão',    0.45, 20, 3),
  ('00000000-0000-0000-0000-000000000713', '00000000-0000-0000-0000-000000000703', 'Pão',    0.40, 10, 6),
  ('00000000-0000-0000-0000-000000000714', '00000000-0000-0000-0000-000000000704', 'Carne',  22.00, 5, 2),
  ('00000000-0000-0000-0000-000000000715', '00000000-0000-0000-0000-000000000705', 'Carne',  20.00, 10, 7),
  ('00000000-0000-0000-0000-000000000716', '00000000-0000-0000-0000-000000000701', 'Tomate', 6.00, 10, 2),
  ('00000000-0000-0000-0000-000000000717', '00000000-0000-0000-0000-000000000702', 'Tomate', 6.50, 5, 1)
) as v (id, fornecedor_id, insumo, preco, minimo, prazo)
join insumo i on i.unidade_id = '00000000-0000-0000-0000-000000000001' and i.nome = v.insumo
join fornecedor f on f.id = v.fornecedor_id::uuid
on conflict do nothing;
