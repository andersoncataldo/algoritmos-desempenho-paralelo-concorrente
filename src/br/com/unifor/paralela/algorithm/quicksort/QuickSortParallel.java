package br.com.unifor.paralela.algorithm.quicksort;

import br.com.unifor.paralela.algorithm.Sorter;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Implementação paralela do Quick Sort utilizando ForkJoinPool.
 * Após o particionamento, os dois subvetores são processados concorrentemente por threads distintas.
 * Para partições menores que o limiar (threshold), a execução recorre ao QuickSort serial para
 * manter o overhead de sincronização mínimo.
 */
public class QuickSortParallel implements Sorter {

    private final int numThreads;

    public QuickSortParallel(int numThreads) {
        this.numThreads = Math.max(1, numThreads);
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

        int threshold = Math.max(1024, array.length / (numThreads * 4));

        ForkJoinPool pool = new ForkJoinPool(numThreads);
        try {
            pool.invoke(new QuickSortTask(array, 0, array.length - 1, threshold));
        } finally {
            pool.shutdown();
        }
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
