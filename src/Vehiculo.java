import java.util.Locale;

public final class Vehiculo {
    private final String placa;
    private final double capacidadMaxima;
    private final boolean refrigerado;
    private Despacho despachoActivo;

    public Vehiculo(String placa, double capacidadMaxima, boolean refrigerado) {
        this.placa = Validaciones.texto(placa, "La placa").toUpperCase(Locale.ROOT);
        this.capacidadMaxima = Validaciones.positivo(capacidadMaxima, "La capacidad");
        this.refrigerado = refrigerado;
    }

    public String getPlaca() { return placa; }
    public double getCapacidadMaxima() { return capacidadMaxima; }
    public boolean tieneRefrigeracion() { return refrigerado; }
    public boolean estaDisponible() { return despachoActivo == null; }
    public Despacho getDespachoActivo() { return despachoActivo; }

    void asignarDespacho(Despacho despacho) {
        if (despacho == null || !estaDisponible()) {
            throw new IllegalStateException("El vehiculo ya tiene un despacho sin cerrar.");
        }
        despachoActivo = despacho;
    }

    void liberar(Despacho despacho) {
        if (despachoActivo != despacho) {
            throw new IllegalStateException("El vehiculo no pertenece a este despacho.");
        }
        despachoActivo = null;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s | capacidad %.2f kg | refrigeracion: %s | %s",
                placa, capacidadMaxima, refrigerado ? "si" : "no",
                estaDisponible() ? "disponible" : "despacho " + despachoActivo.getId());
    }
}
