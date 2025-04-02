package phpeterle.modelos;

public class Viagem {

    public final Localidade localidade;
    public final TipoViagem tipoViagem;
    private Double probabilidade;

    public Viagem(Localidade localidade, TipoViagem tipoViagem) {
        this.localidade = localidade;
        this.tipoViagem = tipoViagem;
        probabilidade = 0d;
    }

    public Double getProbabilidade() {
        return probabilidade;
    }

    public void setProbabilidade(Double probabilidade) {
        this.probabilidade = probabilidade;
    }
}
