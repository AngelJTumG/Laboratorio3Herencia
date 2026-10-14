import java.util.Locale;

public abstract class Envio {
    private final String codigo;
    private final String remitente;
    private final String destinatario;
    private final double pesoReal;
    private final TipoDestino destino;
    private EstadoEnvio estado;
    private int intentos;
    private Despacho despachoAsignado;

    protected Envio(String codigo, String remitente, String destinatario,
                    double pesoReal, TipoDestino destino) {
        this.codigo = Validaciones.texto(codigo, "El codigo").toUpperCase(Locale.ROOT);
        this.remitente = Validaciones.texto(remitente, "El remitente");
        this.destinatario = Validaciones.texto(destinatario, "El destinatario");
        this.pesoReal = Validaciones.positivo(pesoReal, "El peso real");
        if (destino == null) {
            throw new IllegalArgumentException("El destino es obligatorio.");
        }
        this.destino = destino;
        this.estado = EstadoEnvio.EN_BODEGA;
        this.intentos = 0;
    }

    public abstract double calcularCosto();

    public abstract int calcularTiempoEntrega();

    public abstract int obtenerMaxIntentos();

    public abstract String getCategoria();

    public boolean esCompatibleCon(Vehiculo vehiculo) {
        return vehiculo != null;
    }

    public final boolean puedeDespacharse() {
        return estado == EstadoEnvio.EN_BODEGA && despachoAsignado == null
                && intentos < obtenerMaxIntentos();
    }

    // Los cambios de estado son coordinados unicamente por Despacho.
    final void asignarDespacho(Despacho despacho) {
        if (despacho == null || !puedeDespacharse()) {
            throw new IllegalStateException("El envio no esta disponible para asignarlo.");
        }
        despachoAsignado = despacho;
    }

    final void retirarDespacho(Despacho despacho) {
        if (despachoAsignado != despacho || estado != EstadoEnvio.EN_BODEGA) {
            throw new IllegalStateException("El envio no esta asignado a este despacho en preparacion.");
        }
        despachoAsignado = null;
    }

    final void iniciarRuta(Despacho despacho) {
        if (despachoAsignado != despacho || estado != EstadoEnvio.EN_BODEGA
                || intentos >= obtenerMaxIntentos()) {
            throw new IllegalStateException("El envio no puede salir a ruta.");
        }
        intentos++;
        estado = EstadoEnvio.EN_RUTA;
    }

    final void registrarResultado(Despacho despacho, boolean entregado) {
        if (despachoAsignado != despacho || estado != EstadoEnvio.EN_RUTA) {
            throw new IllegalStateException("Este envio no tiene un resultado pendiente en el despacho.");
        }
        if (entregado) {
            estado = EstadoEnvio.ENTREGADO;
        } else if (intentos >= obtenerMaxIntentos()) {
            estado = EstadoEnvio.DEVUELTO;
        } else {
            estado = EstadoEnvio.EN_BODEGA;
        }
        despachoAsignado = null;
    }

    public final String getCodigo() { return codigo; }
    public final String getRemitente() { return remitente; }
    public final String getDestinatario() { return destinatario; }
    public final double getPesoReal() { return pesoReal; }
    public final TipoDestino getDestino() { return destino; }
    public final EstadoEnvio getEstado() { return estado; }
    public final int getIntentos() { return intentos; }
    public final Despacho getDespachoAsignado() { return despachoAsignado; }

    protected String descripcionEspecifica() { return ""; }

    @Override
    public final String toString() {
        String reservado = despachoAsignado == null ? ""
                : " | despacho " + despachoAsignado.getId();
        return String.format(Locale.US,
                "%s | %s | %s -> %s | %.2f kg | %s | %s | intentos %d/%d%s%s",
                codigo, getCategoria(), remitente, destinatario, pesoReal, destino, estado,
                intentos, obtenerMaxIntentos(), reservado, descripcionEspecifica());
    }
}
