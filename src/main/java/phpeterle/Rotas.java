package phpeterle;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import phpeterle.modelos.*;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Hello world!
 */
public class Rotas {
    public static void main(String[] args) {
        try {

                int dia = 5;

                CSVReader localidadesFile = new CSVReaderBuilder(new FileReader("src/main/files/Demanda 2.csv")).build();
                List<String[]> localidadesString = localidadesFile.readAll();

                List<Localidade> localidadeArrayList = localidadesString.stream()
                        .skip(2)
                        .map(item -> new Localidade(item[0], Double.parseDouble(item[1]), Double.parseDouble(item[2]), Integer.parseInt(item[4 + dia]), 0,false))
                        .toList();

                CSVReader rotasFile = new CSVReaderBuilder(new FileReader("src/main/files/Rotas 3.csv")).build();
                List<String[]> rotasString = rotasFile.readAll();

                ArrayList<ArrayList<Localidade>> caminhoes = new ArrayList<>();

                rotasString.stream()
                        .skip(1)
                        .forEach(item -> {
                            System.out.println(item[0] + "  " + item[6 + dia]);
                            if(item[6 + dia].equals("Sim")) {
                                if(caminhoes.isEmpty()) {
                                    caminhoes.add(new ArrayList<>());
                                }
                                ArrayList<Localidade> localidadesVisitadas = caminhoes.get(caminhoes.size() - 1);
                                Localidade localidade = localidadeArrayList.stream()
                                        .filter(localidade1 -> localidade1.getNome().equals(item[0]))
                                        .toList()
                                        .get(0);

                                localidadesVisitadas.add(localidade);
                                if(localidade.getNome().equals("DEPÓSITO") && localidadesVisitadas.size() > 1) {
                                    caminhoes.add(new ArrayList<>());
                                }
                            }

                        });

                List<Localidade> demanda = localidadeArrayList.stream().filter(localidade ->
                     localidade.getQtdItensReceber() > 0
                ).toList();

                ArrayList<Localidade> localidadesVisitadas = new ArrayList<>();
                System.out.println(demanda.size());

                double distanciaTotal = 0d;
                for (ArrayList<Localidade> rotas: caminhoes) {
                    System.out.println("Caminhão " );
                    for (int j = 0; j < rotas.size() - 1; j++) {
                        Localidade localidade = rotas.get(j);
                        Localidade localidade2 = rotas.get(j + 1);
                        localidadesVisitadas.add(localidade2);
                        double distancia = localidade.calcularDistancia(localidade2);
                        System.out.println("Localidade inicial " + localidade.getNome() + " para localidade destino " + localidade2.getNome() + " (" + distancia + ")");
                        distanciaTotal += distancia;
                    }
                    System.out.println("\n\n" );
                }

                demanda.forEach(localidade -> {
                   Optional<Localidade> demandavisitada = localidadesVisitadas.stream().filter(localidade1 -> localidade.getNome().equals(localidade1.getNome())).findFirst();
                   if(!demandavisitada.isPresent()) {
                       System.out.println("Demanda nao visitada " + localidade.getNome());
                       throw new RuntimeException();
                   }
                });
            System.out.printf("Distância: %.2f ", distanciaTotal);
                Double total = (distanciaTotal * AntColonyOptimization.custoPoKm);
                System.out.printf("Resultado: %.3f", total);

                System.out.println(caminhoes.size());
                System.out.println(caminhoes.get(caminhoes.size() -1).size());




        } catch (Exception e) {
            System.out.println("Erro ao ler arquivo " + e.getMessage());
        }
        
        
        System.out.println("Finalizado ");
    }

}
