import java.util.Locale;

public final class PaqueteFragil extends Paquete {
    private final double valorDeclarado;

    public PaqueteFragil(String codigo, String remitente, String destinatario,
                         double pesoReal, TipoDestino destino, double largo,
                         double ancho, double alto, double valorDeclarado) {
        super(codigo, remitente, destinatario, pesoReal, destino, largo, ancho, alto);
        this.valorDeclarado = Validaciones.positivo(valorDeclarado, "El valor declarado");
    }

    @Override
    public double calcularCosto() {
        return super.calcularCosto() + 15.0 + 0.03 * valorDeclarado;
    }

    @Override
    public int calcularTiempoEntrega() {
        return super.calcularTiempoEntrega() + 1;
    }

    @Override
    public int obtenerMaxIntentos() { return 2; }

    @Override
    public String getCategoria() { return "Paquete fragil"; }

    @Override
    protected String descripcionEspecifica() {
        return super.descripcionEspecifica()
                + String.format(Locale.US, " | valor declarado Q%.2f", valorDeclarado);
    }
}
