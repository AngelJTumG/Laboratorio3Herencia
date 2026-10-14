import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SistemaEnvios {
    private final ArrayList<Envio> envios = new ArrayList<>();
    private final ArrayList<Vehiculo> vehiculos = new ArrayList<>();
    private final ArrayList<Despacho> despachos = new ArrayList<>();
    private final Map<String, Double> ingresosPorCategoria = new LinkedHashMap<>();
    private double ingresosRegistro;
    private double ingresosReintentos;

    public SistemaEnvios() {
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        // Datos preinstalados, sin ventas ni cargos. P001 y F001 facturan peso volumetrico.
        envios.add(new Sobre("S001", "Ana", "Luis", 0.2, TipoDestino.URBANO));
        envios.add(new Sobre("S002", "Marta", "Jose", 0.4, TipoDestino.DEPARTAMENTAL));
        envios.add(new Paquete("P001", "Iris", "Pedro", 3.2, TipoDestino.URBANO,
                40, 30, 20));
        envios.add(new Paquete("P002", "Mario", "Luz", 1.2, TipoDestino.DEPARTAMENTAL,
                10, 10, 10));
        envios.add(new PaqueteFragil("F001", "Sara", "Omar", 3.2, TipoDestino.URBANO,
                40, 30, 20, 1200));
        envios.add(new PaqueteFragil("F002", "Eva", "Juan", 1.5,
                TipoDestino.DEPARTAMENTAL, 10, 20, 20, 500));
        envios.add(new CargaRefrigerada("R001", "Clinica", "Hospital", 2,
                TipoDestino.URBANO, 4));
        envios.add(new CargaRefrigerada("R002", "Granja", "Mercado", 4.5,
                TipoDestino.URBANO, -25));
        vehiculos.add(new Vehiculo("V-101", 20, false));
        vehiculos.add(new Vehiculo("V-202", 15, true));
        vehiculos.add(new Vehiculo("V-303", 30, false));
    }

    public String cotizarEnvio(Envio envio) {
        if (envio == null) {
            throw new IllegalArgumentException("Debe indicar el envio a cotizar.");
        }
        return String.format(Locale.US, "%s | costo Q%.2f | plazo %d dia(s) habil(es)",
                envio.getCategoria(), Validaciones.redondearDinero(envio.calcularCosto()),
                envio.calcularTiempoEntrega());
    }

    public void registrarEnvio(Envio envio) {
        if (envio == null) {
            throw new IllegalArgumentException("Debe indicar el envio a registrar.");
        }
        if (buscarEnvio(envio.getCodigo()) != null) {
            throw new IllegalArgumentException("Ya existe un envio con ese codigo.");
        }
        if (envio.getEstado() != EstadoEnvio.EN_BODEGA || envio.getIntentos() != 0
                || envio.getDespachoAsignado() != null) {
            throw new IllegalArgumentException("Solo se registra un envio nuevo.");
        }
        double cobro = Validaciones.redondearDinero(envio.calcularCosto());
        double siguienteRegistro = Validaciones.redondearDinero(ingresosRegistro + cobro);
        String categoria = envio.getCategoria();
        double siguienteCategoria = Validaciones.redondearDinero(
                ingresosPorCategoria.getOrDefault(categoria, 0.0) + cobro);
        // Se agregan el envio y su cobro solo despues de completar todas las validaciones.
        envios.add(envio);
        ingresosRegistro = siguienteRegistro;
        ingresosPorCategoria.put(categoria, siguienteCategoria);
    }

    public void registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new IllegalArgumentException("Debe indicar el vehiculo.");
        }
        if (buscarVehiculo(vehiculo.getPlaca()) != null) {
            throw new IllegalArgumentException("Ya existe un vehiculo con esa placa.");
        }
        vehiculos.add(vehiculo);
    }

    public Despacho crearDespacho(String id, String placa) {
        String codigo = Validaciones.texto(id, "El identificador");
        if (buscarDespacho(codigo) != null) {
            throw new IllegalArgumentException("Ya existe un despacho con ese identificador.");
        }
        Vehiculo vehiculo = exigirVehiculo(placa);
        Despacho despacho = new Despacho(codigo, vehiculo);
        despachos.add(despacho);
        return despacho;
    }

    public void asignarEnvio(String idDespacho, String codigoEnvio) {
        exigirDespacho(idDespacho).agregarEnvio(exigirEnvio(codigoEnvio));
    }

    public void retirarEnvio(String idDespacho, String codigoEnvio) {
        exigirDespacho(idDespacho).retirarEnvio(exigirEnvio(codigoEnvio));
    }

    public void registrarSalida(String idDespacho) {
        Despacho despacho = exigirDespacho(idDespacho);
        double nuevosReintentos = ingresosReintentos;
        Map<String, Double> nuevosIngresos = new LinkedHashMap<>(ingresosPorCategoria);

        // Calculamos los cobros antes de cambiar el despacho. Si algo falla, no cambia nada.
        for (Envio envio : despacho.getEnvios()) {
            if (envio.getIntentos() > 0) {
                nuevosReintentos = Validaciones.redondearDinero(nuevosReintentos + 15);
                String categoria = envio.getCategoria();
                double anterior = nuevosIngresos.getOrDefault(categoria, 0.0);
                nuevosIngresos.put(categoria, Validaciones.redondearDinero(anterior + 15));
            }
        }

        despacho.salir();
        // Los Q15 se registran SOLO cuando el despacho pudo salir.
        ingresosReintentos = nuevosReintentos;
        ingresosPorCategoria.clear();
        ingresosPorCategoria.putAll(nuevosIngresos);
    }

    public void registrarResultado(String idDespacho, String codigoEnvio, boolean entregado) {
        exigirDespacho(idDespacho).registrarResultado(exigirEnvio(codigoEnvio), entregado);
    }

    public Envio buscarEnvio(String codigo) {
        if (codigo == null) return null;
        for (Envio envio : envios) {
            if (envio.getCodigo().equalsIgnoreCase(codigo.trim())) return envio;
        }
        return null;
    }

    public Vehiculo buscarVehiculo(String placa) {
        if (placa == null) return null;
        for (Vehiculo vehiculo : vehiculos) {
            if (vehiculo.getPlaca().equalsIgnoreCase(placa.trim())) return vehiculo;
        }
        return null;
    }

    public Despacho buscarDespacho(String id) {
        if (id == null) return null;
        for (Despacho despacho : despachos) {
            if (despacho.getId().equalsIgnoreCase(id.trim())) return despacho;
        }
        return null;
    }

    private Envio exigirEnvio(String codigo) {
        Envio envio = buscarEnvio(codigo);
        if (envio == null) throw new IllegalArgumentException("No existe el envio indicado.");
        return envio;
    }

    private Vehiculo exigirVehiculo(String placa) {
        Vehiculo vehiculo = buscarVehiculo(placa);
        if (vehiculo == null) throw new IllegalArgumentException("No existe el vehiculo indicado.");
        return vehiculo;
    }

    private Despacho exigirDespacho(String id) {
        Despacho despacho = buscarDespacho(id);
        if (despacho == null) throw new IllegalArgumentException("No existe el despacho indicado.");
        return despacho;
    }

    public List<Envio> getEnvios() { return new ArrayList<Envio>(envios); }
    public List<Vehiculo> getVehiculos() { return new ArrayList<Vehiculo>(vehiculos); }
    public List<Despacho> getDespachos() { return new ArrayList<Despacho>(despachos); }
    public double getIngresosRegistro() { return ingresosRegistro; }
    public double getIngresosReintentos() { return ingresosReintentos; }
    public Map<String, Double> getIngresosPorCategoria() {
        return new LinkedHashMap<String, Double>(ingresosPorCategoria);
    }

    private ArrayList<String> obtenerCategorias() {
        ArrayList<String> categorias = new ArrayList<>();
        for (Envio envio : envios) {
            if (!categorias.contains(envio.getCategoria())) {
                categorias.add(envio.getCategoria());
            }
        }
        return categorias;
    }

    public String reporteEnvios() {
        StringBuilder resultado = new StringBuilder("ENVIOS POR CATEGORIA Y ESTADO\n");
        for (String categoria : obtenerCategorias()) {
            resultado.append(categoria).append(": ");
            for (EstadoEnvio estado : EstadoEnvio.values()) {
                int cantidad = 0;
                for (Envio envio : envios) {
                    if (envio.getCategoria().equals(categoria) && envio.getEstado() == estado) {
                        cantidad++;
                    }
                }
                resultado.append(estado).append("=").append(cantidad).append("  ");
            }
            resultado.append('\n');
        }
        return resultado.toString();
    }

    public String reporteIngresos() {
        StringBuilder resultado = new StringBuilder(String.format(Locale.US,
                "INGRESOS\nPor registros: Q%.2f\nPor reintentos: Q%.2f\nTotal: Q%.2f\nPor categoria:\n",
                ingresosRegistro, ingresosReintentos, ingresosRegistro + ingresosReintentos));
        for (String categoria : obtenerCategorias()) {
            resultado.append(String.format(Locale.US, "  %s: Q%.2f%n",
                    categoria, ingresosPorCategoria.getOrDefault(categoria, 0.0)));
        }
        return resultado.toString();
    }

    public String reporteDespachos() {
        StringBuilder resultado = new StringBuilder("DESPACHOS SIN CERRAR\n");
        int activos = 0;
        for (Despacho despacho : despachos) {
            if (despacho.getEstado() != EstadoDespacho.CERRADO) {
                resultado.append(despacho).append('\n');
                activos++;
            }
        }
        if (activos == 0) resultado.append("No hay despachos sin cerrar.\n");
        return resultado.toString();
    }
}
