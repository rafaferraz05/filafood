# language: pt

@f2-rn2
Funcionalidade: Cadastro de zona de entrega

  Cenário: Cadastrar zona com os dados obrigatórios
    Quando o gestor da unidade cadastra a zona "Centro" com centro em -8.0533, -34.8813, raio de 3 km, valor base 5,00, raio gratuito 2 km, valor por quilômetro excedente 1,50, teto 10,00 e valor mínimo de pedido 30,00
    Então a zona "Centro" fica ativa

  Cenário: Desativar zona e deixar de receber novas entregas
    Dado a zona "Centro" ativa e sem entregas em andamento
    Quando o gestor da unidade desativa a zona "Centro"
    Então a zona "Centro" fica inativa
    E nenhuma nova entrega é atribuída à zona "Centro"

  Cenário: Reativar zona desativada
    Dado a zona "Centro" inativa
    Quando o gestor da unidade reativa a zona "Centro"
    Então a zona "Centro" fica ativa
