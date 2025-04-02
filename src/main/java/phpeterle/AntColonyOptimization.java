package phpeterle;

import me.tongfei.progressbar.ProgressBar;
import phpeterle.modelos.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
 * default
 * private double c = 1.0;             //number of trails
 * private double alpha = 1;           //pheromone importance
 * private double beta = 5;            //distance priority
 * private double evaporation = 0.5;
 * private double Q = 500;             //pheromone left on trail per ant
 * private double antFactor = 0.8;     //no of ants per node
 * private double randomFactor = 0.01; //introducing randomness
 * private int maxIterations = 1000;
 */

public class AntColonyOptimization {
    private final double alpha;
    private final double beta;
    private final double evaporacao;
    private final double Q;
    private final double fatorAleatoriedade;
    
    private final int interacoesMaximas;

    public static final int custoCaminhao = 3000;
    public static final double custoPoKm = 3.5;
    
    private final List<Localidade> localidades;
    private final List<Solucao> solucoes = new ArrayList<>();
    private final Random random = new Random();
    private final String label;
    
    private List<Formiga> melhorCaminho;
    private double custoMelhorCaminho;
    
    public AntColonyOptimization(double alpha, double beta, double evaporacao, double q, double fatorAleatoriedade, int interacoesMaximas, int qtdFormigas, List<Localidade> localidades, List<Localidade> hoteis, String label) {
        this.alpha = alpha;
        this.beta = beta;
        this.evaporacao = evaporacao;
        Q = q;
        this.fatorAleatoriedade = fatorAleatoriedade;
        this.interacoesMaximas = interacoesMaximas;
        this.label = label;
        
        this.localidades = localidades;
        
        for (int i = 0; i < qtdFormigas; i++)
            solucoes.add(new Solucao(localidades, hoteis));
    }

    public List<Formiga> comecarOtimizacao() {
        ProgressBar pb = new ProgressBar("Iterações", interacoesMaximas);
        pb.start();
        for (int i = 1; i <= interacoesMaximas; i++) {
            pb.step();
            otimizar();
        }
        pb.stop();
        
        exibirRelatorio();
        
        return melhorCaminho;
    }

    public double getCusto() {
        ProgressBar pb = new ProgressBar("Iterações", interacoesMaximas);
        pb.start();
        for (int i = 1; i <= interacoesMaximas; i++) {
            pb.step();
            otimizar();
        }
        pb.stop();

        return custoMelhorCaminho;
    }
    
    public void exibirRelatorio() {
        
        StringBuilder caminho = new StringBuilder();
        double distanciaTotal = 0;
        for (int i = 0; i < melhorCaminho.size(); i++) {
            Formiga formiga = melhorCaminho.get(i);
            distanciaTotal += formiga.distanciaPercorrida;
            caminho.append("\nCaminhão ").append(i + 1).append("\n");
            caminho.append(formiga.getHistorico());
        }

        caminho.append("\n").append("DISTÂNCIA PERCORRIDA: ").append(distanciaTotal);
        caminho.append("\n").append("CUSTO TOTAL: ").append(custoMelhorCaminho);

        System.out.println("\nMelhor caminho " + label + caminho);
    }
    
    public void otimizar() {
        resetarFormigas();
        encontrarSolucoes();
        autalizarMelhorSolucao();
        limparFeromonioRotas();
    }

    private void resetarFormigas() {
        for (Solucao solucao : solucoes) {
            solucao.limpar();
        }
    }
    
    private void encontrarSolucoes() {
        for (Solucao solucao : solucoes) {
            while (!solucao.finalizouPercurso()) {
                solucao.utilizarNovaFormiga();
                Viagem viagem = selecionarProximaViagem(solucao);
                solucao.visitarLocalidade(viagem);
            }
            atualizarFeromonioRotas();
        }
    }
    
    private Viagem selecionarProximaViagem(Solucao solucao) {
        if (random.nextDouble() < fatorAleatoriedade) {
            return solucao.escolherProxCidadeAleatoria();
        }
        
        List<Viagem> possiveisLocalidades = solucao.getPossiveisLocalidadesParaVisitar();
        
        calcularProbabilidadeCidades(solucao, possiveisLocalidades);
        double r = random.nextDouble();
        Double total = 0d;
        for (Viagem viagem : possiveisLocalidades) {
            total += viagem.getProbabilidade();
            if (total >= r) return viagem;
        }

        throw new RuntimeException("Não possui outras cidades " + possiveisLocalidades.size());
    }
    
    public void calcularProbabilidadeCidades(Solucao solucao, List<Viagem> possiveisViagens) {
        Localidade localidadeAtual = solucao.getUltimoCaminhao().getUltimaLocalidade();
        double totalFeromonio = 0.0;

        if(possiveisViagens.size() == 1) {
            Viagem viagem = possiveisViagens.get(0);
            viagem.setProbabilidade(1.0);
            return;
        }

        for (Viagem viagem : possiveisViagens) {
            Localidade localidade = viagem.localidade;
                Double feromonio = localidadeAtual.getFeromonio(localidade);
                if(feromonio.equals(0d)) feromonio = 1d;
                double distancia = localidadeAtual.calcularDistancia(localidade);
                totalFeromonio += Math.pow(feromonio, alpha) * Math.pow(1.0 / distancia, beta);
        }
        for (Viagem viagem : possiveisViagens) {
            Localidade localidade = viagem.localidade;
                Double feromonio = localidadeAtual.getFeromonio(localidade);
                if(feromonio.equals(0d)) feromonio = 1d;
                double distancia = localidadeAtual.calcularDistancia(localidade);
                double heuristica = Math.pow(1.0 /distancia , beta);
                double numerator = Math.pow(feromonio, alpha) * heuristica;
                double probabilidade = numerator / totalFeromonio;
                if(Double.isNaN(probabilidade)) {
                    viagem.setProbabilidade(0.0);
                } else {
                    viagem.setProbabilidade(probabilidade);
                }

        }
    }
    
    private void atualizarFeromonioRotas() {
        for (Localidade localidadeA : localidades) {
            for (Localidade localidadeB : localidades) {
                double feromonioAtualizado = localidadeA.getFeromonio(localidadeB) * (1 - evaporacao);
                localidadeA.setFeromonio(localidadeB, feromonioAtualizado);
            }
            
        }
        for (Solucao solucao : solucoes) {
            double contribuicaoPorKm = Q / solucao.distanciaPercorrida();
            for (Formiga formiga : solucao.formigas) {
                for (int i = 0; i < formiga.cidadesVisitadas.size() - 1; i++) {
                    Localidade localidadeA = formiga.cidadesVisitadas.get(i);
                    Localidade localidadeB = formiga.cidadesVisitadas.get(i + 1);
                    
                    if (localidadeB != null) {
                        double contribuicao = contribuicaoPorKm * localidadeA.calcularDistancia(localidadeB);
                        localidadeA.setFeromonio(localidadeB, localidadeA.getFeromonio(localidadeB) + contribuicao);
                    }

                }
            }
        }
    }
    
    private void autalizarMelhorSolucao() {
        if (melhorCaminho == null || melhorCaminho.isEmpty()) {
            melhorCaminho = solucoes.get(0).formigas;
            custoMelhorCaminho = calcularCusto(solucoes.get(0));
        }

        for (Solucao s : solucoes) {
            double custo = calcularCusto(s);
            if (custo < custoMelhorCaminho) {
                custoMelhorCaminho = custo;
                melhorCaminho = new ArrayList<>(s.formigas);
            }
        }

    }

    public static double calcularCusto(Solucao solucao) {
        return (solucao.distanciaPercorrida() * custoPoKm);
    }
    
    private void limparFeromonioRotas() {
        for (Localidade localidade : localidades) {
            localidade.limparFeromonios();
        }
    }
}