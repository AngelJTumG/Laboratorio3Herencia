import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Despacho {
    private final String id;
    private final Vehiculo vehiculo;
    private final ArrayList<Envio> envios;
    private EstadoDespacho estado;
    private int resultadosPendientes;

    public Despacho(String id, Vehiculo vehiculo) {
        this.id = Validaciones.texto(id, "El identificador").toUpperCase(Locale.ROOT);
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehiculo es obligatorio.");
        }
        if (!vehiculo.estaDisponible()) {
            throw new IllegalStateException("El vehiculo ya tiene un despacho sin cerrar.");
        }
        this.vehiculo = vehiculo;
        this.envios = new ArrayList<>();
        this.estado = EstadoDespacho.EN_PREPARACION;
        vehiculo.asignarDespacho(this);
    }

    public String getId() { return id; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public EstadoDespacho getEstado() { return estado; }
    public List<Envio> getEnvios() { return new ArrayList<Envio>(envios); }
    public int getResultadosPendientes() { return resultadosPendientes; }

    public double calcularPesoTotal() {
        double total = 0;
        for (Envio envio : envios) {
            total += envio.getPesoReal();
        }
        return total;
    }

    public double calcularCapacidadDisponible() {
        return Math.max(0.0, vehiculo.getCapacidadMaxima() - calcularPesoTotal());
    }

    public void agregarEnvio(Envio envio) {
        if (estado != EstadoDespacho.EN_PREPARACION) {
            throw new IllegalStateException("Solo se asignan envios a despachos en preparacion.");
        }
        if (envio == null || !envio.puedeDespacharse()) {
            throw new IllegalStateException("El envio no esta disponible en bodega.");
        }
        if (!envio.esCompatibleCon(vehiculo)) {
            throw new IllegalArgumentException("El envio es incompatible con el vehiculo.");
        }
        // Para cargar el vehiculo se usa el peso REAL, no el peso facturable.
        double nuevoPeso = calcularPesoTotal() + envio.getPesoReal();
        if (nuevoPeso > vehiculo.getCapacidadMaxima() + 0.000000001) {
            throw new IllegalArgumentException("Se supera la capacidad real del vehiculo.");
        }
        envio.asignarDespacho(this);
        envios.add(envio);
    }

    public void retirarEnvio(Envio envio) {
        if (estado != EstadoDespacho.EN_PREPARACION) {
            throw new IllegalStateException("Solo se retiran envios antes de la salida.");
        }
        if (envio == null || !envios.contains(envio) || envio.getDespachoAsignado() != this) {
            throw new IllegalArgumentException("El envio no esta asignado a este despacho.");
        }
        envio.retirarDespacho(this);
        envios.remove(envio);
    }

    public void salir() {
        if (estado != EstadoDespacho.EN_PREPARACION) {
            throw new IllegalStateException("El despacho ya salio o esta cerrado.");
        }
        if (envios.isEmpty()) {
            throw new IllegalStateException("Un despacho vacio no puede salir.");
        }
        // Se revisa toda la carga antes de modificar un solo envio.
        for (Envio envio : envios) {
            if (envio.getDespachoAsignado() != this || envio.getEstado() != EstadoEnvio.EN_BODEGA
                    || envio.getIntentos() >= envio.obtenerMaxIntentos()) {
                throw new IllegalStateException("Hay un envio no disponible para salir.");
            }
        }
        for (Envio envio : envios) {
            envio.iniciarRuta(this);
        }
        resultadosPendientes = envios.size();
        estado = EstadoDespacho.EN_RUTA;
    }

    public void registrarResultado(Envio envio, boolean entregado) {
        if (estado != EstadoDespacho.EN_RUTA) {
            throw new IllegalStateException("Solo se registran resultados de un despacho en ruta.");
        }
        if (envio == null || !envios.contains(envio) || envio.getDespachoAsignado() != this
                || envio.getEstado() != EstadoEnvio.EN_RUTA) {
            throw new IllegalArgumentException("El envio no tiene un resultado pendiente aqui.");
        }
        envio.registrarResultado(this, entregado);
        resultadosPendientes--;
        if (resultadosPendientes == 0) {
            cerrar();
        }
    }

    private void cerrar() {
        estado = EstadoDespacho.CERRADO;
        vehiculo.liberar(this);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "%s | vehiculo %s | %s | %d envio(s) | %.2f/%.2f kg | disponibles %.2f kg | pendientes %d",
                id, vehiculo.getPlaca(), estado, envios.size(), calcularPesoTotal(),
                vehiculo.getCapacidadMaxima(), calcularCapacidadDisponible(), resultadosPendientes);
    }
}
