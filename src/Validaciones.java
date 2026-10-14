public final class Validaciones {
    private Validaciones() {
    }

    public static String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " no puede quedar en blanco.");
        }
        return valor.trim();
    }

    public static double positivo(double valor, String campo) {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(campo + " debe ser un numero positivo y finito.");
        }
        return valor;
    }

    public static double redondearDinero(double valor) {
        if (!Double.isFinite(valor) || valor < 0 || valor > 9e15) {
            throw new IllegalArgumentException("El monto esta fuera del rango permitido.");
        }
        return Math.round(valor * 100.0) / 100.0;
    }
}
