package br.com.unifor.paralela.algorithm.insertionsort;

import br.com.unifor.paralela.algorithm.Sorter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Implementação paralela do Insertion Sort via Particionamento em Blocos (Block Insertion Sort).
 * 
 * 1. O vetor é dividido em P blocos independentes (onde P é a quantidade de threads).
 * 2. Cada thread executa o Insertion Sort local no seu bloco concorrentemente.
 * 3. Os blocos ordenados são mesclados hierarquicamente em paralelo até a formação do vetor final.
 */
public class InsertionSortParallel implements Sorter {

    private final int numThreads;

    public InsertionSortParallel(int numThreads) {
        this.numThreads = Math.max(1, numThreads);
    }

    @Override
    public String getName() {
        return "Insertion Sort Paralelo (" + numThreads + " threads)";
    }

    @Override
    public boolean isParallel() {
        return true;
    }

    @Override
    public int getThreads() {
        return numThreads;
    }

    @Override
    public void sort(int[] array) {
        if (array == null || array.length <= 1) {
            return;
        }

        if (numThreads == 1) {
            new InsertionSortSerial().sort(array);
            return;
        }

        int n = array.length;
        int threadsEfetivas = Math.min(numThreads, Math.max(1, n / 10));
        if (threadsEfetivas <= 1) {
            new InsertionSortSerial().sort(array);
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadsEfetivas);
        try {
            // Calcula os limites de cada bloco
            int[] inicios = new int[threadsEfetivas];
            int[] fins = new int[threadsEfetivas];
            int tamanhoBloco = (n + threadsEfetivas - 1) / threadsEfetivas;

            for (int i = 0; i < threadsEfetivas; i++) {
                inicios[i] = i * tamanhoBloco;
                fins[i] = Math.min(inicios[i] + tamanhoBloco, n);
            }

            // Etapa 1: Ordenação local de cada bloco em paralelo
            List<Future<?>> tarefasOrdenacao = new ArrayList<>();
            for (int i = 0; i < threadsEfetivas; i++) {
                final int ini = inicios[i];
                final int fim = fins[i];
                tarefasOrdenacao.add(executor.submit(() -> InsertionSortSerial.sortRange(array, ini, fim)));
            }

            for (Future<?> f : tarefasOrdenacao) {
                f.get();
            }

            // Etapa 2: Mescla hierárquica paralela dos blocos ordenados
            int[] aux = new int[n];
            int passo = 1;
            while (passo < threadsEfetivas) {
                final int passoAtual = passo;
                List<Future<?>> tarefasMescla = new ArrayList<>();

                for (int i = 0; i < threadsEfetivas; i += 2 * passo) {
                    final int blocoEsquerdo = i;
                    final int blocoDireito = i + passo;

                    if (blocoDireito < threadsEfetivas) {
                        tarefasMescla.add(executor.submit(() -> {
                            int iniEsq = inicios[blocoEsquerdo];
                            int fimEsq = fins[blocoDireito - 1];
                            int iniDir = inicios[blocoDireito];
                            int fimDir = fins[Math.min(blocoDireito + passoAtual - 1, threadsEfetivas - 1)];

                            mesclarSubvetores(array, aux, iniEsq, iniDir, fimDir);
                        }));
                    }
                }

                for (Future<?> f : tarefasMescla) {
                    f.get();
                }

                passo *= 2;
            }

        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Mescla duas regiões adjacentes ordenadas: [inicio, meio) e [meio, fim)
     */
    private static void mesclarSubvetores(int[] array, int[] aux, int inicio, int meio, int fim) {
        int i = inicio;
        int j = meio;
        int k = inicio;

        while (i < meio && j < fim) {
            if (array[i] <= array[j]) {
                aux[k++] = array[i++];
            } else {
                aux[k++] = array[j++];
            }
        }

        while (i < meio) {
            aux[k++] = array[i++];
        }

        while (j < fim) {
            aux[k++] = array[j++];
        }

        // Copia de volta para o vetor original
        System.arraycopy(aux, inicio, array, inicio, fim - inicio);
    }
}
