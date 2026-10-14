import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class Main {
    private final Scanner entrada;
    private final SistemaEnvios sistema;

    public Main() {
        entrada = new Scanner(System.in);
        sistema = new SistemaEnvios();
    }

    public static void main(String[] args) {
        new Main().ejecutar();
    }

    public void ejecutar() {
        System.out.println("EnviaGT | Sistema de envios");
        System.out.println("Se cargaron 8 envios y 3 vehiculos de ejemplo, sin ingresos.");
        try {
            while (true) {
                mostrarMenu();
                String opcion = entrada.nextLine().trim();
                try {
                    switch (opcion) {
                        case "1": cotizarORegistrar(); break;
                        case "2": registrarVehiculo(); break;
                        case "3": crearDespacho(); break;
                        case "4": asignarEnvio(); break;
                        case "5": retirarEnvio(); break;
                        case "6": salirDespacho(); break;
                        case "7": registrarResultado(); break;
                        case "8": consultarEnvios(); break;
                        case "9": consultarVehiculos(); break;
                        case "10": System.out.print(sistema.reporteEnvios()); break;
                        case "11": System.out.print(sistema.reporteIngresos()); break;
                        case "12": System.out.print(sistema.reporteDespachos()); break;
                        case "0": System.out.println("Hasta luego."); return;
                        default: System.out.println("Opcion no reconocida. Intente de nuevo.");
                    }
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    System.out.println("Operacion rechazada: " + ex.getMessage());
                }
            }
        } catch (NoSuchElementException ex) {
            System.out.println("Entrada finalizada. Hasta luego.");
        }
    }

    private void mostrarMenu() {
        System.out.println("\n1. Cotizar o registrar envio");
        System.out.println("2. Registrar vehiculo");
        System.out.println("3. Crear despacho");
        System.out.println("4. Asignar envio a despacho");
        System.out.println("5. Retirar envio de despacho");
        System.out.println("6. Salir despacho");
        System.out.println("7. Registrar resultado de entrega");
        System.out.println("8. Consultar envios");
        System.out.println("9. Consultar vehiculos");
        System.out.println("10. Reporte de envios");
        System.out.println("11. Reporte de ingresos");
        System.out.println("12. Reporte de despachos abiertos");
        System.out.println("0. Salir");
        System.out.print("Opcion: ");
    }

    private String texto(String pregunta) {
        System.out.print(pregunta);
        return entrada.nextLine().trim();
    }

    private double numeroPositivo(String pregunta) {
        while (true) {
            String valor = texto(pregunta);
            try {
                return Validaciones.positivo(Double.parseDouble(valor.replace(',', '.')), pregunta);
            } catch (NumberFormatException ex) {
                System.out.println("Ingrese un numero valido, por ejemplo 3.2.");
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private double temperatura() {
        while (true) {
            String valor = texto("Temperatura requerida (-25 a 8 grados C): ");
            try {
                double t = Double.parseDouble(valor.replace(',', '.'));
                if (!Double.isFinite(t) || t < -25 || t > 8) {
                    System.out.println("La temperatura debe estar entre -25 y 8 grados C.");
                    continue;
                }
                return t;
            } catch (NumberFormatException ex) {
                System.out.println("Ingrese una temperatura numerica valida.");
            }
        }
    }

    private TipoDestino destino() {
        while (true) {
            String valor = texto("Destino (1=urbano, 2=departamental): ");
            if (valor.equals("1")) return TipoDestino.URBANO;
            if (valor.equals("2")) return TipoDestino.DEPARTAMENTAL;
            System.out.println("Elija 1 o 2.");
        }
    }

    private boolean respuestaSiNo(String pregunta) {
        while (true) {
            String valor = texto(pregunta).toLowerCase(Locale.ROOT);
            if (valor.equals("s") || valor.equals("si")) return true;
            if (valor.equals("n") || valor.equals("no")) return false;
            System.out.println("Escriba s o n.");
        }
    }

    private void cotizarORegistrar() {
        System.out.println("Categoria: 1=Sobre  2=Paquete  3=Paquete fragil  4=Carga refrigerada");
        String categoria = texto("Seleccione: ");
        if (!categoria.matches("[1-4]")) {
            throw new IllegalArgumentException("La categoria debe estar entre 1 y 4.");
        }
        String codigo = texto("Codigo: ");
        String remitente = texto("Remitente: ");
        String destinatario = texto("Destinatario: ");
        double peso = numeroPositivo("Peso real (kg): ");
        TipoDestino tipoDestino = destino();
        Envio envio;
        switch (categoria) {
            case "1":
                envio = new Sobre(codigo, remitente, destinatario, peso, tipoDestino);
                break;
            case "2":
                envio = new Paquete(codigo, remitente, destinatario, peso, tipoDestino,
                        numeroPositivo("Largo (cm): "), numeroPositivo("Ancho (cm): "),
                        numeroPositivo("Alto (cm): "));
                break;
            case "3":
                envio = new PaqueteFragil(codigo, remitente, destinatario, peso, tipoDestino,
                        numeroPositivo("Largo (cm): "), numeroPositivo("Ancho (cm): "),
                        numeroPositivo("Alto (cm): "), numeroPositivo("Valor declarado (Q): "));
                break;
            default:
                envio = new CargaRefrigerada(codigo, remitente, destinatario, peso,
                        tipoDestino, temperatura());
        }
        System.out.println("Cotizacion: " + sistema.cotizarEnvio(envio));
        if (respuestaSiNo("Registrar y cobrar ahora? (s/n): ")) {
            sistema.registrarEnvio(envio);
            System.out.println("Envio registrado y cobrado. Estado: EN_BODEGA.");
        } else {
            System.out.println("Cotizacion finalizada sin cambios ni cobros.");
        }
    }

    private void registrarVehiculo() {
        String placa = texto("Placa: ");
        double capacidad = numeroPositivo("Capacidad maxima (kg): ");
        boolean frio = respuestaSiNo("Tiene refrigeracion? (s/n): ");
        sistema.registrarVehiculo(new Vehiculo(placa, capacidad, frio));
        System.out.println("Vehiculo registrado.");
    }

    private void crearDespacho() {
        String id = texto("Identificador del despacho: ");
        String placa = texto("Placa del vehiculo: ");
        Despacho despacho = sistema.crearDespacho(id, placa);
        System.out.println("Despacho creado: " + despacho);
    }

    private void asignarEnvio() {
        String id = texto("Identificador del despacho: ");
        String codigo = texto("Codigo del envio: ");
        sistema.asignarEnvio(id, codigo);
        System.out.println("Envio asignado. No se genero ningun cobro.");
    }

    private void retirarEnvio() {
        String id = texto("Identificador del despacho: ");
        String codigo = texto("Codigo del envio: ");
        sistema.retirarEnvio(id, codigo);
        System.out.println("Envio retirado y disponible en bodega.");
    }

    private void salirDespacho() {
        String id = texto("Identificador del despacho: ");
        sistema.registrarSalida(id);
        System.out.println("Despacho en ruta; se cobraron los reintentos correspondientes.");
    }

    private void registrarResultado() {
        String id = texto("Identificador del despacho: ");
        String codigo = texto("Codigo del envio: ");
        boolean entregado = respuestaSiNo("Entregado? (s=entregado, n=fallido): ");
        sistema.registrarResultado(id, codigo, entregado);
        Envio envio = sistema.buscarEnvio(codigo);
        System.out.println("Estado del envio: " + envio.getEstado());
        System.out.println("Estado del despacho: " + sistema.buscarDespacho(id).getEstado());
    }

    private void consultarEnvios() {
        String codigo = texto("Codigo (Enter para todos): ");
        if (!codigo.isEmpty()) {
            Envio envio = sistema.buscarEnvio(codigo);
            if (envio == null) throw new IllegalArgumentException("No existe ese envio.");
            System.out.println(envio);
        } else {
            for (Envio envio : sistema.getEnvios()) System.out.println(envio);
        }
    }

    private void consultarVehiculos() {
        String placa = texto("Placa (Enter para todos): ");
        if (!placa.isEmpty()) {
            Vehiculo vehiculo = sistema.buscarVehiculo(placa);
            if (vehiculo == null) throw new IllegalArgumentException("No existe ese vehiculo.");
            System.out.println(vehiculo);
        } else {
            for (Vehiculo vehiculo : sistema.getVehiculos()) System.out.println(vehiculo);
        }
    }
}
