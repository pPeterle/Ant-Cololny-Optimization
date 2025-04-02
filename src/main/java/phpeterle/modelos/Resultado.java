package phpeterle.modelos;

public class Resultado {
    private final double custoTotal;
    private final long tempo;
    private final double a;
    private final double b;

    public Resultado(double custoTotal, long tempo, double a, double b) {
        this.custoTotal = custoTotal;
        this.tempo = tempo;
        this.a = a;
        this.b = b;
    }

    public double getCustoTotal() {
        return custoTotal;
    }

    public long getTempo() {
        return tempo;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }
}
