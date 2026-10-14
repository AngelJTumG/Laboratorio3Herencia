public final class Sobre extends Envio {
    public Sobre(String codigo, String remitente, String destinatario,
                 double pesoReal, TipoDestino destino) {
        super(codigo, remitente, destinatario, pesoReal, destino);
        if (pesoReal > 0.5) {
            throw new IllegalArgumentException("Un sobre no puede pesar mas de 0.5 kg.");
        }
    }

    @Override
    public double calcularCosto() {
        return getDestino() == TipoDestino.URBANO ? 20.0 : 35.0;
    }

    @Override
    public int calcularTiempoEntrega() {
        return getDestino() == TipoDestino.URBANO ? 1 : 2;
    }

    @Override
    public int obtenerMaxIntentos() { return 3; }

    @Override
    public String getCategoria() { return "Sobre"; }
}
