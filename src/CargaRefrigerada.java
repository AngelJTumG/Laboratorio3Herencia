import java.util.Locale;

public final class CargaRefrigerada extends Envio {
    private final double temperatura;

    public CargaRefrigerada(String codigo, String remitente, String destinatario,
                            double pesoReal, TipoDestino destino, double temperatura) {
        super(codigo, remitente, destinatario, pesoReal, destino);
        if (destino != TipoDestino.URBANO) {
            throw new IllegalArgumentException("La carga refrigerada solo admite destino urbano.");
        }
        if (!Double.isFinite(temperatura) || temperatura < -25 || temperatura > 8) {
            throw new IllegalArgumentException("La temperatura debe estar entre -25 y 8 grados C.");
        }
        this.temperatura = temperatura;
    }

    @Override
    public double calcularCosto() { return 25.0 + 12.0 * getPesoReal() + 60.0; }

    @Override
    public int calcularTiempoEntrega() { return 1; }

    @Override
    public int obtenerMaxIntentos() { return 1; }

    @Override
    public String getCategoria() { return "Carga refrigerada"; }

    @Override
    public boolean esCompatibleCon(Vehiculo vehiculo) {
        return super.esCompatibleCon(vehiculo) && vehiculo.tieneRefrigeracion();
    }

    @Override
    protected String descripcionEspecifica() {
        return String.format(Locale.US, " | %.2f grados C", temperatura);
    }
}
