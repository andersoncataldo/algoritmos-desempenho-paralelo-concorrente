package br.com.unifor.paralela;

import br.com.unifor.paralela.algorithm.Sorter;
import br.com.unifor.paralela.algorithm.bubblesort.BubbleSortParallel;
import br.com.unifor.paralela.algorithm.bubblesort.BubbleSortSerial;
import br.com.unifor.paralela.algorithm.insertionsort.InsertionSortParallel;
import br.com.unifor.paralela.algorithm.insertionsort.InsertionSortSerial;
import br.com.unifor.paralela.algorithm.mergesort.MergeSortParallel;
import br.com.unifor.paralela.algorithm.mergesort.MergeSortSerial;
import br.com.unifor.paralela.algorithm.quicksort.QuickSortParallel;
import br.com.unifor.paralela.algorithm.quicksort.QuickSortSerial;
import br.com.unifor.paralela.benchmark.BenchmarkRunner;
import br.com.unifor.paralela.benchmark.CsvExporter;
import br.com.unifor.paralela.model.BenchmarkResult;
import br.com.unifor.paralela.model.BenchmarkSummary;
import br.com.unifor.paralela.model.DataDistribution;
import br.com.unifor.paralela.ui.MainWindow;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ponto de entrada da aplicação.
 * Por padrão, inicializa a interface gráfica Swing.
 * Se o parâmetro "--cli" for fornecido, executa o benchmark em modo headless via terminal.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && "--cli".equalsIgnoreCase(args[0])) {
            executarModoCli();
        } else {
            iniciarModoGrafico();
        }
    }

    private static void iniciarModoGrafico() {
        try {
            // Aplica o Look & Feel nativo do sistema operacional (Windows)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            MainWindow janela = new MainWindow();
            janela.setVisible(true);
        });
    }

    private static void executarModoCli() {
        System.out.println("===============================================================");
        System.out.println(" UNIFOR - Benchmark de Algoritmos (Serial vs Paralelo) - CLI");
        System.out.println("===============================================================");

        List<Sorter> algoritmos = Arrays.asList(
                new BubbleSortSerial(),
                new BubbleSortParallel(2),
                new BubbleSortParallel(4),
                new InsertionSortSerial(),
                new InsertionSortParallel(2),
                new InsertionSortParallel(4),
                new MergeSortSerial(),
                new MergeSortParallel(2),
                new MergeSortParallel(4),
                new QuickSortSerial(),
                new QuickSortParallel(2),
                new QuickSortParallel(4)
        );

        List<Integer> tamanhos = Arrays.asList(1000, 5000, 10000);
        List<DataDistribution> distros = Arrays.asList(DataDistribution.ALEATORIO, DataDistribution.INVERSO);
        int amostras = 5;

        BenchmarkRunner runner = new BenchmarkRunner();
        List<BenchmarkResult> todasAmostras = new ArrayList<>();

        System.out.println("Executando testes automatizados (5 amostras por configuração)...");
        List<BenchmarkSummary> resumos = runner.executar(algoritmos, tamanhos, distros, amostras,
                new BenchmarkRunner.ProgressListener() {
                    @Override
                    public void onProgress(int atual, int total, String mensagem) {
                        System.out.printf("[%d/%d] %s%n", atual, total, mensagem);
                    }

                    @Override
                    public void onSampleFinished(BenchmarkResult resultado) {
                        todasAmostras.add(resultado);
                    }

                    @Override
                    public void onSummaryFinished(BenchmarkSummary resumo) {
                        System.out.printf(" >> %s | N=%d | %s -> Média: %.3f ms | Speedup: %.2fx%n",
                                resumo.getAlgoritmo(), resumo.getTamanhoVetor(),
                                resumo.getDistribuicao().name(), resumo.getTempoMedioMs(), resumo.getSpeedup());
                    }
                });

        try {
            File pastaOutput = new File("output");
            File csvResumo = CsvExporter.exportarResumo(resumos, pastaOutput);
            File csvAmostras = CsvExporter.exportarAmostras(todasAmostras, pastaOutput);
            System.out.println("Resultados exportados com sucesso:");
            System.out.println(" - " + csvResumo.getAbsolutePath());
            System.out.println(" - " + csvAmostras.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Erro ao exportar CSV: " + e.getMessage());
        }

        System.out.println("Benchmark finalizado.");
    }
}
