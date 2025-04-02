package phpeterle;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import phpeterle.modelos.*;
import phpeterle.view.KmlFile;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) {
        try {

            ThreadPoolExecutor executor =
                    (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

            List<Callable<Void>> tasks = new ArrayList<>();
            List<Execucao> execucoes = new ArrayList();
            execucoes.add(new Execucao(5, 15));
            execucoes.add(new Execucao(5, 15));
            execucoes.add(new Execucao(5, 15));
            execucoes.add(new Execucao(5, 15));
            execucoes.add(new Execucao(5, 15));

            for (int i = 0; i < execucoes.size(); i++) {
                Execucao execucao = execucoes.get(i);
                int finalI = i;

                CSVReader localidadesFile = new CSVReaderBuilder(new FileReader("src/main/files/Demanda 2.csv")).build();
                List<String[]> localidadesString = localidadesFile.readAll();

                List<Localidade> localidadeArrayList = localidadesString.stream()
                        .skip(2)
                        .map(item -> new Localidade(item[0], Double.parseDouble(item[1]), Double.parseDouble(item[2]), Integer.parseInt(item[4 + finalI]), Integer.parseInt(item[3]),false))
                        .toList();

                CSVReader hoteisCsv = new CSVReaderBuilder(new FileReader("src/main/files/Hoteis 2.csv")).build();
                List<String[]> hoteisString = hoteisCsv.readAll();

                List<Localidade> hoteisArrayList = hoteisString.stream()
                        .skip(1)
                        .map(item -> new Localidade(item[1], Double.parseDouble(item[4]), Double.parseDouble(item[5]), 0, 0, true))
                        .toList();

                List<phpeterle.modelos.Localidade> todasLocalidades = Stream.concat(localidadeArrayList.stream(), hoteisArrayList.stream()).toList();


                AntColonyOptimization aco = new AntColonyOptimization(execucao.getA(), execucao.getB(), 0.85, 10000, 0.1, 15000, 400, todasLocalidades, hoteisArrayList, Integer.toString(finalI + 1));

                tasks.add(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        List<Formiga> melhorCaminho =  aco.comecarOtimizacao();

                        KmlFile kmlFile = new KmlFile();
                        kmlFile.criarArquivo(melhorCaminho, "Mapa " + (finalI + 1));

                        return null;
                    }
                });

            }

            executor.invokeAll(tasks);

//            List<Future<Resultado>> resultados =  executor.invokeAll(tasks);
//            Resultado melhorResultado = new Resultado(0, 0, 0, 0);
//
//            for (Future<Resultado> tarefaResultado: resultados) {
//                Resultado resultado = tarefaResultado.get();
//
//                if(resultado.getCustoTotal() < melhorResultado.getCustoTotal() || melhorResultado.getCustoTotal() == 0) {
//                    melhorResultado = resultado;
//                }
//
//                System.out.printf("O valor de alpha %f e o valor de beta %f teve o  menor custo %.2f com o tempo de %d \n", resultado.getA(), resultado.getB(),resultado.getCustoTotal(), resultado.getTempo() / 1000);
//
//            }
//            System.out.println("Melhor resultado: \n\n\n\n");
//            System.out.printf("O valor de alpha %f e o valor de beta %f teve o  menor custo %.2f com o tempo de %d \n", melhorResultado.getA(), melhorResultado.getB(),melhorResultado.getCustoTotal(), melhorResultado.getTempo() / 1000);

//
//                            CSVReader localidadesFile = new CSVReaderBuilder(new FileReader("src/main/files/Demanda 2.csv")).build();
//                List<String[]> localidadesString = localidadesFile.readAll();
//
//                List<Localidade> localidadeArrayList = localidadesString.stream()
//                        .skip(2)
//                        .map(item -> new Localidade(item[0], Double.parseDouble(item[1]), Double.parseDouble(item[2]), Integer.parseInt(item[4]), Integer.parseInt(item[3]),false))
//                        .toList();
//
//                CSVReader hoteisCsv = new CSVReaderBuilder(new FileReader("src/main/files/Hoteis 2.csv")).build();
//                List<String[]> hoteisString = hoteisCsv.readAll();
//
//                List<Localidade> hoteisArrayList = hoteisString.stream()
//                        .skip(1)
//                        .map(item -> new Localidade(item[1], Double.parseDouble(item[4]), Double.parseDouble(item[5]), 0, 0, true))
//                        .toList();
//
//                List<Localidade> todasLocalidades = Stream.concat(localidadeArrayList.stream(), hoteisArrayList.stream()).toList();
//
//            AntColonyOptimization aco = new AntColonyOptimization(2, 3, 0.1, 10000, 0.05, 1000, 20, todasLocalidades, hoteisArrayList, "label");
//
//
//            List<Formiga> melhorCaminho = aco.comecarOtimizacao();
//
//                        KmlFile kmlFile = new KmlFile();
//            kmlFile.criarArquivo(melhorCaminho, "Mapa 1");

//
//            javax.swing.SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    new PanAndZoom(melhorCaminho, localidadeArrayList, hoteisArrayList);
//                }
//            });



        } catch (Exception e) {
            System.out.println("Erro ao ler arquivo " + e.getMessage());
        }
        
        
        System.out.println(" Finalizado ");
    }

}
