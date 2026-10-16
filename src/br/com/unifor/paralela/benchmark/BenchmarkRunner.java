package br.com.unifor.paralela.benchmark;

import br.com.unifor.paralela.algorithm.Sorter;
import br.com.unifor.paralela.model.BenchmarkResult;
import br.com.unifor.paralela.model.BenchmarkSummary;
import br.com.unifor.paralela.model.DataDistribution;

import java.util.*;

/**
 * Orquestrador dos experimentos de benchmark.
 * Executa warm-up para aquecimento da JVM/JIT, realiza pelo menos 5 amostras por teste,
 * valida a ordenação correta e calcula métricas consolidadas (média, desvio padrão, speedup e eficiência).
 */
public class BenchmarkRunner {

    public interface ProgressListener {
        void onProgress(int atual, int total, String mensagem);
        void onSampleFinished(BenchmarkResult resultado);
        void onSummaryFinished(BenchmarkSummary resumo);
    }

    private volatile boolean cancelado = false;

    public void cancelar() {
        this.cancelado = true;
    }

    public boolean isCancelado() {
        return cancelado;
    }

    /**
     * Executa o benchmark completo para as listas de algoritmos, tamanhos e distribuições fornecidas.
     */
    public List<BenchmarkSummary> executar(List<Sorter> algoritmos,
                                          List<Integer> tamanhos,
                                          List<DataDistribution> distribuicoes,
                                          int numAmostras,
                                          ProgressListener listener) {
        this.cancelado = false;
        List<BenchmarkSummary> resumos = new ArrayList<>();
        List<BenchmarkResult> todasAmostras = new ArrayList<>();

        int totalPassos = algoritmos.size() * tamanhos.size() * distribuicoes.size();
        int passoAtual = 0;

        // 1. Warm-up da JVM para estabilizar JIT Compiler
        if (listener != null) {
            listener.onProgress(0, totalPassos, "Aquecendo JVM (Warm-up)...");
        }
        executarWarmUp(algoritmos);

        // Mapa para armazenar os tempos médios seriais: "AlgoritmoBase_Tamanho_Distribuicao" -> tempoMedioMs
        Map<String, Double> temposSeriaisBase = new HashMap<>();

        // Executa primeiro os algoritmos seriais para termos os baselines de Speedup
        List<Sorter> ordenadosPorPrioridade = new ArrayList<>(algoritmos);
        ordenadosPorPrioridade.sort((a, b) -> Boolean.compare(a.isParallel(), b.isParallel()));

        for (Sorter sorter : ordenadosPorPrioridade) {
            for (DataDistribution dist : distribuicoes) {
                for (int tamanho : tamanhos) {
                    if (cancelado) {
                        if (listener != null) listener.onProgress(passoAtual, totalPassos, "Benchmark cancelado pelo usuário.");
                        return resumos;
                    }

                    passoAtual++;
                    String statusMsg = String.format("Executando: %s | N=%d | %s (%d/%d)",
                            sorter.getName(), tamanho, dist.getDescricao(), passoAtual, totalPassos);

                    if (listener != null) {
                        listener.onProgress(passoAtual, totalPassos, statusMsg);
                    }

                    List<Double> tempos = new ArrayList<>();

                    for (int amostra = 1; amostra <= numAmostras; amostra++) {
                        if (cancelado) break;

                        // Gera o vetor original com a semente da amostra
                        int[] original = ArrayGenerator.gerar(tamanho, dist, 1000L * amostra + tamanho);
                        int[] copia = Arrays.copyOf(original, original.length);

                        // Medição com alta precisão
                        long inicioNs = System.nanoTime();
                        sorter.sort(copia);
                        long fimNs = System.nanoTime();

                        double tempoMs = (fimNs - inicioNs) / 1_000_000.0;

                        // Validação de correção
                        if (!validarOrdenacao(copia)) {
                            throw new IllegalStateException("Falha de integridade: " + sorter.getName() +
                                    " gerou um vetor não ordenado para tamanho " + tamanho);
                        }

                        BenchmarkResult res = new BenchmarkResult(
                                sorter.getName(), sorter.isParallel(), sorter.getThreads(),
                                tamanho, dist, amostra, tempoMs
                        );
                        todasAmostras.add(res);
                        tempos.add(tempoMs);

                        if (listener != null) {
                            listener.onSampleFinished(res);
                        }
                    }

                    BenchmarkSummary summary = new BenchmarkSummary(
                            sorter.getName(), sorter.isParallel(), sorter.getThreads(),
                            tamanho, dist, tempos
                    );

                    String nomeBase = extrairNomeBase(sorter.getName());
                    String chaveSerial = nomeBase + "_" + tamanho + "_" + dist.name();

                    if (!sorter.isParallel()) {
                        temposSeriaisBase.put(chaveSerial, summary.getTempoMedioMs());
                    } else {
                        Double tempoSerial = temposSeriaisBase.get(chaveSerial);
                        if (tempoSerial != null) {
                            summary.calcularSpeedupEEficiencia(tempoSerial);
                        }
                    }

                    resumos.add(summary);
                    if (listener != null) {
                        listener.onSummaryFinished(summary);
                    }
                }
            }
        }

        if (listener != null) {
            listener.onProgress(totalPassos, totalPassos, "Benchmark concluído com sucesso!");
        }

        return resumos;
    }

    /**
     * Aquecimento breve da JVM em dados reduzidos para disparar a compilação JIT C2.
     */
    private void executarWarmUp(List<Sorter> algoritmos) {
        int[] dadosWarmUp = ArrayGenerator.gerar(2000, DataDistribution.ALEATORIO, 999L);
        for (Sorter sorter : algoritmos) {
            try {
                int[] copia = Arrays.copyOf(dadosWarmUp, dadosWarmUp.length);
                sorter.sort(copia);
            } catch (Exception ignored) {
            }
        }
    }

    private static boolean validarOrdenacao(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }

    private static String extrairNomeBase(String nomeCompleto) {
        if (nomeCompleto.startsWith("Bubble")) return "BubbleSort";
        if (nomeCompleto.startsWith("Insertion")) return "InsertionSort";
        if (nomeCompleto.startsWith("Merge")) return "MergeSort";
        if (nomeCompleto.startsWith("Quick")) return "QuickSort";
        return nomeCompleto;
    }
}
