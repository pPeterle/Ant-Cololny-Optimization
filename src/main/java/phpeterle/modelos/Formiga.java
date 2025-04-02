package phpeterle.modelos;

import java.util.ArrayList;
import java.util.List;

public class Formiga {

    private final int velocidadeMediaKmPorMinuto = 1;

    private final int maxCarga = 120;

    final private StringBuilder historico;
    final private List<Localidade> localidades;
    final private List<Localidade> hoteis;
    private double jornadaDeTrabalhoDia1 = (8 * 60) + 30;
    
    public ArrayList<Localidade> cidadesVisitadas;
    private double jornadaDeTrabalhoDia2 = (8 * 60) + 30;
    private boolean primeiroDia = true;
    private int qtdCarga = 0;
    private Viagem ultimaViagem;

    public double distanciaPercorrida = 0d;

    Formiga(List<Localidade> localidades, List<Localidade> hoteis) {
        this.localidades = localidades;
        this.cidadesVisitadas = new ArrayList<>();
        
        // inicia o caminhão do depósito
        this.cidadesVisitadas.add(localidades.get(0));
        this.historico = new StringBuilder();
        this.historico.append(localidades.get(0).getNome());
        this.hoteis = hoteis;
    }

    public void visitarLocalidade(Viagem viagem) {
        if (!cidadesVisitadas.isEmpty()) {
            Localidade ultimaLocalidade = cidadesVisitadas.get(cidadesVisitadas.size() - 1);

            if (viagem.localidade.getNome().equals(ultimaLocalidade.getNome())) {
                throw new RuntimeException("Visitando a mesma cidade");
            }
        }

        int novaCarga = viagem.localidade.getQtdItensReceber();
        
        qtdCarga += novaCarga;
        
        if (qtdCarga > maxCarga) {
            throw new RuntimeException("Carga ultrapassou a quantidade máxima do caminhão");
        }

        Localidade proxLocalidade = viagem.localidade;
        Localidade localidadeAtual = this.cidadesVisitadas.get(this.cidadesVisitadas.size() - 1);

        ultimaViagem = viagem;

        switch (viagem.tipoViagem) {
            case VIAGEM_ENTRE_CIDADES -> {
                double distanciaProxCidade = localidadeAtual.calcularDistancia(proxLocalidade);

                historico.append(" -> ").append(proxLocalidade.getNome()).append(" (").append(distanciaProxCidade).append(") ");
                // minutos do descolamento

                reduzirJornadaTrabalho(distanciaProxCidade * velocidadeMediaKmPorMinuto);
                // minutos para descarga

                if(proxLocalidade == this.cidadesVisitadas.get(0))
                    reduzirJornadaTrabalho(proxLocalidade.getTempoDescarga());

                distanciaPercorrida += distanciaProxCidade;
                cidadesVisitadas.add(proxLocalidade);
            }
            case HOTEL -> {
                if(!proxLocalidade.hotel) throw new RuntimeException("Visitando localidade que não é hotel");

                double distanciaProxCidade = localidadeAtual.calcularDistancia(proxLocalidade);

                historico.append(" -> ").append(" Dormiu em ").append(proxLocalidade.getNome()).append(" (").append(distanciaProxCidade).append(") ");
                reduzirJornadaTrabalho(distanciaProxCidade * velocidadeMediaKmPorMinuto);
                reduzirJornadaTrabalho(proxLocalidade.getTempoDescarga());

                cidadesVisitadas.add(proxLocalidade);


                primeiroDia = false;
                distanciaPercorrida += distanciaProxCidade;
            }
            case IMPOSSIVEL -> throw new RuntimeException("Nenhuma categoria de entrega encontrada");

        }
        
    }
    
    public boolean podeVoltarDeposito() {
        if (qtdCarga == maxCarga) return true;

        boolean podeRealizarMaisAlgumaEntrega = false;
        for (Localidade localidade : localidades) {
            if (!localidade.hotel && !localidade.recebeuEntrega()) {
                podeRealizarMaisAlgumaEntrega = podeVisitarCidade(localidade).tipoViagem != TipoViagem.IMPOSSIVEL;
                if (podeRealizarMaisAlgumaEntrega) break;
            }

        }

        if (!podeRealizarMaisAlgumaEntrega && cidadesVisitadas.size() == 1) {
            throw new RuntimeException("Nenhuma rota satisfaz as condições");
        }

        return !podeRealizarMaisAlgumaEntrega ;
    }
    
    public Localidade getUltimaLocalidade() {
        return cidadesVisitadas.get(cidadesVisitadas.size() - 1);
    }

    public Viagem podeVisitarCidade(Localidade proxLocalidade) {

        Localidade deposito = this.cidadesVisitadas.get(0);
        Localidade localidadeAtual = this.cidadesVisitadas.get(this.cidadesVisitadas.size() - 1);


        double distanciaProxCidade = localidadeAtual.calcularDistancia(proxLocalidade);
        double distanciaVoltar = proxLocalidade.calcularDistancia(deposito);


        boolean podeVoltarPrimeiroDia = primeiroDia && jornadaDeTrabalhoDia1 > (distanciaProxCidade * velocidadeMediaKmPorMinuto);
        boolean podeVoltarSegundoDia = !primeiroDia && jornadaDeTrabalhoDia2 > (distanciaProxCidade * velocidadeMediaKmPorMinuto);

        if(proxLocalidade.getNome().equals("DEPÓSITO") && podeVoltarDeposito() && (podeVoltarSegundoDia || podeVoltarPrimeiroDia)) return new Viagem(proxLocalidade, TipoViagem.VIAGEM_ENTRE_CIDADES);
        if (proxLocalidade.recebeuEntrega() && !proxLocalidade.hotel) return new Viagem(proxLocalidade, TipoViagem.IMPOSSIVEL);

        Localidade hotel = this.buscarHotelMaisProximo(proxLocalidade);
        double distanciaIrHotel = proxLocalidade.calcularDistancia(hotel);

        boolean restricaoDeCarga = (qtdCarga + proxLocalidade.getQtdItensReceber()) <= maxCarga;


        double tempoDistanciaProxCidadeEHotelComTempoDescarga = (((distanciaProxCidade + distanciaIrHotel) * velocidadeMediaKmPorMinuto) + proxLocalidade.getTempoDescarga());
        double tempoDistanciaProxCidadeEVoltarComTempoDescarga = (((distanciaProxCidade + distanciaVoltar) * velocidadeMediaKmPorMinuto) + proxLocalidade.getTempoDescarga());

        boolean consegueRealizarEntregaNoMesmoDia = primeiroDia && !proxLocalidade.hotel && (jornadaDeTrabalhoDia1 > tempoDistanciaProxCidadeEHotelComTempoDescarga || jornadaDeTrabalhoDia1 > tempoDistanciaProxCidadeEVoltarComTempoDescarga) && restricaoDeCarga;

        boolean consegueRealizarEntregaNoProxDia = !primeiroDia && !proxLocalidade.hotel && jornadaDeTrabalhoDia2 > tempoDistanciaProxCidadeEVoltarComTempoDescarga && restricaoDeCarga;

        if (consegueRealizarEntregaNoMesmoDia || consegueRealizarEntregaNoProxDia)
            return new Viagem(proxLocalidade, TipoViagem.VIAGEM_ENTRE_CIDADES);

        boolean consegueVisitarHotel = primeiroDia && proxLocalidade.hotel && qtdCarga > 0 && jornadaDeTrabalhoDia1 > distanciaProxCidade * velocidadeMediaKmPorMinuto;

        if (consegueVisitarHotel)
            return new Viagem(proxLocalidade, TipoViagem.HOTEL);

        return new Viagem(proxLocalidade, TipoViagem.IMPOSSIVEL);
    }
    
    private void reduzirJornadaTrabalho(double minutosTrabalhados) {
        if (primeiroDia) {
            jornadaDeTrabalhoDia1 -= minutosTrabalhados;
            if(jornadaDeTrabalhoDia1 < 0) {
                throw new  RuntimeException("Jornada de trabalho 1 negativa");
            }
        } else {
            jornadaDeTrabalhoDia2 -= minutosTrabalhados;
            if(jornadaDeTrabalhoDia2 < 0) {
                throw new  RuntimeException("Jornada de trabalho 2 negativa");
            }
        }
    }

    private Localidade buscarHotelMaisProximo(Localidade localidade) {
        Double menorDistancia = this.hoteis.get(0).calcularDistancia(localidade);
        Localidade hotel = this.hoteis.get(0);

        for(Localidade h: this.hoteis) {
            Double distancia = h.calcularDistancia(localidade);
            if( distancia< menorDistancia) {
                menorDistancia = distancia;
                hotel = h;
            }
        }

        return  hotel;
    }

    public int getQtdCarga() {
        return qtdCarga;
    }

    public StringBuilder getHistorico() {
        return historico;
    }
}
