package phpeterle.modelos;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Localidade {
    final private double x;
    final private double y;
    final private String nome;
    final public Boolean hotel;
    final private int qtdItensReceber;
    final private int tempoDescarga;
    final private Map<String, Double> distancias;
    private Map<String, Double> feromonios;


    private boolean recebeuEntrega = false;
    
    public Localidade(String cityName, double x, double y, int qtdItensReceber, int tempoDescarga, boolean hotel) {
        this.nome = cityName;
        this.x = x;
        this.y = y;
        this.tempoDescarga = tempoDescarga;
        feromonios = new HashMap<>();
        distancias = new HashMap<>();
        this.qtdItensReceber = qtdItensReceber;
        this.hotel = hotel;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public String getNome() {
        return nome;
    }
    
    public int getQtdItensReceber() {
        return qtdItensReceber;
    }

    public int getTempoDescarga() {
        return tempoDescarga;
    }
    
    public Double getFeromonio(Localidade localidade) {
        Double feromonio = this.feromonios.get(localidade.getNome());

        if(feromonio != null) return  feromonio;

        return  0d;
    }

    public void setFeromonio(Localidade localidade, double feromonio) {
        Double feromonioAtual = this.feromonios.get(localidade.getNome());
        if(feromonioAtual != null && feromonioAtual.equals(feromonio)) return;
        this.feromonios.put(localidade.getNome(), feromonio);
        localidade.setFeromonio(this, feromonio);
    }

    public void setDistancia(Localidade localidade, double distancia) {
        if(this.distancias.get(localidade.getNome()) != null) return;
        this.distancias.put(localidade.getNome(), distancia);
        localidade.setDistancia(this, distancia);
    }


    public double calcularDistancia(Localidade proxLocalidade) {
        if (proxLocalidade == null) return 0;

        Double distanciaCache = this.distancias.get(proxLocalidade.getNome());
        if(distanciaCache != null) return distanciaCache;

        
        double distancia = distance(getX(), proxLocalidade.getX(), getY(), proxLocalidade.getY());
        setDistancia(proxLocalidade, distancia);
        return distancia;
    }
    
    private double distance(double lat1, double lat2, double lon1,
                                  double lon2) {
        
        final int R = 6371; // Radius of the earth
        final double weight = 1.23;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (R * c * weight); // convert to meters
    }
    
    public boolean recebeuEntrega() {
        if(qtdItensReceber == 0) return true;
        
        return recebeuEntrega;
    }
    
    public void setRecebeuEntrega(boolean recebeuEntrega) {
        this.recebeuEntrega = recebeuEntrega;
    }
    
    public void limparFeromonios() {
        feromonios = new HashMap<>();
    }
    
    public Localidade copiar() {
        return new Localidade(nome, x, y, qtdItensReceber, tempoDescarga, hotel);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Localidade that = (Localidade) o;
        return Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0 && qtdItensReceber == that.qtdItensReceber && recebeuEntrega == that.recebeuEntrega && Objects.equals(nome, that.nome) && Objects.equals(hotel, that.hotel) && Objects.equals(feromonios, that.feromonios);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, nome, hotel, qtdItensReceber, feromonios, recebeuEntrega);
    }
}