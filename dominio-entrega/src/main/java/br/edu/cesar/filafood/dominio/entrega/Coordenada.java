package br.edu.cesar.filafood.dominio.entrega;

/**
 * Ponto no mapa. A distância é calculada em linha reta, em quilômetros, conforme o escopo do
 * projeto: não há integração com serviço de mapas nem rota real.
 */
public record Coordenada(double latitude, double longitude) {

    /** Raio médio da Terra em quilômetros. */
    public static final double RAIO_DA_TERRA_KM = 6371.0;

    public double distanciaEmKm(Coordenada outra) {
        double diferencaLatitude = Math.toRadians(outra.latitude - latitude);
        double diferencaLongitude = Math.toRadians(outra.longitude - longitude)
                * Math.cos(Math.toRadians((latitude + outra.latitude) / 2));
        return Math.sqrt(diferencaLatitude * diferencaLatitude + diferencaLongitude * diferencaLongitude)
                * RAIO_DA_TERRA_KM;
    }
}
