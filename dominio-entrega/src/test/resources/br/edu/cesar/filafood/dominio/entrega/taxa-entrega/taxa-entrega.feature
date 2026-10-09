# language: pt

@f2-rn3
Funcionalidade: Cálculo da taxa de entrega

  Contexto:
    Dado a zona "Centro" com centro em -8.0533, -34.8813, raio de 5 km, valor base 5,00, raio gratuito 2 km, valor por quilômetro excedente 1,50, teto 10,00 e valor mínimo de pedido 30,00
    E a zona "Grande Recife" com centro em -8.0533, -34.8813, raio de 20 km, valor base 8,00, raio gratuito 5 km, valor por quilômetro excedente 1,00, teto 25,00 e valor mínimo de pedido 100,00

  Cenário: Escolher a zona de menor raio que contém o endereço
    Dado um pedido de 25,00 com endereço a 2 km do centro das duas zonas
    Quando o sistema calcula a taxa de entrega do pedido
    Então a taxa de entrega é calculada pela zona "Centro"

  Cenário: Cobrar o valor base dentro do raio gratuito
    Dado um pedido de 25,00 com endereço a 1,5 km do centro da zona "Centro"
    Quando o sistema calcula a taxa de entrega do pedido
    Então a taxa de entrega é 5,00

  Cenário: Somar o valor por quilômetro que excede o raio gratuito
    Dado um pedido de 25,00 com endereço a 4 km do centro da zona "Centro"
    Quando o sistema calcula a taxa de entrega do pedido
    Então a taxa de entrega é 8,00

  Cenário: Limitar a taxa ao teto da zona
    Dado a zona "Orla" com centro em -8.1900, -34.8813, raio de 10 km, valor base 10,00, raio gratuito 2 km, valor por quilômetro excedente 2,00, teto 15,00 e valor mínimo de pedido 30,00
    E um pedido de 25,00 com endereço a 8 km do centro da zona "Orla"
    Quando o sistema calcula a taxa de entrega do pedido
    Então a taxa de entrega é 15,00

  Cenário: Zerar a taxa quando o valor do pedido passa do valor mínimo da zona
    Dado um pedido de 35,00 com endereço a 4 km do centro da zona "Centro"
    Quando o sistema calcula a taxa de entrega do pedido
    Então a taxa de entrega é 0,00

  Cenário: Não recalcular a taxa depois do despacho
    Dado um pedido despachado com taxa de entrega 8,00 pela zona "Centro"
    Quando o gestor da unidade altera o valor base da zona "Centro" para 6,00
    Então a taxa de entrega do pedido continua 8,00
