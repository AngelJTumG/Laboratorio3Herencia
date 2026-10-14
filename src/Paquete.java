import java.util.Locale;

public class Paquete extends Envio {
    private final double largo;
    private final double ancho;
    private final double alto;

    public Paquete(String codigo, String remitente, String destinatario, double pesoReal,
                   TipoDestino destino, double largo, double ancho, double alto) {
        super(codigo, remitente, destinatario, pesoReal, destino);
        this.largo = Validaciones.positivo(largo, "El largo");
        this.ancho = Validaciones.positivo(ancho, "El ancho");
        this.alto = Validaciones.positivo(alto, "El alto");
        if (!Double.isFinite(calcularPesoVolumetrico())) {
            throw new IllegalArgumentException("Las dimensiones generan un peso fuera de rango.");
        }
    }

    public final double calcularPesoVolumetrico() {
        return largo * ancho * alto / 5000.0;
    }

    public final int calcularPesoFacturable() {
        double mayor = Math.max(getPesoReal(), calcularPesoVolumetrico());
        if (mayor > Integer.MAX_VALUE - 1) {
            throw new IllegalArgumentException("El peso facturable excede el rango permitido.");
        }
        return (int) Math.ceil(mayor);
    }

    @Override
    public double calcularCosto() {
        double base = getDestino() == TipoDestino.URBANO ? 25.0 : 40.0;
        return base + 8.0 * calcularPesoFacturable();
    }

    @Override
    public int calcularTiempoEntrega() {
        return getDestino() == TipoDestino.URBANO ? 2 : 4;
    }

    @Override
    public int obtenerMaxIntentos() { return 3; }

    @Override
    public String getCategoria() { return "Paquete"; }

    @Override
    protected String descripcionEspecifica() {
        return String.format(Locale.US, " | %.2f x %.2f x %.2f cm | facturable %d kg",
                largo, ancho, alto, calcularPesoFacturable());
    }
}
