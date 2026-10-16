package br.com.unifor.paralela.algorithm.quicksort;

import br.com.unifor.paralela.algorithm.Sorter;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Implementação paralela do Quick Sort utilizando ForkJoinPool.
 * Reutiliza a instância do ForkJoinPool e utiliza limiar adaptativo
 * para delegar partições menores ao QuickSort serial com Insertion Sort.
 */
public class QuickSortParallel implements Sorter {

    private final int numThreads;
    private final ForkJoinPool pool;

    public QuickSortParallel(int numThreads) {
        this.numThreads = Math.max(1, numThreads);
        this.pool = new ForkJoinPool(this.numThreads);
    }

    @Override
    public String getName() {
        return "Quick Sort Paralelo (" + numThreads + " threads)";
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
            new QuickSortSerial().sort(array);
            return;
        }

        int threshold = Math.max(QuickSortSerial.INSERTION_THRESHOLD, array.length / (numThreads * 4));
        pool.invoke(new QuickSortTask(array, 0, array.length - 1, threshold));
    }

    private static class QuickSortTask extends RecursiveAction {
        private final int[] array;
        private final int esquerda;
        private final int direita;
        private final int threshold;

        public QuickSortTask(int[] array, int esquerda, int direita, int threshold) {
            this.array = array;
            this.esquerda = esquerda;
            this.direita = direita;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            if (direita - esquerda < threshold) {
                QuickSortSerial.quickSort(array, esquerda, direita);
                return;
            }

            if (esquerda < direita) {
                int indicePivo = QuickSortSerial.particionar(array, esquerda, direita);
                QuickSortTask tarefaEsquerda = new QuickSortTask(array, esquerda, indicePivo, threshold);
                QuickSortTask tarefaDireita = new QuickSortTask(array, indicePivo + 1, direita, threshold);

                invokeAll(tarefaEsquerda, tarefaDireita);
            }
        }
    }
}
