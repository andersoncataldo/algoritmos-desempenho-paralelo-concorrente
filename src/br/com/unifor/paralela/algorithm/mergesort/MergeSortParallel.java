package br.com.unifor.paralela.algorithm.mergesort;

import br.com.unifor.paralela.algorithm.Sorter;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Implementação paralela otimizada do Merge Sort com ForkJoinPool.
 * - Utiliza threshold balanceado por thread para equilibrar paralelismo e custo de agendamento.
 * - Incorpora atalho de mescla (se o maior elemento à esquerda for <= menor à direita, pula a mescla).
 * - Utiliza vetor auxiliar único alocado no início para eliminar alocações intermediárias.
 */
public class MergeSortParallel implements Sorter {

    private final int numThreads;
    private final ForkJoinPool pool;

    public MergeSortParallel(int numThreads) {
        this.numThreads = Math.max(1, numThreads);
        this.pool = new ForkJoinPool(this.numThreads);
    }

    @Override
    public String getName() {
        return "Merge Sort Paralelo (" + numThreads + " threads)";
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
            new MergeSortSerial().sort(array);
            return;
        }

        int[] aux = new int[array.length];
        int threshold = Math.max(MergeSortSerial.INSERTION_THRESHOLD, array.length / (numThreads * 4));

        pool.invoke(new MergeSortTask(array, aux, 0, array.length - 1, threshold));
    }

    private static class MergeSortTask extends RecursiveAction {
        private final int[] array;
        private final int[] aux;
        private final int esquerda;
        private final int direita;
        private final int threshold;

        public MergeSortTask(int[] array, int[] aux, int esquerda, int direita, int threshold) {
            this.array = array;
            this.aux = aux;
            this.esquerda = esquerda;
            this.direita = direita;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            if (direita - esquerda < threshold) {
                MergeSortSerial.sort(array, aux, esquerda, direita);
                return;
            }

            int meio = esquerda + (direita - esquerda) / 2;
            MergeSortTask tarefaEsquerda = new MergeSortTask(array, aux, esquerda, meio, threshold);
            MergeSortTask tarefaDireita = new MergeSortTask(array, aux, meio + 1, direita, threshold);

            invokeAll(tarefaEsquerda, tarefaDireita);

            // Otimização: se os blocos já estiverem em ordem, não precisa mesclar
            if (array[meio] <= array[meio + 1]) {
                return;
            }

            MergeSortSerial.merge(array, aux, esquerda, meio, direita);
        }
    }
}
